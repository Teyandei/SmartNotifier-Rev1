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

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface RuleDao {

    @Query("SELECT * FROM rules WHERE channelId = :channelId ORDER BY lineNumber ASC")
    suspend fun getByChannel(channelId: String): List<RuleRow>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: RuleRow): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<RuleRow>)

    @Update
    suspend fun update(rule: RuleRow)

    @Delete
    suspend fun delete(rule: RuleRow)

    @Query("DELETE FROM rules WHERE channelId = :channelId")
    suspend fun deleteByChannel(channelId: String)

    @Transaction
    suspend fun upsert(rule: RuleRow) {
        if (rule.id == 0L) insert(rule) else update(rule)
    }

    @Transaction
    suspend fun replaceForChannel(channelId: String, rows: List<RuleRow>) {
        deleteByChannel(channelId)
        if (rows.isNotEmpty()) {
            insertAll(rows)
        }
    }
}
