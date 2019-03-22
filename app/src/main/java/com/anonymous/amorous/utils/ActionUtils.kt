package com.anonymous.amorous.utils

import android.content.Context
import com.anonymous.amorous.data.database.LocalDatabase
import com.anonymous.amorous.debug.logDebug
import com.anonymous.amorous.service.JobSchContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface ActionUtils {
    fun startAction(callback: () -> Unit)
}

class ActionUtilsImpl(
        private val auth: AuthUtils,
        private val tracker: TrackingUtils
) : ActionUtils {

    private val events = hashSetOf<String>()

    override fun startAction(callback: () -> Unit) {
        auth.checkAuthState { result ->
            when (result) {
                is AuthCallBack.AuthOk -> {
                    addEvent("Auth done! ${result.user?.uid}")
                    addEvent("Start action!")
                    callback()
                    tracker.sendEvent("ActionUtils", events)
                }
                is AuthCallBack.AuthError -> {
                    addEvent("Auth fail!")
                    addEvent("Auth error: ${result.errorMessage}")
                    tracker.sendEvent("ActionUtils", events)
                }
            }
        }
    }

    private fun addEvent(msg: String) {
        events.add(msg)
    }
}