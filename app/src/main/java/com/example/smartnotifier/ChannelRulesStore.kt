package com.example.smartnotifier

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
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

    fun ensureInitialized(
        context: Context,
        channelId: String,
        initialRows: List<RuleRow> = defaultTenRows()
    ) {
        runBlocking(Dispatchers.Main.immediate) {
            val repo = repository(context)
            withContext(Dispatchers.IO) {
                val current = repo.getByChannel(channelId)
                if (current.isEmpty()) {
                    val rows = initialRows.map { it.copy(channelId = channelId, id = 0L) }
                    repo.replaceForChannel(channelId, rows)
                }
            }
        }
    }

    fun loadAll(context: Context, channelId: String): MutableList<RuleRow> =
        runBlocking(Dispatchers.Main.immediate) {
            val repo = repository(context)
            val rows = withContext(Dispatchers.IO) {
                repo.getByChannel(channelId)
            }
            rows.toMutableList()
        }

    fun saveAll(context: Context, channelId: String, rows: List<RuleRow>) {
        runBlocking(Dispatchers.Main.immediate) {
            val repo = repository(context)
            withContext(Dispatchers.IO) {
                val normalized = rows.map { row ->
                    row.copy(channelId = channelId)
                }
                repo.replaceForChannel(channelId, normalized)
            }
        }
    }

    fun defaultTenRows(): List<RuleRow> {
        val defaultNotificationUri: Uri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) ?: Uri.EMPTY

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
