package com.anonymous.amorous.utils

import android.content.Context
import android.util.Log
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


    override fun startAction(callback: () -> Unit) {
        auth.startAuth { result ->
            when (result) {
                is AuthCallBack.AuthOk -> {
                    addEvent("Auth done! ${result.user?.uid}")
                    addEvent("Start action!")
                    callback()
                    tracker.sendOnServer()
                }
                is AuthCallBack.AuthError -> {
                    addEvent("Auth fail!")
                    addEvent("Auth error: ${result.errorMessage}")
                    tracker.sendOnServer()
                }
            }
        }
    }

    private fun addEvent(msg: String) {
        tracker.sendEvent("ActionUtils", msg)
    }
}