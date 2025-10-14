// SmartNotifierService.kt
package com.example.smartnotifier

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class SmartNotifierService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i("SmartNotifier", "✅ NotificationListener connected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        val pkg = sbn.packageName
        val title = sbn.notification.extras.getString("android.title")
        val text  = sbn.notification.extras.getString("android.text")
        Log.i("SmartNotifier", "🔔 通知受信: [$pkg] $title - $text")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn ?: return
        Log.i("SmartNotifier", "🗑 通知削除: [${sbn.packageName}]")
    }
}
