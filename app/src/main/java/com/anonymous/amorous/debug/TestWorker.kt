package com.anonymous.amorous.debug

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.workers.BaseCoroutineWorker
import kotlinx.coroutines.delay

class TestWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {
    override suspend fun doWorkAsync(): Result {

        delay(10000)

        return Result.success()
    }

}