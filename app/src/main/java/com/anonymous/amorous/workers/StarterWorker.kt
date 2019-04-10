package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.utils.WorkersManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.koin.standalone.inject

class StarterWorker(
        context: Context,
        parameters: WorkerParameters
) : BaseCoroutineWorker(context, parameters) {

    override suspend fun workAction(): Result {
        var isGood = false
        val retryCount = pref.getWorkerRetryCountValue("StarterWorker")
        action.startAction { isOk ->
            isGood = isOk
            if (isOk) {
                addEvent("StarterWorker", "Auth is done! Check status worker!")
                if (retryCount > 0) pref.updateWorkerRetryCountValue("StarterWorker", 0)
                when {
                    configuration.getWorkerStatus() -> {
                        addEvent("StarterWorker", "Start workers!")
                        manager.startGeneralWorkers()
                    }
                    else -> {
                        addEvent("StarterWorker", "Stop all workers!")
                        manager.stopAllWorkers()
                    }
                }
            } else {
                addEvent("StarterWorker", "Что-то пошло не так. Ушел на новый круг!")
            }
        }
        return if (isGood) Result.success() else result(retryCount)
    }

    private fun result(retryCount: Int): Result = when {
        retryCount < configuration.getWorkerRetryCount() -> {
            val value = retryCount.inc()
            addEvent("StarterWorker", "Ooopss... Что-то пошло по пизде. Retry count $value")
            pref.updateWorkerRetryCountValue("StarterWorker", value)
            Result.retry()
        }
        else -> {
            addEvent("StarterWorker", "Все пошло по пизде! Сушите весла!")
//            sendEvents() // Нужно заменить на что-то, ибо без авторизации об ошибке никогда не узнаем
            pref.updateWorkerRetryCountValue("StarterWorker", 0)
            Result.failure()
        }
    }
}