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
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.whatsappsaver.MainActivity

class ClipboardMonitorService : AccessibilityService() {

    private lateinit var clipboardManager: ClipboardManager
    private var lastText = ""
    private var connected = false

    companion object {
        const val TAG = "ClipboardMonitor"
        const val CHANNEL_ID = "clipboard_monitor"
        const val FOREGROUND_ID = 100

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
        connected = true
        Log.d(TAG, "=== الخدمة اتصلت بنجاح ===")

        serviceInfo = serviceInfo.apply {
            // نراقب تغيرات النافذة + تغيرات المحتوى
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 300
            // هذا العلم يسمح بقراءة الحافظة
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            // نراقب WhatsApp فقط
            packageNames = arrayOf("com.whatsapp", "com.whatsapp.w4b")
        }

        createNotificationChannel()
        startForeground(FOREGROUND_ID, buildRunningNotification())

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        Toast.makeText(this, "WhatsApp Saver يعمل الآن!", Toast.LENGTH_LONG).show()
        Log.d(TAG, "=== الخدمة جاهزة لمراقبة WhatsApp ===")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!connected) return
        event ?: return

        // محاولة قراءة الحافظة عند كل حدث من WhatsApp
        tryReadClipboard()
    }

    private fun tryReadClipboard() {
        try {
            val clip = clipboardManager.primaryClip ?: return
            if (clip.itemCount == 0) return

            val text = clip.getItemAt(0).text?.toString() ?: return

            // تجاهل النص الفاضي والمكرر
            if (text.isBlank()) return
            if (text == lastText) return
            if (text.length < 2) return

            lastText = text
            Log.d(TAG, "تم نسخ: ${text.take(50)}")

            showSaveNotification(text)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
        }
    }

    private fun showSaveNotification(text: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_save)
            .setContentTitle("حفظ الرسالة؟")
            .setContentText(text.take(120))
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pending)
            .addAction(android.R.drawable.ic_menu_save, "فتح وحفظ", pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(200, notification)
    }

    private fun buildRunningNotification(): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("WhatsApp Saver نشط")
            .setContentText("جاهز لمراقبة الرسائل المنسوخة")
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "WhatsApp Saver",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "إشعارات حفظ الرسائل"
            enableVibration(true)
            setShowBadge(true)
        }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    override fun onInterrupt() {
        Log.d(TAG, "الخدمة قُوطعت")
    }

    override fun onDestroy() {
        super.onDestroy()
        connected = false
        Log.d(TAG, "الخدمة توقفت")
    }
}
