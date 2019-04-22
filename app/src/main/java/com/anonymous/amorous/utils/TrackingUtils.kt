package com.anonymous.amorous.utils

import android.content.Context
import android.util.Log
import androidx.core.os.bundleOf
import com.anonymous.amorous.BuildConfig
import com.anonymous.amorous.data.database.FirestoreDb
import com.anonymous.amorous.data.models.Event
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface TrackingUtils {
    fun sendEvent(tag: String, event: String)
    fun log(msg: String?)
}

class TrackingUtilsImpl(private val context: Context,
                        private val db: FirestoreDb) : TrackingUtils {

    private val analytics by lazy { FirebaseAnalytics.getInstance(context) }

    override fun log(msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.d("Amorous", msg)
        }
    }

    override fun sendEvent(tag: String, event: String) {
        if (BuildConfig.DEBUG) {
            log(event)
        } else {
            GlobalScope.launch(Dispatchers.IO) {
                db.saveEvent(Event(title = tag, event = event, date = Event.getTime()))
            }
        }
//        analytics.logEvent("events", bundleOf("tag" to tag, "event" to event))
    }

}