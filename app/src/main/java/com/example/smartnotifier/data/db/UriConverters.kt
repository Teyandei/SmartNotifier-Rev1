/*
 * SmartNotifier-Rev1
 * Copyright (C) 2025  Takeaki Yoshizawa
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.example.smartnotifier.data.db

import android.net.Uri
import androidx.room.TypeConverter

class UriConverters {
    @TypeConverter
    fun fromUri(uri: Uri?): String? = uri?.toString()
    @TypeConverter
    fun toUri(s: String?): Uri? = s?.let(Uri::parse)
}