package com.cie.app

import android.app.role.RoleManager
import android.content.Context
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class MainActivity076 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.WHITE, AndroidColor.WHITE)
        )
        setContent { Cie076App(this) }
    }
}

@Composable
private fun Cie076App(context: Context) {
    val repo = remember { StableRepository(context) }
    val scope = rememberCoroutineScope()
    var screen by rememberSaveable { mutableStateOf(ModernScreen076.SHIELD) }
    var companies by remember { mutableStateOf(repo.cachedCompanies()) }
    var loading by remember { mutableStateOf(false) }
    var online by remember { mutableStateOf<Boolean?>(null) }
    var status by remember { mutableStateOf("CIE is ready") }
    var activityVersion by remember { mutableIntStateOf(0) }

    fun sync(showSuccess: Boolean = true) {
        scope.launch {
            loading = true
            runCatching { repo.sync() }
                .onSuccess {
                    companies = it
                    online = true
                    if (showSuccess) status = "CIE is up to date"
                }
                .onFailure {
                    companies = repo.cachedCompanies()
                    online = false
                    status = repo.humanError(it)
                }
            loading = false
        }
    }

    LaunchedEffect(Unit) { sync(showSuccess = false) }

    Cie076Theme {
        Scaffold(
            containerColor = Cie076Colors.Background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (screen in listOf(ModernScreen076.SHIELD, ModernScreen076.BLOCKED, ModernScreen076.ACTIVITY)) {
                    Cie076BottomBar(screen) { screen = it }
                }
            }
        ) { padding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Cie076Colors.Background)
                    .padding(bottom = padding.calculateBottomPadding())
            ) {
                when (screen) {
                    ModernScreen076.SHIELD -> Cie076ShieldScreen(
                        context = context,
                        repo = repo,
                        companies = companies,
                        loading = loading,
                        onSettings = { screen = ModernScreen076.SETTINGS },
                        onActivity = { activityVersion++; screen = ModernScreen076.ACTIVITY }
                    )
                    ModernScreen076.BLOCKED -> Cie076BlockedScreen(
                        companies = companies,
                        loading = loading,
                        onSettings = { screen = ModernScreen076.SETTINGS },
                        onReport = { screen = ModernScreen076.REPORT },
                        onToggle = { company, blocked ->
                            scope.launch {
                                loading = true
                                runCatching { repo.setPolicy(company.id, blocked) }
                                    .onSuccess {
                                        companies = it
                                        status = if (blocked) "${company.name} is blocked" else "${company.name} is allowed"
                                    }
                                    .onFailure { status = repo.humanError(it) }
                                loading = false
                            }
                        }
                    )
                    ModernScreen076.ACTIVITY -> Cie076ActivityScreen(
                        events = repo.recentEvents().also { activityVersion },
                        onSettings = { screen = ModernScreen076.SETTINGS },
                        onClear = { repo.clearActivity(); activityVersion++ }
                    )
                    ModernScreen076.SETTINGS -> Cie076SettingsScreen(
                        repo = repo,
                        online = online,
                        status = status,
                        loading = loading,
                        onBack = { screen = ModernScreen076.SHIELD },
                        onSync = { sync(true) },
                        onHealthCheck = {
                            scope.launch {
                                online = repo.serviceHealthy()
                                status = if (online == true) "CIE service is online" else "CIE service is unreachable"
                            }
                        },
                        onReport = { screen = ModernScreen076.REPORT }
                    )
                    ModernScreen076.REPORT -> Cie076ReportScreen(
                        loading = loading,
                        onBack = { screen = ModernScreen076.BLOCKED },
                        onSubmit = { type, value, category, reason ->
                            scope.launch {
                                loading = true
                                runCatching { repo.submitReport(type, value, category, reason) }
                                    .onSuccess {
                                        status = "Report received"
                                        screen = ModernScreen076.BLOCKED
                                    }
                                    .onFailure { status = repo.humanError(it) }
                                loading = false
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun Cie076ShieldScreen(
    context: Context,
    repo: StableRepository,
    companies: List<CompanyRow>,
    loading: Boolean,
    onSettings: () -> Unit,
    onActivity: () -> Unit
) {
    var callRole by remember { mutableStateOf(cie076HasCallRole(context)) }
    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        callRole = cie076HasCallRole(context)
    }
    val events = repo.recentEvents()
    val blockedCount = companies.count { it.blocked }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Cie076Hero(callRole, loading, onSettings) }
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text(
                    if (callRole) "Protection active" else "Protection needs setup",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.9).sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "CIE recognizes the company behind incoming business calls and applies your company-wide block policy.",
                    color = Cie076Colors.Muted,
                    fontSize = 15.sp,
                    lineHeight = 21.sp
                )
            }
        }
        item {
            Box(Modifier.padding(horizontal = 20.dp)) {
                Cie076Card(container = androidx.compose.ui.graphics.Color(0xFFF0F7FF)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("CIE Shield", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(3.dp))
                            Text(if (callRole) "On-device call protection is on" else "Enable Android call screening", color = Cie076Colors.Muted, fontSize = 13.sp)
                        }
                        Cie076Toggle(callRole) { enabled ->
                            if (enabled && !callRole) cie076RequestCallRole(context)?.let(roleLauncher::launch)
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Cie076MetricTile(blockedCount.toString(), "Blocked", Modifier.weight(1f))
                        Cie076MetricTile(events.size.toString(), "Calls", Modifier.weight(1f))
                    }
                }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("Recent", fontSize = 23.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp)
                Spacer(Modifier.weight(1f))
                if (events.isNotEmpty()) TextButton(onClick = onActivity) { Text("View all") }
            }
        }
        if (events.isEmpty()) {
            item {
                Box(Modifier.padding(horizontal = 20.dp)) {
                    Cie076EmptyState("No call activity yet", withPhoneIcon = true)
                }
            }
        } else {
            items(events.take(3), key = { "${it.timestamp}-${it.companyName}-${it.blocked}" }) {
                Box(Modifier.padding(horizontal = 20.dp)) { Cie076ActivityRow(it) }
            }
        }
    }
}

private fun cie076HasCallRole(context: Context): Boolean {
    val manager = context.getSystemService(RoleManager::class.java) ?: return false
    return manager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) && manager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
}

private fun cie076RequestCallRole(context: Context): android.content.Intent? {
    val manager = context.getSystemService(RoleManager::class.java) ?: return null
    return if (manager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) && !manager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
        manager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
    } else null
}
