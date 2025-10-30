/*
 * SmartNotifier-Rev1
 * Copyright (C) 2025  Takeaki Yoshizawa
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.example.smartnotifier

import android.Manifest
import android.app.AlertDialog
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
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch
import android.util.Log

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
            Log.d("MainActivity", "Notification permission granted.")
        } else {
            Log.d("MainActivity", "Notification permission denied.")
        }
    }

    private data class RuleRowViews(val pattern: TextView, val sound: TextView, val enable: SwitchMaterial)
    private val ruleRowViews = mutableListOf<RuleRowViews>()
    private val uriConverters = UriConverters()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val topBar = findViewById<MaterialToolbar>(R.id.topBar)
        // 【重要】メニューファイルをツールバーに設定します
        topBar.inflateMenu(R.menu.main_menu)
        topBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_help -> {
                    startActivity(Intent(this, HelpActivity::class.java))
                    true
                }
                else -> false
            }
        }

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

        ruleRowViews.forEachIndexed { index, views ->
            views.enable.setOnCheckedChangeListener { switchView, isChecked ->
                if (isChecked) {
                    // --- ルールを有効にしようとした時 ---

                    // 1. 通知リスナー権限をチェック
                    if (!hasNotificationListenerAccess()) {
                        showListenerAccessDialog()
                        switchView.isChecked = false
                        return@setOnCheckedChangeListener
                    }

                    // 2. 通知発行権限をチェック (Android 13+)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        showPostNotificationDialog()
                        switchView.isChecked = false
                        return@setOnCheckedChangeListener
                    }

                    // 3. 全ての権限が揃っている場合のみ、状態を更新
                    viewModel.updateRuleEnabled(index, true)
                } else {
                    // --- ルールを無効にする時 ---
                    viewModel.updateRuleEnabled(index, false)
                }
            }
        }
    }

    private fun hasNotificationListenerAccess(): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
    }

    private fun showListenerAccessDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_listener_title))
            .setMessage(getString(R.string.permission_listener_message))
            .setPositiveButton(getString(R.string.permission_listener_positive)) { _, _ ->
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
            .setNegativeButton(getString(R.string.permission_listener_negative), null)
            .show()
    }

    private fun showPostNotificationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_post_notification_title))
            .setMessage(getString(R.string.permission_post_notification_message))
            .setPositiveButton(getString(R.string.permission_post_notification_positive)) { _, _ ->
                // OSの権限要求ダイアログを直接表示
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton(getString(R.string.permission_post_notification_negative), null)
            .show()
    }

    private fun setDisplayUnit(rules: List<RuleDisplayItem>) {
        ruleRowViews.forEachIndexed { index, views ->
            val rule = rules.getOrNull(index)

            val soundUri = uriConverters.toUri(rule?.soundKeyDisplay) ?: Uri.EMPTY
            val soundName = if (soundUri == Uri.EMPTY) "" else RingtoneManager.getRingtone(this, soundUri)?.getTitle(this) ?: "Undefined"

            views.pattern.text = rule?.searchText.orEmpty()
            views.sound.text = soundName
            views.enable.isChecked = rule?.isEnabled == true
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