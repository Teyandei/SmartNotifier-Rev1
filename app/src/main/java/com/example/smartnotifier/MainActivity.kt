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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(application, ChannelRulesStore) as T
            }
        }
    }

    // A helper data class to hold the views for a single rule row
    private data class RuleRowViews(
        val pattern: TextView,
        val sound: TextView,
        val enable: SwitchMaterial
    )

    // Pre-fetched views to avoid repeated findViewById calls
    private val ruleRowViews = mutableListOf<RuleRowViews>()

    // UriConverters can be a member variable as it's stateless
    private val uriConverters = UriConverters()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find all views once and store them
        initializeRuleViews()

        ensureChannels(this)

        lifecycleScope.launch {
            // Use repeatOnLifecycle for better lifecycle management
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.rulesData.collect { displayList ->
                    Log.d("MainActivity", "UI更新データ受信: ルール数=${displayList.size}")
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

    private fun hasNotificationAccess(): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
    }

    private fun promptNotificationAccess() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    private fun setDisplayUnit(rules: List<RuleDisplayItem>) {
        ruleRowViews.forEachIndexed { index, views ->
            val rule = rules.getOrNull(index)

            val soundUri = uriConverters.toUri(rule?.soundKeyDisplay) ?: Uri.EMPTY
            val soundName = if (soundUri == Uri.EMPTY) {
                ""
            } else {
                RingtoneManager.getRingtone(this, soundUri)?.getTitle(this) ?: "Undefined"
            }

            views.pattern.text = rule?.searchText.orEmpty()
            views.sound.text = soundName

            // This pattern is important to prevent infinite loops.
            // 1. Remove the listener.
            views.enable.setOnCheckedChangeListener(null)
            // 2. Set the state from the data.
            views.enable.isChecked = rule?.isEnabled == true
            // 3. Re-add the listener for user interactions.
            views.enable.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateRuleEnabled(index, isChecked)

                if (isChecked && !hasNotificationAccess()) {
                    promptNotificationAccess()
                }
            }
        }
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
        viewModel.reloadData()
    }
}
