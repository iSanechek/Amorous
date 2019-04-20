package com.anonymous.amorous

import android.content.Context
import android.content.SharedPreferences
import com.anonymous.amorous.data.database.FirestoreDb
import com.anonymous.amorous.data.database.FirestoreDbImpl
import com.anonymous.amorous.service.JobSchContract
import com.anonymous.amorous.service.JobSchedulerService
import com.anonymous.amorous.utils.*
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

val appModule = module {

    single {
        androidApplication()
                .applicationContext
                .getSharedPreferences("amx", Context.MODE_PRIVATE)
    } bind (SharedPreferences::class)

    single<PrefUtils> {
        PrefUtilsImpl(
                get()
        )
    }

    single<FirestoreDb> {
        FirestoreDbImpl(get())
    }

    single<ConfigurationUtils> {
        ConfigurationUtilsImpl(get())
    }

    single<JobSchContract> {
        JobSchedulerService()
    }

    factory<WorkersManager> {
        WorkersManagerImpl(
                get(),
                get()
        )
    }

    factory<AuthUtils> {
        AuthUtilsImpl(
                get()
        )
    }

    single<UploadBitmapUtils> {
        UploadBitmapUtilsImpl(
                get(),
                get(),
                get()
        )
    }

    factory<ScanContract> {
        ScannerUtils(get(), get())
    }

    single<TrackingUtils> {
        TrackingUtilsImpl(androidContext())
    }

    single<FileUtils> {
        FileUtilsImpl(
                get()
        )
    }
}