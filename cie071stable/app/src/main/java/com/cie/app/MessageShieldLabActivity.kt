package com.cie.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class MessageShieldLabActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val selection = remember { CieLanguageStore.selection(this) }
            CieLocalizedContent(selection) { MessageShieldLab(this) }
        }
    }
}

private object LabColors {
    val Background = Color(0xFFF7F7F5)
    val Card = Color.White
    val Text = Color(0xFF111318)
    val Muted = Color(0xFF737780)
    val Border = Color(0xFFE2E4E8)
    val Blue = Color(0xFF0B63CE)
    val Green = Color(0xFF1B8A57)
    val Red = Color(0xFFC64848)
}

@Composable
private fun MessageShieldLab(activity: ComponentActivity) {
    val repo = remember { StableRepository(activity) }
    val identityNetwork = remember { IdentityNetworkStore(activity) }
    val scope = rememberCoroutineScope()
    var sender by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("Size özel %25 indirim kampanyası. Hemen yararlanın.") }
    var result by remember { mutableStateOf<MessageDecision?>(null) }
    var cachedCount by remember { mutableIntStateOf(identityNetwork.cachedCount()) }
    var statusKey by remember { mutableStateOf(if (identityNetwork.isFresh()) "identity_ready" else "identity_needs_sync") }
    var syncing by remember { mutableStateOf(false) }

    fun syncNetwork() {
        scope.launch {
            syncing = true
            runCatching { identityNetwork.sync() }
                .onSuccess { cachedCount = it; statusKey = "identity_synced" }
                .onFailure {
                    cachedCount = identityNetwork.cachedCount()
                    statusKey = if (cachedCount > 0) "using_cached" else "identity_unavailable"
                }
            syncing = false
        }
    }

    LaunchedEffect(Unit) { syncNetwork() }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = LabColors.Blue,
            background = LabColors.Background,
            surface = LabColors.Card,
            onBackground = LabColors.Text,
            onSurface = LabColors.Text
        )
    ) {
        Column(Modifier.fillMaxSize().background(LabColors.Background)) {
            Cie076PageHero(cieText("message_lab")) { activity.finish() }
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FlatLabCard {
                    Text(cieText(statusKey), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(cieText("verified_cached", cachedCount), color = LabColors.Muted, fontSize = 12.sp)
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { syncNetwork() },
                        enabled = !syncing,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text(if (syncing) cieText("syncing") else cieText("sync_identity")) }
                }

                FlatLabCard {
                    Text(cieText("permission_free"), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(cieText("permission_free_desc"), color = LabColors.Muted, lineHeight = 20.sp)
                }

                FlatLabCard {
                    Text(cieText("test_message"), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = sender,
                        onValueChange = { sender = it; result = null },
                        label = { Text(cieText("sender_or_phone")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                    val first = identityNetwork.cachedSignals().firstOrNull { it.signalType == "sender_id" || it.signalType == "phone" || it.signalType == "short_code" }
                    if (first != null) {
                        TextButton(onClick = { sender = first.normalizedValue; result = null }) {
                            Text(cieText("use_cached_identity", first.companyName))
                        }
                    }
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it; result = null },
                        label = { Text(cieText("message")) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { body = "Size özel %25 indirim kampanyası. Hemen yararlanın."; result = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text(cieText("marketing")) }
                        OutlinedButton(
                            onClick = { body = "Güvenlik doğrulama mesajı."; result = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text(cieText("security")) }
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { result = MessageShieldEngine.evaluate(repo, identityNetwork, sender, body) },
                        enabled = sender.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
                    ) { Text(cieText("run_identity_test")) }
                }

                result?.let { decision ->
                    FlatLabCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(decision.companyName ?: cieText("unknown_company"), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text(decision.companyId ?: cieText("no_company_match"), color = LabColors.Muted, fontSize = 11.sp)
                            }
                            Surface(
                                color = if (decision.blocked) Color(0xFFFFECEC) else Color(0xFFEAF8F1),
                                shape = RoundedCornerShape(99.dp),
                                shadowElevation = 0.dp,
                                tonalElevation = 0.dp
                            ) {
                                Text(
                                    if (decision.blocked) cieText("block") else cieText("allow"),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    color = if (decision.blocked) LabColors.Red else LabColors.Green,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        HorizontalDivider(color = LabColors.Border)
                        Spacer(Modifier.height(12.dp))
                        LabDetail(cieText("sender_type"), decision.senderType)
                        LabDetail(cieText("normalized"), decision.normalizedSender.ifBlank { "—" })
                        LabDetail(cieText("message_type"), decision.kind.name.lowercase().replaceFirstChar { it.uppercase() })
                        LabDetail(cieText("identity_confidence"), decision.identityConfidence?.let { "${(it * 100).toInt()}%" } ?: cieText("not_matched"))
                        Spacer(Modifier.height(8.dp))
                        Text(decision.reason, color = LabColors.Muted, lineHeight = 20.sp)
                    }
                }

                FlatLabCard {
                    Text(cieText("company_policy"), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(cieText("company_policy_desc"), color = LabColors.Muted, lineHeight = 20.sp)
                }
            }
        }
    }
}

@Composable
private fun FlatLabCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LabColors.Card),
        border = BorderStroke(1.dp, LabColors.Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(18.dp), content = content)
    }
}

@Composable
private fun LabDetail(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = LabColors.Muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    }
    Spacer(Modifier.height(6.dp))
}
