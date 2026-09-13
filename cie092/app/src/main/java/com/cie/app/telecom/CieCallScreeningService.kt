package com.cie.app.telecom

import android.telecom.Call
import android.telecom.CallScreeningService
import com.cie.app.core.CieRepository

class CieCallScreeningService : CallScreeningService() {
    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart
        val repo = CieRepository(applicationContext)
        val (blocked, companyName) = runCatching { repo.lookupCall(number) }.getOrDefault(false to null)

        val response = CallResponse.Builder()
            .setDisallowCall(blocked)
            .setRejectCall(blocked)
            .setSilenceCall(false)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()

        respondToCall(callDetails, response)
        if (companyName != null || blocked) {
            runCatching { repo.recordCallEvent(companyName, blocked) }
        }
    }
}
