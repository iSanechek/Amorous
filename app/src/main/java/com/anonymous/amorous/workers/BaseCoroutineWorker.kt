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

    val scanner: ScannerUtils by inject()
    val database: LocalDatabase by inject()
    val pref: PrefUtils by inject()
    val configuration: ConfigurationUtils by inject()
    val fileUtils: FileUtils by inject()
    val remoteDatabase: RemoteDatabaseUtils by inject()

    override val coroutineContext: CoroutineDispatcher
        get() = Dispatchers.IO

    override suspend fun doWork(): Result = try {
        doWorkAsync()
    } catch (e: Exception) {
        sendEvent("BaseWorker", hashSetOf("Do worker error! ${e.message}"))
        Result.failure()
    }

    abstract suspend fun doWorkAsync(): Result

    fun sendEvent(tag: String, events: HashSet<String>) {
        tracker.sendEvent(tag, events)
    }

    fun addEvent(event: String) {
        listEvents.add(event)
    }

    fun getEvents(): HashSet<String> = listEvents

}