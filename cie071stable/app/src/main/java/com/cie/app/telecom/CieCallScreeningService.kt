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

        // STIR verification is transport evidence, not identity by itself.
        // PASSED can promote an already verified CIE identity to network-attested.
        // FAILED is treated as a strong spoof signal and never whitelists a display name.
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

        val activityLabel = buildString {
            append(decision.label)
            append(" · ")
            append(carrierLabel)
            when {
                verificationStatus == Connection.VERIFICATION_STATUS_FAILED -> append(" · spoof risk")
                runtimeIdentity?.entityType.equals("government", true) && verificationStatus == Connection.VERIFICATION_STATUS_PASSED -> append(" · government verified")
                runtimeIdentity != null -> append(" · identity matched")
            }
        }

        runCatching {
            repo.recordCallEvent(
                companyName = activityLabel,
                blocked = decision.block,
                confidence = decision.resolution?.confidence ?: runtimeIdentity?.confidence
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
