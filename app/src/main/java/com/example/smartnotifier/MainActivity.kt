package com.example.smartnotifier

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.provider.Settings
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.activity.viewModels
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartnotifier.data.db.UriConverters

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels {
        // ViewModelProvider.Factoryの実装
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            // 修正後の正しいシグネチャ： T の制約を ViewModel に戻す
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // MainViewModelを生成し、依存関係（ChannelRulesStore）を渡す
                // MainViewModelは AndroidViewModel を継承しているため、このキャストは安全です。
                return MainViewModel(application, ChannelRulesStore) as T
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity", "onCreate arrived")
        setContentView(R.layout.activity_main)
        Log.d("MainActivity", "setContentView end")

        ensureChannels(this@MainActivity) // ← 先にチャンネル作成
        Log.d("MainActivity", "ensureChannels end")

        lifecycleScope.launch {
            // 監視対象を rulesData に変更
            viewModel.rulesData.collect { displayList ->
                Log.d("MainActivity", "UI更新データ受信: ルール数=${displayList.size}")

                // setDisplayUnit List<RuleDisplayItem> を受け取る
                setDisplayUnit(displayList)
            }
        }

        val btn: Button = findViewById(R.id.openRuleEdit)
        btn.setOnClickListener {
            startActivity(Intent(this, RuleEditActivity::class.java))
        }
        Log.d("MainActivity", "onCreate end")
    }

    private fun hasNotificationAccess(): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
    }

    private fun promptNotificationAccess() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    private fun setDisplayUnit(rules: List<RuleDisplayItem>) {
        val uriConverters = UriConverters()
        // 1. まず、改行で個々のルール（行）に分割する
        // 例: リストビューやテーブルにデータを設定する
        // TextViewなどに単一行で表示する場合は、デバッグ用に joinToString などを使えます
        val textToDisplay = if (rules.isEmpty()) {
            "データなし"
        } else {
            rules.joinToString("\n") {
                "Search: ${it.searchText}, Key: ${it.soundKeyDisplay}, Enable: ${it.isEnabled}"
            }
        }
        Log.d("MainActivity",textToDisplay)

        fun bind(i: Int, patId: Int, sndId: Int, swId: Int) {
            val row = rules.getOrNull(i)
            val soundUri = uriConverters.toUri(row?.soundKeyDisplay) ?: Uri.EMPTY
            val soundName = if (soundUri == Uri.EMPTY) {
                ""
            } else {
                RingtoneManager.getRingtone(this, soundUri)?.getTitle(this) ?: "Undefined"
            }
            findViewById<TextView>(patId).text = row?.searchText.orEmpty()
            findViewById<TextView>(sndId).text = soundName

            val sw = findViewById<SwitchMaterial>(swId)

            // 1. **無限ループ回避のための最重要処理:** リスナーを一旦解除する
            //    これにより、次の行で sw.isChecked を設定してもリスナーは発火しない
            sw.setOnCheckedChangeListener(null)

            // 2. データに基づいてスイッチの状態を設定
            sw.isChecked = row?.isEnabled == true

            // 3. 新しいリスナーを再設定（ユーザー操作時のみ発火する）
            sw.setOnCheckedChangeListener { _, isChecked ->
                // スイッチが押されたら、MainActivityではなくViewModelの関数を呼ぶ
                viewModel.updateRuleEnabled(i, isChecked) // <--- ViewModelに処理を委譲

                // OFF→ON になり、まだ通知アクセスが無いときは誘導
                if (isChecked && !hasNotificationAccess()) {
                    promptNotificationAccess()
                }
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
    }

    fun ensureChannels(context: Context) {
        val nm = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val ch = NotificationChannel(
            ChannelID.ChannelId.CHATGPT_TASK.id,
            "ChatGPTタスク通知",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        nm.createNotificationChannel(ch)
    }

    override fun onResume() {
        super.onResume()

        // ViewModelにデータの再ロードを指示する
        viewModel.reloadData()
    }
}
