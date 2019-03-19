package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.database.LocalDatabase
import com.anonymous.amorous.utils.ConfigurationUtils
import com.anonymous.amorous.utils.PrefUtils
import com.anonymous.amorous.utils.ScannerUtils
import com.anonymous.amorous.utils.TrackingUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

abstract class BaseCoroutineWorker(
        context: Context,
        parameters: WorkerParameters
) : CoroutineWorker(context, parameters), KoinComponent {

    private val tracker: TrackingUtils by inject()

    val scanner: ScannerUtils by inject()
    val database: LocalDatabase by inject()
    val pref: PrefUtils by inject()
    val configuration: ConfigurationUtils by inject()

    override val coroutineContext: CoroutineDispatcher
        get() = Dispatchers.IO

    override suspend fun doWork(): Result = try {
        doWorkAsync()
    } catch (e: Exception) {
        val events = hashMapOf<String, String>()
        events["BaseWorker"] = "Do worker error! ${e.message}"
        sendEvent(events)
        Result.failure()
    }

    abstract suspend fun doWorkAsync(): Result

    fun sendEvent(events: HashMap<String, String>) {
        tracker.sendEvent(events)
    }
}