package com.anonymous.amorous

import android.content.Context
import android.content.SharedPreferences
import com.anonymous.amorous.data.database.DatabaseHandler
import com.anonymous.amorous.data.database.LocalDatabase
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
        ConfigurationUtilsImpl()
    }

    single<JobSchContract> {
        JobSchedulerService()
    }

    single<ActionContract> {
        ActionUtils(
                get(),
                androidContext(),
                get(),
                get(),
                get(),
                get()
        )
    }

    single<WorkersManager> {
        WorkersManagerImpl()
    }

    single<AuthContract> {
        AuthUtils(
                get()
        )
    }

    factory<UploadBitmapUtils> {
        UploadBitmapUtilsImpl(
                get(),
                get(),
                get(),
                get()
        )
    }

    factory<ScanContract> {
        ScannerUtils(
                get()
        )
    }

    factory<TrackingUtils> {
        TrackingUtilsImpl()
    }

    single<LocalDatabase> {
        DatabaseHandler(androidContext().applicationContext)
    }

    factory<RemoteDatabaseUtils> {
        DatabaseUtilsImpl(
        )
    }

    single<FileUtils> {
        FileUtilsImpl(
                get()
        )
    }
}