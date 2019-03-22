package com.anonymous.amorous.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.anonymous.amorous.utils.ActionUtils
import com.anonymous.amorous.utils.TrackingUtils
import org.koin.android.ext.android.inject

class AmorousService : Service() {

    private val track: TrackingUtils by inject()
    private val jss: JobSchContract by inject()
    private val action: ActionUtils by inject()

    override fun onCreate() {
        super.onCreate()
        track.sendEvent(TAG, hashSetOf("Service on create"))
        checkJobService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =
            START_REDELIVER_INTENT

    override fun onDestroy() {
        track.sendEvent(TAG, hashSetOf("Service is destroy!"))
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun checkJobService() {
        val events = hashSetOf<String>()
        events.add("Start check jobs service work")
        when {
            !jss.serviceIsRun(this) -> {
                jss.scheduleJob(this)
                events.add("Jobs service is not running! Start service!")
            }
            else -> events.add("Jobs service is running!")
        }
        track.sendEvent(TAG, events)
    }

    companion object {
        private const val TAG = "AmorousService"
    }
}
