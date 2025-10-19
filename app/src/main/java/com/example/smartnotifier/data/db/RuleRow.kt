package com.example.smartnotifier.data.db

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rules")
data class RuleRow(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val channelId: String,
    val appPackage: String? = null,
    val soundKey: Uri? = null,          // Uri をそのまま扱える（Converterで保存）
    val enabled: Boolean = false,
    val priority: Int = 0,
    @ColumnInfo(name = "note")
    val searchText: String? = null
)
