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
    var languageSelection by remember { mutableStateOf(CieLanguageStore.selection(context)) }
    CieLocalizedContent(languageSelection) {
        Cie076LocalizedApp(
            context = context,
            languageSelection = languageSelection,
            onLanguageChange = { selection ->
                CieLanguageStore.setSelection(context, selection)
                languageSelection = selection
            }
        )
    }
}

@Composable
private fun Cie076LocalizedApp(
    context: Context,
    languageSelection: String,
    onLanguageChange: (String) -> Unit
) {
    val repo = remember { StableRepository(context) }
    val scope = rememberCoroutineScope()
    var screen by rememberSaveable { mutableStateOf(ModernScreen076.SHIELD) }
    var companies by remember { mutableStateOf(repo.cachedCompanies()) }
    var loading by remember { mutableStateOf(false) }
    var online by remember { mutableStateOf<Boolean?>(null) }
    var statusKey by remember { mutableStateOf("status_ready") }
    var statusArg by remember { mutableStateOf<String?>(null) }
    var activityVersion by remember { mutableIntStateOf(0) }

    val status = statusArg?.let { cieText(statusKey, it) } ?: cieText(statusKey)

    fun setErrorStatus(error: Throwable) {
        val raw = repo.humanError(error)
        statusArg = null
        statusKey = when {
            raw.contains("No internet", ignoreCase = true) -> "error_no_internet"
            raw.contains("too long", ignoreCase = true) || raw.contains("timeout", ignoreCase = true) -> "error_timeout"
            else -> "error_sync"
        }
    }

    fun sync(showSuccess: Boolean = true) {
        scope.launch {
            loading = true
            runCatching { repo.sync() }
                .onSuccess {
                    companies = it
                    online = true
                    if (showSuccess) {
                        statusKey = "status_up_to_date"
                        statusArg = null
                    }
                }
                .onFailure {
                    companies = repo.cachedCompanies()
                    online = false
                    setErrorStatus(it)
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
                                        statusKey = if (blocked) "status_company_blocked" else "status_company_allowed"
                                        statusArg = company.name
                                    }
                                    .onFailure { setErrorStatus(it) }
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
                        languageSelection = languageSelection,
                        currentLanguage = LocalCieLanguage.current,
                        onLanguageChange = onLanguageChange,
                        onBack = { screen = ModernScreen076.SHIELD },
                        onSync = { sync(true) },
                        onHealthCheck = {
                            scope.launch {
                                online = repo.serviceHealthy()
                                statusKey = if (online == true) "status_service_online" else "status_service_unreachable"
                                statusArg = null
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
                                        statusKey = "status_report_received"
                                        statusArg = null
                                        screen = ModernScreen076.BLOCKED
                                    }
                                    .onFailure { setErrorStatus(it) }
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

    Column(Modifier.fillMaxSize()) {
        Cie076Hero(callRole, loading, onSettings)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        if (callRole) cieText("protection_active") else cieText("protection_needs_setup"),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.9).sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        cieText("shield_description"),
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
                                Text(
                                    if (callRole) cieText("on_device_on") else cieText("enable_android_screening"),
                                    color = Cie076Colors.Muted,
                                    fontSize = 13.sp
                                )
                            }
                            Cie076Toggle(callRole) { enabled ->
                                if (enabled && !callRole) cie076RequestCallRole(context)?.let(roleLauncher::launch)
                            }
                        }
                        Spacer(Modifier.height(18.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Cie076MetricTile(blockedCount.toString(), cieText("blocked"), Modifier.weight(1f))
                            Cie076MetricTile(events.size.toString(), cieText("calls"), Modifier.weight(1f))
                        }
                    }
                }
            }
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(cieText("recent"), fontSize = 23.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp)
                    Spacer(Modifier.weight(1f))
                    if (events.isNotEmpty()) TextButton(onClick = onActivity) { Text(cieText("view_all")) }
                }
            }
            if (events.isEmpty()) {
                item {
                    Box(Modifier.padding(horizontal = 20.dp)) {
                        Cie076EmptyState(cieText("no_call_activity"), withPhoneIcon = true)
                    }
                }
            } else {
                items(events.take(3), key = { "${it.timestamp}-${it.companyName}-${it.blocked}" }) {
                    Box(Modifier.padding(horizontal = 20.dp)) { Cie076ActivityRow(it) }
                }
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
