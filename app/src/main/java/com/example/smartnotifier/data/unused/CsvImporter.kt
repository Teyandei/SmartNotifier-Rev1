package com.example.smartnotifier.data.unused

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.smartnotifier.data.db.AppDatabase
import com.example.smartnotifier.data.db.RuleRow
import java.io.File

object CsvImporter {
    suspend fun importIfNeeded(context: Context, db: AppDatabase) = withContext(Dispatchers.IO) {
        val flag = File(context.filesDir, ".csv_migrated_flag")
        if (flag.exists()) return@withContext

        val dataDir = File(context.filesDir, "data")
        if (dataDir.exists()) {
            dataDir.listFiles { f -> f.extension == "csv" }?.forEach { csv ->
                // TODO: 実CSV仕様に合わせてパース
                // 例: channelId はファイル名から、行は "appPackage,soundUri,enabled,priority,searchText"
                val channelId = csv.nameWithoutExtension
                csv.useLines { lines ->
                    lines.forEach { line ->
                        val cols = line.split(',')
                        val row = RuleRow(
                            channelId = channelId,
                            appPackage = cols.getOrNull(0),
                            soundKey  = cols.getOrNull(1)?.takeIf { it.isNotBlank() }?.let { android.net.Uri.parse(it) },
                            enabled   = cols.getOrNull(2)?.toBooleanStrictOrNull() ?: true,
                            priority  = cols.getOrNull(3)?.toIntOrNull() ?: 0,
                            searchText = cols.getOrNull(4)
                        )
                        db.ruleDao().upsert(row)
                    }
                }
            }
        }
        flag.writeText("done")
    }
}
