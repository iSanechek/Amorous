package com.anonymous.amorous.utils

import android.util.Log
import com.anonymous.amorous.BuildConfig
import org.json.JSONObject

interface TrackingUtils {
    fun sendEvent(events: HashMap<String, String>)
    fun log(msg: String?)
}

class TrackingUtilsImpl : TrackingUtils {

    override fun log(msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.d("Amorous", msg)
        }
    }

    override fun sendEvent(events: HashMap<String, String>) {
        if (!BuildConfig.DEBUG) {

            val root = JSONObject()
            for (event in events) {
                val json = """{"event" : { "title": "${event.key}","date": ${System.currentTimeMillis()},"message": "${event.value}"}"""

                log(json)
            }

        }
    }
}