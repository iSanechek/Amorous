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
        return when (result) {
            is ScanCallback.ResultOk -> {
                val items = result.items
                if (items.isNotEmpty()) {
                    addEvent("Scanning is done! Result size: ${items.size}")
                    database.saveCandidates(items)
                    sendEvent(TAG, getEvents())
                    Result.success()
                } else {
                    val retryCount = pref.getWorkerRetryCountValue(TAG)
                    if (retryCount < configuration.getWorkerRetryCount()) {
                        val value = retryCount.inc()
                        pref.updateWorkerRetryCountValue(TAG, value)
                        addEvent("Scanning is done with empty result! Retry scan! Retry count $value")
                        sendEvent(TAG, getEvents())
                        Result.retry()
                    } else {
                        addEvent("Scanning is done with empty result! Retry count is out! Count $retryCount")
                        sendEvent(TAG, getEvents())
                        Result.failure()
                    }
                }
            }
            is ScanCallback.ResultFail -> {
                val errorResult = result.fail
                return when (errorResult) {
                    is ScanCallback.Fail.NoPermission -> {
                        addEvent("Scanning is fail! No permission!")
                        sendEvent(TAG, getEvents())
                        Result.failure()
                    }
                    is ScanCallback.Fail.NotReadable -> {
                        addEvent("Scanning is fail! FS is not readable!")
                        sendEvent(TAG, getEvents())
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