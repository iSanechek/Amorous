package com.anonymous.amorous.workers

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.models.Candidate
import com.anonymous.amorous.utils.UploadBitmapUtils
import org.koin.standalone.inject

class UploadThumbnailWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    private val upload: UploadBitmapUtils by inject()

    override suspend fun doWorkAsync(): Result = try {
        val cache = database.getCandidates("SELECT * FROM candidate WHERE thumbnailstatus =? ORDER BY date ASC LIMIT 5", arrayOf("thumbnail_upload_need"))
        sendEvent(TAG, "Candidates for remote upload! Candidates size ${cache.size}")
        when {
            cache.isEmpty() -> sendEvent(TAG, "Retry candidates thumbnail for remote upload!")
            else -> for (candidate in cache) {

                upload.uploadThumbnail(candidate) { result ->
                    remoteDatabase.writeCandidateInDatabase(result) {
                        when {
                            it.isSuccess -> {
                                Log.e("TAG", "Upload done")
                                database.updateCandidate(it.getOrDefault(result))
                            }
                            it.isFailure -> sendEvent(TAG, it.exceptionOrNull()?.message ?: "Error add ${result.name} in remote database!")
                        }
                    }
                }
            }
        }
        Result.success()
    } catch (e: Exception) {
        sendEvent("", "Error upload thumbnail! ${e.message}")
        Result.failure()
    }

    companion object {
        private const val TAG = "UploadThumbnailWorker"
    }
}