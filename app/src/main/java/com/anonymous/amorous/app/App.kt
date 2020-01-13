package com.anonymous.amorous.app

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.anonymous.amorous.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(listOf(appModule))
        }
    }

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
            .setMinimumLoggingLevel(Log.VERBOSE)
            .build()
}