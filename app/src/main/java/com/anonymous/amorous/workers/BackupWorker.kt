package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.models.Candidate
import com.anonymous.amorous.empty
import kotlinx.coroutines.coroutineScope

class BackupWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    override suspend fun workAction(): Result = coroutineScope {
        try {
            val cache = db.getCandidates("originalStatus", "original_need_backup")
            if (cache.isNotEmpty()) {
                for (candidate in cache) {
                    addEvent(TAG, "Создание бэкапа для ${candidate.name}!")
                    val originalPath = candidate.originalPath
                    if (originalPath.isNotEmpty()) {
                        if (checkFile(originalPath)) {
                            val path = createBackup(originalPath)
                            if (path.isNotEmpty()) {
                                db.updateCandidate(uid = candidate.uid, column1 = "tempPath", value1 = path, column2 = "backupStatus", value2 = Candidate.BACKUP_READE)
                                addEvent(TAG, "Бэкап для ${candidate.name} готов!")
                            } else addEvent(TAG, "Не удалось создать бэкап для ${candidate.name}")
                        } else {
                            addEvent(TAG, "Original not find!")
                            db.updateCandidate(uid = candidate.uid, column = "backupStatus", value = Candidate.ORIGINAL_FILE_REMOVED)
                        }
                    } else {
                        addEvent(TAG, "Оригинальный путь пустой для ${candidate.name}")
                    }
                }
            } else {
                addEvent(TAG, "No candidates for backup!")
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

    private var iter = 0
    private suspend fun createBackup(originalPath: String): String {
        val path = fileUtils.copyToCacheFolder(applicationContext, originalPath)
        return when {
            fileUtils.checkFileExists(path) -> path
            iter != 1 -> {
                iter.inc()
                createBackup(originalPath)
            }
            else -> String.empty()
        }
    }

    private fun checkFile(path: String): Boolean = fileUtils.checkFileExists(path)

    companion object {
        private const val TAG = "BackupWorker"
    }
}