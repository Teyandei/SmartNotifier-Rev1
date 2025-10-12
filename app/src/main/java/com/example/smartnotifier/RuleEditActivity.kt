package com.example.smartnotifier

import android.app.AlertDialog
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class RuleEditActivity : AppCompatActivity() {

    private val channelId = "jawbone"      // 編集対象のチャンネルID（必要に応じて渡す）

    private lateinit var btnSave: MaterialButton
    private var dirty = false

    // 表示用のローカルコピー（保存まではここを書き換える）
    private lateinit var rows: MutableList<RuleRow>

    private var pickIndex: Int = -1
    private val pickSound =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && pickIndex >= 0) {
                val uri: Uri? =
                    result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                if (uri != null) {
                    // 選択した音名を取得して表示
                    val title = RingtoneManager.getRingtone(this, uri)?.getTitle(this) ?: uri.toString()
                    // 画面の soundX を更新
                    soundEdits[pickIndex].setText(title)
                    // rows に URI 文字列を保持（保存時に使う）
                    rows[pickIndex] = rows[pickIndex].copy(soundKey = uri.toString())
                    setDirty(true)
                }
            }
            pickIndex = -1
        }

    // 3項目分の View 参照（4〜10を増やすなら配列に追加）
    private val titleEdits by lazy {
        arrayOf<EditText>(
            findViewById(R.id.title1),
            findViewById(R.id.title2),
            findViewById(R.id.title3),
            findViewById(R.id.title4),
            findViewById(R.id.title5),
            findViewById(R.id.title6),
            findViewById(R.id.title7),
            findViewById(R.id.title8),
            findViewById(R.id.title9),
            findViewById(R.id.title10)
       )
    }
    private val soundEdits by lazy {
        arrayOf<EditText>(
            findViewById(R.id.sound1),
            findViewById(R.id.sound2),
            findViewById(R.id.sound3),
            findViewById(R.id.sound4),
            findViewById(R.id.sound5),
            findViewById(R.id.sound6),
            findViewById(R.id.sound7),
            findViewById(R.id.sound8),
            findViewById(R.id.sound9),
            findViewById(R.id.sound10)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rule_edit)

        btnSave = findViewById(R.id.btnSave)
        val btnBack: MaterialButton = findViewById(R.id.btnBack)

        // TSVを用意 → 読み込み
        ChannelRulesStore.ensureInitialized(this, channelId)
        rows = ChannelRulesStore.loadAll(this, channelId)

        // 初期表示：タイトルとサウンド名（soundKeyはURI想定→タイトルに変換）
        for (i in titleEdits.indices) {
            val row = rows.getOrNull(i) ?: continue
            titleEdits[i].setText(row.title)

            val display = soundDisplayName(row.soundKey)
            soundEdits[i].setText(display)

            // 編集検知：タイトル
            titleEdits[i].addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    rows[i] = rows[i].copy(title = s?.toString().orEmpty())
                    setDirty(true)
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            // 通知音欄：タップでピッカー
            soundEdits[i].setOnClickListener {
                pickIndex = i
                launchPicker(rows[i].soundKey.takeIf { it.isNotBlank() })
            }
        }

        // 保存
        btnSave.setOnClickListener {
            // enable は編集していないので既存値を保持
            ChannelRulesStore.saveAll(this, channelId, rows)
            setDirty(false)
        }

        // 戻る
        btnBack.setOnClickListener {
            if (!dirty) {
                finish()
            } else {
                AlertDialog.Builder(this)
                    .setMessage("変更した内容は反映されません。よろしいですか？")
                    .setPositiveButton("はい") { _, _ -> finish() }
                    .setNegativeButton("いいえ", null)
                    .show()
            }
        }
    }

    private fun setDirty(v: Boolean) {
        dirty = v
        btnSave.isEnabled = v
    }

    private fun launchPicker(current: String?) {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            if (!current.isNullOrBlank()) {
                runCatching { Uri.parse(current) }.getOrNull()?.let {
                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, it)
                }
            }
        }
        pickSound.launch(intent)
    }

    // URI or 任意のキーから表示名を作る
    private fun soundDisplayName(key: String): String {
        if (key.isBlank()) return ""  // 未設定
        return runCatching {
            val uri = Uri.parse(key)
            RingtoneManager.getRingtone(this, uri)?.getTitle(this)
        }.getOrNull() ?: key
    }
}
