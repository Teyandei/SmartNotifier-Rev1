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
