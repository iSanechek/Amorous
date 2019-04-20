package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.utils.UploadBitmapUtils
import kotlinx.coroutines.*
import org.koin.standalone.inject

class UploadThumbnailWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {
    private val upload: UploadBitmapUtils by inject()

    override val coroutineContext: CoroutineDispatcher
        get() = Dispatchers.IO

    override suspend fun workAction(): Result {
        db.getCandidates(
                "thumbnailStatus",
                "thumbnail_upload_need",
                configuration.uploadBitmapLimit("upload_thumbnails_limit")
        ).map {
            upload.uploadThumbnail(it)
        }.forEach {
                    db.updateCandidate(
                            uid = it.uid,
                            column1 = "thumbnailStatus",
                            value1 = it.thumbnailStatus,
                            column2 = "thumbnailRemoteUrl",
                            value2 = it.thumbnailRemoteUrl
                    )
                }
        return Result.success()
    }
}