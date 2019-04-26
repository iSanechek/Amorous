package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.utils.UploadBitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.koin.standalone.inject

class UploadThumbnailWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {
    private val upload: UploadBitmapUtils by inject()

    override suspend fun workAction(): Result = coroutineScope {
        try {
                val candidates = db.getCandidates(
                        "thumbnailStatus",
                        "thumbnail_upload_need",
                        configuration.uploadBitmapLimit("upload_thumbnails_limit")
                )
            addEvent("UploadThumbnailWorker", "Candidates for upload size ${candidates.size}")
                for (candidate in candidates) {
                    val result = withContext(Dispatchers.IO) { upload.uploadThumbnail(candidate) }
                    db.updateCandidate(
                            uid = result.uid,
                            column1 = "thumbnailStatus",
                            value1 = result.thumbnailStatus,
                            column2 = "thumbnailRemoteUrl",
                            value2 = result.thumbnailRemoteUrl
                    )
                }
            Result.success()
        } catch (e: Exception) {
            addEvent("UploadThumbnailWorker", "Oops! Ошибочка ${e.message}")
            Result.failure()
        }
    }
}