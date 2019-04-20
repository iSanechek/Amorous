package com.anonymous.amorous.utils

import android.content.Context
import android.util.Log
import androidx.core.os.bundleOf
import com.anonymous.amorous.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics

interface TrackingUtils {
    fun sendEvent(tag: String, event: String)
    fun log(msg: String?)
}

class TrackingUtilsImpl(private val context: Context) : TrackingUtils {

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
        }
//        analytics.logEvent("events", bundleOf("tag" to tag, "event" to event))
    }

}