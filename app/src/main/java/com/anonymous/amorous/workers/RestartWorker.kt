package com.anonymous.amorous.workers

import android.content.Context
import android.content.Intent
import androidx.work.WorkerParameters
import com.anonymous.amorous.service.AmorousService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class RestartWorker(
        context: Context,
        parameters: WorkerParameters
) : BaseCoroutineWorker(context, parameters) {

    override val coroutineContext: CoroutineDispatcher
        get() = Dispatchers.Main

    override suspend fun workAction(): Result {
        applicationContext.startService(Intent(applicationContext, AmorousService::class.java))
        return Result.success()
    }
}