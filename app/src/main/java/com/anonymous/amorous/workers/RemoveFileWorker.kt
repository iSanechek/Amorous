package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.models.Candidate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RemoveFileWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    override suspend fun workAction(): Result = try {
        GlobalScope.launch(Dispatchers.IO) {
            val cache = database.getOriginalsCandidate("original_remove_backup")
            if (cache.isNotEmpty()) {
                for (candidate in cache) {
                    addEvent(TAG, "Remove backup for $candidate")
                    val path = candidate.tempPath
                    if (fileUtils.removeFile(path)) {
                        val isRemoved = if (fileUtils.checkFileExists(path)) "NOPE :(" else "YES"
                        addEvent(TAG, "Cache file ${candidate.name} $isRemoved")
                        hzFun(candidate)
                    } else {
                        val files = fileUtils.getAllFilesFromCacheFolder(applicationContext)
                        for (file in files) {
                            if (candidate.name == file.name) {
                                if (fileUtils.removeFile(file.absolutePath)) {
                                    val isRemoved = fileUtils.checkFileExists(file.absolutePath)
                                    addEvent(TAG, "Cache file ${candidate.name} $isRemoved")
                                    hzFun(candidate)
                                } else addEvent(TAG, "I can't remove file ${candidate.name}")
                            } else {
                                addEvent(TAG, "File not find ${candidate.name}")
                                val c = candidate.copy(backupStatus = Candidate.NO_BACKUP)
                                database.updateCandidate(c)
                                remoteDatabase.writeCandidateInDatabase(c){}
                            }
                        }
                    }
                }
            } else {
                addEvent(TAG, "")
            }
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

    private fun isRemoved(path: String) = when {
        fileUtils.checkFileExists(path) -> "NOPE :("
        else -> "YES"
    }

    private fun hzFun(candidate: Candidate) = when {
        fileUtils.checkFileExists(candidate.originalPath) -> {
            val c = candidate.copy(backupStatus = Candidate.NO_BACKUP)
            GlobalScope.launch { database.updateCandidate(c) }
            remoteDatabase.writeCandidateInDatabase(c) {}
        }
        else -> {
            val c = candidate.copy(
                    backupStatus = Candidate.NO_BACKUP,
                    originalStatus = Candidate.ORIGINAL_FILE_REMOVED,
                    date = System.currentTimeMillis())
            GlobalScope.launch { database.removeCandidate(c) }
            remoteDatabase.writeCandidateInDatabase(c) {}
        }
    }

    companion object {
        private const val TAG = "RemoveFileWorker"
    }

}