package com.cie.app.sms

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.cie.app.MainActivity
import com.cie.app.R
import com.cie.app.core.CieMessage
import com.cie.app.core.SmsStore

class CieSmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_DELIVER_ACTION) return
        val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (parts.isEmpty()) return
        val sender = parts.first().displayOriginatingAddress.orEmpty()
        val body = parts.joinToString("") { it.displayMessageBody.orEmpty() }
        val timestamp = parts.first().timestampMillis.takeIf { it > 0 } ?: System.currentTimeMillis()
        val store = SmsStore(context)
        val classification = store.classify(sender, body)
        val message = store.add(sender, body, timestamp, classification)

        if (!message.filtered) {
            runCatching {
                context.contentResolver.insert(
                    Telephony.Sms.Inbox.CONTENT_URI,
                    ContentValues().apply {
                        put(Telephony.Sms.ADDRESS, sender)
                        put(Telephony.Sms.BODY, body)
                        put(Telephony.Sms.DATE, timestamp)
                        put(Telephony.Sms.READ, 0)
                        put(Telephony.Sms.SEEN, 0)
                    }
                )
            }
            notifyAllowed(context, message)
        }
    }

    private fun notifyAllowed(context: Context, message: CieMessage) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channelId = "cie_messages"
        manager.createNotificationChannel(NotificationChannel(channelId, "CIE Messages", NotificationManager.IMPORTANCE_DEFAULT))
        val open = PendingIntent.getActivity(
            context,
            10,
            Intent(context, MainActivity::class.java).putExtra("open_messages", true),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = Notification.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_cie_notification)
            .setContentTitle(message.companyName ?: message.sender)
            .setContentText(message.body.take(120))
            .setStyle(Notification.BigTextStyle().bigText(message.body))
            .setContentIntent(open)
            .setVisibility(Notification.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .build()
        manager.notify(message.id.hashCode(), notification)
    }
}

class CieMmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.WAP_PUSH_DELIVER_ACTION) return
    }
}

class CieRespondViaMessageService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        stopSelf(startId)
        return START_NOT_STICKY
    }
}
