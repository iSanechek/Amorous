package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.database.FirestoreDb
import com.anonymous.amorous.data.models.Event
import com.anonymous.amorous.empty
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
    val scanner: ScanContract by inject()
    val pref: PrefUtils by inject()
    val configuration: ConfigurationUtils by inject()
    val fileUtils: FileUtils by inject()
    val manager: WorkersManager by inject()
    val auth: AuthUtils by inject()
    val db: FirestoreDb by inject()

    override suspend fun doWork(): Result = try {
        workAction()
    } catch (e: Exception) {
        addEvent("BaseWorker", "Do worker error! ${e.message}")
        e.printStackTrace()
        Result.failure()
    }

    abstract suspend fun workAction(): Result

    fun addEvent(tag: String, event: String) {
        tracker.sendEvent(tag, event)
    }
}