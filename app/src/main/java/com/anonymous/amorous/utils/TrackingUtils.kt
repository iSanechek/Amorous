package com.anonymous.amorous.utils

import android.util.Log
import com.anonymous.amorous.BuildConfig
import com.anonymous.amorous.data.database.LocalDatabase
import com.anonymous.amorous.data.models.Event
import com.anonymous.amorous.data.models.Message
import com.anonymous.amorous.empty
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashSet

interface TrackingUtils {
    fun sendEvent(tag: String, events: HashSet<String>)
    fun sendEvent(tag: String, event: Event)
    fun sendOnServer()
    fun log(msg: String?)
}

class TrackingUtilsImpl(private val remoteDatabase: RemoteDatabase,
                        private val database: LocalDatabase) : TrackingUtils {

    override fun sendOnServer() {
        try {
            val events = database.getEvents()
            if (events.isNotEmpty()) {
                val jo = JSONObject()
                val items = JSONArray()
                for (event in events) {
                    val item = JSONObject()
                    item.put("id", event.id)
                    item.put("title", event.title)
                    item.put("date", event.date)
                    item.put("event", event.event)
                    items.put(item)
                }
                val msg = jo.put("events", items)
                remoteDatabase.writeMessageInDatabase(Message("", msg.toString())) {
                    database.clearEvents(events.map { it.id }.toSet())
                }
            } else addEvent("Events is empty!")
        } catch (e: JSONException) {
            addEvent(e.message ?: "Create message json error!")
        }
    }

    override fun log(msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.d("Amorous", msg)
        }
    }

    override fun sendEvent(tag: String, event: Event) {
        log(event.toString())
        database.saveEvent(event)
    }

    override fun sendEvent(tag: String, events: HashSet<String>) {

    }

    private fun addEvent(msg: String) {
        remoteDatabase.writeMessageInDatabase(Message(String.empty(), msg)) {}
    }
}