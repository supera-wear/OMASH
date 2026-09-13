package com.cie.app.core

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.cie.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.util.Locale
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class CompanyRow(
    val id: String,
    val name: String,
    val legalName: String,
    val category: String,
    val verified: Boolean,
    val reputation: Double,
    val blockCalls: Boolean,
    val blockMarketingSms: Boolean
) { val blocked: Boolean get() = blockCalls || blockMarketingSms }

data class CallEvent(val companyName: String?, val blocked: Boolean, val timestamp: Long)

data class CallCacheEntry(
    val phone: String,
    val companyId: String,
    val companyName: String,
    val category: String,
    val confidence: Double
)

data class MessageIdentity(
    val signalType: String,
    val normalizedValue: String,
    val companyId: String,
    val companyName: String,
    val category: String,
    val confidence: Double,
    val evidenceLevel: String,
    val independentDevices: Int
)

enum class MessageCategory { SECURITY, TRANSACTIONAL, SERVICE, MARKETING, SPAM, PERSONAL, UNKNOWN }

data class CieMessage(
    val id: String,
    val sender: String,
    val body: String,
    val timestamp: Long,
    val category: MessageCategory,
    val filtered: Boolean,
    val companyId: String?,
    val companyName: String?
)

data class SmsClassification(
    val category: MessageCategory,
    val confidence: Double,
    val company: CompanyRow?,
    val filter: Boolean
)

class CieRepository(context: Context) {
    private val app = context.applicationContext
    private val prefs = app.getSharedPreferences("cie_full_core_v3", Context.MODE_PRIVATE)
    private val installKey = InstallKeyStore(app).getOrCreate()
    private val api = CieApiClient(BuildConfig.CIE_API_BASE_URL, BuildConfig.CIE_MESSAGE_API_BASE_URL, installKey)

    suspend fun sync(): List<CompanyRow> = withContext(Dispatchers.IO) {
        api.register(BuildConfig.VERSION_NAME)
        val companies = api.companies()
        val calls = api.callCache()
        val messages = runCatching { api.messageCache() }.getOrElse { readMessageCache() }
        writeCompanies(companies)
        writeCallCache(calls)
        writeMessageCache(messages)
        prefs.edit().putLong(KEY_SYNCED_AT, System.currentTimeMillis()).apply()
        companies
    }

    fun cachedCompanies(): List<CompanyRow> = parseCompanies(prefs.getString(KEY_COMPANIES, null))

    suspend fun setPolicy(companyId: String, blockCalls: Boolean, blockMarketingSms: Boolean): List<CompanyRow> = withContext(Dispatchers.IO) {
        api.setPolicy(companyId, blockCalls, blockMarketingSms)
        sync()
    }

    suspend fun submitReport(senderType: String, senderValue: String, category: String, reason: String) = withContext(Dispatchers.IO) {
        api.register(BuildConfig.VERSION_NAME)
        api.submitReport(senderType, senderValue, category, reason)
    }

    suspend fun serviceHealthy(): Boolean = withContext(Dispatchers.IO) { runCatching { api.health() }.getOrDefault(false) }

    fun humanError(error: Throwable): String = when {
        error.message?.contains("Unable to resolve host", true) == true -> "No internet connection. Local protection stays active."
        error.message?.contains("timeout", true) == true -> "CIE took too long to respond. Local protection stays active."
        else -> "Could not sync with CIE. Local protection stays active."
    }

    fun lookupCall(raw: String?): Pair<Boolean, String?> {
        val normalized = normalizePhone(raw)
        if (normalized.isBlank()) return false to null
        if (testNumber()?.let(::normalizePhone) == normalized) return true to "CIE Beta Test"
        if (!cacheFresh()) return false to null
        val match = readCallCache().firstOrNull { normalizePhone(it.phone) == normalized } ?: return false to null
        val block = match.confidence >= 0.98
        return block to match.companyName
    }

    fun recordCallEvent(companyName: String?, blocked: Boolean) {
        val current = recentEvents().toMutableList()
        current.add(0, CallEvent(companyName, blocked, System.currentTimeMillis()))
        while (current.size > 100) current.removeLast()
        val arr = JSONArray()
        current.forEach { e -> arr.put(JSONObject().apply {
            put("companyName", e.companyName ?: "")
            put("blocked", e.blocked)
            put("timestamp", e.timestamp)
        }) }
        prefs.edit().putString(KEY_EVENTS, arr.toString()).apply()
    }

