package com.example.smartnotifier

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import com.example.smartnotifier.data.db.DatabaseProvider
import com.example.smartnotifier.data.db.RuleRow
import com.example.smartnotifier.data.repo.ChannelRulesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

object ChannelRulesStore {

    private fun repository(context: Context): ChannelRulesRepository {
        return ChannelRulesRepository(DatabaseProvider.get(context))
    }

    suspend fun ensureInitialized(
        context: Context,
        channelId: String,
        initialRows: List<RuleRow> = defaultTenRows()
    )  = withContext(Dispatchers.IO) {
        Log.d("ChannelRulesStore", "ensureInitialized: start on ${Thread.currentThread().name}")

        val repo = repository(context)
        val current = repo.getByChannel(channelId)
        if (current.isEmpty()) {
            val rows = initialRows.map { it.copy(channelId = channelId, id = 0L) }
            repo.replaceForChannel(channelId, rows)
        }
        Log.d("ChannelRulesStore", "ensureInitialized: end")
    }

    suspend fun getByChannel(context: Context, channelId: String) : List<RuleRow> {
        val repo = repository(context)
        val rows = withContext(Dispatchers.IO) {
            repo.getByChannel(channelId)
        }
        return rows
    }
    suspend fun loadAll(context: Context, channelId: String): List<RuleRow>
    = withContext(Dispatchers.IO) {
        Log.d("ChannelRulesStore", "loadAll start.")
        val repo = repository(context)
        repo.getByChannel(channelId)
    }

    suspend fun upsertRule(context: Context, rule: RuleRow) {
        withContext(Dispatchers.IO) {
            repository(context).upsert(rule)
        }
    }

    // 【★修正点】saveAll を削除または saveByChannel に置き換え、suspendファンクションにする
    // RuleEditViewModelから呼ばれる saveByChannel
    suspend fun saveByChannel(context: Context, channelId: String, rows: List<RuleRow>) {
        withContext(Dispatchers.IO) {
            val repo = repository(context)
            val normalized = rows.map { row ->
                // DBに保存する前に、念のため channelId が正しいことを保証し、
                // 新規行の場合に id=0Lとなるようにする。（※ id=0LのチェックはRuleDaoのupsertで対応済みなら不要）
                row.copy(channelId = channelId)
            }
            // ChannelRulesRepository の replaceForChannel が呼ばれ、DBが更新される
            repo.replaceForChannel(channelId, normalized)
            Log.d("ChannelRulesStore", "saveByChannel end. Saved ${rows.size} rows for channel $channelId")
        }
    }
    fun defaultTenRows(): List<RuleRow> {
        Log.d("ChannelRulesStore", "defaultTenRows: start on ${Thread.currentThread().name}")
        val defaultNotificationUri: Uri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) ?: Uri.EMPTY
        Log.d("ChannelRulesStore", "defaultNotificationUri = ${defaultNotificationUri}")
        return List(10) { index ->
            RuleRow(
                channelId = ChannelID.ChannelId.CHATGPT_TASK.toString(),
                appPackage = null,
                soundKey = defaultNotificationUri,
                enabled = false,
                priority = 0,
                searchText = "slot${index + 1}"
            )
        }
    }
}
