package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.utils.UploadBitmapUtils
import org.koin.standalone.inject

class OriginalUploadWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    private val upload: UploadBitmapUtils by inject()

    override suspend fun doWorkAsync(): Result = try {

        val cache = database.getCandidates("SELECT * FROM c WHERE n_o_u =? ORDER BY d ASC LIMIT 5", arrayOf("original_upload_need"))
        if (cache.isNotEmpty()) {
            for (candidate in cache) {
                upload.uploadBitmap(candidate) {
                    database.updateCandidate(it)
                }
            }
        }

        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }
}