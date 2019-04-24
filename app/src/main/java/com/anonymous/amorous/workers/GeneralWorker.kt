package com.anonymous.amorous.workers

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
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
        addEvent(TAG, "Запущен General Worker! Проверка авторизации!")
        return if (auth.isAuth()) {
            addEvent(TAG, "Auth is Ok!")
            val userData = db.getUser()
            when {
                userData.authState == User.NEED_RE_AUTH -> doActionAfterSign(auth.reSignIn(userData))
                userData.authState == User.NEED_SIGN_OUT -> {
                    auth.authOut(userData)
                    Result.success()
                }
                else -> {
                    manager.startGeneralWorkers()
                    Result.success()
                }
            }
        } else {
            var userData = db.getUser()
            if (userData.phoneId.isEmpty()) {
                userData = userData.copy(phoneId = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID))
            }

            when {
                userData.authState == User.NEED_SIGN_IN -> doActionAfterSign(auth.authIn(userData))
                else -> {
                    Log.e(TAG, "user -> $userData")
                    addEvent(TAG, "Что-то пошла при авторизации не так!")
                    Result.success()
                }
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
            addEvent(TAG, "Ooopss... Что-то пошло по пизде. Повтор в $value раз!")
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
            addEvent(TAG, "Понеслась пизда по кочкам")
            manager.startGeneralWorkers()
        }
    }
    companion object {
        private const val TAG = "StarterWorker"
    }
}