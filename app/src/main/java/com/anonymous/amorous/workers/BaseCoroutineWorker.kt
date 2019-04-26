package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.database.RemoteDb
import com.anonymous.amorous.utils.*
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
    val db: RemoteDb by inject()

    override suspend fun doWork(): Result = workAction()

    abstract suspend fun workAction(): Result

    fun addEvent(tag: String, event: String) {
        tracker.sendEvent(tag, event)
    }

    fun getTime(key: String): Long = System.currentTimeMillis() - pref.getTimeUpdate(key)

    fun getWorkerUpdateTime(key: String): Long = configuration.getTimeForWorkerUpdate(key) * 60000

    fun updateTime(key: String) {
        pref.setTimeUpdate(key, System.currentTimeMillis())
    }
}