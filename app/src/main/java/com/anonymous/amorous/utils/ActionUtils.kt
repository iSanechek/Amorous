package com.anonymous.amorous.utils

import android.content.Context
import com.anonymous.amorous.data.database.LocalDatabase
import com.anonymous.amorous.debug.logDebug
import com.anonymous.amorous.service.JobSchContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface ActionContract {
    fun prepareAction()
}

class ActionUtils(
        private val jss: JobSchContract,
        private val context: Context,
        private val auth: AuthContract,
        private val scan: ScanContract,
        private val db: LocalDatabase,
        private val upload: UploadBitmapUtils) : ActionContract {

    override fun prepareAction() {
        val checkAuthResult = auth.checkAuthState()
        when(checkAuthResult) {
            is AuthCallBack.NeedAuth -> {
                val authResult = auth.startAuth()
                when(authResult) {
                    is AuthCallBack.AuthOk -> {
                        logDebug {
                            "Auth is Ok ${authResult.user?.displayName}"
                        }
                        startAction()
                    }
                    is AuthCallBack.AuthError -> logDebug {
                        "Auth Error ${authResult.errorMessage}"
                    }
                    is AuthCallBack.AuthEmpty -> logDebug {
                        "Auth empty"
                    }
                    else -> logDebug {
                        "WTF authResult $authResult"
                    }
                }
            }
            is AuthCallBack.AuthOk -> {
                logDebug {
                    "Auth check is Ok ${checkAuthResult.user?.uid?.substring(0, 6)}"
                }

                startAction()
            }
            else -> logDebug {
                "WTF authResult $checkAuthResult"
            }
        }
    }

    private fun startAction() {
        jss.scheduleJob(context)

        val result = scan.startScan()
        when(result) {
            is ScanCallback.ResultOk -> {
                val items = result.items
                logDebug {
                    "Scan result size ${items.size}"
                }

                GlobalScope.launch(Dispatchers.IO) {
                    db.saveCandidates(items)
                }

//                val item = result.items[7]
//                upload.uploadBitmap(item)
            }
            is ScanCallback.ResultFail -> {
                logDebug {
                    "Scan fail ${result.fail}"
                }
            }
        }
    }
}