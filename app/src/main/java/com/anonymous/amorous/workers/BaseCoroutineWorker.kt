package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.database.LocalDatabase
import com.anonymous.amorous.utils.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

abstract class BaseCoroutineWorker(
        context: Context,
        parameters: WorkerParameters
) : CoroutineWorker(context, parameters), KoinComponent {

    private val tracker: TrackingUtils by inject()
    private val listEvents = hashSetOf<String>()

    val scanner: ScanContract by inject()
    val database: LocalDatabase by inject()
    val pref: PrefUtils by inject()
    val configuration: ConfigurationUtils by inject()
    val fileUtils: FileUtils by inject()
    val remoteDatabase: RemoteDatabase by inject()

    override val coroutineContext: CoroutineDispatcher
        get() = Dispatchers.IO

    override suspend fun doWork(): Result = try {
        doWorkAsync()
    } catch (e: Exception) {
        sendEvent("BaseWorker", "Do worker error! ${e.message}")
        sendEvents()
        Result.failure()
    }

    abstract suspend fun doWorkAsync(): Result

    fun sendEvent(tag: String, event: String) {
        tracker.sendEvent(tag, event)
    }

    fun sendEvents() {
        tracker.sendOnServer()
    }
}