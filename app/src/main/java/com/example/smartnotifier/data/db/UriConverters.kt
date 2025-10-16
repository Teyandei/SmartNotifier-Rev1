package com.example.smartnotifier.data.db

import android.net.Uri
import androidx.room.TypeConverter

class UriConverters {
    @TypeConverter
    fun fromUri(uri: Uri?): String? = uri?.toString()
    @TypeConverter
    fun toUri(s: String?): Uri? = s?.let(Uri::parse)
}