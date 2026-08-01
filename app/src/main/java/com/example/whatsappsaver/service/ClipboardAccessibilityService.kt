package com.example.whatsappsaver.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import com.example.whatsappsaver.MainActivity

class ClipboardAccessibilityService : AccessibilityService() {

    private lateinit var clipboardManager: ClipboardManager
    private var lastText = ""

    companion object {
        const val CHANNEL_ID = "whatsapp_saver_channel"
        private var instance: ClipboardAccessibilityService? = null

        fun isRunning(): Boolean = instance != null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this

        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }

        createNotificationChannel()

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener {
            onClipboardChanged()
        }
    }

    private fun onClipboardChanged() {
        val clip = clipboardManager.primaryClip ?: return
        if (clip.itemCount == 0) return

        val text = clip.getItemAt(0).text?.toString() ?: return
        if (text.isBlank() || text == lastText) return

        lastText = text

        // تحقق إن الواجهة الأمامية هي WhatsApp
        if (isWhatsAppForeground()) {
            showSaveNotification(text)
        }
    }

    private fun isWhatsAppForeground(): Boolean {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val tasks = am.runningAppProcesses ?: return false
        for (task in tasks) {
            if (task.importance == android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (pkg in task.pkgList) {
                    if (pkg.contains("whatsapp", ignoreCase = true)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun showSaveNotification(text: String) {
        // زر فتح التطبيق
        val openIntent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpen = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // زر حفظ سريع بالتصنيف الافتراضي
        val quickSaveIntent = Intent(this, MainActivity::class.java).apply {
            action = "QUICK_SAVE"
            putExtra("save_text", text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingQuickSave = PendingIntent.getActivity(
            this, 1, quickSaveIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setContentTitle("رسالة من WhatsApp")
            .setContentText(text.take(80))
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pendingOpen)
            .addAction(android.R.drawable.ic_menu_save, "حفظ سريع", pendingQuickSave)
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

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}
