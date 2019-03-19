package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.utils.ScanCallback

class ScanningWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    override suspend  fun doWorkAsync(): Result {
        val result = scanner.startScan()
        val events = hashMapOf<String, String>()
        return when (result) {
            is ScanCallback.ResultOk -> {
                val items = result.items
                if (items.isNotEmpty()) {
                    events[TAG] = "Scanning is done! Result size: ${items.size}"
                    database.saveCandidates(items)
                    sendEvent(events)
                    Result.success()
                } else {
                    val retryCount = pref.getWorkerRetryCountValue(TAG)
                    if (retryCount < configuration.getWorkerRetryCount()) {
                        val value = retryCount.inc()
                        pref.updateWorkerRetryCountValue(TAG, value)
                        events[TAG] = "Scanning is done with empty result! Retry scan! Retry count $value"
                        sendEvent(events)
                        Result.retry()
                    } else {
                        events[TAG] = "Scanning is done with empty result! Retry count is out! Count $retryCount"
                        sendEvent(events)
                        Result.failure()
                    }
                }
            }
            is ScanCallback.ResultFail -> {
                val errorResult = result.fail
                return when (errorResult) {
                    is ScanCallback.Fail.NoPermission -> {
                        events[TAG] = "Scanning is fail! No permission!"
                        sendEvent(events)
                        Result.failure()
                    }
                    is ScanCallback.Fail.NotReadable -> {
                        events[TAG] = "Scanning is fail! FS is not readable!"
                        sendEvent(events)
                        Result.failure()
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "ScanningWorker"
    }
}