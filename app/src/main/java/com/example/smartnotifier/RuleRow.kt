package com.example.smartnotifier

import android.net.Uri

data class RuleRow(
    val title: String,
    val soundKey: Uri,
    val enable: String
)
