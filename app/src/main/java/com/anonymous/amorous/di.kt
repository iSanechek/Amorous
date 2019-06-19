package com.anonymous.amorous

import android.content.Context
import android.content.SharedPreferences
import com.anonymous.amorous.data.database.RemoteDb
import com.anonymous.amorous.data.database.RemoteDbImpl
import com.anonymous.amorous.service.JobSchContract
import com.anonymous.amorous.service.JobSchedulerService
import com.anonymous.amorous.utils.*
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

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

    single<RemoteDb> {
        RemoteDbImpl()
    }

    single<ConfigurationUtils> {
        ConfigurationUtilsImpl(get())
    }

    single<JobSchContract> {
        JobSchedulerService()
    }

    factory<WorkersManager> {
        WorkersManagerImpl(
                androidContext(),
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
        ScannerUtils(get())
    }

    single<TrackingUtils> {
        TrackingUtilsImpl(androidContext(), get())
    }

    single<FileUtils> {
        FileUtilsImpl(
                get()
        )
    }
}