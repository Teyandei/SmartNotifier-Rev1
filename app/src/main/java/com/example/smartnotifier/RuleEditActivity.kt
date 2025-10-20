package com.example.smartnotifier

import android.app.AlertDialog
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.example.smartnotifier.data.db.RuleRow
import kotlinx.coroutines.launch
import kotlin.getValue

class RuleEditActivity : AppCompatActivity() {
    private val viewModel: RuleEditViewModel by viewModels {
        // ViewModelProvider.Factoryの実装
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            // 修正後の正しいシグネチャ： T の制約を ViewModel に戻す
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                // RuleEditViewModelを生成し、依存関係（ChannelRulesStore）を渡す
                // RuleEditViewModelは AndroidViewModel を継承しているため、このキャストは安全です。
                return RuleEditViewModel(application, ChannelRulesStore) as T
            }
        }
    }

    private lateinit var btnSave: MaterialButton
    private var dirty = false

    // 表示用のローカルコピー（保存まではここを書き換える）
    private var rulesRowsCache: MutableList<RuleRow> = mutableListOf()

    private lateinit var lastValidSearchTexts: MutableList<String>
    private var isRestoringTitle = false

    private var pickIndex: Int = -1
    private val pickSound =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && pickIndex >= 0) {

                // ★FIX: Activity の intent ではなく、結果の intent を使う
                val dataIntent = result.data

                if (dataIntent != null) {
                    // 型付き getParcelableExtra で安全に取得
                    val uri: Uri? = IntentCompat.getParcelableExtra(
                        dataIntent,
                        RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                        Uri::class.java
                    )

                    // デフォルト着信音を選んだ場合は DEFAULT_NOTIFICATION_URI が来ることがある
                    val picked =
                        uri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                    if (picked != null) {
                        // 表示名に変換
                        val title = RingtoneManager.getRingtone(this, picked)?.getTitle(this)
                            ?: picked.toString()

                        // 画面へ反映
                        soundEdits[pickIndex].setText(title)

                        // キャッシュへ保存（保存ボタン押下でDB反映）
                        rulesRowsCache[pickIndex] = rulesRowsCache[pickIndex].copy(soundKey = picked)

                        setDirty(true)
                    }
                } else {
                    Log.w("RuleEditActivity", "dataIntent was null")
                }
            }
            pickIndex = -1
        }

    /*
        EditText View 参照
     */
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

        lifecycleScope.launch {
            // 監視対象を rulesData に変更
            viewModel.rulesData.collect { rules ->
                // RuleRowのリストをローカルキャッシュにディープコピーして保持
                rulesRowsCache.clear()
                rulesRowsCache.addAll(rules.map { it.copy() }) // 【重要】編集用にコピーを保持

                // 画面の各UIコンポーネントにデータをバインド
                setDisplayUnit(rulesRowsCache)
            }
        }

        // 保存
        btnSave = findViewById(R.id.btnSave)
        btnSave.setOnClickListener {
            // ViewModelのDB更新関数を呼び出し、編集済みキャッシュを渡す
            viewModel.updateByChannel(ChannelID.CHATGPT_TASK, rulesRowsCache)

            // 保存完了後、保存ボタンを非活性化する。RuleActivityは戻るボタンでのみ終了する。
            setDirty(false)
        }

        // 戻る
        val btnBack: MaterialButton = findViewById(R.id.btnBack)
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

    private fun setDisplayUnit(rules: List<RuleRow>) {
        if (rules.isEmpty()) {
            return
        }

        rules.forEachIndexed { i, rule ->
            if (i >= titleEdits.size) return@forEachIndexed // 安全装置

            val searchText = rule.searchText ?: ""
            titleEdits[i].setText(searchText)
            titleEdits[i].addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (isRestoringTitle) return

                    val text = s?.toString() ?: ""
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
                    rulesRowsCache[i] = rulesRowsCache[i].copy(searchText = text)
                    setDirty(true)
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            val soundName = RingtoneManager.getRingtone(this, rule.soundKey)?.getTitle(this) ?: "Undefined"
            soundEdits[i].setText(soundName)
            soundEdits[i].setOnClickListener {
                pickIndex = i
                launchPicker(rulesRowsCache[i].soundKey?.takeIf { it != Uri.EMPTY })
            }
        }

        // 編集前の状態（lastValidSearchTexts）も初期化
        lastValidSearchTexts = rules.map { it.searchText.orEmpty() }.toMutableList()

        // 保存ボタンの状態をリセット
        setDirty(false)
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
}
