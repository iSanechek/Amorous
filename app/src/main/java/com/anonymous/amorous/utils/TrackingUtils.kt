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

interface TrackingUtils {
    fun sendEvent(tag: String, event: String)
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
//                remoteDatabase.writeMessage(Message(String.empty(), msg.toString())) {
//                    when {
//                        it.isSuccess -> { database.clearEvents(events.map { event -> event.id }.toSet()) }
//                        it.isFailure -> sendError(Message(String.empty(), "Ошибка при отправке events! ${it.exceptionOrNull()?.message}"))
//                    }
//                }
            }
        } catch (e: JSONException) {
            sendError(Message(String.empty(), "Ошибка при создание json! ${e.message}"))
        }
    }

    override fun log(msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.d("Amorous", msg)
        }
    }

    override fun sendEvent(tag: String, event: String) {
        log(event)
//        database.saveEvent(Event(id = Event.getUid(), title = tag, date = Event.getTime(), event = event))
    }

    private fun sendError(msg: Message) {
        remoteDatabase.writeMessage(msg) {}
    }
}