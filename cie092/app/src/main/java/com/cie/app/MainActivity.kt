package com.cie.app

import android.app.role.RoleManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.cie.app.ui.CieApp
import com.cie.app.ui.CieTheme

class MainActivity : ComponentActivity() {
    private var onRoleResult: ((Boolean) -> Unit)? = null

    private val roleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        onRoleResult?.invoke(result.resultCode == RESULT_OK)
        onRoleResult = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CieTheme {
                CieApp(
                    isCallScreeningRoleHeld = { isCallScreeningRoleHeld() },
                    requestCallScreeningRole = { callback -> requestCallScreeningRole(callback) }
                )
            }
        }
    }

    private fun isCallScreeningRoleHeld(): Boolean {
        val manager = getSystemService(Context.ROLE_SERVICE) as RoleManager
        return manager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) && manager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    private fun requestCallScreeningRole(callback: (Boolean) -> Unit) {
        val manager = getSystemService(Context.ROLE_SERVICE) as RoleManager
        if (!manager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
            callback(false)
            return
        }
        if (manager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
            callback(true)
            return
        }
        onRoleResult = callback
        roleLauncher.launch(manager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
    }
}
