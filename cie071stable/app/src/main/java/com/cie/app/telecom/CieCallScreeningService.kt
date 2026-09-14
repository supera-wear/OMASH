package com.cie.app.telecom

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.Connection
import com.cie.app.CieTrustEngine
import com.cie.app.IdentityNetworkStore
import com.cie.app.StableRepository

/**
 * Local-first decision path. Android binds this service while CIE holds the
 * call-screening role, so the UI does not need to be open.
 */
class CieCallScreeningService : CallScreeningService() {
    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart.orEmpty()
        val repo = StableRepository(applicationContext)
        val normalized = StableRepository.normalizePhone(number)
        val cachedIdentity = runCatching {
            IdentityNetworkStore(applicationContext).resolve("phone", normalized)
        }.getOrNull()

        val verificationStatus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            callDetails.callerNumberVerificationStatus
        } else {
            Connection.VERIFICATION_STATUS_NOT_VERIFIED
        }

        val carrierLabel = when (verificationStatus) {
            Connection.VERIFICATION_STATUS_PASSED -> "Carrier verified"
            Connection.VERIFICATION_STATUS_FAILED -> "Carrier verification failed"
            else -> "Carrier not verified"
        }

        // STIR verification is treated as transport evidence, not as identity by itself.
        // A passed result can promote an already verified CIE identity to network-attested.
        // A failed result is a strong spoof signal and must never whitelist a displayed name.
        val runtimeIdentity = cachedIdentity?.copy(
            networkAttested = verificationStatus == Connection.VERIFICATION_STATUS_PASSED,
            riskScore = if (verificationStatus == Connection.VERIFICATION_STATUS_FAILED) {
                maxOf(cachedIdentity.riskScore, 0.98)
            } else {
                cachedIdentity.riskScore
            },
            purpose = if (verificationStatus == Connection.VERIFICATION_STATUS_FAILED) {
                "spoof_suspected"
            } else {
                cachedIdentity.purpose
            }
        )

        val baseDecision = CieTrustEngine.hardenCall(
            repo.decideCall(number),
            runtimeIdentity
        )

        val decision = if (verificationStatus == Connection.VERIFICATION_STATUS_FAILED) {
            baseDecision.copy(
                block = true,
                label = runtimeIdentity?.companyName ?: baseDecision.label
            )
        } else {
            baseDecision
        }

        val reason = when {
            verificationStatus == Connection.VERIFICATION_STATUS_FAILED -> "Possible caller-ID spoofing detected by the carrier"
            runtimeIdentity?.entityType.equals("government", true) && verificationStatus == Connection.VERIFICATION_STATUS_PASSED -> "Verified government identity and carrier verification passed"
            runtimeIdentity != null && decision.block -> "Verified company identity matched a blocked policy"
            runtimeIdentity != null -> "Verified company identity matched"
            else -> "No verified company identity available"
        }

        runCatching {
            repo.recordCallEvent(
                companyName = decision.label,
                blocked = decision.block,
                confidence = decision.resolution?.confidence ?: runtimeIdentity?.confidence,
                verification = carrierLabel,
                reason = reason
            )
        }

        respondToCall(
            callDetails,
            CallResponse.Builder()
                .setDisallowCall(decision.block)
                .setRejectCall(decision.block)
                .setSilenceCall(decision.block)
                .setSkipCallLog(false)
                .setSkipNotification(decision.block)
                .build()
        )
    }
}
