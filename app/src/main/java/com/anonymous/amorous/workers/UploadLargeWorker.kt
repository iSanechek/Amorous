package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.models.Candidate
import com.anonymous.amorous.utils.UploadBitmapUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.standalone.inject

class UploadLargeWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    private val upload: UploadBitmapUtils by inject()

    override val coroutineContext: CoroutineDispatcher
        get() = Dispatchers.IO

    override suspend fun workAction(): Result = try {
        val cache = db.getCandidates(
                "originalStatus",
                "original_upload_large",
                configuration.uploadBitmapLimit("upload_large_limit")
        )
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
                                else -> db.updateCandidate(uid = candidate.uid, column = "originalStatus", value = Candidate.ORIGINAL_FILE_REMOVED)
                            }
                        }
                    }
                    else -> addEvent(TAG, "Candidate ${candidate.name} for upload large fail! Path empty")
                }
            }
            else -> addEvent(TAG, "Candidate for upload large is empty!")
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

    private suspend fun up(candidate: Candidate) {
        val c = upload.uploadOriginal(candidate)
        db.updateCandidate(
                uid = c.uid,
                column1 = "originalStatus",
                value1 = c.originalStatus,
                column2 = "originalRemoteUrl",
                value2 = c.originalRemoteUrl
        )
    }

    companion object {
        private const val TAG = "UploadLargeWorker"
    }

}