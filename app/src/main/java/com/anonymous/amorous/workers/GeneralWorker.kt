package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.models.User
import com.anonymous.amorous.utils.WorkersManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.standalone.inject

class GeneralWorker(
        context: Context,
        parameters: WorkerParameters
) : BaseCoroutineWorker(context, parameters) {

    override val coroutineContext: CoroutineDispatcher
        get() = Dispatchers.Main

    override suspend fun workAction(): Result {
        addEvent(TAG, "Run StarterWorker! Check auth!")
        val user = auth.auth(db.getUser())
        return when {
            user.uid != null -> {
                doAction(user)
                Result.success()
            }
            else -> {
                addEvent(TAG, "Auth is fail. Start auth!")
                when {
                    user.uid != null -> {
                        doAction(user)
                        Result.success()
                    }
                    else -> result(pref.getWorkerRetryCountValue(TAG))
                }
            }
        }
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
            pref.updateWorkerRetryCountValue("StarterWorker", 0)
            Result.failure()
        }
    }

    private suspend fun doAction(user: User) {
        db.saveUser(user)
        val retryCount = pref.getWorkerRetryCountValue(TAG)
        if (retryCount > 0) pref.updateWorkerRetryCountValue(TAG, 0)
        if (configuration.getWorkerStatus()) {
            addEvent(TAG, "Start workers")
            manager.startGeneralWorkers()
        }
    }
    companion object {
        private const val TAG = "StarterWorker"
    }
}