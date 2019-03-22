package com.anonymous.amorous.utils

import android.util.Log
import com.anonymous.amorous.BuildConfig
import com.anonymous.amorous.data.Event
import java.util.*
import kotlin.collections.HashSet

interface TrackingUtils {
    fun sendEvent(tag: String, events: HashSet<String>)
    fun log(msg: String?)
}

class TrackingUtilsImpl(private val remoteDatabase: RemoteDatabase) : TrackingUtils {

    override fun log(msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.d("Amorous", msg)
        }
    }

    override fun sendEvent(tag: String, events: HashSet<String>) {
        if (!BuildConfig.DEBUG) {
            val builder = StringBuilder()
            for (event in events) {
                builder.append(event)
                builder.append(",")
            }
            val event = Event(id = UUID.randomUUID().toString(), title = tag, date = System.currentTimeMillis(), event = builder.toString())
            remoteDatabase.writeEventInDatabase(event)
        }
    }
}