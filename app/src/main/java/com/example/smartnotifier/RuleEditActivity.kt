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
import androidx.core.content.IntentCompat
import com.google.android.material.button.MaterialButton
import com.example.smartnotifier.data.db.RuleRow

class RuleEditActivity : AppCompatActivity() {
    private lateinit var btnSave: MaterialButton
    private var dirty = false

    // 表示用のローカルコピー（保存まではここを書き換える）
    private lateinit var rows: MutableList<RuleRow>
    private lateinit var lastValidSearchTexts: MutableList<String>
    private var isRestoringTitle = false

    private var pickIndex: Int = -1
    private val pickSound =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && pickIndex >= 0) {

                // ★FIX: Activity の intent ではなく、結果の intent を使う
                val dataIntent = result.data

                // 型付き getParcelableExtra で安全に取得
                val uri: Uri? = IntentCompat.getParcelableExtra(
                    dataIntent,
                    RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                    Uri::class.java
                )

                // デフォルト着信音を選んだ場合は DEFAULT_NOTIFICATION_URI が来ることがある
                val picked = uri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                if (picked != null) {
                    // 表示名に変換
                    val title = RingtoneManager.getRingtone(this, picked)?.getTitle(this) ?: picked.toString()

                    // 画面へ反映
                    soundEdits[pickIndex].setText(title)

                    // rowsへ保存（保存ボタン押下でCSVへ反映）
                    rows[pickIndex] = rows[pickIndex].copy(soundKey = picked)

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
        ChannelRulesStore.ensureInitialized(this, ChannelID.ChannelId.CHATGPT_TASK.id)
        rows = ChannelRulesStore.loadAll(this, ChannelID.ChannelId.CHATGPT_TASK.id)
        lastValidSearchTexts = MutableList(titleEdits.size) { index ->
            rows.getOrNull(index)?.searchText.orEmpty()
        }

        // 初期表示：タイトルとサウンド名（soundKeyはURI想定→タイトルに変換）
        for (i in titleEdits.indices) {
            val row = rows.getOrNull(i) ?: continue
            val initialText = row.searchText.orEmpty()
            titleEdits[i].setText(initialText)
            lastValidSearchTexts[i] = initialText

            val display = soundDisplayName(row.soundKey)
            soundEdits[i].setText(display)

            // 編集検知：タイトル
            titleEdits[i].addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (isRestoringTitle) {
                        return
                    }

                    val text = s?.toString().orEmpty()
                    if (text.isBlank()) {
                        val previous = lastValidSearchTexts[i]
                        isRestoringTitle = true
                        titleEdits[i].setText(previous)
                        titleEdits[i].setSelection(previous.length)
                        isRestoringTitle = false
                        AlertDialog.Builder(this@RuleEditActivity)
                            .setMessage("空白には設定できません")
                            .setPositiveButton("OK", null)
                            .show()
                        return
                    }

                    lastValidSearchTexts[i] = text
                    rows[i] = rows[i].copy(searchText = text)
                    setDirty(true)
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            // 通知音欄：タップでピッカー
            soundEdits[i].setOnClickListener {
                pickIndex = i
                launchPicker(rows[i].soundKey?.takeIf { it != Uri.EMPTY })
            }
        }

        // 保存
        btnSave.setOnClickListener {
            // enable は編集していないので既存値を保持
            ChannelRulesStore.saveAll(this, ChannelID.ChannelId.CHATGPT_TASK.id, rows)
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

    private fun launchPicker(current: Uri?) {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            current?.let {
                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, it)
            }
        }
        pickSound.launch(intent)
    }

    // URI or 任意のキーから表示名を作る
    private fun soundDisplayName(key: Uri?): String {
        if (key == null || key == Uri.EMPTY) return ""  // 未設定
        return runCatching {
            RingtoneManager.getRingtone(this, key)?.getTitle(this)
        }.getOrNull() ?: key.toString()
    }
}
