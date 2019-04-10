package com.android.camera.storage

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

val storageModule = module {
    single {
        Room.databaseBuilder(
                androidContext().applicationContext,
                Database::class.java,
                "amorous.db"
        )
                .fallbackToDestructiveMigration()
                .build()
    }

    factory{
        get<Database>().candidateDao()
    }
}