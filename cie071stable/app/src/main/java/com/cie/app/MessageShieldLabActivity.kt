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
        setContent { MessageShieldLab(this) }
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
    var status by remember { mutableStateOf(if (identityNetwork.isFresh()) "Identity Network ready" else "Identity Network needs sync") }
    var syncing by remember { mutableStateOf(false) }

    fun syncNetwork() {
        scope.launch {
            syncing = true
            runCatching { identityNetwork.sync() }
                .onSuccess {
                    cachedCount = it
                    status = "Identity Network synced"
                }
                .onFailure {
                    cachedCount = identityNetwork.cachedCount()
                    status = if (cachedCount > 0) "Using cached identities" else "Identity Network unavailable"
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
        Box(
            Modifier.fillMaxSize().background(LabColors.Background).safeDrawingPadding()
        ) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Message Shield Lab", fontSize = 30.sp, fontWeight = FontWeight.Bold)
                        Text("CIE 0.7.5 Identity Network", color = LabColors.Muted, fontSize = 13.sp)
                    }
                    TextButton(onClick = { activity.finish() }) { Text("Close") }
                }

                FlatLabCard {
                    Text(status, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("$cachedCount verified identities cached locally", color = LabColors.Muted, fontSize = 12.sp)
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { syncNetwork() },
                        enabled = !syncing,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text(if (syncing) "Syncing…" else "Sync identity network") }
                }

                FlatLabCard {
                    Text("Permission-free simulation", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "This lab does not read your SMS. Sender identities come from CIE's verified backend cache; message content stays on this phone.",
                        color = LabColors.Muted,
                        lineHeight = 20.sp
                    )
                }

                FlatLabCard {
                    Text("Test message", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = sender,
                        onValueChange = { sender = it; result = null },
                        label = { Text("Sender ID or phone number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                    val first = identityNetwork.cachedSignals().firstOrNull { it.signalType == "sender_id" || it.signalType == "phone" || it.signalType == "short_code" }
                    if (first != null) {
                        TextButton(onClick = { sender = first.normalizedValue; result = null }) {
                            Text("Use cached identity: ${first.companyName}")
                        }
                    }
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it; result = null },
                        label = { Text("Message") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                body = "Size özel %25 indirim kampanyası. Hemen yararlanın."
                                result = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("Marketing") }
                        OutlinedButton(
                            onClick = {
                                body = "Güvenlik doğrulama mesajı."
                                result = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("Security") }
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { result = MessageShieldEngine.evaluate(repo, identityNetwork, sender, body) },
                        enabled = sender.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
                    ) { Text("Run identity test") }
                }

                result?.let { decision ->
                    FlatLabCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(decision.companyName ?: "Unknown company", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text(decision.companyId ?: "No company ID match", color = LabColors.Muted, fontSize = 11.sp)
                            }
                            Surface(
                                color = if (decision.blocked) Color(0xFFFFECEC) else Color(0xFFEAF8F1),
                                shape = RoundedCornerShape(99.dp),
                                shadowElevation = 0.dp,
                                tonalElevation = 0.dp
                            ) {
                                Text(
                                    if (decision.blocked) "BLOCK" else "ALLOW",
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
                        LabDetail("Sender type", decision.senderType)
                        LabDetail("Normalized", decision.normalizedSender.ifBlank { "—" })
                        LabDetail("Message type", decision.kind.name.lowercase().replaceFirstChar { it.uppercase() })
                        LabDetail("Identity confidence", decision.identityConfidence?.let { "${(it * 100).toInt()}%" } ?: "Not matched")
                        Spacer(Modifier.height(8.dp))
                        Text(decision.reason, color = LabColors.Muted, lineHeight = 20.sp)
                    }
                }

                FlatLabCard {
                    Text("Company-wide policy", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Verified phone numbers, sender IDs and short codes can point to one company ID. Block the company once and every trusted identity inherits the same marketing policy.",
                        color = LabColors.Muted,
                        lineHeight = 20.sp
                    )
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
