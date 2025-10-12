package com.example.smartnotifier

import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import kotlin.text.set

class MainActivity : AppCompatActivity() {

    private val CHANNEL_ID = "jawbone" // ChatGPTタスクで使用される通知のチャンネルID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn: Button = findViewById(R.id.openRuleEdit)
        ChannelRulesStore.ensureInitialized(this, CHANNEL_ID)
        setDisplayUnit()

        btn.setOnClickListener {
            startActivity(Intent(this, RuleEditActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        setDisplayUnit()
    }

    private fun setDisplayUnit() {
        val rows = ChannelRulesStore.loadAll(this, CHANNEL_ID)

        fun bind(i: Int, patId: Int, sndId: Int, swId: Int) {
            val row = rows.getOrNull(i)
            val soundName = RingtoneManager.getRingtone(this, row?.soundKey?.toUri())?.getTitle(this) ?: row?.soundKey
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
            sw.setOnCheckedChangeListener { _, isChecked ->
                // rows[index] を "1"/"0" に更新
                val cur = rows.getOrNull(index)
                if (cur != null) {
                    rows[index] = cur.copy(enable = if (isChecked) "1" else "0")
                    // TSVに保存（アトミックセーブ）
                    ChannelRulesStore.saveAll(this, CHANNEL_ID, rows)
                }
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
}
