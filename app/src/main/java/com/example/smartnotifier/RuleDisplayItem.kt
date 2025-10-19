package com.example.smartnotifier

data class RuleDisplayItem(
    val searchText: String,
    val soundKeyDisplay: String, // 表示用にStringに変換されたサウンドキー
    val isEnabled: Boolean
)
