package com.cie.app

import java.util.Locale
import kotlin.math.max

/**
 * CIE trust/risk layer shared by Call Shield and Message Shield.
 *
 * Important security rule: a sender is never treated as government merely because
 * it *claims* to be government. Government bypass is only granted to a verified
 * Identity Network record with very high confidence.
 */
object CieTrustEngine {
    enum class EntityType { GOVERNMENT, FINANCIAL, COMMERCIAL, UTILITY, HEALTHCARE, OTHER, UNKNOWN }
    enum class ThreatLevel { SAFE, SUSPICIOUS, SCAM, UNKNOWN }

    data class MessageAssessment(
        val threat: ThreatLevel,
        val riskScore: Double,
        val commercialIntent: Double,
        val verifiedGovernment: Boolean,
        val reason: String
    )

    fun hardenCall(decision: CallDecision): CallDecision {
        val identity = decision.resolution ?: return decision

        // Government is allow-listed only after a strong verified identity match.
        if (isVerifiedGovernment(identity.companyName, identity.category, identity.confidence, verified = true)) {
            return decision.copy(block = false, label = identity.companyName)
        }

        // A verified Identity Network entry explicitly categorized as scam/fraud/spam
        // overrides a normal company policy and is blocked locally.
        if (isThreatCategory(identity.category) && identity.confidence >= 0.95) {
            return decision.copy(block = true, label = identity.companyName)
        }

        return decision
    }

    fun assessMessage(
        identity: MessageIdentitySignal?,
        sender: String,
        body: String,
        kind: MessageKind
    ): MessageAssessment {
        val text = body.lowercase(Locale.ROOT)
        val normalizedSender = sender.lowercase(Locale.ROOT)
        val commercial = commercialIntentScore(text, kind)

        val verifiedGovernment = identity != null && isVerifiedGovernment(
            identity.companyName,
            identity.category,
            identity.confidence,
            identity.verified
        )
        if (verifiedGovernment && identity.riskScore < 0.50) {
            return MessageAssessment(
                threat = ThreatLevel.SAFE,
                riskScore = identity.riskScore.coerceIn(0.0, 1.0),
                commercialIntent = commercial,
                verifiedGovernment = true,
                reason = "Verified government identity. CIE always allows official government communication."
            )
        }

        var risk = identity?.riskScore?.coerceIn(0.0, 1.0) ?: 0.0
        val claimsGovernment = containsAny(text + " " + normalizedSender, GOVERNMENT_CLAIMS)
        val claimsFinancial = containsAny(text + " " + normalizedSender, FINANCIAL_CLAIMS)
        val hasCredentialRequest = containsAny(text, CREDENTIAL_TERMS)
        val hasPaymentRequest = containsAny(text, PAYMENT_TERMS)
        val hasUrgency = containsAny(text, URGENCY_TERMS)
        val hasLink = LINK_REGEX.containsMatchIn(text)
        val riskyLink = RISKY_LINK_REGEX.containsMatchIn(text)

        if (identity?.purpose.equals("fraud", true) || identity?.purpose.equals("scam", true)) risk = max(risk, 0.98)
        if (identity?.entityType.equals("fraud", true) || identity?.entityType.equals("scam", true)) risk = max(risk, 0.98)
        if (identity != null && isThreatCategory(identity.category)) risk = max(risk, 0.95)

        // Impersonation: claiming government/bank identity is not enough. The sender must
        // resolve to the correct verified identity before it can be trusted.
        if (claimsGovernment && !verifiedGovernment) {
            risk += if (hasLink || hasCredentialRequest || hasPaymentRequest) 0.58 else 0.30
        }

        val verifiedFinancial = identity != null &&
            classifyEntity(identity.companyName, identity.category, identity.entityType) == EntityType.FINANCIAL &&
            identity.verified && identity.confidence >= 0.98
        if (claimsFinancial && !verifiedFinancial) {
            risk += if (hasLink || hasCredentialRequest || hasPaymentRequest) 0.48 else 0.22
        }

        if (hasCredentialRequest && hasLink) risk += 0.28
        if (hasPaymentRequest && hasUrgency) risk += 0.24
        if (riskyLink) risk += 0.30
        if (hasUrgency && (hasCredentialRequest || hasPaymentRequest)) risk += 0.16
        if (identity == null && kind == MessageKind.SECURITY && hasLink) risk += 0.18

        risk = risk.coerceIn(0.0, 1.0)
        val threat = when {
            risk >= 0.75 -> ThreatLevel.SCAM
            risk >= 0.45 -> ThreatLevel.SUSPICIOUS
            identity != null -> ThreatLevel.SAFE
            else -> ThreatLevel.UNKNOWN
        }

        val reason = when (threat) {
            ThreatLevel.SCAM -> "High-confidence scam or impersonation pattern detected."
            ThreatLevel.SUSPICIOUS -> "Sender or message contains suspicious identity signals."
            ThreatLevel.SAFE -> "Sender identity and message signals are consistent."
            ThreatLevel.UNKNOWN -> "No verified sender identity is available."
        }

        return MessageAssessment(threat, risk, commercial, false, reason)
    }

