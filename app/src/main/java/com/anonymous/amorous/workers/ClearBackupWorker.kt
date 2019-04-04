package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters

class ClearBackupWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {
    override suspend fun doWorkAsync(): Result = try {
        addEvent(TAG, "Start remove all data! :(")
        when {
            fileUtils.getCacheFolderSize(applicationContext) > 0L -> when {
                fileUtils.clearCacheFolder(applicationContext) -> {
                    val size = fileUtils.getCacheFolderSize(applicationContext)
                    when (size) {
                        0L -> {
                            addEvent(TAG, "Clear cache folder done!")
                            database.clearDb()
                        }
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

    companion object {
        private const val TAG = "ClearBackupWorker"
    }
}