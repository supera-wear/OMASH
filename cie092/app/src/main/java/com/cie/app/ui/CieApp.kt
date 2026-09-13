package com.cie.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun CieApp(
    isCallScreeningRoleHeld: () -> Boolean,
    requestCallScreeningRole: ((Boolean) -> Unit) -> Unit
) {
    var tab by remember { mutableStateOf(CieTab.Shield) }
    var showSettings by remember { mutableStateOf(false) }
    var roleHeld by remember { mutableStateOf(isCallScreeningRoleHeld()) }
    var status by remember { mutableStateOf<String?>(null) }
    var companies by remember {
        mutableStateOf(
            listOf(
                Company("DemoTel Telekom", "Telecom", false),
                Company("Sample Retail", "Retail", true),
                Company("Bank Services", "Financial services", false)
            )
        )
    }

    Scaffold(
        containerColor = Background,
        bottomBar = { if (!showSettings) CieBottomBar(tab) { tab = it } }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (showSettings) {
                SettingsScreen(onBack = { showSettings = false }, onStatus = { status = it })
            } else {
                when (tab) {
                    CieTab.Shield -> ShieldScreen(
                        roleHeld = roleHeld,
                        blockedCount = companies.count { it.blocked },
                        status = status,
                        onSettings = { showSettings = true },
                        onOpenBlocked = { tab = CieTab.Blocked },
                        onEnableCalls = {
                            requestCallScreeningRole { ok ->
                                roleHeld = ok
                                status = if (ok) "Call Shield active" else "Permission not granted"
                            }
                        }
                    )
                    CieTab.Blocked -> BlockedScreen(companies) { index, blocked ->
                        companies = companies.toMutableList().also { list ->
                            list[index] = list[index].copy(blocked = blocked)
                        }
                        status = if (blocked) "Company blocked" else "Company allowed"
                    }
                    CieTab.Activity -> ActivityScreen()
                }
            }
        }
    }
}
