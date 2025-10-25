package com.example.smartnotifier

import android.app.AlertDialog
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
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
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RuleEditViewModel(application, ChannelRulesStore) as T
            }
        }
    }

    private lateinit var btnSave: MaterialButton
    private var dirty = false

    private var rulesRowsCache: MutableList<RuleRow> = mutableListOf()
    private lateinit var lastValidSearchTexts: MutableList<String>
    private var pickIndex: Int = -1

    private val pickSound =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && pickIndex >= 0) {
                val dataIntent = result.data
                if (dataIntent != null) {
                    val uri: Uri? = IntentCompat.getParcelableExtra(
                        dataIntent,
                        RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                        Uri::class.java
                    )
                    val picked = uri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    if (picked != null) {
                        val title = RingtoneManager.getRingtone(this, picked)?.getTitle(this) ?: picked.toString()
                        soundEdits[pickIndex].setText(title)
                        rulesRowsCache[pickIndex] = rulesRowsCache[pickIndex].copy(soundKey = picked)
                        setDirty(true)
                    }
                } else {
                    Log.w("RuleEditActivity", "dataIntent was null")
                }
            }
            pickIndex = -1
        }

    private val titleEdits by lazy {
        List(10) { i -> findViewById<EditText>(resources.getIdentifier("title${i + 1}", "id", packageName)) }
    }
    private val soundEdits by lazy {
        List(10) { i -> findViewById<EditText>(resources.getIdentifier("sound${i + 1}", "id", packageName)) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rule_edit)

        lifecycleScope.launch {
            viewModel.rulesData.collect { rules ->
                rulesRowsCache.clear()
                rulesRowsCache.addAll(rules.map { it.copy() })
                setDisplayUnit(rulesRowsCache)
            }
        }

        setupListeners()

        btnSave = findViewById(R.id.btnSave)
        btnSave.setOnClickListener {
            viewModel.updateByChannel(AppConstants.CHATGPT_TASK, rulesRowsCache)
            setDirty(false)
        }

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

    private fun setupListeners() {
        titleEdits.forEachIndexed { i, editText ->
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) { // フォーカスが外れた時に検証
                    val text = editText.text.toString()
                    if (text.isBlank()) {
                        editText.setText(lastValidSearchTexts[i]) // 元のテキストに戻す
                        AlertDialog.Builder(this@RuleEditActivity)
                            .setMessage("空白には設定できません")
                            .setPositiveButton("OK", null)
                            .show()
                    } else if (text != lastValidSearchTexts[i]) {
                        lastValidSearchTexts[i] = text
                        rulesRowsCache[i] = rulesRowsCache[i].copy(searchText = text)
                        setDirty(true)
                    }
                }
            }
        }

        soundEdits.forEachIndexed { i, editText ->
            editText.setOnClickListener {
                pickIndex = i
                launchPicker(rulesRowsCache.getOrNull(i)?.soundKey?.takeIf { it != Uri.EMPTY })
            }
        }
    }

    private fun setDisplayUnit(rules: List<RuleRow>) {
        if (rules.isEmpty()) return

        rules.forEachIndexed { i, rule ->
            if (i >= titleEdits.size) return@forEachIndexed

            titleEdits[i].setText(rule.searchText ?: "")
            val soundName = RingtoneManager.getRingtone(this, rule.soundKey)?.getTitle(this) ?: "Undefined"
            soundEdits[i].setText(soundName)
        }

        lastValidSearchTexts = rules.map { it.searchText.orEmpty() }.toMutableList()
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