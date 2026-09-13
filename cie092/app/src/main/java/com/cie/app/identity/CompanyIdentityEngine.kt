package com.cie.app.identity

import java.util.Locale

data class CompanyIdentity(
    val canonicalName: String,
    val phonePrefixes: Set<String>,
    val senderIds: Set<String>,
    val defaultAction: ProtectionAction
)

enum class ProtectionAction { ALLOW, SILENCE, BLOCK }

data class MatchResult(
    val company: CompanyIdentity?,
    val confidence: Double,
    val action: ProtectionAction,
    val reason: String
)

object CompanyIdentityEngine {
    private val companies = listOf(
        CompanyIdentity("DemoTel Telekom", setOf("+90555010"), setOf("DEMOTEL"), ProtectionAction.ALLOW),
        CompanyIdentity("Sample Retail", setOf("+90555020"), setOf("SAMPLERT"), ProtectionAction.BLOCK),
        CompanyIdentity("Bank Services", setOf("+90555030"), setOf("BANKSRV"), ProtectionAction.ALLOW)
    )

    fun matchPhone(rawNumber: String?): MatchResult {
        val number = normalizePhone(rawNumber)
        if (number.isBlank()) return MatchResult(null, 0.0, ProtectionAction.ALLOW, "No caller identifier")
        val match = companies.firstOrNull { company -> company.phonePrefixes.any { number.startsWith(it) } }
        return if (match != null) {
            MatchResult(match, 0.98, match.defaultAction, "Matched verified company range")
        } else {
            MatchResult(null, 0.0, ProtectionAction.ALLOW, "Unknown caller; fail-open")
        }
    }

    fun matchSenderId(rawSenderId: String?): MatchResult {
        val sender = rawSenderId.orEmpty().trim().uppercase(Locale.ROOT).replace(Regex("[^A-Z0-9]"), "")
        val match = companies.firstOrNull { company -> company.senderIds.any { it == sender } }
        return if (match != null) MatchResult(match, 0.97, match.defaultAction, "Matched verified sender ID")
        else MatchResult(null, 0.0, ProtectionAction.ALLOW, "Unknown sender")
    }

    private fun normalizePhone(raw: String?): String {
        val value = raw.orEmpty().trim()
        if (value.isBlank()) return ""
        val plus = value.startsWith("+")
        val digits = value.filter { it.isDigit() }
        return if (plus) "+$digits" else digits
    }
}