    fun recentEvents(): List<CallEvent> = runCatching {
        val arr = JSONArray(prefs.getString(KEY_EVENTS, "[]"))
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            CallEvent(o.optString("companyName").takeIf { it.isNotBlank() }, o.optBoolean("blocked"), o.optLong("timestamp"))
        }
    }.getOrDefault(emptyList())

    fun clearActivity() = prefs.edit().remove(KEY_EVENTS).apply()
    fun cachedCallCount(): Int = readCallCache().size
    fun cachedMessageIdentityCount(): Int = readMessageCache().size
    fun lastSync(): Long = prefs.getLong(KEY_SYNCED_AT, 0L)

    fun setTestNumber(number: String) {
        require(normalizePhone(number).length >= 8) { "Enter a valid phone number" }
        prefs.edit().putString(KEY_TEST, normalizePhone(number)).apply()
    }
    fun testNumber(): String? = prefs.getString(KEY_TEST, null)
    fun clearTestNumber() = prefs.edit().remove(KEY_TEST).apply()

    fun lookupMessageIdentity(sender: String): MessageIdentity? {
        if (!cacheFresh()) return null
        val normalized = normalizeSender(sender)
        val match = readMessageCache().firstOrNull { normalizeSender(it.normalizedValue) == normalized } ?: return null
        val trusted = when (match.evidenceLevel.lowercase(Locale.ROOT)) {
            "verified" -> match.confidence >= 0.94
            "corroborated" -> match.confidence >= 0.98 && match.independentDevices >= 5
            else -> false
        }
        return match.takeIf { trusted && it.signalType in setOf("phone", "sender_id", "short_code") }
    }

    private fun cacheFresh(): Boolean {
        val t = lastSync()
        val now = System.currentTimeMillis()
        return t > 0L && now >= t && now - t <= 7L * 24 * 60 * 60 * 1000
    }

    private fun writeCompanies(items: List<CompanyRow>) {
        val arr = JSONArray()
        items.forEach { c -> arr.put(JSONObject().apply {
            put("id", c.id); put("name", c.name); put("legalName", c.legalName); put("category", c.category)
            put("verified", c.verified); put("reputation", c.reputation); put("blockCalls", c.blockCalls); put("blockMarketingSms", c.blockMarketingSms)
        }) }
        prefs.edit().putString(KEY_COMPANIES, arr.toString()).apply()
    }

    private fun parseCompanies(raw: String?): List<CompanyRow> = runCatching {
        val arr = JSONArray(raw ?: "[]")
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            CompanyRow(o.getString("id"), o.getString("name"), o.optString("legalName", o.getString("name")), o.optString("category", "other"), o.optBoolean("verified"), o.optDouble("reputation", 50.0), o.optBoolean("blockCalls"), o.optBoolean("blockMarketingSms"))
        }
    }.getOrDefault(emptyList())

    private fun writeCallCache(items: List<CallCacheEntry>) {
        val arr = JSONArray()
        items.forEach { e -> arr.put(JSONObject().apply {
            put("phone", e.phone); put("companyId", e.companyId); put("companyName", e.companyName); put("category", e.category); put("confidence", e.confidence)
        }) }
        prefs.edit().putString(KEY_CALL_CACHE, arr.toString()).apply()
    }

    private fun readCallCache(): List<CallCacheEntry> = runCatching {
        val arr = JSONArray(prefs.getString(KEY_CALL_CACHE, "[]"))
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            CallCacheEntry(o.getString("phone"), o.getString("companyId"), o.getString("companyName"), o.optString("category", "other"), o.optDouble("confidence"))
        }
    }.getOrDefault(emptyList())

    private fun writeMessageCache(items: List<MessageIdentity>) {
        val arr = JSONArray()
        items.forEach { e -> arr.put(JSONObject().apply {
            put("signalType", e.signalType); put("normalizedValue", e.normalizedValue); put("companyId", e.companyId); put("companyName", e.companyName)
            put("category", e.category); put("confidence", e.confidence); put("evidenceLevel", e.evidenceLevel); put("independentDevices", e.independentDevices)
        }) }
        prefs.edit().putString(KEY_MESSAGE_CACHE, arr.toString()).apply()
    }

    private fun readMessageCache(): List<MessageIdentity> = runCatching {
        val arr = JSONArray(prefs.getString(KEY_MESSAGE_CACHE, "[]"))
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            MessageIdentity(o.getString("signalType"), o.getString("normalizedValue"), o.getString("companyId"), o.getString("companyName"), o.optString("category", "other"), o.optDouble("confidence"), o.optString("evidenceLevel", "verified"), o.optInt("independentDevices"))
        }
    }.getOrDefault(emptyList())

    private companion object {
        const val KEY_COMPANIES = "companies"
        const val KEY_CALL_CACHE = "call_cache"
        const val KEY_MESSAGE_CACHE = "message_cache"
        const val KEY_EVENTS = "events"
        const val KEY_TEST = "test_number"
        const val KEY_SYNCED_AT = "synced_at"
    }
}

