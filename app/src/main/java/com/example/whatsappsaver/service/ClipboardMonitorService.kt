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
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import com.example.whatsappsaver.MainActivity

class ClipboardMonitorService : AccessibilityService() {

    private lateinit var clipboardManager: ClipboardManager
    private var whatsappOpen = false
    private var notificationShown = false

    companion object {
        const val TAG = "WASaver"
        const val CHANNEL_ID = "wa_saver"
        const val FOREGROUND_ID = 999
        const val SAVE_NOTIF_ID = 200

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
        Log.d(TAG, "=== Service Connected ===")

        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 500
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }

        createNotificationChannel()

        // إشعار دائم يبين إن الخدمة شغالة
        startForeground(FOREGROUND_ID, buildOngoing())

        // مراقبة الحافظة — المهم: Listener يشتغل لكن القراءة ممنوعة
        // لذلك نرسل إ_notification والتطبيق يقرأ لما يفتح
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener {
            onClipboardChanged()
        }

        Log.d(TAG, "=== Clipboard Listener Registered ===")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val pkg = event.packageName?.toString() ?: return

        // تجاهل أحداث التطبيق نفسه
        if (pkg == this.packageName) return

        val wasOpen = whatsappOpen
        whatsappOpen = pkg.contains("whatsapp", ignoreCase = true)

        if (whatsappOpen && !wasOpen) {
            Log.d(TAG, "WhatsApp opened")
            notificationShown = false
        }
    }

    private fun onClipboardChanged() {
        Log.d(TAG, "Clipboard changed! whatsappOpen=$whatsappOpen")

        // نرسل إشعار فقط لو WhatsApp كان مفتوح
        if (!whatsappOpen) {
            Log.d(TAG, "WhatsApp not open, ignoring")
            return
        }

        // منع التكرار
        if (notificationShown) {
            Log.d(TAG, "Notification already shown, skipping")
            return
        }

        notificationShown = true
        Log.d(TAG, ">>> Showing save notification")

        // إشعار بدون محتوى — التطبيق يقرأ لما يفتح
        showTapNotification()

        // إعادة تعيين بعد 5 ثواني للسماح بنسخ رسالة ثانية
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            notificationShown = false
        }, 5000)
    }

    private fun showTapNotification() {
        // لما المستخدم يضغط → التطبيق يفتح ويقرأ الحافظة
        val intent = Intent(this, MainActivity::class.java).apply {
            action = "SAVE_FROM_CLIPBOARD"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
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
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(SAVE_NOTIF_ID, notification)
    }

    private fun buildOngoing(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("WhatsApp Saver نشط")
            .setContentText("جاهز لمراقبة النسخ")
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
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
        Log.d(TAG, "=== Service Destroyed ===")
    }
}
