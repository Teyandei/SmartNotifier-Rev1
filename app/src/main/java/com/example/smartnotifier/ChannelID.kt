package com.example.smartnotifier

object ChannelID {
    enum class ChannelId(val id: String) {
        CHATGPT_TASK("jawbone");

        override fun toString(): String = id
    }
}
