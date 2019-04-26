package com.anonymous.amorous.service

import android.util.Log
import com.anonymous.amorous.data.database.RemoteDb
import com.anonymous.amorous.utils.TrackingUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.inject

class RemoteControlService : FirebaseMessagingService() {

    private val db: RemoteDb by inject()
    private val tracker: TrackingUtils by inject()

    override fun onCreate() {
        addEvent("Service created!")
        log("onCreate")
    }

    override fun onMessageReceived(p0: RemoteMessage?) {
        addEvent("Message Received!")
        log("onMessageReceived ${p0.toString()}")
    }

    override fun onNewToken(p0: String?) {
        addEvent("New token!")
        p0?.let {
//            GlobalScope.launch(Dispatchers.IO) {
//                db.saveMessage(Message(message = it))
//            }
        }
        log("onNewToken")
    }

    override fun onDestroy() {
        addEvent("Service is destroy!")
        log("Destroy")
    }

    private fun addEvent(event: String) {
        tracker.sendEvent("RemoteControlService", event)
    }

    private fun log(msg: String) {
        Log.e("HYI!", msg)
    }
}