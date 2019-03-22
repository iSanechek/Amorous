package com.anonymous.amorous.utils

import android.content.Context
import com.anonymous.amorous.data.database.LocalDatabase
import com.anonymous.amorous.debug.logDebug
import com.anonymous.amorous.service.JobSchContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface ActionUtils {
    fun startAction()
}

class ActionUtilsImpl(
        private val jss: JobSchContract,
        private val context: Context,
        private val auth: AuthUtils,
        private val scan: ScanContract,
        private val db: LocalDatabase,
        private val tracker: TrackingUtils
) : ActionUtils {

    private val events = hashSetOf<String>()

    override fun startAction() {
        auth.checkAuthState { result ->
            when (result) {
                is AuthCallBack.AuthOk -> {
                    addEvent("Auth done! ${result.user?.uid}")
                    addEvent("Start action!")
                    jss.scheduleJob(context)
                    val scanResult = scan.startScan()
                    when(scanResult) {
                        is ScanCallback.ResultOk -> {
                            val items = scanResult.items
                            when {
                                items.isNotEmpty() -> GlobalScope.launch(Dispatchers.IO) {
                                    db.saveCandidates(items)
                                }
                                else -> addEvent("Result scan null. :(")
                            }
                        }
                        is ScanCallback.ResultFail -> addEvent("Scan fail ${scanResult.fail}")
                    }
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