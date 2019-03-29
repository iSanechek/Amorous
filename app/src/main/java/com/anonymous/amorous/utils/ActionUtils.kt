package com.anonymous.amorous.utils

interface ActionUtils {
    fun startAction(callback: () -> Unit)
}

class ActionUtilsImpl(
        private val auth: AuthUtils,
        private val tracker: TrackingUtils
) : ActionUtils {

    override fun startAction(callback: () -> Unit) {
        auth.checkAuthState { status ->
            when (status) {
                is AuthCallBack.AuthOk -> {
                    addEvent("Auth done! ${status.user?.uid}")
                    addEvent("Start action!")
                    callback()
                }
                is AuthCallBack.NeedAuth -> {
                    addEvent("Auth fail! Need auth!")
                    startAuth(callback)
                }
            }
        }
    }

    private fun startAuth(callback: () -> Unit) {
        auth.startAuth { result ->
            when (result) {
                is AuthCallBack.AuthOk -> {
                    addEvent("Auth done! ${result.user?.uid}")
                    addEvent("Start action!")
                    callback()
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