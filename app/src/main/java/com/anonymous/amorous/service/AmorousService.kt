package com.anonymous.amorous.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.anonymous.amorous.utils.ConfigurationUtils
import com.anonymous.amorous.utils.TrackingUtils
import com.anonymous.amorous.utils.WorkersManager
import org.koin.android.ext.android.inject

class AmorousService : Service() {

    private val track: TrackingUtils by inject()
    private val manager: WorkersManager by inject()
    private val jss: JobSchContract by inject()
    private val config: ConfigurationUtils by inject()

    override fun onCreate() {
        super.onCreate()
        sendEvent("Service on create")
        startJobService()
        manager.startGeneralWorker()
        manager.restartWorker()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =
            START_NOT_STICKY

    override fun onDestroy() {
        sendEvent("Service is destroy!")
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun sendEvent(event: String) {
        track.sendEvent(TAG, event)
    }

    private fun startJobService() {
        if (!jss.serviceIsRun(this)) {
            if (config.getStartJobsServiceStatus()) {
                sendEvent("Schedule JobService!")
                jss.scheduleJob(this)
            } else {
                sendEvent("JobService false on service!")
            }
        } else {
            sendEvent("Job service is running!")
        }
    }

    companion object {
        private const val TAG = "AmorousService"
    }
}
