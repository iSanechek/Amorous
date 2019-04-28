package com.anonymous.amorous.data.models

import com.anonymous.amorous.empty
import java.util.*

data class Event(
        val userId: String = String.empty(),
        val title: String = String.empty(),
        val date: Long = 0L,
        val event: String = String.empty(),
        val userUid: String = String.empty()
) {

    fun toMap(): Map<String, Any> = mapOf(
            "userId" to userId,
            "title" to title,
            "date" to date,
            "event" to event,
            "userUid" to userUid)

    companion object {
        fun getTime() = System.currentTimeMillis()
    }
}