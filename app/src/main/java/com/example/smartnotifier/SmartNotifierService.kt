package com.example.smartnotifier

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
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

        // ターゲットアプリ以外は無視
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
                Log.i("SmartNotifier", "✅ ルールに一致: '${matchedRule.searchText}' -> 再通知します")

                // ★【最重要】元の通知をキャンセルして、デフォルト音が鳴るのを防ぐ
                cancelNotification(sbn.key)

                repostNotification(matchedRule.soundKey, title, text)
            }
        }
    }

    private fun repostNotification(soundUri: android.net.Uri?, title: String, text: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelIdForSound = "repost_${soundUri?.toString() ?: "default"}"

        if (notificationManager.getNotificationChannel(channelIdForSound) == null) {
            // 【改善点】チャンネル名に、通知音自身の名前を使う
            val soundTitle = soundUri?.let { RingtoneManager.getRingtone(this, it)?.getTitle(this) } ?: "Custom Sound"
            val channel = NotificationChannel(channelIdForSound, soundTitle, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Custom sound notifications by Smart Notifier"
                setSound(soundUri, null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelIdForSound)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: 正式なアイコンに要変更
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