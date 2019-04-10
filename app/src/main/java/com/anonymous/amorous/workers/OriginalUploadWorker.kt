package com.anonymous.amorous.workers

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import com.anonymous.amorous.DB_T_C
import com.anonymous.amorous.data.models.Candidate
import com.anonymous.amorous.utils.UploadBitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.standalone.inject

class OriginalUploadWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    private val upload: UploadBitmapUtils by inject()

    override suspend fun workAction(): Result = try {
        GlobalScope.launch(Dispatchers.IO) {
            val cache = database.getOriginalsCandidate("original_upload_need")
            when {
                cache.isNotEmpty() -> for (candidate in cache) {
                    val path = when {
                        candidate.backupStatus == Candidate.BACKUP_READE -> candidate.tempPath
                        else -> candidate.originalPath
                    }
                    when {
                        path.isNotEmpty() -> {
                            when {
                                fileUtils.checkFileExists(path) -> up(candidate)
                                else -> when {
                                    fileUtils.checkFileExists(candidate.originalPath) -> up(candidate)
                                    else -> {
                                        database.removeCandidate(candidate)
                                        remoteDatabase.writeCandidateInDatabase(candidate.copy(originalStatus = Candidate.ORIGINAL_FILE_REMOVED, date = System.currentTimeMillis())) {}
                                    }
                                }
                            }
                        }
                        else -> addEvent(TAG, "Candidate ${candidate.name} for upload original fail! Path empty")
                    }
                }
                else -> addEvent(TAG, "Candidate for upload original is empty!")
            }
        }
        Result.success()
    } catch (e: Exception) {
        val retryCount = pref.getWorkerRetryCountValue(TAG)
        if (retryCount < configuration.getWorkerRetryCount()) {
            val value = retryCount.inc()
            pref.updateWorkerRetryCountValue(TAG, value)
            addEvent(TAG, "Загрузка оригинала завершилось с ошибкой. Повторная попатка номер $value")
            Result.retry()
        } else {
            addEvent(TAG, "Не удалось загрузить оригинал $retryCount")
            pref.updateWorkerRetryCountValue(TAG, 0)
            Result.failure()
        }
    }

    private fun up(candidate: Candidate) {
        upload.uploadOriginal(candidate) {
            GlobalScope.launch { database.updateCandidate(it) }
            remoteDatabase.writeCandidateInDatabase(it) {}
        }
    }

    companion object {
        private const val TAG = "OriginalUploadWorker"
    }
}