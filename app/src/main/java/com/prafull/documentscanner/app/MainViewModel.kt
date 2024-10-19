package com.prafull.documentscanner.app

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prafull.documentscanner.app.UriValidator.exists
import com.prafull.documentscanner.app.local.DocumentDao
import com.prafull.documentscanner.app.local.DocumentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileNotFoundException

class MainViewModel : ViewModel(), KoinComponent {

    private val dao: DocumentDao by inject()
    var currUri by mutableStateOf<Uri?>(null)
    private val context: Context by inject()
    private val _createdDocuments = MutableStateFlow<List<DocumentEntity>>(emptyList())
    val createdDocuments = _createdDocuments.asStateFlow()
    init {
        getDocuments()
    }

    private fun getDocuments() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.getAllDocuments().collectLatest { entities ->
                _createdDocuments.update {
                    entities
                }
            }
        }
    }

    fun addDocument(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = DocumentEntity(
                name = uri.lastPathSegment ?: "Document",
                uri = uri
            )
            dao.insertDocument(entity)
        }
    }
}

object UriValidator {
    private fun isUriExists(context: Context, uri: Uri?): Boolean {
        if (uri == null) return false

        return try {
            when (uri.scheme) {
                // For content URIs
                ContentResolver.SCHEME_CONTENT -> {
                    val cursor = context.contentResolver.query(uri, null, null, null, null)
                    cursor?.use {
                        return cursor.count > 0
                    } ?: false
                }
                // For file URIs
                ContentResolver.SCHEME_FILE -> {
                    val file = File(uri.path ?: return false)
                    file.exists()
                }
                // For other schemes
                else -> {
                    try {
                        context.contentResolver.openInputStream(uri)?.use {
                            true
                        } ?: false
                    } catch (e: FileNotFoundException) {
                        false
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Extension function for Uri
    fun Uri.exists(context: Context): Boolean {
        return isUriExists(context, this)
    }
}