package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.models.Candidate
import com.anonymous.amorous.utils.UploadBitmapUtils
import org.koin.standalone.inject

class OriginalUploadWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    private val upload: UploadBitmapUtils by inject()

    override suspend fun doWorkAsync(): Result = try {

        val cache = database.getCandidates("SELECT * FROM c WHERE n_o_u =? ORDER BY d ASC LIMIT 5", arrayOf("original_upload_need"))
        when {
            cache.isNotEmpty() -> for (candidate in cache) {
                val path = when {
                    candidate.backupStatus == Candidate.BACKUP_READE -> candidate.tempPath
                    else -> candidate.originalPath
                }
                when {
                    path != null -> {
                        val isFileExists = fileUtils.checkFileExists(path)
                        when {
                            isFileExists -> upload.uploadBitmap(candidate) {
                                addEvent("Upload original for ${candidate.name} is done!")
                                database.updateCandidate(it)
                            }
                            else -> {
                                addEvent("Original file for ${candidate.name} remove!")
                                database.removeCandidate(candidate)
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
}