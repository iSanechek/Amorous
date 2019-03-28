package com.anonymous.amorous

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import org.koin.android.ext.android.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(appModule))
    }
}