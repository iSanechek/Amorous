package com.anonymous.amorous

import android.app.Application
import org.koin.android.ext.android.startKoin

private const val PRIVATE_KEY = "22680796247-0v2ng45jjakbs7jnn0kgf0obqe0c5kbf.apps.googleusercontent.com@amorous-5f2ae.iam.gserviceaccount.com"

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(appModule))

    }
}