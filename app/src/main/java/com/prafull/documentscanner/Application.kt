package com.prafull.documentscanner

import android.app.Application
import androidx.room.Room
import com.prafull.documentscanner.app.MainViewModel
import com.prafull.documentscanner.app.local.DocumentDB
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@Application)
            modules(module {
                viewModel { MainViewModel() }
                single<DocumentDB> {
                    Room.databaseBuilder(
                        get(),
                        DocumentDB::class.java,
                        "document_db"
                    ).build()
                }
                single {
                    get<DocumentDB>().documentDao()
                }
            })
        }
    }
}