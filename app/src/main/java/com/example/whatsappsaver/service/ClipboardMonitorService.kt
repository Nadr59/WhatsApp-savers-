package com.example.whatsappsaver.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.whatsappsaver.MainActivity

class ClipboardMonitorService : AccessibilityService() {

    private lateinit var clipboardManager: ClipboardManager
    private var lastText = ""
    private var whatsappActive = false
    private val handler = Handler(Looper.getMainLooper())
    private var checkRunnable: Runnable? = null

    companion object {
        const val TAG = "ClipboardMonitor"
        const val CHANNEL_ID = "clipboard_monitor"
        const val FOREGROUND_ID = 100
        const val CHECK_INTERVAL = 800L // كل ثانية

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
        Log.d(TAG, "=== الخدمة اتصلت ===")

        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_CLICKED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 200
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }

        createNotificationChannel()
        startForeground(FOREGROUND_ID, buildRunningNotification())

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        Toast.makeText(this, "WhatsApp Saver يعمل!", Toast.LENGTH_SHORT).show()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val pkg = event.packageName?.toString() ?: return

        val isWhatsApp = pkg.contains("whatsapp", ignoreCase = true) &&
                !pkg.contains(this.packageName, ignoreCase = true)

        if (isWhatsApp && !whatsappActive) {
            // WhatsApp صار في المقدمة → ابدأ المراقبة
            whatsappActive = true
            startClipboardChecking()
            Log.d(TAG, "WhatsApp فعال - بدأت المراقبة")
        } else if (!isWhatsApp && whatsappActive) {
            // WhatsApp طار من المقدمة → وقف المراقبة
            whatsappActive = false
            stopClipboardChecking()
            Log.d(TAG, "WhatsApp طار - وقفت المراقبة")
        }

        // نحاول نقرأ الحافظة من داخل الحدث (مسموح)
        if (whatsappActive) {
            tryReadClipboard()
        }
    }

    private fun startClipboardChecking() {
        stopClipboardChecking()
        checkRunnable = object : Runnable {
            override fun run() {
                if (whatsappActive) {
                    tryReadClipboard()
                    handler.postDelayed(this, CHECK_INTERVAL)
                }
            }
        }
        handler.postDelayed(checkRunnable!!, CHECK_INTERVAL)
    }

    private fun stopClipboardChecking() {
        checkRunnable?.let { handler.removeCallbacks(it) }
        checkRunnable = null
    }

    private fun tryReadClipboard() {
        try {
            val clip = clipboardManager.primaryClip ?: return
            if (clip.itemCount == 0) return

            val text = clip.getItemAt(0).text?.toString() ?: return

            if (text.isBlank()) return
            if (text == lastText) return
            if (text.length < 2) return

            lastText = text
            Log.d(TAG, ">>> تم نسخ: ${text.take(60)}")

            showSaveNotification(text)

        } catch (e: SecurityException) {
            Log.e(TAG, "Security: ${e.message}")
            // ممنوع نقرأ الحافظة - نرسل إ_notification يطلب الضغط
            showTapNotification()
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
            .setContentTitle("رسالة منسوخة من WhatsApp")
            .setContentText(text.take(120))
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pending)
            .addAction(android.R.drawable.ic_menu_save, "فتح وحفظ", pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 250, 100, 250))
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(200, notification)
    }

    private fun showTapNotification() {
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
            .setContentTitle("تم نسخ شيء من WhatsApp")
            .setContentText("اضغط هنا لحفظه")
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 250, 100, 250))
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
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    override fun onInterrupt() {
        Log.d(TAG, "الخدمة قُوطعت")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopClipboardChecking()
        whatsappActive = false
        Log.d(TAG, "الخدمة توقفت")
    }
}
