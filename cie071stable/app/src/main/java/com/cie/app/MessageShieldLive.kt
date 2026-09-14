package com.cie.app

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Telephony
import android.telephony.SmsManager
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * CIE Message Shield Live beta.
 *
 * Incoming SMS content is evaluated on-device. Message bodies are not uploaded by
 * this code. Commercial/scam messages are quarantined in CIE rather than deleted.
 */
data class CieShieldedMessage(
    val id: String,
    val sender: String,
    val body: String,
    val companyName: String?,
    val kind: MessageKind,
    val quarantined: Boolean,
    val reason: String,
    val timestamp: Long
)

class CieMessageStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("cie_message_shield_live_v1", Context.MODE_PRIVATE)

    @Synchronized
    fun add(message: CieShieldedMessage) {
        val current = all().toMutableList()
        current.add(0, message)
        while (current.size > MAX_MESSAGES) current.removeLast()
        val array = JSONArray()
        current.forEach { item ->
            array.put(JSONObject().apply {
                put("id", item.id)
                put("sender", item.sender)
                put("body", item.body)
                put("companyName", item.companyName ?: "")
                put("kind", item.kind.name)
                put("quarantined", item.quarantined)
                put("reason", item.reason)
                put("timestamp", item.timestamp)
            })
        }
        prefs.edit().putString(KEY_MESSAGES, array.toString()).apply()
    }

    fun all(): List<CieShieldedMessage> = runCatching {
        val array = JSONArray(prefs.getString(KEY_MESSAGES, "[]"))
        (0 until array.length()).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            CieShieldedMessage(
                id = item.optString("id").ifBlank { UUID.randomUUID().toString() },
                sender = item.optString("sender"),
                body = item.optString("body"),
                companyName = item.optString("companyName").takeIf(String::isNotBlank),
                kind = runCatching { MessageKind.valueOf(item.optString("kind")) }.getOrDefault(MessageKind.UNKNOWN),
                quarantined = item.optBoolean("quarantined", false),
                reason = item.optString("reason"),
                timestamp = item.optLong("timestamp", 0L)
            )
        }
    }.getOrDefault(emptyList())

    fun clear() = prefs.edit().remove(KEY_MESSAGES).apply()

    companion object {
        private const val KEY_MESSAGES = "messages"
        private const val MAX_MESSAGES = 250
    }
}

object CieSmsRole {
    fun isHeld(context: Context): Boolean {
        val manager = context.getSystemService(RoleManager::class.java) ?: return false
        return manager.isRoleAvailable(RoleManager.ROLE_SMS) && manager.isRoleHeld(RoleManager.ROLE_SMS)
    }

    fun requestIntent(context: Context): Intent? {
        val manager = context.getSystemService(RoleManager::class.java) ?: return null
        return if (manager.isRoleAvailable(RoleManager.ROLE_SMS) && !manager.isRoleHeld(RoleManager.ROLE_SMS)) {
            manager.createRequestRoleIntent(RoleManager.ROLE_SMS)
        } else null
    }

    fun requiredRuntimePermissions(): Array<String> = buildList {
        add(Manifest.permission.RECEIVE_SMS)
        add(Manifest.permission.SEND_SMS)
        add(Manifest.permission.RECEIVE_MMS)
        add(Manifest.permission.RECEIVE_WAP_PUSH)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
    }.toTypedArray()

    fun hasCorePermissions(context: Context): Boolean {
        val required = listOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_MMS,
            Manifest.permission.RECEIVE_WAP_PUSH
        )
        return required.all { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
    }
}

class CieSmsDeliverReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_DELIVER_ACTION || !CieSmsRole.isHeld(context)) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                if (parts.isEmpty()) return@launch
                val sender = parts.firstOrNull()?.displayOriginatingAddress.orEmpty()
                val body = parts.joinToString(separator = "") { it.displayMessageBody ?: it.messageBody.orEmpty() }
                val timestamp = parts.firstOrNull()?.timestampMillis?.takeIf { it > 0L } ?: System.currentTimeMillis()

                val repo = StableRepository(context)
                val identityNetwork = IdentityNetworkStore(context)
                val decision = MessageShieldEngine.evaluate(repo, identityNetwork, sender, body)
                val quarantined = decision.blocked || decision.threatLevel == CieTrustEngine.ThreatLevel.SCAM

                // The default SMS app is responsible for preserving the incoming SMS.
                // CIE writes every message to the Android SMS provider; quarantine only
                // changes how CIE displays/notifies it. No message is silently deleted.
                runCatching { preserveSmsInProvider(context, sender, body, timestamp) }

                val stored = CieShieldedMessage(
                    id = UUID.randomUUID().toString(),
                    sender = sender,
                    body = body,
                    companyName = decision.companyName,
                    kind = decision.kind,
                    quarantined = quarantined,
                    reason = decision.reason,
                    timestamp = timestamp
                )
                CieMessageStore(context).add(stored)
                CieMessageNotifier.notify(context, stored)
            } finally {
                pending.finish()
            }
        }
    }

    private fun preserveSmsInProvider(context: Context, sender: String, body: String, timestamp: Long) {
        val values = ContentValues().apply {
            put(Telephony.Sms.ADDRESS, sender)
            put(Telephony.Sms.BODY, body)
            put(Telephony.Sms.DATE, timestamp)
            put(Telephony.Sms.DATE_SENT, timestamp)
            put(Telephony.Sms.READ, 0)
            put(Telephony.Sms.SEEN, 0)
        }
        context.contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values)
    }
}

/** ROLE_SMS requires an MMS receiver. Full MMS parsing is intentionally deferred. */
class CieMmsDeliverReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.WAP_PUSH_DELIVER_ACTION || !CieSmsRole.isHeld(context)) return
        val language = CieLanguageStore.resolve(CieLanguageStore.selection(context))
        val message = CieShieldedMessage(
            id = UUID.randomUUID().toString(),
            sender = "MMS",
            body = CieSmsI18n.text(language, "mms_beta_body"),
            companyName = null,
            kind = MessageKind.UNKNOWN,
            quarantined = false,
            reason = CieSmsI18n.text(language, "mms_beta_reason"),
            timestamp = System.currentTimeMillis()
        )
        CieMessageStore(context).add(message)
        CieMessageNotifier.notify(context, message)
    }
}

class CieRespondViaMessageService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && CieSmsRole.isHeld(this) && checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            val recipient = intent.data?.schemeSpecificPart.orEmpty().substringBefore('?')
            val body = intent.getStringExtra(Intent.EXTRA_TEXT) ?: intent.getStringExtra("sms_body") ?: ""
            if (recipient.isNotBlank() && body.isNotBlank()) runCatching { sendSms(recipient, body) }
        }
        stopSelf(startId)
        return START_NOT_STICKY
    }

    private fun sendSms(recipient: String, body: String) {
        val manager = SmsManager.getDefault()
        val parts = manager.divideMessage(body)
        if (parts.size > 1) manager.sendMultipartTextMessage(recipient, null, ArrayList(parts), null, null)
        else manager.sendTextMessage(recipient, null, body, null, null)
    }
}

object CieMessageNotifier {
    private const val CHANNEL_MESSAGES = "cie_messages"
    private const val CHANNEL_QUARANTINE = "cie_quarantine"

    fun notify(context: Context, message: CieShieldedMessage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(NotificationChannel(CHANNEL_MESSAGES, "CIE Messages", NotificationManager.IMPORTANCE_DEFAULT))
            manager.createNotificationChannel(NotificationChannel(CHANNEL_QUARANTINE, "CIE Quarantine", NotificationManager.IMPORTANCE_LOW))
        }

        val language = CieLanguageStore.resolve(CieLanguageStore.selection(context))
        val title = if (message.quarantined) CieSmsI18n.text(language, "notification_quarantined") else CieSmsI18n.text(language, "notification_message")
        val openIntent = Intent(context, MessageShieldLiveActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, if (message.quarantined) CHANNEL_QUARANTINE else CHANNEL_MESSAGES)
        } else {
            @Suppress("DEPRECATION") Notification.Builder(context)
        }
        builder
            .setSmallIcon(R.drawable.cie_shield_app_icon)
            .setContentTitle(title)
            .setContentText(message.companyName ?: message.sender.ifBlank { "CIE" })
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVisibility(Notification.VISIBILITY_PRIVATE)
        manager.notify(message.id.hashCode(), builder.build())
    }
}

class MessageShieldLiveActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.WHITE, AndroidColor.WHITE)
        )
        setContent {
            val selection = remember { CieLanguageStore.selection(this) }
            CieLocalizedContent(selection) { MessageShieldLiveScreen(this) }
        }
    }
}

