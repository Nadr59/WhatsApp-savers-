package com.example.whatsappsaver.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (ClipboardAccessibilityService.isRunning(context)) {
                // الخدمة Accessibility تشتغل تلقائياً
                // لا نحتاج نسوي شيء
            }
        }
    }
}
