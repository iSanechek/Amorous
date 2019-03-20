package com.anonymous.amorous.data

data class Event(
        val id: String,
        val title: String,
        val date: Long,
        val event: String
) {

    companion object {
        const val TABLE_NAME = "event"

        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DATE = "date"
        const val COLUMN_EVENT = "event"
    }
}