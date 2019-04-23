package com.anonymous.amorous.workers

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.models.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class GeneralWorker(
        context: Context,
        parameters: WorkerParameters
) : BaseCoroutineWorker(context, parameters) {

    override val coroutineContext: CoroutineDispatcher
        get() = Dispatchers.Main

    @SuppressLint("HardwareIds")
    override suspend fun workAction(): Result {
        addEvent(TAG, "Run StarterWorker! Check authIn!")
        var userData = db.getUser()
        if (userData.phoneId.isEmpty()) {
            userData = userData.copy(phoneId = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID))
        }
        return when {
            userData.authState == User.NEED_SIGN_IN -> {
                doActionAfterSign(auth.authIn(userData))
            }
            userData.authState == User.NEED_RE_AUTH -> {
                doActionAfterSign(auth.reSignIn(userData))
                Result.success()
            }
            userData.authState == User.NEED_SIGN_OUT -> {
                doAction(auth.authOut(userData))
                Result.success()
            }
            else -> {
                addEvent(TAG, "Что-то пошла при авторизации!")
                doAction(userData.copy(authState = User.AUTH_FAIL))
                Result.failure()
            }
        }
    }

    private suspend fun doActionAfterSign(u: User): Result = when(u.authState) {
        User.AUTH_DONE -> {
            doAction(u)
            Result.success()
        }
        User.AUTH_FAIL -> result(pref.getWorkerRetryCountValue(TAG))
        else -> {
            doAction(u)
            Result.failure()
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