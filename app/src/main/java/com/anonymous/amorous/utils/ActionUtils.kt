package com.anonymous.amorous.utils

interface ActionUtils {
    fun startAction(callback: (Boolean) -> Unit)
}

class ActionUtilsImpl(
        private val auth: AuthUtils,
        private val tracker: TrackingUtils
) : ActionUtils {

    override fun startAction(callback: (Boolean) -> Unit) {
        auth.checkAuthState { status ->
            when (status) {
                is AuthCallBack.AuthOk -> {
                    addEvent("Auth done! ${status.user?.uid}")
                    addEvent("Start action!")
                    callback(true)
                }
                is AuthCallBack.NeedAuth -> {
                    addEvent("Auth fail! Need auth!")
                    startAuth(callback)
                }
            }
        }
    }

    private fun startAuth(callback: (Boolean) -> Unit) {
        auth.startAuth { result ->
            when (result) {
                is AuthCallBack.AuthOk -> {
                    addEvent("Auth done! ${result.user?.uid}")
                    addEvent("Start action!")
                    callback(true)
                }
                is AuthCallBack.AuthError -> {
                    addEvent("Auth fail!")
                    addEvent("Auth error: ${result.errorMessage}")
                    callback(false)
                }
            }
        }
    }
    private fun addEvent(msg: String) {
        tracker.sendEvent("ActionUtils", msg)
    }
}