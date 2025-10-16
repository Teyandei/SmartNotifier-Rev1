package com.example.smartnotifier.data.repo

import com.example.smartnotifier.data.db.AppDatabase
import com.example.smartnotifier.data.db.RuleRow

class ChannelRulesRepository(
    private val db: AppDatabase
) {
    private val dao = db.ruleDao()

    suspend fun getByChannel(channelId: String) = dao.getByChannel(channelId)
    suspend fun getEnabledByPackage(pkg: String) = dao.getEnabledByPackage(pkg)
    suspend fun upsert(rule: RuleRow) = dao.upsert(rule)
    suspend fun delete(rule: RuleRow) = dao.delete(rule)
}