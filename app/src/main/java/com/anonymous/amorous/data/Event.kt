package com.anonymous.amorous.data

import com.anonymous.amorous.empty
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Event(
        val id: String? = String.empty(),
        val title: String? = String.empty(),
        val date: Long? = 0L,
        val event: String? = String.empty()
) {

    @Exclude
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
    }
}