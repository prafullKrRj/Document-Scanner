package com.prafull.documentscanner.app.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow


@Dao
interface DocumentDao {


    @Upsert
    suspend fun insertDocument(documentEntity: DocumentEntity)

    @Query("SELECT * FROM DocumentEntity")
    fun getAllDocuments(): Flow<List<DocumentEntity>>
}