package com.example.smartnotifier

import android.content.Context
import java.io.File
import java.nio.charset.Charset
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.net.toUri

object ChannelRulesStore {

    private val UTF8: Charset = Charsets.UTF_8

    // For Debug
    private fun debugLogFile(context: Context, channelId: String) {
        val f = File(context.filesDir, "$channelId.csv")
        android.util.Log.i(
            "RulesStore",
            "pkg=${context.packageName}, filesDir=${context.filesDir.absolutePath}, " +
                    "path=${f.absolutePath}, exists=${f.exists()}, size=${if (f.exists()) f.length() else -1}"
        )
    }

    // 例: "jawbone" → "jawbone.csv"（実体はTSV）
    private fun fileFor(context: Context, channelId: String): File =
        File(context.filesDir, "${channelId}.csv")

    /**
     * 初期化：ファイルが無ければ10行の雛形を作る
     */
    fun ensureInitialized(
        context: Context,
        channelId: String,
        initialRows: List<RuleRow> = defaultTenRows()
    ) {
        debugLogFile(context, channelId)
        val f = fileFor(context, channelId)
        if (!f.exists()) {
            saveAll(context, channelId, initialRows)
        }
    }

    /**
     * 読み込み：TSVをList<RuleRow>に
     *（不正行はスキップ。タブは基本使わない前提。タイトルにタブが入る可能性がある場合は事前置換推奨）
     */
    fun loadAll(context: Context, channelId: String): MutableList<RuleRow> {
        val f = fileFor(context, channelId)
        if (!f.exists()) return mutableListOf()
        val lines = f.readLines(UTF8)
        val out = mutableListOf<RuleRow>()
        for (line in lines) {
            if (line.isBlank()) continue
            val parts = line.split('\t', limit = 3) // タブ区切り、2列に限定
            if (parts.size == 3) {
                out += RuleRow(parts[0], parts[1].toUri(), parts[2])
            }
        }
        return out
    }

    /**
     * 保存：List<RuleRow>をTSVに（アトミックセーブ）
     */
    fun saveAll(context: Context, channelId: String, rows: List<RuleRow>) {
        val f = fileFor(context, channelId)
        val tmp = File(f.parentFile, f.name + ".tmp")
        val content = buildString {
            rows.forEach { row ->
                // タブや改行は使わない前提。混入が心配なら置換を推奨（例：\t→␉、\n→␤）
                append(row.title)
                append('\t')
                append(row.soundKey)
                append('\t')
                append(row.enable)
                append('\n')
            }
        }
        tmp.writeText(content, UTF8)
        if (f.exists()) f.delete()
        tmp.renameTo(f)
    }

    /**
     * 1行を「タイトルが一致するなら更新、無ければ末尾に追加」
     */
    fun upsert(context: Context, channelId: String, row: RuleRow) {
        val list = loadAll(context, channelId)
        val idx = list.indexOfFirst { it.title.equals(row.title, ignoreCase = true) }
        if (idx >= 0) list[idx] = row else list += row
        saveAll(context, channelId, list)
    }

    /**
     * 雛形10行（必要に応じて好きな文面に変更）
     */
    fun defaultTenRows(): List<RuleRow> {

        // Contextを使って、現在設定されているデフォルト通知音のUriを取得します
        val defaultNotificationUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // リストの生成と返却
        return listOf(
            // RuleRowの第2パラメータに Uri オブジェクトを渡します
            RuleRow("slot1", defaultNotificationUri, ""),
            RuleRow("slot2",  defaultNotificationUri, ""),
            RuleRow("slot3",  defaultNotificationUri, ""),
            RuleRow("slot4",  defaultNotificationUri, ""),
            RuleRow("slot5",  defaultNotificationUri, ""),
            RuleRow("slot6",  defaultNotificationUri, ""),
            RuleRow("slot7",  defaultNotificationUri, ""),
            RuleRow("slot8",  defaultNotificationUri, ""),
            RuleRow("slot9",  defaultNotificationUri, ""),
            RuleRow("slot10", defaultNotificationUri, "")
        )
    }
}