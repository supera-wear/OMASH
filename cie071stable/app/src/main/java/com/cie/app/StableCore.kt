package com.cie.app

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.UUID

data class CompanyRow(
    val id: String,
    val name: String,
    val legalName: String,
    val category: String,
    val verified: Boolean,
    val reputation: Double,
    val blockCalls: Boolean,
    val blockMarketingSms: Boolean
) {
    val blocked: Boolean get() = blockCalls
}

data class CallCacheEntry(
    val phone: String,
    val companyId: String,
    val companyName: String,
    val category: String,
    val confidence: Double
)

data class CallEvent(
    val companyName: String?,
    val blocked: Boolean,
    val confidence: Double?,
    val timestamp: Long
)

class StableRepository(context: Context) {
    private val app = context.applicationContext
    private val prefs = app.getSharedPreferences("cie_stable_071", Context.MODE_PRIVATE)
    private val api = StableApiClient(BuildConfig.CIE_API_BASE_URL, installKey())

    suspend fun sync(): List<CompanyRow> = withContext(Dispatchers.IO) {
        api.register(BuildConfig.VERSION_NAME)
        val companies = api.companies()
        val calls = api.callCache()
        writeCompanies(companies)
        writeCallCache(calls)
        prefs.edit().putLong(KEY_SYNCED_AT, System.currentTimeMillis()).apply()
        companies
    }

    fun cachedCompanies(): List<CompanyRow> = parseCompanies(prefs.getString(KEY_COMPANIES, "[]"))

    suspend fun setPolicy(companyId: String, blocked: Boolean): List<CompanyRow> = withContext(Dispatchers.IO) {
        api.setPolicy(companyId, blocked, blocked)
        sync()
    }

    suspend fun submitReport(senderType: String, senderValue: String, category: String, reason: String): String =
        withContext(Dispatchers.IO) {
            api.register(BuildConfig.VERSION_NAME)
            api.submitReport(senderType, senderValue, category, reason)
        }

    suspend fun serviceHealthy(): Boolean = withContext(Dispatchers.IO) {
        runCatching { api.health() }.getOrDefault(false)
    }

    fun humanError(error: Throwable): String = when {
        error is StableApiException -> error.userMessage()
        error.message?.contains("Unable to resolve host", true) == true -> "No internet connection. Local protection stays active."
        error.message?.contains("timeout", true) == true -> "CIE took too long to respond. Local protection stays active."
        else -> "Could not sync with CIE. Local protection stays active."
    }

    fun lookupCall(rawNumber: String?): Pair<Boolean, CallCacheEntry?> {
        val number = normalizePhone(rawNumber.orEmpty())
        if (number.isBlank()) return false to null
        if (testNumber()?.let(::normalizePhone) == number) {
            return true to CallCacheEntry(number, "cie-beta-test", "CIE Beta Test", "test", 1.0)
        }
        if (!isCallCacheFresh()) return false to null
        val match = readCallCache().firstOrNull { normalizePhone(it.phone) == number } ?: return false to null
        return (match.confidence >= MIN_BLOCK_CONFIDENCE) to match
    }

    fun recordCallEvent(companyName: String?, blocked: Boolean, confidence: Double?) {
        val current = recentEvents().toMutableList()
        current.add(0, CallEvent(companyName, blocked, confidence, System.currentTimeMillis()))
        while (current.size > 100) current.removeLast()
        val array = JSONArray()
        current.forEach { event ->
            array.put(JSONObject().apply {
                put("companyName", event.companyName ?: "")
                put("blocked", event.blocked)
                if (event.confidence != null) put("confidence", event.confidence)
                put("timestamp", event.timestamp)
            })
        }
        prefs.edit().putString(KEY_EVENTS, array.toString()).apply()
    }

    fun recentEvents(): List<CallEvent> = runCatching {
        val array = JSONArray(prefs.getString(KEY_EVENTS, "[]"))
        (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            CallEvent(
                companyName = item.optString("companyName").takeIf(String::isNotBlank),
                blocked = item.optBoolean("blocked", false),
                confidence = if (item.has("confidence")) item.optDouble("confidence") else null,
                timestamp = item.optLong("timestamp", 0L)
            )
        }
    }.getOrDefault(emptyList())

