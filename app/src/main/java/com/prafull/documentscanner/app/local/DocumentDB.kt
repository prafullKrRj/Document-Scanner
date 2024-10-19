package com.prafull.documentscanner.app.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(entities = [DocumentEntity::class], version = 1, exportSchema = false)
@TypeConverters(UriConverter::class)
abstract class DocumentDB : RoomDatabase() {

    abstract fun documentDao(): DocumentDao
}