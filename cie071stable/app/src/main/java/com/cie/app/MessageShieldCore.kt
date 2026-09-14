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
    val confidence: Double,
    val verified: Boolean = false,
    val category: String = "",
    val entityType: String = "",
    val purpose: String = "unknown",
    val riskScore: Double = 0.0
)

data class MessageDecision(
    val senderType: String,
    val normalizedSender: String,
    val companyId: String?,
    val companyName: String?,
    val identityConfidence: Double?,
    val kind: MessageKind,
    val blocked: Boolean,
    val reason: String,
    val threatLevel: CieTrustEngine.ThreatLevel = CieTrustEngine.ThreatLevel.UNKNOWN,
    val riskScore: Double = 0.0,
    val commercialIntent: Double = 0.0
)

/** Permission-free Message Shield policy engine backed by the CIE Identity Network cache. */
object MessageShieldEngine {
    private const val MIN_IDENTITY_CONFIDENCE = 0.95
    private const val STRONG_COMMERCIAL_INTENT = 0.72

    fun evaluate(
        repo: StableRepository,
        identityNetwork: IdentityNetworkStore,
        sender: String,
        body: String
    ): MessageDecision {
        val (senderType, normalizedSender) = normalizeSender(sender)
        val kind = classify(body)
        val identity = identityNetwork.resolve(senderType, normalizedSender)
        val assessment = CieTrustEngine.assessMessage(identity, normalizedSender, body, kind)

        // Verified official government communication is always allowed. A sender that
        // merely claims to be government does NOT receive this bypass.
        if (assessment.verifiedGovernment) {
            return MessageDecision(
                senderType = senderType,
                normalizedSender = normalizedSender,
                companyId = identity?.companyId,
                companyName = identity?.companyName,
                identityConfidence = identity?.confidence,
                kind = kind,
                blocked = false,
                reason = assessment.reason,
                threatLevel = assessment.threat,
                riskScore = assessment.riskScore,
                commercialIntent = assessment.commercialIntent
            )
        }

        // Fraud / impersonation has priority over content type. A scammer saying
        // "verification code" must not be automatically trusted as SECURITY.
        if (assessment.threat == CieTrustEngine.ThreatLevel.SCAM) {
            return MessageDecision(
                senderType = senderType,
                normalizedSender = normalizedSender,
                companyId = identity?.companyId,
                companyName = identity?.companyName,
                identityConfidence = identity?.confidence,
                kind = kind,
                blocked = true,
                reason = assessment.reason,
                threatLevel = assessment.threat,
                riskScore = assessment.riskScore,
                commercialIntent = assessment.commercialIntent
            )
        }

        // CIE can identify strong commercial intent even when the sender has not yet
        // been mapped to a company. This is intentionally stricter than the old
        // fail-open marketing behavior, while security/transactional messages remain.
        if (identity == null) {
            val strongMarketing = kind == MessageKind.MARKETING && assessment.commercialIntent >= STRONG_COMMERCIAL_INTENT
            return MessageDecision(
                senderType = senderType,
                normalizedSender = normalizedSender,
                companyId = null,
                companyName = null,
                identityConfidence = null,
                kind = kind,
                blocked = strongMarketing,
                reason = when {
                    strongMarketing -> "Strong commercial intent detected from an unverified sender."
                    assessment.threat == CieTrustEngine.ThreatLevel.SUSPICIOUS -> assessment.reason
                    identityNetwork.isFresh() -> "Sender is not linked to a verified company. Necessary communication remains allowed."
                    else -> "Identity Network cache is not synced or is stale. Necessary communication remains allowed."
                },
                threatLevel = assessment.threat,
                riskScore = assessment.riskScore,
                commercialIntent = assessment.commercialIntent
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
                reason = "Company match is below the Message Shield action threshold. CIE preserves the message.",
                threatLevel = assessment.threat,
                riskScore = assessment.riskScore,
                commercialIntent = assessment.commercialIntent
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
                reason = "Company identity is known, but the local policy cache is not synced yet.",
                threatLevel = assessment.threat,
                riskScore = assessment.riskScore,
                commercialIntent = assessment.commercialIntent
            )

        // Necessary messages are preserved unless the trust engine already identified
        // a scam/impersonation above.
        if (kind == MessageKind.SECURITY || kind == MessageKind.TRANSACTIONAL || kind == MessageKind.SERVICE) {
            return MessageDecision(
                senderType = senderType,
                normalizedSender = normalizedSender,
                companyId = company.id,
                companyName = company.name,
                identityConfidence = identity.confidence,
                kind = kind,
                blocked = false,
                reason = "Necessary service, security and transactional communication is preserved.",
                threatLevel = assessment.threat,
                riskScore = assessment.riskScore,
                commercialIntent = assessment.commercialIntent
            )
        }

        val explicitMarketingPurpose = identity.purpose.equals("marketing", true) ||
            identity.purpose.equals("sales", true) || identity.purpose.equals("promotion", true)
        val shouldBlock = (kind == MessageKind.MARKETING || explicitMarketingPurpose) &&
            repo.shouldBlockCompany(company.id, CommunicationChannel.MARKETING_SMS)

        return MessageDecision(
            senderType = senderType,
            normalizedSender = normalizedSender,
            companyId = company.id,
            companyName = company.name,
            identityConfidence = identity.confidence,
            kind = kind,
            blocked = shouldBlock,
            reason = when {
                shouldBlock -> "Commercial sender belongs to ${company.name}, and that company is blocked."
                kind == MessageKind.MARKETING || explicitMarketingPurpose -> "Commercial sender belongs to ${company.name}, but that company is allowed."
                assessment.threat == CieTrustEngine.ThreatLevel.SUSPICIOUS -> assessment.reason
                else -> "Sender belongs to ${company.name}; this message type remains allowed."
            },
            threatLevel = assessment.threat,
            riskScore = assessment.riskScore,
            commercialIntent = assessment.commercialIntent
        )
    }

    fun classify(body: String): MessageKind {
        val text = body.lowercase(Locale.ROOT)
        if (text.isBlank()) return MessageKind.UNKNOWN

        val security = listOf(
            "otp", "verification code", "security code", "security verification",
            "doğrulama kodu", "dogrulama kodu", "güvenlik doğrulama", "guvenlik dogrulama",
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
