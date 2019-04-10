package com.anonymous.amorous.workers

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import com.anonymous.amorous.utils.ScanCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ScanningWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    override suspend  fun workAction(): Result {
        var r: Result? = null
        GlobalScope.launch(Dispatchers.IO) {
            scanner.scanFolders { callback ->
                when(callback) {
                    is ScanCallback.ResultOk -> {
                        val items = callback.items
                        r = when {
                            items.isNotEmpty() -> {
                                addEvent(TAG, "Scanning is done! Result size: ${items.size}")
                                GlobalScope.launch { database.saveCandidates(items) }
                                Result.success()
                            }
                            else -> {
                                addEvent(TAG, "Scanning is done with empty result!")
                                Result.success()
                            }
                        }
                    }
                    is ScanCallback.ResultFail -> {
                        val errorResult = callback.fail
                        r = when (errorResult) {
                            is ScanCallback.Fail.NoPermission -> {
                                addEvent(TAG, "No permission! Scanning is fail!")
                                Result.failure()
                            }
                            is ScanCallback.Fail.NotReadable -> {
                                addEvent(TAG, "FS is not readable! Scanning is fail!")
                                Result.failure()
                            }
                            else -> {
                                addEvent(TAG, "Scanning is fail! Неизвестная хуйня $errorResult")
                                Result.failure()
                            }
                        }
                    }
                }
            }
        }

        return when (r) {
            null -> {
                val retryCount = pref.getWorkerRetryCountValue(TAG)
                when {
                    retryCount < configuration.getWorkerRetryCount() -> {
                        val value = retryCount.inc()
                        addEvent(TAG, "Scan folder return fail! Retry $value")
                        pref.updateWorkerRetryCountValue(TAG, value)
                        Result.retry()
                    }
                    else -> {
                        pref.updateWorkerRetryCountValue(TAG, 0)
                        addEvent(TAG, "Scan folder return fail!")
                        Result.failure()
                    }
                }
            }
            else -> r ?: Result.failure()
        }
    }

    companion object {
        private const val TAG = "ScanningWorker"
    }
}