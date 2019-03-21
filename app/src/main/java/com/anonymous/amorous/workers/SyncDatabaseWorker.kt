package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.utils.UploadBitmapUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.standalone.inject

class SyncDatabaseWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    private val upload: UploadBitmapUtils by inject()

    override suspend fun doWorkAsync(): Result {
        val cache = database.getCandidates("SELECT * FROM c WHERE r_c_u =? ORDER BY d ASC LIMIT 10", arrayOf("thumbnail_upload_need"))
        addEvent("Candidates for remote upload! Candidates size ${cache.size}")
        return if (cache.isEmpty()) {
            addEvent("Retry candidates for remote upload!")
            sendEvent(TAG, getEvents())
            Result.retry()
        } else {

            for (candidate in cache) {
                addEvent("Create task for candidate ${candidate.name}")

                upload.uploadBitmap(candidate) {
                    GlobalScope.launch {
                        database.updateCandidate(it)
                    }
                    remoteDatabase.writeCandidateInDatabase("", candidate)
                }

                sendEvent(TAG, getEvents())
            }

            Result.success()
        }
    }

    companion object {
        private const val TAG = "SyncDatabaseWorker"
    }
}