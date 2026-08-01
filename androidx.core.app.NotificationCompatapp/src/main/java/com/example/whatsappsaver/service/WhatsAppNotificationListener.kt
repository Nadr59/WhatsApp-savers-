package com.example.whatsappsaver.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.example.whatsappsaver.MainActivity

class WhatsAppNotificationListener : NotificationListenerService() {

    companion object {
        const val CHANNEL_ID = "whatsapp_saver_channel"
        private var lastText = ""
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return

        // تحقق إن الإشعار من WhatsApp
        if (!sbn.packageName.contains("whatsapp", ignoreCase = true)) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        // تجاهل إشعارات النظام (آخر اتصال، تنزيل، إلخ)
        if (title.contains("當您在使用", ignoreCase = true)) return
        if (title.contains("checking", ignoreCase = true)) return
        if (text.isBlank()) return
        if (text == lastText) return

        // تجاهل إشعارات المجموعات بدون محتوى حقيقي
        if (text.contains("messages") && text.contains("@")) return

        lastText = text
        showSaveNotification(text, title)
    }

    private fun showSaveNotification(text: String, sender: String) {
        createNotificationChannel()

        // فتح التطبيق مع النص
        val openIntent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpen = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setContentTitle("رسالة من $sender")
            .setContentText(text.take(100))
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pendingOpen)
            .addAction(android.R.drawable.ic_menu_save, "حفظ", pendingOpen)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "WhatsApp Saver",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "إشعارات حفظ رسائل WhatsApp"
            enableVibration(true)
            enableLights(true)
        }
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {}
}
