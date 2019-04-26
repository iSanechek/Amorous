package com.anonymous.amorous.utils

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.anonymous.amorous.BuildConfig
import com.anonymous.amorous.data.database.RemoteDb
import com.anonymous.amorous.data.models.Event
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface TrackingUtils {
    fun sendEvent(tag: String, event: String)
    fun log(msg: String?)
}

class TrackingUtilsImpl(private val context: Context, private val db: RemoteDb) : TrackingUtils {

    private val anal by lazy { FirebaseAnalytics.getInstance(context) }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun log(msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.d("Amorous", msg)
        }
    }

    override fun sendEvent(tag: String, event: String) {
        when {
            BuildConfig.DEBUG -> log(event)
            else -> {
                if (auth.currentUser != null) {
                    GlobalScope.launch(Dispatchers.IO) {
                        db.saveEvent(Event(title = tag, event = event, date = Event.getTime()))
                    }
                } else {
                    val args = Bundle()
                    args.putString("title", tag)
                    args.putString("event", event)
                    args.putLong("date", Event.getTime())
                    anal.logEvent("No_Auth", args)
                }
            }
        }
    }
}