package com.example.smartnotifier.data.db

import android.net.Uri
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rules",
    // channelIdとlineNumberの組み合わせがユニークであることを保証するインデックス
    indices = [Index(value = ["channelId", "lineNumber"], unique = true)]
)
data class RuleRow(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L, // 独立したプライマリキー
    val channelId: String,        // チャンネルID
    val lineNumber: Int,          // 表示順 (0-9)
    val searchText: String?,      // 検索キーワード
    val soundKey: Uri?,           // 通知音のURI
    val enabled: Boolean = false  // ルールの有効/無効フラグ
)