    fun clearActivity() { prefs.edit().remove(KEY_EVENTS).apply() }
    fun cachedCallCount(): Int = readCallCache().size
    fun lastSync(): Long = prefs.getLong(KEY_SYNCED_AT, 0L)

    fun isCallCacheFresh(now: Long = System.currentTimeMillis()): Boolean {
        val synced = lastSync()
        return synced > 0L && now >= synced && now - synced <= CACHE_MAX_AGE_MS
    }

    fun setTestNumber(number: String) {
        val normalized = normalizePhone(number)
        require(normalized.length >= 8) { "Enter a valid phone number" }
        prefs.edit().putString(KEY_TEST, normalized).apply()
    }

    fun testNumber(): String? = prefs.getString(KEY_TEST, null)
    fun clearTestNumber() { prefs.edit().remove(KEY_TEST).apply() }

    private fun installKey(): String {
        prefs.getString(KEY_INSTALL, null)?.let { return it }
        val key = UUID.randomUUID().toString() + UUID.randomUUID().toString()
        check(prefs.edit().putString(KEY_INSTALL, key).commit())
        return key
    }

    private fun writeCompanies(items: List<CompanyRow>) {
        val array = JSONArray()
        items.forEach { company ->
            array.put(JSONObject().apply {
                put("id", company.id)
                put("name", company.name)
                put("legalName", company.legalName)
                put("category", company.category)
                put("verified", company.verified)
                put("reputation", company.reputation)
                put("blockCalls", company.blockCalls)
                put("blockMarketingSms", company.blockMarketingSms)
            })
        }
        prefs.edit().putString(KEY_COMPANIES, array.toString()).apply()
    }

    private fun parseCompanies(raw: String?): List<CompanyRow> = runCatching {
        val array = JSONArray(raw ?: "[]")
        (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            CompanyRow(
                id = item.getString("id"),
                name = item.getString("name"),
                legalName = item.optString("legalName", item.getString("name")),
                category = item.optString("category", "other"),
                verified = item.optBoolean("verified", false),
                reputation = item.optDouble("reputation", 50.0),
                blockCalls = item.optBoolean("blockCalls", false),
                blockMarketingSms = item.optBoolean("blockMarketingSms", false)
            )
        }
    }.getOrDefault(emptyList())

    private fun writeCallCache(items: List<CallCacheEntry>) {
        val array = JSONArray()
        items.forEach { entry ->
            array.put(JSONObject().apply {
                put("phone", normalizePhone(entry.phone))
                put("companyId", entry.companyId)
                put("companyName", entry.companyName)
                put("category", entry.category)
                put("confidence", entry.confidence)
            })
        }
        prefs.edit().putString(KEY_CALL_CACHE, array.toString()).apply()
    }

    private fun readCallCache(): List<CallCacheEntry> = runCatching {
        val array = JSONArray(prefs.getString(KEY_CALL_CACHE, "[]"))
        (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            CallCacheEntry(
                phone = item.optString("phone"),
                companyId = item.optString("companyId"),
                companyName = item.optString("companyName"),
                category = item.optString("category", "other"),
                confidence = item.optDouble("confidence", 0.0)
            )
        }
    }.getOrDefault(emptyList())

    companion object {
        const val MIN_BLOCK_CONFIDENCE = 0.98
        private const val CACHE_MAX_AGE_MS = 7L * 24 * 60 * 60 * 1000
        private const val KEY_INSTALL = "install_key"
        private const val KEY_COMPANIES = "companies"
        private const val KEY_CALL_CACHE = "call_cache"
        private const val KEY_EVENTS = "events"
        private const val KEY_TEST = "test_number"
        private const val KEY_SYNCED_AT = "synced_at"

        fun normalizePhone(value: String): String {
            val trimmed = value.trim()
            val digits = trimmed.filter(Char::isDigit)
            return when {
                trimmed.startsWith("+") -> "+$digits"
                digits.startsWith("00") -> "+${digits.drop(2)}"
                digits.startsWith("0") && digits.length >= 10 -> "+90${digits.drop(1)}"
                digits.startsWith("90") && digits.length >= 12 -> "+$digits"
                else -> digits
            }
        }
    }
}

