package com.example.smartnotifier

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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

        val tempChannelId = "smart_notifier_repost"
        // 再通知用のチャンネルがなければ作成
        if (notificationManager.getNotificationChannel(tempChannelId) == null) {
            val channel = NotificationChannel(tempChannelId, "Smart Notifier Repost", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifications reposted by Smart Notifier with custom sounds."
                setSound(soundUri, null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, tempChannelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: 正式なアイコンに要変更
            .setContentTitle(title)
            .setContentText(text)
            .setSound(soundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // 一意のIDで通知を発行（元の通知IDとは別にする）
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // 現状、何もしない
    }
}
