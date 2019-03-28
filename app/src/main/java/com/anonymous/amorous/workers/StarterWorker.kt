package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.utils.WorkersManager
import org.koin.standalone.inject

class StarterWorker(
        context: Context,
        parameters: WorkerParameters
) : BaseCoroutineWorker(context, parameters) {

    private val manager: WorkersManager by inject()

    override suspend fun doWorkAsync(): Result {
        sendEvent("StarterWorker", "Request workers!")
        action.startAction {
            sendEvent("StarterWorker", "Auth is done! Check status worker!")
            when {
                configuration.getWorkerStatus() -> {
                    sendEvent("StarterWorker", "Start workers!")
                    manager.startGeneralWorkers()
                }
                else -> {
                    sendEvent("StarterWorker", "Stop all workers!")
                    manager.stopAllWorkers()
                }
            }
        }

        sendEvents()
        return Result.success()
    }
}