    fun commercialIntentScore(text: String, kind: MessageKind): Double {
        var score = if (kind == MessageKind.MARKETING) 0.45 else 0.0
        val normalized = text.lowercase(Locale.ROOT)
        MARKETING_STRONG.forEach { if (normalized.contains(it)) score += 0.22 }
        MARKETING_WEAK.forEach { if (normalized.contains(it)) score += 0.10 }
        if (normalized.contains('%')) score += 0.12
        return score.coerceIn(0.0, 1.0)
    }

    fun classifyEntity(companyName: String, category: String, explicitType: String? = null): EntityType {
        val explicit = explicitType.orEmpty().lowercase(Locale.ROOT)
        val haystack = "${companyName.lowercase(Locale.ROOT)} ${category.lowercase(Locale.ROOT)} $explicit"
        return when {
            containsAny(haystack, GOVERNMENT_CLAIMS) || explicit == "government" || explicit == "public" -> EntityType.GOVERNMENT
            containsAny(haystack, FINANCIAL_CLAIMS) || explicit == "financial" || explicit == "bank" -> EntityType.FINANCIAL
            containsAny(haystack, listOf("electric", "energy", "water", "gas", "utility", "elektrik", "su", "doğalgaz", "dogalgaz")) -> EntityType.UTILITY
            containsAny(haystack, listOf("hospital", "clinic", "health", "hastane", "sağlık", "saglik")) -> EntityType.HEALTHCARE
            category.isNotBlank() -> EntityType.COMMERCIAL
            else -> EntityType.UNKNOWN
        }
    }

    fun isVerifiedGovernment(
        companyName: String,
        category: String,
        confidence: Double,
        verified: Boolean
    ): Boolean {
        return verified && confidence >= 0.995 &&
            classifyEntity(companyName, category, "") == EntityType.GOVERNMENT
    }

    private fun isThreatCategory(category: String): Boolean {
        val value = category.lowercase(Locale.ROOT)
        return value.contains("fraud") || value.contains("scam") || value.contains("spam") ||
            value.contains("dolandır") || value.contains("dolandir")
    }

    private fun containsAny(text: String, terms: List<String>): Boolean = terms.any(text::contains)

    private val LINK_REGEX = Regex("(?i)(https?://|www\\.|[a-z0-9-]+\\.(com|net|org|co|xyz|top|site|online|click|link))")
    private val RISKY_LINK_REGEX = Regex("(?i)(bit\\.ly|tinyurl\\.com|t\\.co|cutt\\.ly|rb\\.gy|xn--|\\.xyz\\b|\\.top\\b|\\.click\\b)")

    private val GOVERNMENT_CLAIMS = listOf(
        "e-devlet", "edevlet", "turkiye.gov.tr", "gib", "vergi dairesi", "vergi", "sgk",
        "polis", "emniyet", "jandarma", "bakanlık", "bakanlik", "belediye", "kaymakamlık",
        "kaymakamlik", "valilik", "mahkeme", "adalet", "uyap", "devlet", "resmi kurum"
    )

    private val FINANCIAL_CLAIMS = listOf(
        "banka", "bankası", "bankasi", "bank", "kredi kartı", "kredi karti", "hesabınız",
        "hesabiniz", "iban", "eft", "havale", "swift"
    )

    private val CREDENTIAL_TERMS = listOf(
        "şifren", "sifren", "şifre", "sifre", "parola", "password", "otp", "doğrulama kodu",
        "dogrulama kodu", "verification code", "kimlik", "tc kimlik", "giriş yap", "giris yap"
    )

    private val PAYMENT_TERMS = listOf(
        "ödeme yap", "odeme yap", "borç", "borc", "ceza", "vergi öde", "vergi ode", "iban",
        "havale", "transfer", "payment", "pay now", "kart bilgisi", "kart bilgin"
    )

    private val URGENCY_TERMS = listOf(
        "hemen", "acil", "son uyarı", "son uyari", "24 saat", "hesabınız kapan", "hesabiniz kapan",
        "bloke", "iptal edilecek", "yasal işlem", "yasal islem", "urgent", "immediately", "suspended"
    )

    private val MARKETING_STRONG = listOf(
        "kampanya", "indirim", "fırsat", "firsat", "özel teklif", "ozel teklif", "size özel",
        "size ozel", "hemen alın", "hemen alin", "satın al", "satin al", "campaign", "discount",
        "special offer", "buy now", "sale"
    )

    private val MARKETING_WEAK = listOf(
        "teklif", "kazanın", "kazanin", "avantaj", "hediye", "puan", "kupon", "coupon", "offer"
    )
}