private class StableApiClient(private val baseUrl: String, private val installKey: String) {
    fun health(): Boolean = request("/api/health", "GET", authenticated = false).optBoolean("ok", false)

    fun register(appVersion: String) {
        request(
            "/api/v1/devices/register",
            "POST",
            JSONObject().apply {
                put("installKey", installKey)
                put("platform", "android")
                put("locale", Locale.getDefault().toLanguageTag())
                put("appVersion", appVersion)
            },
            authenticated = false
        )
    }

    fun companies(): List<CompanyRow> {
        val array = request("/api/v1/companies", "GET").getJSONArray("companies")
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            CompanyRow(
                id = item.getString("id"),
                name = item.getString("display_name"),
                legalName = item.optString("legal_name", item.getString("display_name")),
                category = item.optString("category", "other"),
                verified = item.optString("verification_status") == "verified",
                reputation = item.optDouble("reputation_score", 50.0),
                blockCalls = item.optBoolean("block_calls", false),
                blockMarketingSms = item.optBoolean("block_marketing_sms", false)
            )
        }
    }

    fun callCache(): List<CallCacheEntry> {
        val array = request("/api/v1/cache", "GET").getJSONArray("entries")
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            CallCacheEntry(
                phone = item.getString("phone"),
                companyId = item.getString("company_id"),
                companyName = item.getString("display_name"),
                category = item.optString("category", "other"),
                confidence = item.optDouble("confidence", 0.0)
            )
        }
    }

    fun setPolicy(companyId: String, blockCalls: Boolean, blockMarketingSms: Boolean) {
        request(
            "/api/v1/companies/$companyId/policy",
            "PUT",
            JSONObject().apply {
                put("block_calls", blockCalls)
                put("block_marketing_sms", blockMarketingSms)
                put("block_service_sms", false)
                put("block_transactional_sms", false)
                put("block_security_sms", false)
            }
        )
    }

    fun submitReport(senderType: String, senderValue: String, category: String, reason: String): String {
        val root = request(
            "/api/v1/reports",
            "POST",
            JSONObject().apply {
                put("senderType", senderType)
                put("senderValue", senderValue)
                put("category", category)
                put("reason", reason)
            }
        )
        return root.optJSONObject("report")?.optString("id").orEmpty()
    }

    private fun request(
        path: String,
        method: String,
        body: JSONObject? = null,
        authenticated: Boolean = true
    ): JSONObject {
        require(baseUrl.startsWith("https://")) { "CIE service URL must use HTTPS" }
        val connection = (URL(baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 5000
            readTimeout = 5000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("User-Agent", "CIE-Android-Stable")
            if (authenticated) setRequestProperty("x-cie-install-key", installKey)
            if (body != null) doOutput = true
        }
        try {
            if (body != null) {
                connection.outputStream.use { stream -> stream.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                val error = runCatching { JSONObject(text).optString("error") }.getOrDefault("")
                throw StableApiException(code, error.ifBlank { "request_failed" })
            }
            return if (text.isBlank()) JSONObject() else JSONObject(text)
        } finally {
            connection.disconnect()
        }
    }
}

private class StableApiException(val status: Int, val apiError: String) : IllegalStateException(apiError) {
    fun userMessage(): String = when (apiError) {
        "device_revoked" -> "This CIE installation has been revoked. Reinstall CIE."
        "rate_limited" -> "Too many requests. Try again in a minute."
        "device_auth_required" -> "Device session expired. Sync again."
        "company_not_found" -> "This company no longer exists in CIE."
        "invalid_report" -> "Check the sender information and try again."
        else -> "CIE service is temporarily unavailable."
    }
}
