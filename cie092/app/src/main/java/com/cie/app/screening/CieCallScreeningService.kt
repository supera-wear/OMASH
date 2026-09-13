package com.cie.app.screening

import android.telecom.Call
import android.telecom.CallScreeningService
import com.cie.app.identity.CompanyIdentityEngine
import com.cie.app.identity.ProtectionAction

class CieCallScreeningService : CallScreeningService() {
    override fun onScreenCall(callDetails: Call.Details) {
        val result = CompanyIdentityEngine.matchPhone(callDetails.handle?.schemeSpecificPart)
        val response = when (result.action) {
            ProtectionAction.BLOCK -> CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build()
            ProtectionAction.SILENCE -> CallResponse.Builder()
                .setSilenceCall(true)
                .build()
            ProtectionAction.ALLOW -> CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)
                .build()
        }
        respondToCall(callDetails, response)
    }
}
