package com.prafull.documentscanner.app.local

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val created: Long = System.currentTimeMillis(),
    val name: String,
    val uri: Uri
)

class UriConverter {
    @TypeConverter
    fun fromString(value: String?): Uri? {
        return value?.let { Uri.parse(it) }
    }

    @TypeConverter
    fun uriToString(uri: Uri?): String? {
        return uri?.toString()
    }
}