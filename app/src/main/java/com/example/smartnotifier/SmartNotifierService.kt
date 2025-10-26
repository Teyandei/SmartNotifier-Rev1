package com.example.smartnotifier

import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.RingtoneManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmartNotifierService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i("SmartNotifier", "✅ NotificationListener connected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        val pkg = sbn.packageName
        val channelId = sbn.notification.channelId.toString()
        val title = sbn.notification.extras.getString("android.title") ?: ""
        val text = sbn.notification.extras.getString("android.text") ?: ""

        if (pkg != AppConstants.CHATGPT_PACKAGENAME) return

        Log.i("SmartNotifier", "🔔 ChatGPT通知受信: (ID:$channelId) $title - $text")

        serviceScope.launch {
            val rules = ChannelRulesStore.getByChannel(applicationContext, channelId)
            val matchedRule = rules.firstOrNull { rule ->
                rule.enabled && rule.searchText?.let { keyword ->
                    title.contains(keyword, ignoreCase = true) || text.contains(keyword, ignoreCase = true)
                } ?: false
            }

            if (matchedRule != null) {
                if (NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
                    Log.i("SmartNotifier", "✅ ルールに一致: '${matchedRule.searchText}' -> 再通知します")
                    cancelNotification(sbn.key)
                    repostNotification(matchedRule.soundKey, title, text, matchedRule.searchText)
                } else {
                    Log.w("SmartNotifier", "ルールには一致しましたが、通知発行権限がないため何もしません。")
                }
            }
        }
    }

    private fun repostNotification(soundUri: android.net.Uri?, title: String, text: String, searchText: String?) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channelIdForSound = "repost_${soundUri?.toString() ?: "default"}"

        if (notificationManager.getNotificationChannel(channelIdForSound) == null) {
            val channelName = if (!searchText.isNullOrBlank()) {
                "Rule: $searchText"
            } else {
                soundUri?.let { RingtoneManager.getRingtone(this, it)?.getTitle(this) } ?: "Custom Sound"
            }
            val channel = NotificationChannel(channelIdForSound, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Custom sound notifications by Smart Notifier"
                setSound(soundUri, null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelIdForSound)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setSound(soundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // 現状、何もしない
    }
}