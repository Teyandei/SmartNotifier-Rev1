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

package com.example.smartnotifier.data.db

import android.net.Uri
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rules",
    // channelIdとlineNumberの組み合わせがユニークであることを保証するインデックス
    indices = [Index(value = ["packageName", "channelId", "lineNumber"], unique = true)]
)
data class RuleRow(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L, // 独立したプライマリキー
    val packageName: String,      // パッケージ名(pkg name)
    val channelId: String,        // チャンネルID
    val lineNumber: Int,          // 表示順 (0-9)
    val searchText: String?,      // 検索キーワード
    val soundKey: Uri?,           // 通知音のURI
    val enabled: Boolean = false  // ルールの有効/無効フラグ
)
