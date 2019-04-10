package com.anonymous.amorous

import android.content.Context
import android.content.SharedPreferences
import com.anonymous.amorous.data.database.LocalDatabase
import com.anonymous.amorous.data.database.StorageImpl
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

    single<ConfigurationUtils> {
        ConfigurationUtilsImpl(get())
    }

    single<JobSchContract> {
        JobSchedulerService()
    }

    single<ActionUtils> {
        ActionUtilsImpl(
                get(),
                get()
        )
    }

    single<WorkersManager> {
        WorkersManagerImpl(
                get(),
                get()
        )
    }

    single<AuthUtils> {
        AuthUtilsImpl(
                get(),
                get(),
                get()
        )
    }

    factory<UploadBitmapUtils> {
        UploadBitmapUtilsImpl(
                get(),
                get()
        )
    }

    single<ScanContract> {
        ScannerUtils(get(), get())
    }

    single<TrackingUtils> {
        TrackingUtilsImpl(
                get(),
                get()
        )
    }

    single<LocalDatabase> {
//        DatabaseHandler(androidContext().applicationContext)
        StorageImpl(get())
    }

    factory<RemoteDatabase> {
        DatabaseUtilsImpl(
        )
    }

    single<FileUtils> {
        FileUtilsImpl(
                get()
        )
    }
}