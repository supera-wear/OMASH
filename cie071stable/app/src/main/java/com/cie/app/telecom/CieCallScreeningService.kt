package com.cie.app.telecom

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.Connection
import com.cie.app.CieCommercialCallEngine
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
            Connection.VERIFICATION_STATUS_PASSED -> "carrier verified"
            Connection.VERIFICATION_STATUS_FAILED -> "carrier verification failed"
            else -> "carrier not verified"
        }

        // Carrier/STIR verification is transport evidence, not company identity by itself.
        val runtimeIdentity = cachedIdentity?.copy(
            networkAttested = verificationStatus == Connection.VERIFICATION_STATUS_PASSED,
            riskScore = if (verificationStatus == Connection.VERIFICATION_STATUS_FAILED) {
                maxOf(cachedIdentity.riskScore, 0.98)
            } else cachedIdentity.riskScore,
            purpose = if (verificationStatus == Connection.VERIFICATION_STATUS_FAILED) {
                "spoof_suspected"
            } else cachedIdentity.purpose
        )

        val hardened = CieTrustEngine.hardenCall(repo.decideCall(number), runtimeIdentity)
        val result = CieCommercialCallEngine.evaluate(
            repo = repo,
            baseDecision = hardened,
            identity = runtimeIdentity,
            carrierVerificationStatus = verificationStatus
        )

        val purposeLabel = result.purpose.name.lowercase().replace('_', ' ')
        val activityLabel = "${result.label} · $purposeLabel · $carrierLabel · ${result.reason}"

        runCatching {
            repo.recordCallEvent(
                companyName = activityLabel,
                blocked = result.block,
                confidence = result.confidence
            )
        }

        respondToCall(
            callDetails,
            CallResponse.Builder()
                .setDisallowCall(result.block)
                .setRejectCall(result.block)
                .setSilenceCall(result.block)
                .setSkipCallLog(false)
                .setSkipNotification(result.block)
                .build()
        )
    }
}
