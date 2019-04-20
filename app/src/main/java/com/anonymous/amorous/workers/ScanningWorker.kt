package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.utils.ScanCallback
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScanningWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    override val coroutineContext: CoroutineDispatcher
        get() = Dispatchers.IO

    override suspend  fun workAction(): Result {
        var isOk = false
        when(val callback = scanner.scanFolders()) {
            is ScanCallback.ResultOk -> {
                val items = callback.items
                isOk = when {
                    items.isNotEmpty() -> {
                        addEvent(TAG, "Scanning is done! Result size: ${items.size}")
                        for (candidate in items) {
                            db.saveCandidate(candidate)
                        }
                        true
                    }
                    else -> {
                        addEvent(TAG, "Scanning is done with empty result!")
                        true
                    }
                }
            }
            is ScanCallback.ResultFail -> {
                val errorResult = callback.fail
                isOk = when (errorResult) {
                    is ScanCallback.Fail.NoPermission -> {
                        addEvent(TAG, "No permission! Scanning is fail!")
                        false
                    }
                    is ScanCallback.Fail.NotReadable -> {
                        addEvent(TAG, "FS is not readable! Scanning is fail!")
                        false
                    }
                    else -> {
                        addEvent(TAG, "Scanning is fail! Неизвестная хуйня $errorResult")
                        false
                    }
                }
            }
        }
        return if (isOk) Result.success() else Result.failure()
    }

    companion object {
        private const val TAG = "ScanningWorker"
    }
}