class SmsStore(context: Context) {
    private val app = context.applicationContext
    private val prefs = app.getSharedPreferences("cie_sms_secure_v3", Context.MODE_PRIVATE)
    private val repo = CieRepository(app)

    fun classify(sender: String, body: String): SmsClassification {
        val lower = body.lowercase(Locale.ROOT)
        val category = when {
            containsAny(lower, "otp", "doğrulama", "dogrulama", "verification", "tek kullanımlık", "tek kullanimlik", "şifre", "sifre", "kodunuz", "security code") -> MessageCategory.SECURITY
            containsAny(lower, "ödeme", "odeme", "payment", "işlem", "islem", "transaction", "sipariş", "siparis", "order", "kargo", "teslimat", "delivery") -> MessageCategory.TRANSACTIONAL
            containsAny(lower, "servis", "service", "randevu", "appointment", "hesabınız", "hesabiniz", "account") -> MessageCategory.SERVICE
            containsAny(lower, "kazandınız", "kazandiniz", "bedava", "ücretsiz", "ucretsiz") && containsAny(lower, "http://", "https://", "tıkla", "tikla", "click") -> MessageCategory.SPAM
            containsAny(lower, "kampanya", "indirim", "fırsat", "firsat", "offer", "sale", "promo", "kupon", "discount") -> MessageCategory.MARKETING
            else -> MessageCategory.UNKNOWN
        }
        val identity = repo.lookupMessageIdentity(sender)
        val company = identity?.let { id -> repo.cachedCompanies().firstOrNull { it.id == id.companyId && it.verified } }
        val confidence = when (category) {
            MessageCategory.SECURITY, MessageCategory.TRANSACTIONAL, MessageCategory.SERVICE -> 0.99
            MessageCategory.SPAM -> 0.98
            MessageCategory.MARKETING -> 0.94
            else -> 0.5
        }
        val filter = when (category) {
            MessageCategory.SECURITY, MessageCategory.TRANSACTIONAL, MessageCategory.SERVICE, MessageCategory.PERSONAL -> false
            MessageCategory.MARKETING -> company?.blockMarketingSms == true && identity != null
            MessageCategory.SPAM -> confidence >= 0.98
            MessageCategory.UNKNOWN -> false
        }
        return SmsClassification(category, confidence, company, filter)
    }

    @Synchronized
    fun add(sender: String, body: String, timestamp: Long, classification: SmsClassification): CieMessage {
        val item = CieMessage(UUID.randomUUID().toString(), sender.ifBlank { "Unknown" }, body, timestamp, classification.category, classification.filter, classification.company?.id, classification.company?.name)
        val list = listOf(item) + read().take(299)
        write(list)
        return item
    }

    @Synchronized
    fun read(): List<CieMessage> {
        val encrypted = prefs.getString("messages", null) ?: return emptyList()
        val raw = runCatching { LocalCrypto.decrypt(encrypted) }.getOrNull() ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                CieMessage(o.getString("id"), o.getString("sender"), o.getString("body"), o.getLong("timestamp"), runCatching { MessageCategory.valueOf(o.getString("category")) }.getOrDefault(MessageCategory.UNKNOWN), o.optBoolean("filtered"), o.optString("companyId").takeIf { it.isNotBlank() }, o.optString("companyName").takeIf { it.isNotBlank() })
            }
        }.getOrDefault(emptyList())
    }

    fun filtered(): List<CieMessage> = read().filter { it.filtered }
    fun inbox(): List<CieMessage> = read().filterNot { it.filtered }

    @Synchronized
    fun restore(id: String) = write(read().map { if (it.id == id) it.copy(filtered = false) else it })

    @Synchronized
    fun clearFiltered() = write(read().filterNot { it.filtered })

    private fun write(items: List<CieMessage>) {
        val arr = JSONArray()
        items.forEach { m -> arr.put(JSONObject().apply {
            put("id", m.id); put("sender", m.sender); put("body", m.body); put("timestamp", m.timestamp); put("category", m.category.name)
            put("filtered", m.filtered); put("companyId", m.companyId ?: ""); put("companyName", m.companyName ?: "")
        }) }
        val encrypted = LocalCrypto.encrypt(arr.toString())
        check(prefs.edit().putString("messages", encrypted).commit())
    }

    private fun containsAny(value: String, vararg tokens: String): Boolean = tokens.any(value::contains)
}

private class InstallKeyStore(context: Context) {
    private val prefs = context.getSharedPreferences("cie_install", Context.MODE_PRIVATE)
    fun getOrCreate(): String {
        prefs.getString("key", null)?.let { return it }
        val key = UUID.randomUUID().toString() + UUID.randomUUID().toString()
        check(prefs.edit().putString("key", key).commit())
        return key
    }
}

