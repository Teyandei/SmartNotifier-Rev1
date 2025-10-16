package com.example.smartnotifier.data.db

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile private var INSTANCE: AppDatabase? = null

    fun get(context: Context): AppDatabase =
        INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "SmartNotifier.db"
            ).fallbackToDestructiveMigration() // 初期導入は破壊的でも可
                .build()
                .also { INSTANCE = it }
        }
}
