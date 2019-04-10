package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.service.JobSchContract
import org.koin.standalone.inject

class CheckerServiceWorker(
        context: Context,
        parameters: WorkerParameters
) : BaseCoroutineWorker(context, parameters) {

    private val jss: JobSchContract by inject()

    override suspend fun workAction(): Result {
        val serviceIsWork = jss.serviceIsRun(applicationContext)
        return if (!serviceIsWork) {
            jss.scheduleJob(applicationContext)
            addEvent(TAG, "Jobs service not running! Job start!")
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
                addEvent(TAG, "Jobs service is working!")
                Result.success()
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