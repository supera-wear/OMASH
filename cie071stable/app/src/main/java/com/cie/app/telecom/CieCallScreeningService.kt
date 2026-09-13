package com.cie.app.telecom

import android.telecom.Call
import android.telecom.CallScreeningService
import com.cie.app.StableRepository

/**
 * Local-only decision path.
 * Identity resolves the caller to a company; the user's company policy decides block/allow.
 * Unknown, stale or ambiguous identities always fail open.
 */
class CieCallScreeningService : CallScreeningService() {
    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart.orEmpty()
        val repo = StableRepository(applicationContext)
        val decision = repo.decideCall(number)

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