@Composable
private fun MessageShieldLiveScreen(activity: ComponentActivity) {
    var roleHeld by remember { mutableStateOf(CieSmsRole.isHeld(activity)) }
    var permissionsReady by remember { mutableStateOf(CieSmsRole.hasCorePermissions(activity)) }
    var showQuarantine by rememberSaveable { mutableStateOf(false) }
    var refresh by remember { mutableIntStateOf(0) }
    val store = remember { CieMessageStore(activity) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        permissionsReady = CieSmsRole.hasCorePermissions(activity)
        refresh++
    }
    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        roleHeld = CieSmsRole.isHeld(activity)
        if (roleHeld) permissionLauncher.launch(CieSmsRole.requiredRuntimePermissions())
        refresh++
    }

    val messages = remember(refresh, showQuarantine) { store.all().filter { it.quarantined == showQuarantine } }

    Cie076Theme {
        Column(Modifier.fillMaxSize().background(Cie076Colors.Background)) {
            Cie076PageHero(CieSmsI18n.compose("live_title")) { activity.finish() }
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(20.dp, 16.dp, 20.dp, 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Cie076Card {
                        Text(
                            if (roleHeld && permissionsReady) CieSmsI18n.compose("live_active") else CieSmsI18n.compose("live_setup"),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(CieSmsI18n.compose("live_desc"), color = Cie076Colors.Muted, lineHeight = 20.sp)
                        Spacer(Modifier.height(12.dp))
                        if (!roleHeld) {
                            Cie076PrimaryButton(CieSmsI18n.compose("activate_sms_role"), true, Modifier.fillMaxWidth()) {
                                CieSmsRole.requestIntent(activity)?.let(roleLauncher::launch)
                            }
                        } else if (!permissionsReady) {
                            Cie076PrimaryButton(CieSmsI18n.compose("grant_sms_permissions"), true, Modifier.fillMaxWidth()) {
                                permissionLauncher.launch(CieSmsRole.requiredRuntimePermissions())
                            }
                        }
                        if (roleHeld) {
                            Spacer(Modifier.height(10.dp))
                            Text(CieSmsI18n.compose("sms_beta_warning"), color = Cie076Colors.Red, fontSize = 12.sp, lineHeight = 17.sp)
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = !showQuarantine, onClick = { showQuarantine = false }, label = { Text(CieSmsI18n.compose("inbox")) })
                        FilterChip(selected = showQuarantine, onClick = { showQuarantine = true }, label = { Text(CieSmsI18n.compose("quarantine")) })
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { refresh++ }) { Text(CieSmsI18n.compose("refresh")) }
                    }
                }

                if (messages.isEmpty()) {
                    item { Cie076EmptyState(CieSmsI18n.compose(if (showQuarantine) "no_quarantine" else "no_messages")) }
                } else {
                    items(messages, key = { it.id }) { message ->
                        Cie076Card {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(message.companyName ?: message.sender.ifBlank { CieSmsI18n.compose("unknown_sender") }, fontWeight = FontWeight.Bold)
                                    Text(message.kind.name.lowercase().replaceFirstChar { it.uppercase() }, color = Cie076Colors.Muted, fontSize = 12.sp)
                                }
                                Surface(
                                    color = if (message.quarantined) Color(0xFFFFECEC) else Color(0xFFEAF8F1),
                                    shape = RoundedCornerShape(99.dp)
                                ) {
                                    Text(
                                        CieSmsI18n.compose(if (message.quarantined) "quarantined_badge" else "allowed_badge"),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        color = if (message.quarantined) Cie076Colors.Red else Cie076Colors.Green,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(message.body, lineHeight = 20.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(message.reason, color = Cie076Colors.Muted, fontSize = 12.sp, lineHeight = 17.sp)
                        }
                    }
                }
            }
        }
    }
}

class CieSmsComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val selection = remember { CieLanguageStore.selection(this) }
            CieLocalizedContent(selection) { CieComposeMessageScreen(this, intent?.data) }
        }
    }
}

