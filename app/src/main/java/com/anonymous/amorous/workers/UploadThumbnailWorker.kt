package com.anonymous.amorous.workers

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.models.Candidate
import com.anonymous.amorous.service.UploadService
import com.anonymous.amorous.service.UploadThumbnailService
import com.anonymous.amorous.utils.UploadBitmapUtils
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.standalone.inject

class UploadThumbnailWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    private val upload: UploadBitmapUtils by inject()

    override suspend fun workAction(): Result {
        val cache = database.getThumbnailsCandidate("thumbnail_upload_need", 10)
        for (item in cache) {
            upload.uploadThumbnail(item) { candidate ->
                remoteDatabase.writeCandidateInDatabase(candidate) {callback ->
                    when {
                        callback.isSuccess -> {
                            val c = callback.getOrDefault(candidate)
                            GlobalScope.launch { database.updateCandidate(c) }
                        }
                        callback.isFailure -> addEvent(TAG, "Upload thumbnail for ${item.name} fail! ${callback.exceptionOrNull()}")
                    }
                }
            }
        }
        return Result.success()
    }

    companion object {
        private const val TAG = "UploadThumbnailWorker"
    }
}