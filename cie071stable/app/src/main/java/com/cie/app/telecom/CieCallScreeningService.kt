package com.cie.app.telecom

import android.telecom.Call
import android.telecom.CallScreeningService
import com.cie.app.CieTrustEngine
import com.cie.app.IdentityNetworkStore
import com.cie.app.StableRepository

/**
 * Local-first decision path.
 * Android binds this service when CIE holds ROLE_CALL_SCREENING, even when the UI
 * is not open. The decision is kept local so it can return well inside Android's
 * five-second screening deadline.
 */
class CieCallScreeningService : CallScreeningService() {
    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart.orEmpty()
        val repo = StableRepository(applicationContext)
        val normalized = StableRepository.normalizePhone(number)
        val networkIdentity = runCatching {
            IdentityNetworkStore(applicationContext).resolve("phone", normalized)
        }.getOrNull()

        // Existing company policy decision + the CIE trust layer. Government bypass
        // requires a network-attested identity; fraud/scam metadata can override allow.
        val decision = CieTrustEngine.hardenCall(
            repo.decideCall(number),
            networkIdentity
        )

        runCatching {
            repo.recordCallEvent(
                companyName = decision.label,
                blocked = decision.block,
                confidence = decision.resolution?.confidence ?: networkIdentity?.confidence
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
