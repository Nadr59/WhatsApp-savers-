package com.example.whatsappsaver.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import com.example.whatsappsaver.MainActivity

class ClipboardMonitorService : AccessibilityService() {

    private var whatsappOpen = false
    private val handler = Handler(Looper.getMainLooper())
    private var resetRunnable: Runnable? = null

    companion object {
        const val CHANNEL_ID = "wa_saver"
        const val FOREGROUND_ID = 999
        private const val RESET_DELAY = 5000L // 5 ثواني قبل الرجوع

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
        startForeground(FOREGROUND_ID, buildNotification(
            "WhatsApp Saver نشط",
            "جاهز لمراقبة النسخ",
            false
        ))
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val pkg = event.packageName?.toString() ?: return

        if (pkg == this.packageName) return

        val isWA = pkg.contains("whatsapp", ignoreCase = true)

        if (isWA) {
            // WhatsApp مفتوح — ألغِ أي رجوع مخطط
            resetRunnable?.let { handler.removeCallbacks(it) }
            resetRunnable = null

            if (!whatsappOpen) {
                whatsappOpen = true
                showWhatsAppNotification()
            }
        } else {
            // WhatsApp أُغلق — لكن ننتظر 5 ثواني (علشان النسخ)
            if (whatsappOpen && resetRunnable == null) {
                resetRunnable = Runnable {
                    whatsappOpen = false
                    showIdleNotification()
                    resetRunnable = null
                }
                handler.postDelayed(resetRunnable!!, RESET_DELAY)
            }
        }
    }

    private fun showWhatsAppNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(FOREGROUND_ID, buildNotification(
            "أنت في WhatsApp",
            "انسخ رسالة ثم اضغط هنا لحفظها",
            true
        ))
    }

    private fun showIdleNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(FOREGROUND_ID, buildNotification(
            "WhatsApp Saver نشط",
            "جاهز لمراقبة النسخ",
            false
        ))
    }

    private fun buildNotification(title: String, text: String, highlight: Boolean): Notification {
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
        resetRunnable?.let { handler.removeCallbacks(it) }
    }
}
