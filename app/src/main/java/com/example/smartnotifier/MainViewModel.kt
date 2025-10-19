package com.example.smartnotifier

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartnotifier.data.db.RuleRow
import com.example.smartnotifier.data.db.UriConverters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class MainViewModel(application: Application, private val rulesStore: ChannelRulesStore) : AndroidViewModel(application) {

    // 1. UIが表示すべきテキストの状態を管理
    private val _rulesData = MutableStateFlow<List<RuleDisplayItem>>(emptyList())
    val rulesData: StateFlow<List<RuleDisplayItem>> = _rulesData.asStateFlow()

    init {
        // ViewModelが生成されたら、すぐに非同期初期化を開始
        // ViewModelScopeはViewModelがクリアされると自動でキャンセルされる
        viewModelScope.launch {
            try {
                // withTimeout はここで設定（5秒の安全装置）
                withTimeout(5_000) {
                    rulesStore.ensureInitialized(
                        getApplication(), // AndroidViewModelからApplication Contextを取得
                        ChannelID.CHATGPT_TASK
                    )
                }

                // StateFlowを更新
                reloadRulesData()

            } catch (t: Throwable) {
                // タイムアウトやその他のエラー発生時
                Log.e("MainViewModel", "ensureInitialized failed", t)
                _rulesData.value = emptyList() // エラー時は空リスト、またはエラー状態を示す別のStateFlowを使う
            }
        }
    }

    // 編集されたルールをDBに保存し、データを再ロードする
    fun updateRuleEnabled(index: Int, isChecked: Boolean) {
        viewModelScope.launch {
            try {
                // 1. 現在の全ルールをDBから再取得（最新のIDを持つデータが必要）
                val currentRuleRows = rulesStore.getByChannel(
                    getApplication(),
                    ChannelID.CHATGPT_TASK
                )

                // 2. 対象のルールを取得し、状態をコピーして変更
                val ruleToUpdate = currentRuleRows.getOrNull(index) ?: return@launch

                // RuleRow.ktからRuleRowを取得してenabledを更新
                val updatedRule = ruleToUpdate.copy(enabled = isChecked)

                // 3. DBに保存（upsert: IDがあれば更新、なければ挿入）
                rulesStore.upsertRule(getApplication(), updatedRule)

                // 4. データの再ロードとStateFlowの更新
                reloadRulesData()

            } catch (t: Throwable) {
                Log.e("MainViewModel", "Failed to update rule $index", t)
                // TODO: エラー発生時はUIにメッセージを表示したり、元の状態に戻したりする処理
            }
        }
    }

    // データの再ロードとStateFlow更新のロジックを分離
    private suspend fun reloadRulesData() {
        val dataToDisplay: List<RuleRow> =  rulesStore.getByChannel(
            getApplication(),
            ChannelID.CHATGPT_TASK
        )
        // List<RuleDisplayItem> を取得
        val displayList = convertRulesToDisplayString(dataToDisplay)

        // StateFlowの型（List<RuleDisplayItem>）に合わせて値を更新
        _rulesData.value = displayList
    }

    private fun convertRulesToDisplayString(dataToDisplay: List<RuleRow>): List<RuleDisplayItem> {
        val converters = UriConverters()

        // map の結果を List<RuleDisplayItem> として return する
        return dataToDisplay.map { row ->
            RuleDisplayItem (
                searchText = row.searchText ?: "N/A",
                // fromUri の結果が null の場合は "N/A" を使用
                soundKeyDisplay = converters.fromUri(row.soundKey)?: "N/A",
                isEnabled = row.enabled
            )
        }
    }


    // UIが手動でリロードを指示するためのpublic関数
    fun reloadData() {
        viewModelScope.launch {
            reloadRulesData()
        }
    }

    // データの再ロードとStateFlow更新のロジック (privateなまま)
//    private suspend fun reloadRulesData() {
//        val dataToDisplay: List<RuleRow> =  rulesStore.getByChannel(
//            getApplication(),
//            ChannelID.CHATGPT_TASK
//        )
//        _rulesData.value = convertRulesToDisplayString(dataToDisplay) // convertRulesToDisplayStringはList<RuleDisplayItem>を返すものとする
//    }
}