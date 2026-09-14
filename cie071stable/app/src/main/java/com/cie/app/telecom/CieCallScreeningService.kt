package com.cie.app.telecom

import android.telecom.Call
import android.telecom.CallScreeningService
import com.cie.app.CieTrustEngine
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

        // Existing company policy decision + the trust layer. Verified government
        // identities are never blocked; verified fraud/scam identities are blocked.
        val decision = CieTrustEngine.hardenCall(repo.decideCall(number))

        runCatching {
            repo.recordCallEvent(
                companyName = decision.label,
                blocked = decision.block,
                confidence = decision.resolution?.confidence
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
