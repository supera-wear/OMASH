package com.cie.app

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class IdentityNetworkStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("cie_identity_network_v1", Context.MODE_PRIVATE)

    suspend fun sync(): Int = withContext(Dispatchers.IO) {
        val root = request("/v1/identities")
        check(root.optBoolean("ok", false)) { "Identity Network unavailable" }
        val entries = root.optJSONArray("entries") ?: JSONArray()
        val safe = JSONArray()
        for (i in 0 until entries.length()) {
            val item = entries.optJSONObject(i) ?: continue
            val type = item.optString("signal_type")
            val normalized = item.optString("normalized_value")
            val companyId = item.optString("company_id")
            val companyName = item.optString("company_name")
            val confidence = item.optDouble("confidence", 0.0)
            if (type !in ALLOWED_TYPES || normalized.isBlank() || companyId.isBlank() || companyName.isBlank()) continue
            safe.put(JSONObject().apply {
                put("signalType", type)
                put("normalizedValue", normalized)
                put("companyId", companyId)
                put("companyName", companyName)
                put("confidence", confidence)
            })
        }
        prefs.edit()
            .putString(KEY_CACHE, safe.toString())
            .putLong(KEY_SYNCED_AT, System.currentTimeMillis())
            .apply()
        safe.length()
    }

    fun cachedSignals(): List<MessageIdentitySignal> = runCatching {
        val array = JSONArray(prefs.getString(KEY_CACHE, "[]"))
        (0 until array.length()).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            val type = item.optString("signalType")
            val normalized = item.optString("normalizedValue")
            val companyId = item.optString("companyId")
            val companyName = item.optString("companyName")
            if (type.isBlank() || normalized.isBlank() || companyId.isBlank() || companyName.isBlank()) return@mapNotNull null
            MessageIdentitySignal(
                signalType = type,
                normalizedValue = normalized,
                companyId = companyId,
                companyName = companyName,
                confidence = item.optDouble("confidence", 0.0)
            )
        }
    }.getOrDefault(emptyList())

    fun resolve(signalType: String, normalizedValue: String): MessageIdentitySignal? {
        if (!isFresh()) return null
        val candidates = cachedSignals()
            .filter { it.signalType == signalType && it.normalizedValue == normalizedValue }
            .sortedByDescending { it.confidence }
        val best = candidates.firstOrNull() ?: return null
        val conflicting = candidates.drop(1).firstOrNull { it.companyId != best.companyId }
        if (conflicting != null && best.confidence - conflicting.confidence < AMBIGUITY_MARGIN) return null
        return best
    }

    fun cachedCount(): Int = cachedSignals().size
    fun lastSync(): Long = prefs.getLong(KEY_SYNCED_AT, 0L)
    fun isFresh(now: Long = System.currentTimeMillis()): Boolean {
        val synced = lastSync()
        return synced > 0L && now >= synced && now - synced <= CACHE_MAX_AGE_MS
    }

    private fun request(path: String): JSONObject {
        val base = BuildConfig.CIE_IDENTITY_BASE_URL.trimEnd('/')
        require(base.startsWith("https://"))
        val connection = (URL(base + path).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 5000
            readTimeout = 5000
            setRequestProperty("Accept", "application/json")
        }
        return try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) error("Identity Network HTTP $code")
            JSONObject(body)
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        private val ALLOWED_TYPES = setOf("phone", "sender_id", "short_code", "domain", "brand")
        private const val KEY_CACHE = "identity_cache"
        private const val KEY_SYNCED_AT = "identity_synced_at"
        private const val CACHE_MAX_AGE_MS = 7L * 24 * 60 * 60 * 1000
        private const val AMBIGUITY_MARGIN = 0.03
    }
}
