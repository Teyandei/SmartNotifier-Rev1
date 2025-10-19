package com.example.smartnotifier

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import com.example.smartnotifier.data.db.DatabaseProvider
import com.example.smartnotifier.data.db.RuleRow
import com.example.smartnotifier.data.repo.ChannelRulesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ChannelRulesStore {

    private fun repository(context: Context): ChannelRulesRepository {
        return ChannelRulesRepository(DatabaseProvider.get(context))
    }

    // ヘルパー関数を導入してIOコンテキストでの処理を抽象化
    private suspend fun <T> onRepo(
        context: Context,
        block: suspend (ChannelRulesRepository) -> T
    ): T = withContext(Dispatchers.IO) {
        block(repository(context))
    }

    suspend fun ensureInitialized(
        context: Context,
        channelId: String,
        initialRows: List<RuleRow> = defaultTenRows()
    ) = onRepo(context) { repo ->
        if (repo.getByChannel(channelId).isEmpty()) {
            val rows = initialRows.map { it.copy(channelId = channelId, id = 0L) }
            repo.replaceForChannel(channelId, rows)
        }
    }

    suspend fun getByChannel(context: Context, channelId: String): List<RuleRow> = onRepo(context) {
        it.getByChannel(channelId)
    }

    suspend fun upsertRule(context: Context, rule: RuleRow) = onRepo(context) {
        it.upsert(rule)
    }

    suspend fun saveByChannel(context: Context, channelId: String, rows: List<RuleRow>) = onRepo(context) { repo ->
        val normalized = rows.map { it.copy(channelId = channelId) }
        repo.replaceForChannel(channelId, normalized)
    }

    private fun defaultTenRows(): List<RuleRow> {
        return List(10) { index ->
            RuleRow(
                channelId = ChannelID.CHATGPT_TASK,
                appPackage = null,
                soundKey = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) ?: Uri.EMPTY,
                enabled = false,
                priority = 0,
                searchText = "slot${index + 1}"
            )
        }
    }
}