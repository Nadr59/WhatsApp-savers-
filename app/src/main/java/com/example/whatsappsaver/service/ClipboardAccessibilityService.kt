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
    private var lastEventTime = 0L

    companion object {
        const val CHANNEL_ID = "whatsapp_saver_channel"
        const val FOREGROUND_ID = 100
        private var instance: ClipboardAccessibilityService? = null

        fun isRunning(context: Context): Boolean {
            val expected = "${context.packageName}/${ClipboardAccessibilityService::class.java.canonicalName}"
            val enabled = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            return enabled.split(":").any { it.equals(expected, ignoreCase = true) }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this

        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 500
            flags = AccessibilityServiceInfo.FLAG_DEFAULT
        }

        createNotificationChannel()
        startForeground(FOREGROUND_ID, buildRunningNotification())

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener {
            onClipboardChanged()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // لا نحتاج شيء هنا - المراقبة عبر listener
    }

    private fun onClipboardChanged() {
        val now = System.currentTimeMillis()
        // تجنب التكرار (أقل من ثانيتين)
        if (now - lastEventTime < 2000) return
        lastEventTime = now

        // لا نقرأ الحافظة هنا! (ممنوع في الخلفية)
        // نرسل إشعار فقط - المستخدم يضغط → التطبيق يفتح → يقرأ الحافظة

        if (isWhatsAppForeground()) {
            showTapToSaveNotification()
        }
    }

    private fun isWhatsAppForeground(): Boolean {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val processes = am.runningAppProcesses ?: return false
        return processes.any { process ->
            process.importance == android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
            process.pkgList?.any { it.contains("whatsapp", ignoreCase = true) } == true
        }
    }

    private fun showTapToSaveNotification() {
        // التطبيق يفتح في المقدمة ويقرأ الحافظة
        val intent = Intent(this, MainActivity::class.java).apply {
            action = "SAVE_FROM_CLIPBOARD"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pending = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setContentTitle("تم نسخ رسالة من WhatsApp!")
            .setContentText("اضغط هنا لحفظها")
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(200, notification)
    }

    private fun buildRunningNotification(): NotificationCompat.Builder {
        val intent = Intent(this, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("WhatsApp Saver نشط")
            .setContentText("مراقبة الحافظة قيد التشغيل")
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
    }

    private fun createNotificationChannel() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // قناة للإشعارات العادية
        val mainChannel = NotificationChannel(
            CHANNEL_ID,
            "WhatsApp Saver",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "إشعارات حفظ الرسائل"
            enableVibration(true)
        }
        nm.createNotificationChannel(mainChannel)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}
