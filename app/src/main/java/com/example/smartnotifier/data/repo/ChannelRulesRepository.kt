package com.example.smartnotifier.data.repo

import com.example.smartnotifier.data.db.AppDatabase
import com.example.smartnotifier.data.db.RuleRow

class ChannelRulesRepository(
    private val db: AppDatabase
) {
    private val dao = db.ruleDao()

    suspend fun getByChannel(channelId: String) = dao.getByChannel(channelId)
    suspend fun upsert(rule: RuleRow) = dao.upsert(rule)
    suspend fun delete(rule: RuleRow) = dao.delete(rule)
    suspend fun deleteByChannel(channelId: String) = dao.deleteByChannel(channelId)
    suspend fun replaceForChannel(channelId: String, rows: List<RuleRow>) =
        dao.replaceForChannel(channelId, rows)
}