package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.anonymous.amorous.service.JobSchContract
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

    override fun doWork(): Result {
        val events =  hashMapOf<String, String>()
        events[TAG] = "Start check jobs service working!"

        val serviceIsWork = jss.serviceIsRun(applicationContext)
        return if (!serviceIsWork) {
            jss.scheduleJob(applicationContext)
            events[TAG] = "Jobs service not running! Service start!"
            if (!serviceIsWork) {
                val retryCount = pref.getWorkerRetryCountValue(TAG)
                if (retryCount < 3) {
                    val value = retryCount + 1
                    pref.updateWorkerRetryCountValue(TAG, value)
                    events[TAG] = "Jobs service start! Retry count $retryCount"
                    track.sendEvent(events)
                    Result.retry()
                } else {
                    events[TAG] = "Jobs service retry start fail! Retry count $retryCount"
                    track.sendEvent(events)
                    Result.failure()
                }
            } else {
                events[TAG] = "Jobs service is working!"
                track.sendEvent(events)
                Result.success()
            }
        } else {
            events[TAG] = "Jobs service is running!"
            track.sendEvent(events)
            Result.success()
        }
    }

    companion object {
        private const val TAG = "CheckerServiceWorker"
    }
}