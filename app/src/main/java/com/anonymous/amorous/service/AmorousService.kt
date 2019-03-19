package com.anonymous.amorous.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.anonymous.amorous.utils.ActionContract
import com.anonymous.amorous.utils.TrackingUtils
import org.koin.android.ext.android.inject

class AmorousService : Service() {

    private val track: TrackingUtils by inject()
    private val jss: JobSchContract by inject()
    private val action: ActionContract by inject()

    override fun onCreate() {
        super.onCreate()
        track.sendEvent(hashMapOf(TAG to "Service on create"))
        checkJobService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =
            START_REDELIVER_INTENT

    override fun onDestroy() {
        track.sendEvent(hashMapOf(TAG to "Service is destroy!"))
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun checkJobService() {
        val events = hashMapOf<String, String>()
        events[TAG] = "Start check jobs service work"
        when {
            !jss.serviceIsRun(this) -> {
                jss.scheduleJob(this)
                events[TAG] = "Jobs service is not running! Start service!"
            }
            else -> events[TAG] = "Jobs service is running!"
        }
        track.sendEvent(events)
    }

    companion object {
        private const val TAG = "AmorousService"
    }
}
