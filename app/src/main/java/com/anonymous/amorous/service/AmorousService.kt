package com.anonymous.amorous.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.anonymous.amorous.utils.ActionUtils
import com.anonymous.amorous.utils.TrackingUtils
import com.anonymous.amorous.utils.WorkersManager
import org.koin.android.ext.android.inject

class AmorousService : Service() {

    private val track: TrackingUtils by inject()
    private val action: ActionUtils by inject()
    private val manager: WorkersManager by inject()

    override fun onCreate() {
        super.onCreate()
        track.sendEvent(TAG, hashSetOf("Service on create"))
        action.startAction {
            manager.startGeneralWorker()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =
            START_REDELIVER_INTENT

    override fun onDestroy() {
        track.sendEvent(TAG, hashSetOf("Service is destroy!"))
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = null

    companion object {
        private const val TAG = "AmorousService"
    }
}
