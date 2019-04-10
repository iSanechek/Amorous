package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.models.Candidate
import com.anonymous.amorous.empty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackupWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    override suspend fun workAction(): Result = try {

        val cache = database.getBackupCandidate("original_need_backup")
        if (cache.isNotEmpty()) {
            for (candidate in cache) {
                addEvent(TAG, "Создание бэкапа для ${candidate.name}!")
                val originalPath = candidate.originalPath
                if (originalPath.isNotEmpty()) {
                    if (checkFile(originalPath)) {
                        val path = createBackup(originalPath)
                        if (path.isNotEmpty()) {
                            val c = candidate.copy(tempPath = path, backupStatus = Candidate.BACKUP_READE, date = System.currentTimeMillis())
                            database.updateCandidate(c)
                            remoteDatabase.writeCandidateInDatabase(c) {}
                            addEvent(TAG, "Бэкап для ${candidate.name} готов!")
                        } else addEvent(TAG, "Не удалось создать бэкап для ${candidate.name}")
                    } else {
                        addEvent(TAG, "Оригинал удален, удаляю кандидата из очереди!")
                        val removeCandidate = database.getCandidates().find { it.uid == candidate.uid }
                        if (removeCandidate != null) {
                            database.removeCandidate(removeCandidate)
                        }

                        remoteDatabase.writeCandidateInDatabase(candidate) {}
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

    private var iter = 0
    private suspend fun createBackup(originalPath: String): String = withContext(Dispatchers.IO) {
        val path = fileUtils.copyToCacheFolder(applicationContext, originalPath)
        when {
            fileUtils.checkFileExists(path) -> path
            iter != 1 -> {
                iter.inc()
                createBackup(originalPath)
            }
            else -> String.empty()
        }
    }

    private suspend fun checkFile(path: String): Boolean = withContext(Dispatchers.IO) {
        fileUtils.checkFileExists(path)
    }

    companion object {
        private const val TAG = "BackupWorker"
    }
}