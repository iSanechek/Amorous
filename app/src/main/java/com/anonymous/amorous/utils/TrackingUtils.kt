package com.anonymous.amorous.utils

import android.util.Log
import com.anonymous.amorous.BuildConfig
import com.anonymous.amorous.data.database.FirestoreDb
import com.anonymous.amorous.data.models.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface TrackingUtils {
    fun sendEvent(tag: String, event: String)
    fun log(msg: String?)
}

class TrackingUtilsImpl(private val db: FirestoreDb) : TrackingUtils {

    override fun log(msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.d("Amorous", msg)
        }
    }

    override fun sendEvent(tag: String, event: String) {
        when {
            BuildConfig.DEBUG -> log(event)
            else -> GlobalScope.launch(Dispatchers.IO) {
                db.saveEvent(Event(title = tag, event = event, date = Event.getTime()))
            }
        }
    }
}