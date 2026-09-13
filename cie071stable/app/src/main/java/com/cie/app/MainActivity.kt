package com.cie.app

import android.app.role.RoleManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

enum class AppScreen { SHIELD, BLOCKED, ACTIVITY, SETTINGS, REPORT }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CieApp(this) }
    }
}

@Composable
fun CieApp(context: Context) {
    val repo = remember { StableRepository(context) }
    val scope = rememberCoroutineScope()
    var screen by rememberSaveable { mutableStateOf(AppScreen.SHIELD) }
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

    CieTheme {
        Scaffold(
            containerColor = CieColors.Background,
            bottomBar = {
                if (screen in listOf(AppScreen.SHIELD, AppScreen.BLOCKED, AppScreen.ACTIVITY)) {
                    CieBottomBar(screen) { screen = it }
                }
            }
        ) { padding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .background(CieColors.Background)
                    .padding(padding)
            ) {
                when (screen) {
                    AppScreen.SHIELD -> ShieldScreen(
                        context = context,
                        repo = repo,
                        companies = companies,
                        status = status,
                        loading = loading,
                        onSettings = { screen = AppScreen.SETTINGS },
                        onActivity = { activityVersion++; screen = AppScreen.ACTIVITY }
                    )
                    AppScreen.BLOCKED -> BlockedScreen(
                        companies = companies,
                        loading = loading,
                        onSettings = { screen = AppScreen.SETTINGS },
                        onReport = { screen = AppScreen.REPORT },
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
                    AppScreen.ACTIVITY -> ActivityScreen(
                        events = repo.recentEvents().also { activityVersion },
                        onSettings = { screen = AppScreen.SETTINGS },
                        onClear = { repo.clearActivity(); activityVersion++ }
                    )
                    AppScreen.SETTINGS -> SettingsScreen(
                        repo = repo,
                        online = online,
                        status = status,
                        loading = loading,
                        onBack = { screen = AppScreen.SHIELD },
                        onSync = { sync(true) },
                        onHealthCheck = {
                            scope.launch {
                                online = repo.serviceHealthy()
                                status = if (online == true) "CIE service is online" else "CIE service is unreachable"
                            }
                        },
                        onReport = { screen = AppScreen.REPORT }
                    )
                    AppScreen.REPORT -> ReportScreen(
                        loading = loading,
                        onBack = { screen = AppScreen.BLOCKED },
                        onSubmit = { type, value, category, reason ->
                            scope.launch {
                                loading = true
                                runCatching { repo.submitReport(type, value, category, reason) }
                                    .onSuccess {
                                        status = "Report received"
                                        screen = AppScreen.BLOCKED
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

private object CieColors {
    val Background = Color(0xFFF7F7F5)
    val Card = Color.White
    val Text = Color(0xFF111318)
    val Muted = Color(0xFF737780)
    val Border = Color(0xFFE2E4E8)
    val Blue = Color(0xFF0B63CE)
    val BlueSoft = Color(0xFFEAF3FF)
    val Green = Color(0xFF1B8A57)
    val Red = Color(0xFFC64848)
    val Track = Color(0xFFE8EAED)
}

@Composable
private fun CieTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = CieColors.Blue,
            onPrimary = Color.White,
            background = CieColors.Background,
            surface = CieColors.Card,
            onBackground = CieColors.Text,
            onSurface = CieColors.Text,
            outline = CieColors.Border
        ),
        content = content
    )
}

@Composable
private fun MainTopBar(onSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("CIE", fontSize = 25.sp, fontWeight = FontWeight.Black, color = CieColors.Text, letterSpacing = (-1).sp)
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onSettings) { Text("Settings", color = CieColors.Text, fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
private fun BackTopBar(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) { Text("‹ Back", color = CieColors.Blue) }
        Spacer(Modifier.weight(1f))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
private fun ShieldScreen(
    context: Context,
    repo: StableRepository,
    companies: List<CompanyRow>,
    status: String,
    loading: Boolean,
    onSettings: () -> Unit,
    onActivity: () -> Unit
) {
    var callRole by remember { mutableStateOf(hasCallRole(context)) }
    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        callRole = hasCallRole(context)
    }
    val events = repo.recentEvents()
    val blockedCount = companies.count { it.blocked }

    LazyColumn(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        contentPadding = PaddingValues(24.dp, 12.dp, 24.dp, 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { MainTopBar(onSettings) }
        item {
            Text(if (callRole) "Protection active" else "Protection needs setup", fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp)
            Spacer(Modifier.height(6.dp))
            Text("CIE is screening incoming business calls on this phone.", color = CieColors.Muted, fontSize = 15.sp, lineHeight = 21.sp)
        }
        item { StatusNotice(if (loading) "Updating CIE…" else status) }
        item {
            FlatCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("CIE Shield", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(if (callRole) "On-device call protection is on" else "Enable Android call screening", color = CieColors.Muted, fontSize = 13.sp)
                    }
                    FlatToggle(callRole) { enabled ->
                        if (enabled && !callRole) requestCallRole(context)?.let(roleLauncher::launch)
                    }
                }
                Spacer(Modifier.height(18.dp))
                HorizontalDivider(color = CieColors.Border)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth()) {
                    MetricSimple(blockedCount.toString(), "Blocked", Modifier.weight(1f))
                    Box(Modifier.width(1.dp).height(44.dp).background(CieColors.Border))
                    MetricSimple(events.size.toString(), "Calls", Modifier.weight(1f))
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Recent", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                if (events.isNotEmpty()) TextButton(onClick = onActivity) { Text("View all") }
            }
        }
        if (events.isEmpty()) item { EmptyState("No call activity yet") }
        else items(events.take(3), key = { "${it.timestamp}-${it.companyName}-${it.blocked}" }) { ActivityRow(it) }
    }
}

@Composable
private fun BlockedScreen(
    companies: List<CompanyRow>,
    loading: Boolean,
    onSettings: () -> Unit,
    onReport: () -> Unit,
    onToggle: (CompanyRow, Boolean) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val shown = remember(companies, query) {
        companies.filter { query.isBlank() || it.name.contains(query, true) || it.legalName.contains(query, true) || it.category.contains(query, true) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        contentPadding = PaddingValues(24.dp, 12.dp, 24.dp, 112.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { MainTopBar(onSettings) }
        item {
            Text("Blocked", fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("Block a verified company across its known phone numbers.", color = CieColors.Muted, lineHeight = 21.sp)
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search companies") },
                shape = RoundedCornerShape(16.dp)
            )
        }
        if (loading && companies.isEmpty()) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        items(shown, key = { it.id }) { company -> CompanyToggleCard(company, onToggle) }
        if (!loading && shown.isEmpty()) item { EmptyState("No companies match your search") }
        item { TextButton(onClick = onReport, modifier = Modifier.fillMaxWidth()) { Text("Report sender") } }
    }
}

@Composable
private fun ActivityScreen(events: List<CallEvent>, onSettings: () -> Unit, onClear: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        contentPadding = PaddingValues(24.dp, 12.dp, 24.dp, 112.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { MainTopBar(onSettings) }
        item {
            Text("Activity", fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("What CIE allowed or blocked on this phone.", color = CieColors.Muted, lineHeight = 21.sp)
        }
        if (events.isEmpty()) item { EmptyState("No activity yet") }
        items(events, key = { "${it.timestamp}-${it.companyName}-${it.blocked}" }) { ActivityRow(it) }
        if (events.isNotEmpty()) item { TextButton(onClick = onClear, modifier = Modifier.fillMaxWidth()) { Text("Clear local activity") } }
    }
}

@Composable
private fun SettingsScreen(
    repo: StableRepository,
    online: Boolean?,
    status: String,
    loading: Boolean,
    onBack: () -> Unit,
    onSync: () -> Unit,
    onHealthCheck: () -> Unit,
    onReport: () -> Unit
) {
    var testNumber by rememberSaveable { mutableStateOf(repo.testNumber().orEmpty()) }
    var testStatus by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        contentPadding = PaddingValues(24.dp, 12.dp, 24.dp, 36.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { BackTopBar("Settings", onBack) }
        item { StatusNotice(status) }
        item {
            FlatCard {
                Text("Sync", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(if (online == true) "CIE service online" else if (online == false) "Using local protection data" else "Service status not checked", color = CieColors.Muted)
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FlatPrimaryButton(if (loading) "Syncing…" else "Sync now", !loading, Modifier.weight(1f), onSync)
                    OutlinedButton(onClick = onHealthCheck, shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f)) { Text("Check service") }
                }
            }
        }
        item {
            FlatCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Message Shield", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Separate Play build", color = CieColors.Muted, fontSize = 13.sp)
                    }
                    FlatBadge("PLAY BUILD")
                }
                Spacer(Modifier.height(10.dp))
                Text("Kept out of CIE Stable so this install stays call-only and does not request SMS permissions.", color = CieColors.Muted, lineHeight = 20.sp)
            }
        }
        item {
            FlatCard {
                Text("Beta Test Lab", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("Use a second phone number to prove Call Shield end-to-end.", color = CieColors.Muted)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = testNumber,
                    onValueChange = { testNumber = it },
                    label = { Text("Second phone number") },
                    placeholder = { Text("+90 5xx xxx xx xx") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FlatPrimaryButton("Arm test", true, Modifier.weight(1f)) {
                        runCatching { repo.setTestNumber(testNumber) }
                            .onSuccess { testStatus = "Test number armed" }
                            .onFailure { testStatus = it.message ?: "Invalid number" }
                    }
                    OutlinedButton(onClick = { repo.clearTestNumber(); testNumber = ""; testStatus = "Test cleared" }, shape = RoundedCornerShape(14.dp), modifier = Modifier.weight(1f)) { Text("Clear") }
                }
                if (testStatus != null) Text(testStatus!!, color = CieColors.Green, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }
        item {
            FlatCard {
                Text("Privacy & local data", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("Call decisions use a local identity cache. Unknown or stale identities fail open. This Stable build has no SMS, contacts or call-log permissions.", color = CieColors.Muted, lineHeight = 20.sp)
                Spacer(Modifier.height(10.dp))
                Text("${repo.cachedCallCount()} cached call identities", color = CieColors.Muted, fontSize = 12.sp)
            }
        }
        item {
            FlatCard {
                Text("Help improve CIE", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("Report an unwanted sender for review. A report never auto-blocks a company by itself.", color = CieColors.Muted)
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = onReport, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("Report sender") }
            }
        }
        item { Text("CIE 0.7.1 Stable", color = CieColors.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp)) }
    }
}

@Composable
private fun ReportScreen(loading: Boolean, onBack: () -> Unit, onSubmit: (String, String, String, String) -> Unit) {
    var type by rememberSaveable { mutableStateOf("phone") }
    var value by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("spam") }
    var reason by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        contentPadding = PaddingValues(24.dp, 12.dp, 24.dp, 36.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { BackTopBar("Report sender", onBack) }
        item { Text("Reports help CIE verify company identity. One report never auto-blocks anyone.", color = CieColors.Muted, lineHeight = 20.sp) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("phone" to "Phone", "sender_id" to "Sender ID", "short_code" to "Short code").forEach { (key, label) ->
                    FilterChip(selected = type == key, onClick = { type = key }, label = { Text(label) })
                }
            }
        }
        item { OutlinedTextField(value, { value = it }, Modifier.fillMaxWidth(), label = { Text("Sender") }, singleLine = true, shape = RoundedCornerShape(16.dp)) }
        item { OutlinedTextField(category, { category = it }, Modifier.fillMaxWidth(), label = { Text("Category") }, singleLine = true, shape = RoundedCornerShape(16.dp)) }
        item { OutlinedTextField(reason, { reason = it }, Modifier.fillMaxWidth(), label = { Text("Why are you reporting this?") }, minLines = 4, shape = RoundedCornerShape(16.dp)) }
        item {
            FlatPrimaryButton(if (loading) "Submitting…" else "Submit report", !loading && value.isNotBlank(), Modifier.fillMaxWidth()) {
                onSubmit(type, value.trim(), category.trim().ifBlank { "spam" }, reason.trim())
            }
        }
    }
}

@Composable
private fun CieBottomBar(current: AppScreen, onSelect: (AppScreen) -> Unit) {
    Surface(
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, CieColors.Border)
    ) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().height(68.dp).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            BottomItem("Shield", "◉", current == AppScreen.SHIELD) { onSelect(AppScreen.SHIELD) }
            BottomItem("Blocked", "⊘", current == AppScreen.BLOCKED) { onSelect(AppScreen.BLOCKED) }
            BottomItem("Activity", "◷", current == AppScreen.ACTIVITY) { onSelect(AppScreen.ACTIVITY) }
        }
    }
}

@Composable
private fun RowScope.BottomItem(label: String, icon: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.weight(1f).fillMaxHeight().clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(icon, color = if (selected) CieColors.Blue else CieColors.Muted, fontSize = 18.sp)
        Spacer(Modifier.height(2.dp))
        Text(label, color = if (selected) CieColors.Blue else CieColors.Muted, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun FlatCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CieColors.Card),
        border = BorderStroke(1.dp, CieColors.Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp)
    ) { Column(Modifier.padding(18.dp), content = content) }
}

@Composable
private fun StatusNotice(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CieColors.BlueSoft),
        border = BorderStroke(1.dp, Color(0xFFD7E8FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp), color = CieColors.Blue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FlatToggle(checked: Boolean, onChange: (Boolean) -> Unit) {
    Box(
        Modifier
            .width(50.dp)
            .height(30.dp)
            .background(if (checked) CieColors.Blue else CieColors.Track, RoundedCornerShape(99.dp))
            .clickable { onChange(!checked) }
            .padding(3.dp)
    ) {
        Box(
            Modifier
                .size(24.dp)
                .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
                .background(Color.White, RoundedCornerShape(99.dp))
        )
    }
}

@Composable
private fun FlatBadge(text: String) {
    Box(Modifier.background(CieColors.BlueSoft, RoundedCornerShape(10.dp)).padding(horizontal = 9.dp, vertical = 5.dp)) {
        Text(text, color = CieColors.Blue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MetricSimple(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(label, color = CieColors.Muted, fontSize = 12.sp)
    }
}

@Composable
private fun CompanyToggleCard(company: CompanyRow, onToggle: (CompanyRow, Boolean) -> Unit) {
    FlatCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(company.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (company.verified) {
                        Spacer(Modifier.width(8.dp))
                        Text("VERIFIED", color = CieColors.Blue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(company.category.replaceFirstChar { it.uppercase() }, color = CieColors.Muted, fontSize = 12.sp)
            }
            FlatToggle(company.blocked) { onToggle(company, it) }
        }
    }
}

@Composable
private fun ActivityRow(event: CallEvent) {
    FlatCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(9.dp).background(if (event.blocked) CieColors.Red else CieColors.Green, RoundedCornerShape(99.dp)))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(event.companyName ?: "Unknown caller", fontWeight = FontWeight.Bold)
                Text(shortDate(event.timestamp), color = CieColors.Muted, fontSize = 11.sp)
            }
            Text(if (event.blocked) "Blocked" else "Allowed", color = if (event.blocked) CieColors.Red else CieColors.Green, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EmptyState(text: String) { FlatCard { Text(text, color = CieColors.Muted, modifier = Modifier.padding(vertical = 8.dp)) } }

@Composable
private fun FlatPrimaryButton(text: String, enabled: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
    ) { Text(text, fontWeight = FontWeight.Bold) }
}

private fun hasCallRole(context: Context): Boolean {
    val manager = context.getSystemService(RoleManager::class.java) ?: return false
    return manager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) && manager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
}

private fun requestCallRole(context: Context): android.content.Intent? {
    val manager = context.getSystemService(RoleManager::class.java) ?: return null
    return if (manager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) && !manager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
        manager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
    } else null
}

private fun shortDate(timestamp: Long): String = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(timestamp))
