package com.example.smartnotifier

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.provider.Settings
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity() {

   override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ensureChannels(this) // ← 先にチャンネル作成

        ChannelRulesStore.ensureInitialized(this, ChannelId.CHATGPT_TASK.id)
        setDisplayUnit()

        val btn: Button = findViewById(R.id.openRuleEdit)
        btn.setOnClickListener {
            startActivity(Intent(this, RuleEditActivity::class.java))
        }

    }

    override fun onResume() {
        super.onResume()
        setDisplayUnit()
    }

    private fun hasNotificationAccess(): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
    }

    private fun promptNotificationAccess() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    private fun setDisplayUnit() {
        val rows = ChannelRulesStore.loadAll(this, ChannelId.CHATGPT_TASK.id)

        fun bind(i: Int, patId: Int, sndId: Int, swId: Int) {
            val row = rows.getOrNull(i)
            val soundName = row?.soundKey?.takeUnless { it == Uri.EMPTY }
                ?.let { RingtoneManager.getRingtone(this, it)?.getTitle(this) ?: it.toString() }
                ?: ""
            findViewById<TextView>(patId).text  = row?.title.orEmpty()
            findViewById<TextView>(sndId).text  = soundName
            findViewById<SwitchMaterial>(swId).isChecked = when (row?.enable?.lowercase()) {
                "1","true","on","yes" -> true
                else -> false
            }
        }

        bind(0, R.id.pattern1, R.id.soundKey1, R.id.enable1)
        bind(1, R.id.pattern2, R.id.soundKey2, R.id.enable2)
        bind(2, R.id.pattern3, R.id.soundKey3, R.id.enable3)
        bind(3, R.id.pattern4, R.id.soundKey4, R.id.enable4)
        bind(4, R.id.pattern5, R.id.soundKey5, R.id.enable5)
        bind(5, R.id.pattern6, R.id.soundKey6, R.id.enable6)
        bind(6, R.id.pattern7, R.id.soundKey7, R.id.enable7)
        bind(7, R.id.pattern8, R.id.soundKey8, R.id.enable8)
        bind(8, R.id.pattern9, R.id.soundKey9, R.id.enable9)
        bind(9, R.id.pattern10, R.id.soundKey10, R.id.enable10)

        // --- スイッチ変更をTSVへ保存 ---
        fun attachSwitch(swId: Int, index: Int) {
            val sw = findViewById<SwitchMaterial>(swId)
            sw.setOnCheckedChangeListener(null)
            sw.setOnCheckedChangeListener { _, isChecked ->
                val cur = rows.getOrNull(index) ?: return@setOnCheckedChangeListener

                // 1) TSV更新
                rows[index] = cur.copy(enable = if (isChecked) "1" else "0")
                ChannelRulesStore.saveAll(this, ChannelId.CHATGPT_TASK.id, rows)

                // 2) OFF→ON になり、まだ通知アクセスが無いときは誘導
                if (isChecked && !hasNotificationAccess()) {
                    promptNotificationAccess()
                }
                // 3) サービス開始/停止はしない（NotificationListenerService は OS がバインド）
            }
        }


        attachSwitch(R.id.enable1, 0)
        attachSwitch(R.id.enable2, 1)
        attachSwitch(R.id.enable3, 2)
        attachSwitch(R.id.enable4, 3)
        attachSwitch(R.id.enable5, 4)
        attachSwitch(R.id.enable6, 5)
        attachSwitch(R.id.enable7, 6)
        attachSwitch(R.id.enable8, 7)
        attachSwitch(R.id.enable9, 8)
        attachSwitch(R.id.enable10, 9)

    }

    fun ensureChannels(context: Context) {
        val nm = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val ch = NotificationChannel(
            ChannelId.CHATGPT_TASK.id,
            "ChatGPTタスク通知",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        nm.createNotificationChannel(ch)
    }

}