@Composable
private fun CieComposeMessageScreen(activity: ComponentActivity, data: Uri?) {
    val initialRecipient = data?.schemeSpecificPart.orEmpty().substringBefore('?')
    val language = LocalCieLanguage.current
    var recipient by rememberSaveable { mutableStateOf(initialRecipient) }
    var body by rememberSaveable { mutableStateOf(activity.intent?.getStringExtra("sms_body") ?: activity.intent?.getStringExtra(Intent.EXTRA_TEXT).orEmpty()) }
    var status by remember { mutableStateOf<String?>(null) }

    Cie076Theme {
        Column(Modifier.fillMaxSize().background(Cie076Colors.Background)) {
            Cie076PageHero(CieSmsI18n.compose("compose_title")) { activity.finish() }
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(recipient, { recipient = it }, Modifier.fillMaxWidth(), label = { Text(CieSmsI18n.compose("recipient")) }, singleLine = true)
                OutlinedTextField(body, { body = it }, Modifier.fillMaxWidth(), label = { Text(CieSmsI18n.compose("message")) }, minLines = 5)
                Cie076PrimaryButton(CieSmsI18n.compose("send"), recipient.isNotBlank() && body.isNotBlank(), Modifier.fillMaxWidth()) {
                    status = runCatching {
                        check(CieSmsRole.isHeld(activity)) { CieSmsI18n.text(language, "must_be_default") }
                        check(activity.checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) { CieSmsI18n.text(language, "sms_permission_missing") }
                        val manager = SmsManager.getDefault()
                        val parts = manager.divideMessage(body)
                        if (parts.size > 1) manager.sendMultipartTextMessage(recipient, null, ArrayList(parts), null, null)
                        else manager.sendTextMessage(recipient, null, body, null, null)
                        CieSmsI18n.text(language, "sent")
                    }.getOrElse { it.message ?: CieSmsI18n.text(language, "send_failed") }
                }
                status?.let { Text(it, color = Cie076Colors.Muted) }
            }
        }
    }
}

internal object CieSmsI18n {
    @Composable
    fun compose(key: String): String = text(LocalCieLanguage.current, key)

    fun text(language: CieLanguage, key: String): String = maps[language]?.get(key) ?: maps[CieLanguage.ENGLISH]?.get(key) ?: key

