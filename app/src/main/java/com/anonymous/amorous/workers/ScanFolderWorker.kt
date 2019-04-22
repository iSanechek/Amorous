package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.models.Candidate
import com.anonymous.amorous.data.models.Folder
import com.anonymous.amorous.toUid
import com.anonymous.amorous.utils.ScanCallback
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class ScanFolderWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    override val coroutineContext: CoroutineDispatcher
        get() = Dispatchers.IO

    override suspend fun workAction(): Result {
        val isOk: Int
        try {
            addEvent(TAG, "Start scan folders!")
            when (val result = scanner.scanRoot()) {
                is ScanCallback.ResultDone -> {
                    val files = result.items
                    addEvent(TAG, "Scan folders is done! Find ${files.size} folders!")
                    for (file in files) {
                        when {
                            file.isDirectory -> {
                                db.saveFolder(Folder(uid = file.name.toUid(), name = file.name, lastModification = file.lastModified()))
                            }
                            file.isFile -> {
                                val name = file.name
                                val type = when {
                                    name.endsWith(".mp4", ignoreCase = true) -> Candidate.VIDEO_TYPE
                                    name.endsWith(".jpg", ignoreCase = true) -> Candidate.IMAGE_TYPE
                                    else -> name.replaceBeforeLast(".", "")
                                }
                                db.saveCandidate(Candidate(
                                        uid = name.toUid(),
                                        thumbnailStatus = Candidate.THUMBNAIL_UPLOAD_NEED,
                                        date = file.lastModified(),
                                        originalStatus = Candidate.ORIGINAL_UPLOAD_READE,
                                        backupStatus = Candidate.NO_BACKUP,
                                        name = name,
                                        remoteUid = "",
                                        tempPath = "",
                                        type = type,
                                        size = file.length(),
                                        originalPath = file.absolutePath))
                            }
                            else -> addEvent(TAG, "Нашлась какая-та хуйня ${file.name}")
                        }
                    }

                    isOk = 1
                }
                is ScanCallback.ResultFail -> {
                    val retryCount = pref.getWorkerRetryCountValue(TAG)
                    isOk = when {
                        retryCount < configuration.getWorkerRetryCount() -> {
                            val value = retryCount.inc()
                            addEvent(TAG, "Scan folder return fail ${result.fail}! Retry $value")
                            pref.updateWorkerRetryCountValue(TAG, value)
                            2
                        }
                        else -> {
                            addEvent(TAG, "Scanning folders is fail! ${result.fail}")
                            0
                        }
                    }
                }
                else -> {
                    addEvent(TAG, "Хуйня какая-та!")
                    isOk = 1
                }
            }
        } catch (e: Exception) {
            val retryCount = pref.getWorkerRetryCountValue(TAG)
            return if (retryCount < configuration.getWorkerRetryCount()) {
                val value = retryCount.inc()
                addEvent(TAG, "Hop hey halaley ${e.message}! Retry $value")
                pref.updateWorkerRetryCountValue(TAG, value)
                Result.retry()
            } else {
                addEvent(TAG, "Retry is fail! :(((")
                Result.failure()
            }
        }
        return when (isOk) {
            0 -> Result.failure()
            1 -> Result.success()
            2 -> Result.retry()
            else -> Result.failure()
        }
    }

    companion object {
        private const val TAG = "ScanFolderWorker"
    }
}