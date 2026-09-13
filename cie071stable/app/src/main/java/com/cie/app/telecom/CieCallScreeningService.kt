package com.cie.app.telecom

import android.telecom.Call
import android.telecom.CallScreeningService
import com.cie.app.StableRepository

/** Local-only call decision path. Unknown and stale identities always fail open. */
class CieCallScreeningService : CallScreeningService() {
    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart.orEmpty()
        val repo = StableRepository(applicationContext)
        val (block, entry) = repo.lookupCall(number)

        runCatching {
            repo.recordCallEvent(
                companyName = entry?.companyName ?: StableRepository.maskPhone(number),
                blocked = block,
                confidence = entry?.confidence
            )
        }

        respondToCall(
            callDetails,
            CallResponse.Builder()
                .setDisallowCall(block)
                .setRejectCall(block)
                .setSilenceCall(block)
                .setSkipCallLog(false)
                .setSkipNotification(block)
                .build()
        )
    }
}