    private val maps = mapOf(
        CieLanguage.ENGLISH to mapOf(
            "live_title" to "Message Shield Live", "live_active" to "Live protection active", "live_setup" to "Message Shield needs setup",
            "live_desc" to "CIE classifies incoming SMS locally. Necessary messages stay in the inbox; commercial or high-risk messages go to quarantine and are never silently deleted.",
            "activate_sms_role" to "Make CIE the SMS app", "grant_sms_permissions" to "Enable SMS access", "sms_beta_warning" to "Live SMS is ready for testing. MMS decoding is not complete yet, so do not use this beta as your only daily MMS app.",
            "inbox" to "Inbox", "quarantine" to "Quarantine", "refresh" to "Refresh", "no_messages" to "No protected messages yet", "no_quarantine" to "Quarantine is empty", "unknown_sender" to "Unknown sender", "quarantined_badge" to "QUARANTINED", "allowed_badge" to "ALLOWED",
            "notification_quarantined" to "CIE quarantined a message", "notification_message" to "New protected message",
            "mms_beta_body" to "MMS received. Full MMS rendering is not enabled in this beta.", "mms_beta_reason" to "CIE preserved an MMS event but does not classify MMS attachments yet.",
            "compose_title" to "New message", "recipient" to "Recipient", "message" to "Message", "send" to "Send", "sent" to "Message sent", "send_failed" to "Could not send message", "must_be_default" to "CIE must be the default SMS app", "sms_permission_missing" to "SMS permission is missing"
        ),
        CieLanguage.DUTCH to mapOf(
            "live_title" to "Message Shield Live", "live_active" to "Live bescherming actief", "live_setup" to "Message Shield moet worden ingesteld",
            "live_desc" to "CIE beoordeelt inkomende sms-berichten lokaal. Noodzakelijke berichten blijven in de inbox; commerciële of risicovolle berichten gaan naar quarantaine en worden nooit stil verwijderd.",
            "activate_sms_role" to "Maak CIE de sms-app", "grant_sms_permissions" to "Sms-toegang inschakelen", "sms_beta_warning" to "Live sms is klaar om te testen. MMS-decodering is nog niet compleet; gebruik deze bèta daarom nog niet als je enige dagelijkse MMS-app.",
            "inbox" to "Inbox", "quarantine" to "Quarantaine", "refresh" to "Vernieuwen", "no_messages" to "Nog geen beschermde berichten", "no_quarantine" to "Quarantaine is leeg", "unknown_sender" to "Onbekende afzender", "quarantined_badge" to "QUARANTAINE", "allowed_badge" to "TOEGESTAAN",
            "notification_quarantined" to "CIE heeft een bericht in quarantaine geplaatst", "notification_message" to "Nieuw beschermd bericht",
            "mms_beta_body" to "MMS ontvangen. Volledige MMS-weergave is nog niet actief in deze bèta.", "mms_beta_reason" to "CIE heeft de MMS-gebeurtenis vastgelegd maar classificeert MMS-bijlagen nog niet.",
            "compose_title" to "Nieuw bericht", "recipient" to "Ontvanger", "message" to "Bericht", "send" to "Verzenden", "sent" to "Bericht verzonden", "send_failed" to "Bericht kon niet worden verzonden", "must_be_default" to "CIE moet de standaard sms-app zijn", "sms_permission_missing" to "Sms-toestemming ontbreekt"
        ),
        CieLanguage.TURKISH to mapOf(
            "live_title" to "Message Shield Live", "live_active" to "Canlı koruma aktif", "live_setup" to "Message Shield kurulumu gerekli",
            "live_desc" to "CIE gelen SMS'leri cihaz üzerinde sınıflandırır. Gerekli mesajlar gelen kutusunda kalır; ticari veya yüksek riskli mesajlar karantinaya alınır ve sessizce silinmez.",
            "activate_sms_role" to "CIE'yi SMS uygulaması yap", "grant_sms_permissions" to "SMS erişimini etkinleştir", "sms_beta_warning" to "Canlı SMS test için hazır. MMS çözümleme henüz tamamlanmadı; bu betayı günlük tek MMS uygulaman olarak kullanma.",
            "inbox" to "Gelen kutusu", "quarantine" to "Karantina", "refresh" to "Yenile", "no_messages" to "Henüz korunan mesaj yok", "no_quarantine" to "Karantina boş", "unknown_sender" to "Bilinmeyen gönderici", "quarantined_badge" to "KARANTİNA", "allowed_badge" to "İZİN VERİLDİ",
            "notification_quarantined" to "CIE bir mesajı karantinaya aldı", "notification_message" to "Yeni korunan mesaj",
            "mms_beta_body" to "MMS alındı. Bu betada tam MMS görüntüleme henüz etkin değil.", "mms_beta_reason" to "CIE MMS olayını kaydetti ancak MMS eklerini henüz sınıflandırmıyor.",
            "compose_title" to "Yeni mesaj", "recipient" to "Alıcı", "message" to "Mesaj", "send" to "Gönder", "sent" to "Mesaj gönderildi", "send_failed" to "Mesaj gönderilemedi", "must_be_default" to "CIE varsayılan SMS uygulaması olmalı", "sms_permission_missing" to "SMS izni eksik"
        ),
        CieLanguage.GERMAN to mapOf(
            "live_title" to "Message Shield Live", "live_active" to "Live-Schutz aktiv", "live_setup" to "Message Shield muss eingerichtet werden",
            "live_desc" to "CIE bewertet eingehende SMS lokal. Notwendige Nachrichten bleiben im Posteingang; kommerzielle oder riskante Nachrichten kommen in Quarantäne und werden nie still gelöscht.",
            "activate_sms_role" to "CIE als SMS-App festlegen", "grant_sms_permissions" to "SMS-Zugriff aktivieren", "sms_beta_warning" to "Live-SMS ist testbereit. Die MMS-Dekodierung ist noch nicht vollständig; nutze diese Beta noch nicht als einzige tägliche MMS-App.",
            "inbox" to "Posteingang", "quarantine" to "Quarantäne", "refresh" to "Aktualisieren", "no_messages" to "Noch keine geschützten Nachrichten", "no_quarantine" to "Quarantäne ist leer", "unknown_sender" to "Unbekannter Absender", "quarantined_badge" to "QUARANTÄNE", "allowed_badge" to "ERLAUBT",
            "notification_quarantined" to "CIE hat eine Nachricht in Quarantäne verschoben", "notification_message" to "Neue geschützte Nachricht",
            "mms_beta_body" to "MMS empfangen. Die vollständige MMS-Anzeige ist in dieser Beta noch nicht aktiviert.", "mms_beta_reason" to "CIE hat das MMS-Ereignis gespeichert, klassifiziert MMS-Anhänge aber noch nicht.",
            "compose_title" to "Neue Nachricht", "recipient" to "Empfänger", "message" to "Nachricht", "send" to "Senden", "sent" to "Nachricht gesendet", "send_failed" to "Nachricht konnte nicht gesendet werden", "must_be_default" to "CIE muss die Standard-SMS-App sein", "sms_permission_missing" to "SMS-Berechtigung fehlt"
        ),
        CieLanguage.PORTUGUESE to mapOf(
            "live_title" to "Message Shield Live", "live_active" to "Proteção ao vivo ativa", "live_setup" to "O Message Shield precisa de configuração",
            "live_desc" to "A CIE classifica SMS recebidos localmente. Mensagens necessárias ficam na caixa de entrada; mensagens comerciais ou de alto risco vão para a quarentena e nunca são apagadas silenciosamente.",
            "activate_sms_role" to "Definir CIE como app de SMS", "grant_sms_permissions" to "Ativar acesso a SMS", "sms_beta_warning" to "O SMS ao vivo está pronto para testes. A decodificação de MMS ainda não está completa; não use esta beta como seu único app diário de MMS.",
            "inbox" to "Caixa de entrada", "quarantine" to "Quarentena", "refresh" to "Atualizar", "no_messages" to "Ainda não há mensagens protegidas", "no_quarantine" to "A quarentena está vazia", "unknown_sender" to "Remetente desconhecido", "quarantined_badge" to "QUARENTENA", "allowed_badge" to "PERMITIDA",
            "notification_quarantined" to "A CIE colocou uma mensagem em quarentena", "notification_message" to "Nova mensagem protegida",
            "mms_beta_body" to "MMS recebido. A visualização completa de MMS ainda não está ativa nesta beta.", "mms_beta_reason" to "A CIE registrou o evento MMS, mas ainda não classifica anexos MMS.",
            "compose_title" to "Nova mensagem", "recipient" to "Destinatário", "message" to "Mensagem", "send" to "Enviar", "sent" to "Mensagem enviada", "send_failed" to "Não foi possível enviar a mensagem", "must_be_default" to "A CIE deve ser o app de SMS padrão", "sms_permission_missing" to "Falta permissão de SMS"
        ),
        CieLanguage.ARABIC to mapOf(
            "live_title" to "Message Shield Live", "live_active" to "الحماية المباشرة مفعلة", "live_setup" to "يحتاج Message Shield إلى الإعداد",
            "live_desc" to "يصنّف CIE رسائل SMS الواردة محليًا على الجهاز. تبقى الرسائل الضرورية في صندوق الوارد، وتنتقل الرسائل التجارية أو عالية الخطورة إلى الحجر ولا تُحذف بصمت.",
            "activate_sms_role" to "اجعل CIE تطبيق الرسائل الافتراضي", "grant_sms_permissions" to "تفعيل الوصول إلى SMS", "sms_beta_warning" to "ميزة SMS المباشرة جاهزة للاختبار. فك ترميز MMS غير مكتمل بعد، لذلك لا تستخدم هذه النسخة التجريبية كتطبيق MMS اليومي الوحيد.",
            "inbox" to "الوارد", "quarantine" to "الحجر", "refresh" to "تحديث", "no_messages" to "لا توجد رسائل محمية بعد", "no_quarantine" to "الحجر فارغ", "unknown_sender" to "مرسل غير معروف", "quarantined_badge" to "محجور", "allowed_badge" to "مسموح",
            "notification_quarantined" to "وضع CIE رسالة في الحجر", "notification_message" to "رسالة محمية جديدة",
            "mms_beta_body" to "تم استلام MMS. عرض MMS الكامل غير مفعّل بعد في هذه النسخة التجريبية.", "mms_beta_reason" to "سجّل CIE حدث MMS لكنه لا يصنّف مرفقات MMS بعد.",
            "compose_title" to "رسالة جديدة", "recipient" to "المستلم", "message" to "الرسالة", "send" to "إرسال", "sent" to "تم إرسال الرسالة", "send_failed" to "تعذر إرسال الرسالة", "must_be_default" to "يجب أن يكون CIE تطبيق SMS الافتراضي", "sms_permission_missing" to "إذن SMS مفقود"
        )
    )
}
