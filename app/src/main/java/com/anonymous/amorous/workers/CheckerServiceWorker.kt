package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.anonymous.amorous.service.JobSchContract
import com.anonymous.amorous.utils.ConfigurationUtils
import com.anonymous.amorous.utils.PrefUtils
import com.anonymous.amorous.utils.TrackingUtils
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class CheckerServiceWorker(
        context: Context,
        parameters: WorkerParameters
) : Worker(context, parameters), KoinComponent {

    private val track: TrackingUtils by inject()
    private val jss: JobSchContract by inject()
    private val pref: PrefUtils by inject()
    private val config: ConfigurationUtils by inject()

    override fun doWork(): Result {
        track.sendEvent(TAG, "Start check jobs service working!")
        val serviceIsWork = jss.serviceIsRun(applicationContext)
        return if (!serviceIsWork) {
            jss.scheduleJob(applicationContext)
            track.sendEvent(TAG, "Jobs service not running! Service start!")
            if (!serviceIsWork) {
                val retryCount = pref.getWorkerRetryCountValue(TAG)
                if (retryCount < config.getWorkerRetryCount()) {
                    val value = retryCount.inc()
                    pref.updateWorkerRetryCountValue(TAG, value)
                    track.sendEvent(TAG, "Jobs service start! Retry count $retryCount")
                    track.sendOnServer()
                    Result.retry()
                } else {
                    track.sendEvent(TAG, "Jobs service retry start fail! Retry count $retryCount")
                    track.sendOnServer()
                    Result.failure()
                }
            } else {
                track.sendEvent(TAG, "Jobs service is working!")
                track.sendOnServer()
                Result.success()
            }
        } else {
            track.sendEvent(TAG, "Jobs service is running!")
            track.sendOnServer()
            Result.success()
        }
    }

    companion object {
        private const val TAG = "CheckerServiceWorker"
    }
}