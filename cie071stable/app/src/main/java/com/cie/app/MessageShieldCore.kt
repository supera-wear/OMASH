package com.cie.app

import java.util.Locale

enum class MessageKind {
    MARKETING,
    SECURITY,
    TRANSACTIONAL,
    SERVICE,
    UNKNOWN
}

data class MessageIdentitySignal(
    val signalType: String,
    val normalizedValue: String,
    val companyId: String,
    val companyName: String,
    val confidence: Double
)

data class MessageDecision(
    val senderType: String,
    val normalizedSender: String,
    val companyId: String?,
    val companyName: String?,
    val identityConfidence: Double?,
    val kind: MessageKind,
    val blocked: Boolean,
    val reason: String
)

/**
 * Permission-free Message Shield core.
 *
 * This class does not read SMS from Android. It only evaluates a supplied sender/body pair.
 * That lets CIE prove the identity -> company -> user policy -> action path without requesting
 * READ_SMS / RECEIVE_SMS / SEND_SMS in the stable sideload line.
 */
object MessageShieldEngine {
    private const val MIN_IDENTITY_CONFIDENCE = 0.95

    // Bootstrap snapshot of verified message-relevant identity signals already present in CIE.
    // Remote message-cache sync can replace/extend this list later without changing policy logic.
    private val bootstrapSignals = listOf(
        MessageIdentitySignal(
            signalType = "phone",
            normalizedValue = "+905551110001",
            companyId = "00000000-0000-4000-8000-000000000001",
            companyName = "DemoTel",
            confidence = 0.995
        ),
        MessageIdentitySignal(
            signalType = "sender_id",
            normalizedValue = "demotel",
            companyId = "00000000-0000-4000-8000-000000000001",
            companyName = "DemoTel",
            confidence = 0.995
        )
    )

    fun evaluate(repo: StableRepository, sender: String, body: String): MessageDecision {
        val (senderType, normalizedSender) = normalizeSender(sender)
        val kind = classify(body)
        val identity = bootstrapSignals.firstOrNull {
            it.signalType == senderType && it.normalizedValue == normalizedSender
        }

        if (identity == null) {
            return MessageDecision(
                senderType = senderType,
                normalizedSender = normalizedSender,
                companyId = null,
                companyName = null,
                identityConfidence = null,
                kind = kind,
                blocked = false,
                reason = "Sender is not yet linked to a verified company. CIE fails open."
            )
        }

        if (identity.confidence < MIN_IDENTITY_CONFIDENCE) {
            return MessageDecision(
                senderType = senderType,
                normalizedSender = normalizedSender,
                companyId = identity.companyId,
                companyName = identity.companyName,
                identityConfidence = identity.confidence,
                kind = kind,
                blocked = false,
                reason = "Company match is below the blocking threshold. CIE fails open."
            )
        }

        val company = repo.cachedCompanies().firstOrNull { it.id == identity.companyId }
            ?: return MessageDecision(
                senderType = senderType,
                normalizedSender = normalizedSender,
                companyId = identity.companyId,
                companyName = identity.companyName,
                identityConfidence = identity.confidence,
                kind = kind,
                blocked = false,
                reason = "Company identity is known, but the local policy cache is not synced yet."
            )

        if (kind == MessageKind.SECURITY || kind == MessageKind.TRANSACTIONAL) {
            return MessageDecision(
                senderType = senderType,
                normalizedSender = normalizedSender,
                companyId = company.id,
                companyName = company.name,
                identityConfidence = identity.confidence,
                kind = kind,
                blocked = false,
                reason = "Security and transactional messages are preserved in CIE Stable."
            )
        }

        val shouldBlock = kind == MessageKind.MARKETING && company.blockMarketingSms
        return MessageDecision(
            senderType = senderType,
            normalizedSender = normalizedSender,
            companyId = company.id,
            companyName = company.name,
            identityConfidence = identity.confidence,
            kind = kind,
            blocked = shouldBlock,
            reason = when {
                shouldBlock -> "Marketing sender belongs to ${company.name}, and that company is blocked."
                kind == MessageKind.MARKETING -> "Marketing sender belongs to ${company.name}, but that company is allowed."
                else -> "Sender belongs to ${company.name}; this message type stays allowed in Stable."
            }
        )
    }

    fun classify(body: String): MessageKind {
        val text = body.lowercase(Locale.ROOT)
        if (text.isBlank()) return MessageKind.UNKNOWN

        val security = listOf(
            "otp", "verification code", "security code", "doğrulama kodu", "dogrulama kodu",
            "tek kullanımlık", "tek kullanimlik", "şifre", "sifre", "giriş kodu", "giris kodu"
        )
        if (security.any(text::contains)) return MessageKind.SECURITY

        val transactional = listOf(
            "ödeme", "odeme", "sipariş", "siparis", "teslimat", "kargo", "işlem", "islem",
            "fatura", "receipt", "payment", "order", "delivery"
        )
        if (transactional.any(text::contains)) return MessageKind.TRANSACTIONAL

        val marketing = listOf(
            "kampanya", "indirim", "fırsat", "firsat", "teklif", "%", "kazanın", "kazanin",
            "hemen alın", "hemen alin", "sale", "discount", "offer", "campaign"
        )
        if (marketing.any(text::contains)) return MessageKind.MARKETING

        val service = listOf("duyuru", "bilgilendirme", "hatırlatma", "hatirlatma", "service", "notice")
        if (service.any(text::contains)) return MessageKind.SERVICE

        return MessageKind.UNKNOWN
    }

    fun normalizeSender(sender: String): Pair<String, String> {
        val value = sender.trim()
        val digits = value.filter(Char::isDigit)
        return when {
            digits.length >= 8 -> "phone" to StableRepository.normalizePhone(value)
            digits.isNotBlank() && digits.length < 8 -> "short_code" to digits
            else -> "sender_id" to value.filter(Char::isLetterOrDigit).lowercase(Locale.ROOT)
        }
    }
}
