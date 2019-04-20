package com.anonymous.amorous.data.models

import com.anonymous.amorous.empty
import java.util.*

data class Event(
        val id: String = String.empty(),
        val title: String = String.empty(),
        val date: Long = 0L,
        val event: String = String.empty()
) {

    fun toMap(): Map<String, Any?> = mapOf(
            "id" to id,
            "title" to title,
            "date" to date,
            "event" to event)

    companion object {
        const val TABLE_NAME = "event"

        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DATE = "date"
        const val COLUMN_EVENT = "event"

        fun getUid() = UUID.randomUUID().toString()
        fun getTime() = System.currentTimeMillis()
    }
}