package com.anonymous.amorous.workers

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.models.Info
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SyncWorker(
        context: Context,
        parameters: WorkerParameters
) : BaseCoroutineWorker(context, parameters) {

    override val coroutineContext: CoroutineDispatcher
        get() = Dispatchers.IO

    override suspend fun workAction(): Result {

        db.saveInfo(Info(
                totalMemory = fileUtils.getTotalSpaceSize(),
                freeMemory = fileUtils.getTotalFreeSpace(),
                cacheFilesSize = fileUtils.getCacheFilesSize(applicationContext),
                cacheFolderSize = fileUtils.getCacheFolderSize(applicationContext),
                lastUpdate = System.currentTimeMillis()))

        GlobalScope.launch(Dispatchers.Main) {
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
        }

        return Result.success()
    }

    companion object {
        private const val TAG = "SyncWorker"
    }
}