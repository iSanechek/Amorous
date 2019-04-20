package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.models.Candidate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RemoveFileWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    override val coroutineContext: CoroutineDispatcher
        get() = Dispatchers.IO

    override suspend fun workAction(): Result = try {
        val cache = db.getCandidates("originalStatus", "original_remove_backup")
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
                            db.updateCandidate(candidate.uid, "backupStatus", Candidate.NO_BACKUP)
                        }
                    }
                }
            }
        } else {
            addEvent(TAG, "Нет дайлов для удаления!")
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

    private suspend fun hzFun(candidate: Candidate) = when {
        fileUtils.checkFileExists(candidate.originalPath) -> db.updateCandidate(
                candidate.uid, "backupStatus",
                Candidate.NO_BACKUP
        )
        else -> db.updateCandidate(
                uid = candidate.uid,
                column1 = "backupStatus",
                value1 = Candidate.NO_BACKUP,
                column2 = "originalStatus",
                value2 = Candidate.ORIGINAL_FILE_REMOVED
        )
    }

    companion object {
        private const val TAG = "RemoveFileWorker"
    }

}