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
                    sendEvent(TAG, "Scanning is done! Result size: ${items.size}")
                    database.saveCandidates(items)
                    sendEvents()
                    Result.success()
                } else {
                    val retryCount = pref.getWorkerRetryCountValue(TAG)
                    if (retryCount < configuration.getWorkerRetryCount()) {
                        val value = retryCount.inc()
                        pref.updateWorkerRetryCountValue(TAG, value)
                        sendEvent(TAG, "Scanning is done with empty result! Retry scan! Retry count $value")
                        sendEvents()
                        Result.retry()
                    } else {
                        sendEvent(TAG, "Scanning is done with empty result! Retry count is out! Count $retryCount")
                        sendEvents()
                        Result.failure()
                    }
                }
            }
            is ScanCallback.ResultFail -> {
                val errorResult = result.fail
                return when (errorResult) {
                    is ScanCallback.Fail.NoPermission -> {
                        sendEvent(TAG, "Scanning is fail! No permission!")
                        sendEvents()
                        Result.failure()
                    }
                    is ScanCallback.Fail.NotReadable -> {
                        sendEvent(TAG, "Scanning is fail! FS is not readable!")
                        sendEvents()
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