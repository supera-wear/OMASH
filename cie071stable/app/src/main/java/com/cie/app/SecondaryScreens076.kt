package com.cie.app

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun Cie076BlockedScreen(
    companies: List<CompanyRow>,
    loading: Boolean,
    onSettings: () -> Unit,
    onReport: () -> Unit,
    onToggle: (CompanyRow, Boolean) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val shown = remember(companies, query) {
        companies.filter {
            query.isBlank() || it.name.contains(query, true) || it.legalName.contains(query, true) || it.category.contains(query, true)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(20.dp, 10.dp, 20.dp, 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Cie076TopBar(onSettings) }
        item {
            Text("Blocked", fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp)
            Spacer(Modifier.height(6.dp))
            Text("Block a verified company across its known calls and marketing identities.", color = Cie076Colors.Muted, lineHeight = 21.sp)
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search companies") },
                shape = RoundedCornerShape(18.dp)
            )
        }
        if (loading && companies.isEmpty()) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        items(shown, key = { it.id }) { company -> Cie076CompanyCard(company, onToggle) }
        if (!loading && shown.isEmpty()) item { Cie076EmptyState("No companies match your search") }
        item { TextButton(onClick = onReport, modifier = Modifier.fillMaxWidth()) { Text("Report sender") } }
    }
}

@Composable
internal fun Cie076ActivityScreen(events: List<CallEvent>, onSettings: () -> Unit, onClear: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(20.dp, 10.dp, 20.dp, 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Cie076TopBar(onSettings) }
        item {
            Text("Activity", fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp)
            Spacer(Modifier.height(6.dp))
            Text("What CIE allowed or blocked on this phone.", color = Cie076Colors.Muted, lineHeight = 21.sp)
        }
        if (events.isEmpty()) item { Cie076EmptyState("No activity yet") }
        items(events, key = { "${it.timestamp}-${it.companyName}-${it.blocked}" }) { Cie076ActivityRow(it) }
        if (events.isNotEmpty()) item {
            TextButton(onClick = onClear, modifier = Modifier.fillMaxWidth()) { Text("Clear local activity") }
        }
    }
}

@Composable
internal fun Cie076SettingsScreen(
    repo: StableRepository,
    online: Boolean?,
    status: String,
    loading: Boolean,
    onBack: () -> Unit,
    onSync: () -> Unit,
    onHealthCheck: () -> Unit,
    onReport: () -> Unit
) {
    val context = LocalContext.current
    var testNumber by rememberSaveable { mutableStateOf(repo.testNumber().orEmpty()) }
    var testStatus by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        contentPadding = PaddingValues(20.dp, 10.dp, 20.dp, 36.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Cie076BackBar("Settings", onBack) }
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(listOf(Cie076Colors.BlueDeep, Cie076Colors.BlueLight)),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Text(status, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (online == false) "Local protection stays active" else "Company identity protection is ready",
                        color = Color.White.copy(alpha = 0.82f),
                        fontSize = 13.sp
                    )
                }
            }
        }
        item {
            Cie076Card {
                Text("Sync", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    if (online == true) "CIE service online" else if (online == false) "Using local protection data" else "Service status not checked",
                    color = Cie076Colors.Muted
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Cie076PrimaryButton(if (loading) "Syncing…" else "Sync now", !loading, Modifier.weight(1f), onSync)
                    OutlinedButton(
                        onClick = onHealthCheck,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) { Text("Check service") }
                }
            }
        }
        item {
            Cie076Card {
                Text("Message Shield", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Identity logic is active in the Message Lab. Real SMS interception remains reserved for the Play build.",
                    color = Cie076Colors.Muted,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { context.startActivity(Intent(context, MessageShieldLabActivity::class.java)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Open Message Lab")
                }
            }
        }
        item {
            Cie076Card {
                Text("Beta Test Lab", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("Use a second phone number to prove Call Shield end-to-end.", color = Cie076Colors.Muted)
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
                    Cie076PrimaryButton("Arm test", true, Modifier.weight(1f)) {
                        runCatching { repo.setTestNumber(testNumber) }
                            .onSuccess { testStatus = "Test number armed" }
                            .onFailure { testStatus = it.message ?: "Invalid number" }
                    }
                    OutlinedButton(
                        onClick = {
                            repo.clearTestNumber()
                            testNumber = ""
                            testStatus = "Test cleared"
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) { Text("Clear") }
                }
                if (testStatus != null) {
                    Text(testStatus!!, color = Cie076Colors.Green, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
        item {
            Cie076Card {
                Text("Privacy & local data", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Call decisions use a local identity cache. Unknown or stale identities fail open. This Stable build has no SMS, contacts or call-log permissions.",
                    color = Cie076Colors.Muted,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(10.dp))
                Text("${repo.cachedCallCount()} cached call identities", color = Cie076Colors.Muted, fontSize = 12.sp)
            }
        }
        item {
            Cie076Card {
                Text("Help improve CIE", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("Report an unwanted sender for review. A report never auto-blocks a company by itself.", color = Cie076Colors.Muted)
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = onReport, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Text("Report sender")
                }
            }
        }
        item {
            Text("CIE Shield 0.7.9", color = Cie076Colors.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
internal fun Cie076ReportScreen(
    loading: Boolean,
    onBack: () -> Unit,
    onSubmit: (String, String, String, String) -> Unit
) {
    var type by rememberSaveable { mutableStateOf("phone") }
    var value by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("spam") }
    var reason by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        contentPadding = PaddingValues(20.dp, 10.dp, 20.dp, 36.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Cie076BackBar("Report sender", onBack) }
        item {
            Text("Reports help CIE verify company identity. One report never auto-blocks anyone.", color = Cie076Colors.Muted, lineHeight = 20.sp)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("phone" to "Phone", "sender_id" to "Sender ID", "short_code" to "Short code").forEach { (key, label) ->
                    FilterChip(selected = type == key, onClick = { type = key }, label = { Text(label) })
                }
            }
        }
        item {
            OutlinedTextField(value, { value = it }, Modifier.fillMaxWidth(), label = { Text("Sender") }, singleLine = true, shape = RoundedCornerShape(16.dp))
        }
        item {
            OutlinedTextField(category, { category = it }, Modifier.fillMaxWidth(), label = { Text("Category") }, singleLine = true, shape = RoundedCornerShape(16.dp))
        }
        item {
            OutlinedTextField(reason, { reason = it }, Modifier.fillMaxWidth(), label = { Text("Why are you reporting this?") }, minLines = 4, shape = RoundedCornerShape(16.dp))
        }
        item {
            Cie076PrimaryButton(if (loading) "Submitting…" else "Submit report", !loading && value.isNotBlank(), Modifier.fillMaxWidth()) {
                onSubmit(type, value.trim(), category.trim().ifBlank { "spam" }, reason.trim())
            }
        }
    }
}
