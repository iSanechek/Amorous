package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.Candidate

class RemoveBackupWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    override suspend fun doWorkAsync(): Result {

        val result = database.getCandidates("SELECT * FROM ${Candidate.TABLE_NAME} WHERE ${Candidate.COLUMN_NEED_LOCAL_BACKUP} = ?",
                arrayOf(Candidate.ORIGINAL_FILE_NEED_REMOVE))
        if (result.isNotEmpty()) {

        }

        return Result.success()
    }
}