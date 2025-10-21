package com.example.smartnotifier

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.smartnotifier.data.db.UriConverters
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(application, ChannelRulesStore) as T
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 権限が付与された。特に何もしなくても、ユーザーは次の操作を続けられる。
        } else {
            // 権限が拒否された。必要であれば、なぜ権限が必要かを説明するUIを表示する。
        }
    }

    private data class RuleRowViews(val pattern: TextView, val sound: TextView, val enable: SwitchMaterial)
    private val ruleRowViews = mutableListOf<RuleRowViews>()
    private val uriConverters = UriConverters()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeRuleViews()
        ensureNotificationChannel(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.rulesData.collect { displayList ->
                    setDisplayUnit(displayList)
                }
            }
        }

        findViewById<Button>(R.id.openRuleEdit).setOnClickListener {
            startActivity(Intent(this, RuleEditActivity::class.java))
        }
    }

    private fun initializeRuleViews() {
        val ids = listOf(
            Triple(R.id.pattern1, R.id.soundKey1, R.id.enable1),
            Triple(R.id.pattern2, R.id.soundKey2, R.id.enable2),
            Triple(R.id.pattern3, R.id.soundKey3, R.id.enable3),
            Triple(R.id.pattern4, R.id.soundKey4, R.id.enable4),
            Triple(R.id.pattern5, R.id.soundKey5, R.id.enable5),
            Triple(R.id.pattern6, R.id.soundKey6, R.id.enable6),
            Triple(R.id.pattern7, R.id.soundKey7, R.id.enable7),
            Triple(R.id.pattern8, R.id.soundKey8, R.id.enable8),
            Triple(R.id.pattern9, R.id.soundKey9, R.id.enable9),
            Triple(R.id.pattern10, R.id.soundKey10, R.id.enable10)
        )

        ids.forEach { (patId, sndId, swId) ->
            ruleRowViews.add(
                RuleRowViews(
                    pattern = findViewById(patId),
                    sound = findViewById(sndId),
                    enable = findViewById(swId)
                )
            )
        }
    }

    private fun hasNotificationListenerAccess(): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
    }

    private fun promptPermissions(isEnablingRule: Boolean) {
        // 1. 通知リスナー権限のチェック (最優先)
        if (!hasNotificationListenerAccess()) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            return
        }

        // 2. 通知発行権限のチェック (ルール有効化時のみ)
        if (isEnablingRule && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun setDisplayUnit(rules: List<RuleDisplayItem>) {
        ruleRowViews.forEachIndexed { index, views ->
            val rule = rules.getOrNull(index)

            val soundUri = uriConverters.toUri(rule?.soundKeyDisplay) ?: Uri.EMPTY
            val soundName = if (soundUri == Uri.EMPTY) "" else RingtoneManager.getRingtone(this, soundUri)?.getTitle(this) ?: "Undefined"

            views.pattern.text = rule?.searchText.orEmpty()
            views.sound.text = soundName

            views.enable.setOnCheckedChangeListener(null)
            views.enable.isChecked = rule?.isEnabled == true
            views.enable.setOnCheckedChangeListener { _, isChecked ->
                // 権限チェックとリクエストをここで行う
                promptPermissions(isEnablingRule = isChecked)
                viewModel.updateRuleEnabled(index, isChecked)
            }
        }
    }

    private fun ensureNotificationChannel(context: Context) {
        val nm = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val ch = NotificationChannel(
            AppConstants.CHATGPT_TASK,
            "ChatGPTタスク通知",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        nm.createNotificationChannel(ch)
    }

    override fun onResume() {
        super.onResume()
        viewModel.reloadData()
    }
}
