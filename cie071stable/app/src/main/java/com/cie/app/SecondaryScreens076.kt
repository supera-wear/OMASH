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

    Column(Modifier.fillMaxSize()) {
        Cie076PageHero(cieText("blocked"), onSettings)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp, 14.dp, 20.dp, 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Text(cieText("blocked_desc"), color = Cie076Colors.Muted, lineHeight = 21.sp) }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(cieText("search_companies")) },
                    shape = RoundedCornerShape(18.dp)
                )
            }
            if (loading && companies.isEmpty()) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            items(shown, key = { it.id }) { company -> Cie076CompanyCard(company, onToggle) }
            if (!loading && shown.isEmpty()) item { Cie076EmptyState(cieText("no_companies")) }
            item { TextButton(onClick = onReport, modifier = Modifier.fillMaxWidth()) { Text(cieText("report_sender")) } }
        }
    }
}

@Composable
internal fun Cie076ActivityScreen(events: List<CallEvent>, onSettings: () -> Unit, onClear: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Cie076PageHero(cieText("activity"), onSettings)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp, 14.dp, 20.dp, 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Text(cieText("activity_desc"), color = Cie076Colors.Muted, lineHeight = 21.sp) }
            if (events.isEmpty()) item { Cie076EmptyState(cieText("no_activity")) }
            items(events, key = { "${it.timestamp}-${it.companyName}-${it.blocked}" }) { Cie076ActivityRow(it) }
            if (events.isNotEmpty()) item {
                TextButton(onClick = onClear, modifier = Modifier.fillMaxWidth()) { Text(cieText("clear_activity")) }
            }
        }
    }
}

