package com.example.smartnotifier

import android.content.Context
import java.io.File
import java.nio.charset.Charset

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
                out += RuleRow(parts[0], parts[1], parts[2])
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
    fun defaultTenRows(): List<RuleRow> = listOf(
        RuleRow("tabako", "content://settings/system/notification_sound", ""),
        RuleRow("start",  "content://settings/system/notification_sound", ""),
        RuleRow("slot3",  "content://settings/system/notification_sound", ""),
        RuleRow("slot4",  "content://settings/system/notification_sound", ""),
        RuleRow("slot5",  "content://settings/system/notification_sound", ""),
        RuleRow("slot6",  "content://settings/system/notification_sound", ""),
        RuleRow("slot7",  "content://settings/system/notification_sound", ""),
        RuleRow("slot8",  "content://settings/system/notification_sound", ""),
        RuleRow("slot9",  "content://settings/system/notification_sound", ""),
        RuleRow("slot10", "content://settings/system/notification_sound", "")
    )
}