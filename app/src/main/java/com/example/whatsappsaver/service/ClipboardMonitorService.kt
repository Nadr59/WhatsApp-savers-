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
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.whatsappsaver.MainActivity

class ClipboardMonitorService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var lastText = ""
    private var lastReadTime = 0L
    private var whatsappOpen = false
    private var eventCount = 0

    companion object {
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

        // استرجع آخر نص محفوظ
        val prefs = getSharedPreferences("wa_saver", MODE_PRIVATE)
        lastText = prefs.getString("last", "") ?: ""

        serviceInfo = serviceInfo.apply {
            // نراقب كل الأحداث
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_CLICKED or
                    AccessibilityEvent.TYPE_VIEW_LONG_CLICKED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }

        createNotificationChannel()
        startForeground(FOREGROUND_ID, buildOngoing("WhatsApp Saver نشط", "جاهز لمراقبة النسخ"))

        Toast.makeText(this, "WhatsApp Saver: الخدمة بدأت!", Toast.LENGTH_LONG).show()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val pkg = event.packageName?.toString() ?: return

        // تجاهل أحداث التطبيق نفسه
        if (pkg.contains(this.packageName)) return

        val isWhatsApp = pkg.contains("whatsapp", ignoreCase = true)

        if (isWhatsApp) {
            if (!whatsappOpen) {
                whatsappOpen = true
                // أرسل Toast مرة واحدة لما يفتح WhatsApp
                handler.post {
                    Toast.makeText(this, "WhatsApp مفتوح - المراقبة نشطة", Toast.LENGTH_SHORT).show()
                }
            }

            eventCount++

            // اقرأ الحافظة بعد تأخير بسيط (لما المستخدم ينسخ)
            handler.postDelayed({
                readClipboard()
            }, 300)

        } else {
            if (whatsappOpen) {
                whatsappOpen = false
            }
        }
    }

    private fun readClipboard() {
        // منع القراءة المتكررة
        val now = System.currentTimeMillis()
        if (now - lastReadTime < 1500) return
        lastReadTime = now

        try {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = cm.primaryClip

            if (clip == null || clip.itemCount == 0) return

            val text = clip.getItemAt(0).text?.toString()
            if (text.isNullOrBlank() || text.length < 2) return
            if (text == lastText) return

            // نص جديد!
            lastText = text

            // احفظ في SharedPreferences
            getSharedPreferences("wa_saver", MODE_PRIVATE)
                .edit().putString("last", text).apply()

            // أرسل Toast
            Toast.makeText(this, "تم النسخ: ${text.take(40)}", Toast.LENGTH_SHORT).show()

            // أرسل إشعار الحفظ
            showSaveNotification(text)

            // حدث إشعار الخدمة
            updateOngoing("تم النسخ!", text.take(50))

        } catch (e: SecurityException) {
            // Android منع القراءة
            Toast.makeText(this, "خطأ أمني: لا يمكن قراءة الحافظة", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_SHORT).show()
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
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(SAVE_NOTIF_ID, notification)
    }

    private fun updateOngoing(title: String, text: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(FOREGROUND_ID, buildOngoing(title, text))
    }

    private fun buildOngoing(title: String, text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle(title)
            .setContentText(text)
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
        handler.removeCallbacksAndMessages(null)
    }
}