private object LocalCrypto {
    private const val ALIAS = "cie_local_message_key_v3"
    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setKeySize(256).build())
        return generator.generateKey()
    }
    fun encrypt(raw: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val joined = cipher.iv + cipher.doFinal(raw.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(joined, Base64.NO_WRAP)
    }
    fun decrypt(encoded: String): String {
        val bytes = Base64.decode(encoded, Base64.NO_WRAP)
        require(bytes.size > 12)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
        return String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), StandardCharsets.UTF_8)
    }
}

private class CieApiClient(private val baseUrl: String, private val messageBaseUrl: String, private val installKey: String) {
    fun health(): Boolean = request(baseUrl, "/api/health", "GET", authenticated = false).optBoolean("ok", false)

    fun register(appVersion: String) {
        request(baseUrl, "/api/v1/devices/register", "POST", JSONObject().apply {
            put("installKey", installKey); put("platform", "android"); put("locale", Locale.getDefault().toLanguageTag()); put("appVersion", appVersion)
        }, authenticated = false)
    }

    fun companies(): List<CompanyRow> {
        val arr = request(baseUrl, "/api/v1/companies", "GET").getJSONArray("companies")
        return (0 until arr.length()).map { i ->
            val c = arr.getJSONObject(i)
            CompanyRow(c.getString("id"), c.getString("display_name"), c.optString("legal_name", c.getString("display_name")), c.optString("category", "other"), c.optString("verification_status") == "verified", c.optDouble("reputation_score", 50.0), c.optBoolean("block_calls"), c.optBoolean("block_marketing_sms"))
        }
    }

    fun setPolicy(companyId: String, blockCalls: Boolean, blockMarketingSms: Boolean) {
        request(baseUrl, "/api/v1/companies/$companyId/policy", "PUT", JSONObject().apply {
            put("block_calls", blockCalls); put("block_marketing_sms", blockMarketingSms); put("block_service_sms", false); put("block_transactional_sms", false); put("block_security_sms", false)
        })
    }

    fun submitReport(senderType: String, senderValue: String, category: String, reason: String) {
        request(baseUrl, "/api/v1/reports", "POST", JSONObject().apply {
            put("senderType", senderType); put("senderValue", senderValue); put("category", category); put("reason", reason)
        })
    }

    fun callCache(): List<CallCacheEntry> {
        val arr = request(baseUrl, "/api/v1/cache", "GET").getJSONArray("entries")
        return (0 until arr.length()).map { i ->
            val e = arr.getJSONObject(i)
            CallCacheEntry(e.getString("phone"), e.getString("company_id"), e.getString("display_name"), e.optString("category", "other"), e.optDouble("confidence"))
        }
    }

    fun messageCache(): List<MessageIdentity> {
        val arr = request(messageBaseUrl, "/api/v1/message-cache", "GET").getJSONArray("entries")
        return (0 until arr.length()).map { i ->
            val e = arr.getJSONObject(i)
            MessageIdentity(e.getString("signal_type"), e.getString("normalized_value"), e.getString("company_id"), e.getString("display_name"), e.optString("category", "other"), e.optDouble("confidence"), e.optString("evidence_level", "verified"), e.optInt("independent_devices"))
        }
    }

    private fun request(origin: String, path: String, method: String, body: JSONObject? = null, authenticated: Boolean = true): JSONObject {
        require(origin.startsWith("https://"))
        val c = (URL(origin.trimEnd('/') + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method; connectTimeout = 5000; readTimeout = 5000
            setRequestProperty("Accept", "application/json"); setRequestProperty("Content-Type", "application/json"); setRequestProperty("User-Agent", "CIE-Android")
            if (authenticated) setRequestProperty("x-cie-install-key", installKey)
            if (body != null) doOutput = true
        }
        try {
            if (body != null) c.outputStream.use { it.write(body.toString().toByteArray(StandardCharsets.UTF_8)) }
            val code = c.responseCode
            val stream = if (code in 200..299) c.inputStream else c.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) throw IllegalStateException("CIE API $code")
            return if (text.isBlank()) JSONObject() else JSONObject(text)
        } finally { c.disconnect() }
    }
}

fun normalizePhone(raw: String?): String {
    val value = raw.orEmpty().trim()
    val digits = value.filter(Char::isDigit)
    return if (value.startsWith("+")) "+$digits" else digits
}

fun normalizeSender(raw: String): String = raw.trim().uppercase(Locale.ROOT).filter { it.isLetterOrDigit() || it == '+' }
