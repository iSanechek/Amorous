package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

abstract class BaseWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    override fun doWork(): Result = try {
        work()
    } catch (e: Exception) {
        Result.failure()
    }

    abstract fun work(): Result
}