// SmartNotifierService.kt
package com.example.smartnotifier

import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class SmartNotifierService : NotificationListenerService() {

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // アプリ名
        val appLabel = try {
            val ai = packageManager.getApplicationInfo(sbn.packageName, 0)
            packageManager.getApplicationLabel(ai).toString()
        } catch (_: Exception) {
            sbn.packageName  // 取得失敗時はパッケージ名
        }

        // タイトル（null回避）
        val title = sbn.notification.extras.getCharSequence("android.title")?.toString() ?: "(no title)"
        // 参考：本文 → sbn.notification.extras.getCharSequence("android.text")?.toString()
        val channelId = sbn.notification.channelId ?: "(no channel)"

        val msg = "[$appLabel]\nTitle: $title\nChannel: $channelId"

        android.util.Log.i(
            "SmartNotif",
            "pkg=${sbn.packageName}, $msg"
        )
        //// サービスからのToastはメインスレッドで
        //mainHandler.post {
        //    Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
        //}
    }
}
