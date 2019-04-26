package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class ClearBackupWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    override suspend fun workAction(): Result = coroutineScope {
        try {
            addEvent(TAG, "Start remove all data! :(")
            when {
                getFolderSize() > 0L -> when {
                    clearFolder() -> {
                        when (val size = getFolderSize()) {
                            0L -> addEvent(TAG, "Clear cache folder done!")
                            else -> addEvent(TAG, "Clear cache folder fail! Size folder $size")
                        }
                    }
                    else -> addEvent(TAG, "Pizdos! I can't remove data from cache folder! :(")
                }
                else -> addEvent(TAG, "Cache folder empty. :)")
            }
            Result.success()
        } catch (e: Exception) {
            val retryCount = pref.getWorkerRetryCountValue(TAG)
            if (retryCount < configuration.getWorkerRetryCount()) {
                val value = retryCount.inc()
                pref.updateWorkerRetryCountValue(TAG, value)
                addEvent(TAG, "Создание бэкапа завершено с ошибкой. Повторная попатка номер $value")
                Result.retry()
            } else {
                addEvent(TAG, "Не удалось создать бэкап $retryCount")
                pref.updateWorkerRetryCountValue(TAG, 0)
                Result.failure()
            }
        }
    }

    private suspend fun getFolderSize(): Long = withContext(Dispatchers.IO) {
        fileUtils.getCacheFolderSize(applicationContext)
    }

    private suspend fun clearFolder(): Boolean = withContext(Dispatchers.IO) {
        fileUtils.clearCacheFolder(applicationContext)
    }

    companion object {
        private const val TAG = "ClearBackupWorker"
    }
}