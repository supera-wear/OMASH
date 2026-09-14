package com.cie.app

import android.telecom.Connection
import java.util.Locale

/**
 * Real-time call purpose engine for CIE Shield.
 *
 * Identity answers WHO is calling. Purpose answers WHY that verified identity is
 * calling. Carrier verification is transport evidence and can override a claimed
 * identity when the network explicitly reports verification failure.
 */
object CieCommercialCallEngine {
    enum class Purpose {
        GOVERNMENT,
        SECURITY,
        TRANSACTIONAL,
        SERVICE,
        MARKETING,
        FRAUD,
        UNKNOWN
    }

    data class Result(
        val block: Boolean,
        val purpose: Purpose,
        val label: String,
        val reason: String,
        val confidence: Double?
    )

    fun evaluate(
        repo: StableRepository,
        baseDecision: CallDecision,
        identity: MessageIdentitySignal?,
        carrierVerificationStatus: Int
    ): Result {
        if (carrierVerificationStatus == Connection.VERIFICATION_STATUS_FAILED) {
            return Result(
                block = true,
                purpose = Purpose.FRAUD,
                label = identity?.companyName ?: baseDecision.label,
                reason = "Carrier verification failed. CIE detected a possible spoofed caller identity.",
                confidence = identity?.confidence ?: baseDecision.resolution?.confidence
            )
        }

        if (identity == null) {
            return Result(
                block = baseDecision.block,
                purpose = purposeFromCategory(baseDecision.resolution?.category.orEmpty()),
                label = baseDecision.label,
                reason = if (baseDecision.block) {
                    "Known company call blocked by your CIE company policy."
                } else {
                    "No verified call-purpose identity is available. CIE preserves the call."
                },
                confidence = baseDecision.resolution?.confidence
            )
        }

        val purpose = classifyPurpose(identity)
        val highConfidenceIdentity = identity.verified && identity.confidence >= 0.95

        if (purpose == Purpose.FRAUD || identity.riskScore >= 0.90) {
            return Result(
                block = true,
                purpose = Purpose.FRAUD,
                label = identity.companyName,
                reason = "CIE Identity Network marks this caller as high-risk, fraud or scam.",
                confidence = identity.confidence
            )
        }

        if (purpose == Purpose.GOVERNMENT) {
            // Real government identities are never blocked merely because a user has
            // blocked a company category. Explicit carrier FAILED was handled above.
            if (highConfidenceIdentity) {
                return Result(
                    block = false,
                    purpose = Purpose.GOVERNMENT,
                    label = identity.companyName,
                    reason = if (carrierVerificationStatus == Connection.VERIFICATION_STATUS_PASSED) {
                        "Verified government identity with positive carrier verification. Always allowed."
                    } else {
                        "Verified official government identity. Carrier did not provide positive attestation, but did not report failure. Allowed."
                    },
                    confidence = identity.confidence
                )
            }
            return Result(
                block = baseDecision.block,
                purpose = Purpose.GOVERNMENT,
                label = identity.companyName,
                reason = "Government claim is not verified strongly enough for a trust bypass.",
                confidence = identity.confidence
            )
        }

        if (purpose in setOf(Purpose.SECURITY, Purpose.TRANSACTIONAL, Purpose.SERVICE) && highConfidenceIdentity) {
            return Result(
                block = false,
                purpose = purpose,
                label = identity.companyName,
                reason = when (purpose) {
                    Purpose.SECURITY -> "Verified security or account-protection call. Preserved."
                    Purpose.TRANSACTIONAL -> "Verified transactional call. Preserved."
                    else -> "Verified service/support call. Preserved."
                },
                confidence = identity.confidence
            )
        }

        if (purpose == Purpose.MARKETING && highConfidenceIdentity) {
            val companyBlocked = repo.shouldBlockCompany(identity.companyId, CommunicationChannel.CALL)
            return Result(
                block = companyBlocked,
                purpose = Purpose.MARKETING,
                label = identity.companyName,
                reason = if (companyBlocked) {
                    "Verified sales/marketing call from ${identity.companyName}. Company is blocked by your CIE policy."
                } else {
                    "Verified sales/marketing call from ${identity.companyName}. Company is currently allowed."
                },
                confidence = identity.confidence
            )
        }

        return Result(
            block = baseDecision.block,
            purpose = purpose,
            label = identity.companyName,
            reason = if (baseDecision.block) {
                "Verified company identity matched and your company policy blocks calls from this company."
            } else {
                "Verified identity matched, but the call purpose is not classified as blocked commercial traffic."
            },
            confidence = identity.confidence
        )
    }

    fun classifyPurpose(identity: MessageIdentitySignal): Purpose {
        val purpose = identity.purpose.lowercase(Locale.ROOT).replace('-', '_').replace(' ', '_')
        val category = identity.category.lowercase(Locale.ROOT)
        val type = identity.entityType.lowercase(Locale.ROOT)
        val haystack = "$purpose $category $type"

        return when {
            containsAny(haystack, FRAUD_TERMS) || identity.riskScore >= 0.90 -> Purpose.FRAUD
            containsAny(haystack, GOVERNMENT_TERMS) -> Purpose.GOVERNMENT
            containsAny(haystack, SECURITY_TERMS) -> Purpose.SECURITY
            containsAny(haystack, TRANSACTIONAL_TERMS) -> Purpose.TRANSACTIONAL
            containsAny(haystack, SERVICE_TERMS) -> Purpose.SERVICE
            containsAny(haystack, MARKETING_TERMS) -> Purpose.MARKETING
            else -> Purpose.UNKNOWN
        }
    }

    private fun purposeFromCategory(category: String): Purpose {
        val normalized = category.lowercase(Locale.ROOT)
        return when {
            containsAny(normalized, FRAUD_TERMS) -> Purpose.FRAUD
            containsAny(normalized, GOVERNMENT_TERMS) -> Purpose.GOVERNMENT
            containsAny(normalized, SECURITY_TERMS) -> Purpose.SECURITY
            containsAny(normalized, TRANSACTIONAL_TERMS) -> Purpose.TRANSACTIONAL
            containsAny(normalized, SERVICE_TERMS) -> Purpose.SERVICE
            containsAny(normalized, MARKETING_TERMS) -> Purpose.MARKETING
            else -> Purpose.UNKNOWN
        }
    }

    private fun containsAny(value: String, terms: Set<String>): Boolean = terms.any(value::contains)

    private val FRAUD_TERMS = setOf("fraud", "scam", "spoof", "impersonation", "dolandir", "dolandır")
    private val GOVERNMENT_TERMS = setOf("government", "government_service", "public", "official", "emergency")
    private val SECURITY_TERMS = setOf("security", "verification", "fraud_alert", "account_protection", "authentication")
    private val TRANSACTIONAL_TERMS = setOf("transactional", "transaction", "payment", "order", "delivery", "billing")
    private val SERVICE_TERMS = setOf("service", "customer_service", "support", "care", "retention", "technical_support")
    private val MARKETING_TERMS = setOf("marketing", "sales", "promotion", "campaign", "telemarketing", "acquisition", "offer")
}
