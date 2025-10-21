package com.example.smartnotifier

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartnotifier.data.db.RuleRow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RuleEditViewModel (application: Application, private val rulesStore: ChannelRulesStore) : AndroidViewModel(application) {
    // 1. UIが表示すべきテキストの状態を管理
    private val _rulesData = MutableStateFlow<List<RuleRow>>(emptyList())
    val rulesData: StateFlow<List<RuleRow>> = _rulesData.asStateFlow()

    init {
        // ViewModelが生成されたら、すぐに非同期初期化を開始
        // ViewModelScopeはViewModelがクリアされると自動でキャンセルされる
        viewModelScope.launch {
            try {
                val ruleRows: List<RuleRow> = rulesStore.getByChannel(
                    getApplication(),
                    AppConstants.CHATGPT_TASK
                )

                // StateFlowを更新
                _rulesData.value = ruleRows

            } catch (t: Throwable) {
                // タイムアウトやその他のエラー発生時
                Log.e("RuleEditViewModel", "loadAll failed", t)
                _rulesData.value = emptyList() // エラー時は空リスト、またはエラー状態を示す別のStateFlowを使う
            }
        }
    }

    // 全ての内容をDBに保存
    fun updateByChannel(channelId: String, ruleUpdate: List<RuleRow>){
        viewModelScope.launch {
            try {
                // 保存する前に、各ルールに正しいlineNumber（インデックス）を割り当てる
                val numberedRows = ruleUpdate.mapIndexed { index, rule ->
                    rule.copy(lineNumber = index)
                }

                rulesStore.saveByChannel(
                    getApplication(),
                    channelId,
                    numberedRows
                )
            } catch (t: Throwable) {
                // タイムアウトやその他のエラー発生時
                Log.e("RuleEditViewModel", "saveByChannel failed", t)
            }
        }
    }
}