@Composable
internal fun Cie076SettingsScreen(
    repo: StableRepository,
    online: Boolean?,
    status: String,
    loading: Boolean,
    languageSelection: String,
    currentLanguage: CieLanguage,
    onLanguageChange: (String) -> Unit,
    onBack: () -> Unit,
    onSync: () -> Unit,
    onHealthCheck: () -> Unit,
    onReport: () -> Unit
) {
    val context = LocalContext.current
    var testNumber by rememberSaveable { mutableStateOf(repo.testNumber().orEmpty()) }
    var testStatusKey by remember { mutableStateOf<String?>(null) }
    var languageMenuOpen by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Cie076PageHero(cieText("settings"), onBack)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp, 14.dp, 20.dp, 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
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
                            if (online == false) cieText("local_protection_active") else cieText("company_protection_ready"),
                            color = Color.White.copy(alpha = 0.82f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
            item {
                Cie076Card {
                    Text(cieText("language"), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(cieText("language_desc"), color = Cie076Colors.Muted, lineHeight = 20.sp)
                    Spacer(Modifier.height(12.dp))
                    Box(Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { languageMenuOpen = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                if (languageSelection == CieLanguageStore.SYSTEM) {
                                    "${cieText("language_system")} · ${currentLanguage.nativeName}"
                                } else currentLanguage.nativeName
                            )
                        }
                        DropdownMenu(
                            expanded = languageMenuOpen,
                            onDismissRequest = { languageMenuOpen = false },
                            modifier = Modifier.fillMaxWidth(0.88f)
                        ) {
                            DropdownMenuItem(
                                text = { Text(cieText("language_system")) },
                                onClick = {
                                    languageMenuOpen = false
                                    onLanguageChange(CieLanguageStore.SYSTEM)
                                }
                            )
                            CieLanguage.entries.forEach { language ->
                                DropdownMenuItem(
                                    text = { Text(language.nativeName) },
                                    onClick = {
                                        languageMenuOpen = false
                                        onLanguageChange(language.code)
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(cieText("language_current", currentLanguage.nativeName), color = Cie076Colors.Muted, fontSize = 12.sp)
                }
            }
            item {
                Cie076Card {
                    Text(cieText("sync"), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (online == true) cieText("service_online") else if (online == false) cieText("local_data") else cieText("status_not_checked"),
                        color = Cie076Colors.Muted
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Cie076PrimaryButton(if (loading) cieText("syncing") else cieText("sync_now"), !loading, Modifier.weight(1f), onSync)
                        OutlinedButton(
                            onClick = onHealthCheck,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) { Text(cieText("check_service")) }
                    }
                }
            }
            item {
                Cie076Card {
                    Text(cieText("message_shield"), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(CieSmsI18n.compose("live_desc"), color = Cie076Colors.Muted, lineHeight = 20.sp)
                    Spacer(Modifier.height(12.dp))
                    Cie076PrimaryButton(CieSmsI18n.compose("live_title"), true, Modifier.fillMaxWidth()) {
                        context.startActivity(Intent(context, MessageShieldLiveActivity::class.java))
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { context.startActivity(Intent(context, MessageShieldLabActivity::class.java)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text(cieText("open_message_lab")) }
                }
            }
            item {
                Cie076Card {
                    Text(cieText("beta_test_lab"), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(cieText("beta_test_desc"), color = Cie076Colors.Muted)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = testNumber,
                        onValueChange = { testNumber = it },
                        label = { Text(cieText("second_phone")) },
                        placeholder = { Text("+90 5xx xxx xx xx") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Cie076PrimaryButton(cieText("arm_test"), true, Modifier.weight(1f)) {
                            runCatching { repo.setTestNumber(testNumber) }
                                .onSuccess { testStatusKey = "test_armed" }
                                .onFailure { testStatusKey = "invalid_number" }
                        }
                        OutlinedButton(
                            onClick = {
                                repo.clearTestNumber()
                                testNumber = ""
                                testStatusKey = "test_cleared"
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) { Text(cieText("clear")) }
                    }
                    testStatusKey?.let {
                        Text(cieText(it), color = Cie076Colors.Green, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
            item {
                Cie076Card {
                    Text(cieText("privacy_local"), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        cieText("privacy_desc"),
                        color = Cie076Colors.Muted,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(cieText("cached_call_identities", repo.cachedCallCount()), color = Cie076Colors.Muted, fontSize = 12.sp)
                }
            }
            item {
                Cie076Card {
                    Text(cieText("help_improve"), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(cieText("help_improve_desc"), color = Cie076Colors.Muted)
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(onClick = onReport, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                        Text(cieText("report_sender"))
                    }
                }
            }
            item {
                Text("CIE Shield 0.8.2", color = Cie076Colors.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
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

    Column(Modifier.fillMaxSize()) {
        Cie076PageHero(cieText("report_sender"), onBack)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp, 14.dp, 20.dp, 36.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Text(cieText("report_desc"), color = Cie076Colors.Muted, lineHeight = 20.sp) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("phone" to cieText("phone"), "sender_id" to cieText("sender_id"), "short_code" to cieText("short_code")).forEach { (key, label) ->
                        FilterChip(selected = type == key, onClick = { type = key }, label = { Text(label) })
                    }
                }
            }
            item {
                OutlinedTextField(value, { value = it }, Modifier.fillMaxWidth(), label = { Text(cieText("sender")) }, singleLine = true, shape = RoundedCornerShape(16.dp))
            }
            item {
                OutlinedTextField(category, { category = it }, Modifier.fillMaxWidth(), label = { Text(cieText("category")) }, singleLine = true, shape = RoundedCornerShape(16.dp))
            }
            item {
                OutlinedTextField(reason, { reason = it }, Modifier.fillMaxWidth(), label = { Text(cieText("report_reason")) }, minLines = 4, shape = RoundedCornerShape(16.dp))
            }
            item {
                Cie076PrimaryButton(if (loading) cieText("submitting") else cieText("submit_report"), !loading && value.isNotBlank(), Modifier.fillMaxWidth()) {
                    onSubmit(type, value.trim(), category.trim().ifBlank { "spam" }, reason.trim())
                }
            }
        }
    }
}
