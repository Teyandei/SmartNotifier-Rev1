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
                    ChannelID.CHATGPT_TASK
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
    fun updateByCannel(channelId: String, ruleUpdate: MutableList<RuleRow>){
        viewModelScope.launch {
            try {
                // 【★修正点】ChannelRulesStoreの保存関数を呼び出す
                rulesStore.saveByChannel(
                    getApplication(),
                    channelId,      // ChannelID.ChannelId の id (String) を渡す
                    ruleUpdate
                )
                // 保存完了後、Activity側で画面を閉じたい場合は、ここでStateFlowを使って通知することも可能
            } catch (t: Throwable) {
                // タイムアウトやその他のエラー発生時
                Log.e("RuleEditViewModel", "saveByChannel failed", t)
            }
        }
    }
}