package com.example.whatsappsaver.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
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

class ClipboardAccessibilityService : AccessibilityService() {

    private lateinit var clipboardManager: ClipboardManager
    private var lastText = ""

    companion object {
        const val CHANNEL_ID = "whatsapp_saver_channel"
        const val SERVICE_NOTIFICATION_ID = 100
        private var instance: ClipboardAccessibilityService? = null

        fun isRunning(context: Context): Boolean {
            val service = "${context.packageName}/${ClipboardAccessibilityService::class.java.canonicalName}"
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            return enabledServices.contains(service)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this

        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 300
            packageNames = arrayOf("com.whatsapp", "com.whatsapp.w4b")
        }

        createNotificationChannel()

        // إشعار دائم يوضح إن الخدمة شغالة
        showRunningNotification()

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener {
            onClipboardChanged()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // عندما يتغير النافذة (مثلاً WhatsApp يفتح)
        event?.packageName?.toString()?.let { pkg ->
            if (pkg.contains("whatsapp", ignoreCase = true)) {
                onClipboardChanged()
            }
        }
    }

    private fun onClipboardChanged() {
        try {
            val clip = clipboardManager.primaryClip ?: return
            if (clip.itemCount == 0) return

            val text = clip.getItemAt(0).text?.toString() ?: return
            if (text.isBlank() || text == lastText) return

            lastText = text
            showSaveNotification(text)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showRunningNotification() {
        val intent = Intent(this, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("WhatsApp Saver يعمل")
            .setContentText("مراقبة الحافظة نشطة - انسخ رسالة من WhatsApp")
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(SERVICE_NOTIFICATION_ID, notification)
    }

    private fun showSaveNotification(text: String) {
        // فتح التطبيق لاختيار التصنيف
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

        // حفظ سريع بالتصنيف الافتراضي
        val quickIntent = Intent(this, MainActivity::class.java).apply {
            action = "QUICK_SAVE"
            putExtra("save_text", text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingQuick = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt() + 1, quickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setContentTitle("رسالة جديدة من WhatsApp")
            .setContentText(text.take(100))
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pendingOpen)
            .addAction(android.R.drawable.ic_menu_save, "حفظ سريع", pendingQuick)
            .addAction(android.R.drawable.ic_menu_edit, "فتح وتصنيف", pendingOpen)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "WhatsApp Saver",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "إشعارات حفظ رسائل WhatsApp"
        }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}
