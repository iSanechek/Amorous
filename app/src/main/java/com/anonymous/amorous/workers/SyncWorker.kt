package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.models.Info
import kotlinx.coroutines.coroutineScope

class SyncWorker(
        context: Context,
        parameters: WorkerParameters
) : BaseCoroutineWorker(context, parameters) {

    override suspend fun workAction(): Result = coroutineScope {

        db.saveInfo(Info(
                totalMemory = fileUtils.getTotalSpaceSize(),
                freeMemory = fileUtils.getTotalFreeSpace(),
                cacheFilesSize = fileUtils.getCacheFilesSize(applicationContext),
                cacheFolderSize = fileUtils.getCacheFolderSize(applicationContext),
                lastUpdate = System.currentTimeMillis()))

        // worker status
        if (!configuration.getWorkerStatus()) {
            addEvent(TAG, "Stop all workers!")
            manager.stopAllWorkers()
        }

        // clear all data
        if (configuration.removeAllData()) {
            manager.startClearFolderWorker()
        }

        manager.startBackupWorker()
        manager.startRemoveBackupWorker()
        manager.startSearchWorker()

        Result.success()
    }

    companion object {
        private const val TAG = "SyncWorker"
    }
}