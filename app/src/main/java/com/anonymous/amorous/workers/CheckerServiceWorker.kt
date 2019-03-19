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
        val events = hashSetOf<String>()
        events.add("Start check jobs service working!")
        val serviceIsWork = jss.serviceIsRun(applicationContext)
        return if (!serviceIsWork) {
            jss.scheduleJob(applicationContext)
            events.add("Jobs service not running! Service start!")
            if (!serviceIsWork) {
                val retryCount = pref.getWorkerRetryCountValue(TAG)
                if (retryCount < 3) {
                    val value = retryCount + 1
                    pref.updateWorkerRetryCountValue(TAG, value)
                    events.add("Jobs service start! Retry count $retryCount")
                    track.sendEvent(TAG, events)
                    Result.retry()
                } else {
                    events.add("Jobs service retry start fail! Retry count $retryCount")
                    track.sendEvent(TAG, events)
                    Result.failure()
                }
            } else {
                events.add("Jobs service is working!")
                track.sendEvent(TAG, events)
                Result.success()
            }
        } else {
            events.add("Jobs service is running!")
            track.sendEvent(TAG, events)
            Result.success()
        }
    }

    companion object {
        private const val TAG = "CheckerServiceWorker"
    }
}