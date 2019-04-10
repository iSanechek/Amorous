package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.models.Candidate
import com.anonymous.amorous.data.models.Folder
import com.anonymous.amorous.toUid
import com.anonymous.amorous.utils.ScanCallback

class ScanFolderWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    override suspend fun workAction(): Result {
        var r: Result = Result.failure()
        try {
            addEvent(TAG, "Start scan folders!")
            scanner.scanRoot { result ->
                when (result) {
                    is ScanCallback.ResultDone -> {
                        val files = result.items
                        addEvent(TAG, "Scan folders is done! Find ${files.size} folders!")
                        for (file in files) {
                            when {
                                file.isDirectory -> {
                                    addEvent(TAG, "Find folder ${file.name}")
                                    val folder = Folder(name = file.name, lastModification = file.lastModified())
                                    remoteDatabase.writeFolderInDatabase(folder) {
                                        when {
                                            it.isSuccess -> { }
                                            it.isFailure -> { addEvent(TAG, "Write folder ${folder.name} in error! ${it.exceptionOrNull()}") }
                                        }
                                    }
                                }
                                file.isFile -> {
                                    val name = file.name
                                    addEvent(TAG, "Find file $name")
                                    val type = when {
                                        name.endsWith(".mp4", ignoreCase = true) -> Candidate.VIDEO_TYPE
                                        name.endsWith(".jpg", ignoreCase = true) -> Candidate.IMAGE_TYPE
                                        else -> name.replaceBeforeLast(".", "")
                                    }
                                    val c = Candidate(
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
                                            originalPath = file.absolutePath)
                                    database.saveCandidate(c)
                                    remoteDatabase.writeCandidateInDatabase(c) {
                                        when {
                                            it.isSuccess -> database.saveCandidate(it.getOrDefault(c))
                                            it.isFailure -> {
                                                addEvent(TAG, "Не удалось добавть в базу $c")
                                                database.saveCandidate(c)
                                            }
                                        }
                                    }
                                }
                                else -> addEvent(TAG, "Нашлась какая-та хуйня ${file.name}")
                            }
                        }

                        r = Result.success()
                    }
                    is ScanCallback.ResultFail -> {
                        val retryCount = pref.getWorkerRetryCountValue(TAG)
                        r = when {
                            retryCount < configuration.getWorkerRetryCount() -> {
                                val value = retryCount.inc()
                                addEvent(TAG, "Scan folder return fail ${result.fail}! Retry $value")
                                pref.updateWorkerRetryCountValue(TAG, value)
                                Result.retry()
                            }
                            else -> {
                                addEvent(TAG, "Scanning folders is fail! ${result.fail}")
                                Result.failure()
                            }
                        }
                    }
                    else -> {
                        addEvent(TAG, "Хуйня какая-та!")
                        r = Result.success()
                    }
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
        return r
    }

    companion object {
        private const val TAG = "ScanFolderWorker"
    }
}