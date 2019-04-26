package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.service.JobSchContract
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import org.koin.core.parameter.parametersOf
import org.koin.standalone.inject

class CheckerServiceWorker(
        context: Context,
        parameters: WorkerParameters
) : BaseCoroutineWorker(context, parameters) {

    private val jss: JobSchContract by inject()

    override suspend fun workAction(): Result = coroutineScope {
        jss.scheduleJob(applicationContext)
        val serviceIsWork = jss.serviceIsRun(applicationContext)
        if (!serviceIsWork) {
            val retryCount = pref.getWorkerRetryCountValue(TAG)
            if (retryCount < configuration.getWorkerRetryCount()) {
                val value = retryCount.inc()
                pref.updateWorkerRetryCountValue(TAG, value)
                addEvent(TAG, "Jobs service not starting! Retry! Count $retryCount")
                Result.retry()
            } else {
                addEvent(TAG, "Jobs service retry start fail! Retry count $retryCount")
                Result.failure()
            }
        } else {
            addEvent(TAG, "Jobs service is running!")
            Result.success()
        }
    }

    companion object {
        private const val TAG = "CheckerServiceWorker"
    }
}