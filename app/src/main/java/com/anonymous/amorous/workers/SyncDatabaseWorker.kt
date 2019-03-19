package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.Candidate

class SyncDatabaseWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    override suspend fun doWorkAsync(): Result {
        val cache = database.getCandidates("SELECT * FROM ${Candidate.TABLE_NAME} " +
                "WHERE ${Candidate.COLUMN_THUMBNAIL_STATUS} =:${Candidate.THUMBNAIL_UPLOAD_NEED} " +
                "ORDER BY ${Candidate.COLUMN_DATE} ASC LIMIT =:10", null)
        addEvent("Candidates for remote upload! Candidates size ${cache.size}")
        return if (cache.isEmpty()) {
            addEvent("Retry candidates for remote upload!")
            sendEvent(TAG, getEvents())
            Result.retry()
        } else {
            val iterator = cache.listIterator()
            while (iterator.hasNext()) {

            }

            Result.success()
        }
    }

    companion object {
        private const val TAG = "SyncDatabaseWorker"
    }
}