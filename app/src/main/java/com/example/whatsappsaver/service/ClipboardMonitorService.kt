package com.example.whatsappsaver.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import com.example.whatsappsaver.MainActivity

class ClipboardMonitorService : AccessibilityService() {

    private var whatsappOpen = false

    companion object {
        const val CHANNEL_ID = "wa_saver"
        const val FOREGROUND_ID = 999

        fun isRunning(context: Context): Boolean {
            val expected = "${context.packageName}/${ClipboardMonitorService::class.java.canonicalName}"
            val enabled = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            return enabled.split(":").any { it.equals(expected, ignoreCase = true) }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 500
        }

        createNotificationChannel()

        // إشعار دائم — يتغير حسب السياق
        startForeground(FOREGROUND_ID, buildNotification(
            "WhatsApp Saver نشط",
            "جاهز لمراقبة النسخ",
            false
        ))
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val pkg = event.packageName?.toString() ?: return

        // تجاهل أحداث التطبيق نفسه
        if (pkg == this.packageName) return

        val isWA = pkg.contains("whatsapp", ignoreCase = true)

        if (isWA && !whatsappOpen) {
            // WhatsApp فُتح
            whatsappOpen = true
            updateNotification(
                "أنت في WhatsApp",
                "انسخ أي رسالة ثم اضغط هنا لحفظها",
                true
            )
        } else if (!isWA && whatsappOpen) {
            // WhatsApp أُغلق
            whatsappOpen = false
            updateNotification(
                "WhatsApp Saver نشط",
                "جاهز لمراقبة النسخ",
                false
            )
        }
    }

    private fun updateNotification(title: String, text: String, highlight: Boolean) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(FOREGROUND_ID, buildNotification(title, text, highlight))
    }

    private fun buildNotification(title: String, text: String, highlight: Boolean): Notification {
        // لما المستخدم يضغط → التطبيق يفتح → يقرأ الحافظة
        val intent = Intent(this, MainActivity::class.java).apply {
            action = "SAVE_FROM_CLIPBOARD"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pending)
            .setOngoing(true)
            .setAutoCancel(false)

        if (highlight) {
            // إشعار بارز لما WhatsApp مفتوح
            builder
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(longArrayOf(0, 200))
                .addAction(android.R.drawable.ic_menu_save, "حفظ الآن", pending)
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_LOW)
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "WhatsApp Saver",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "إشعارات حفظ الرسائل"
            enableVibration(true)
            setShowBadge(true)
        }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
    }
}
