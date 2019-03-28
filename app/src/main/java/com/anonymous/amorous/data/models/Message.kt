package com.anonymous.amorous.data.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Message(val id: String, val message: String) {

    @Exclude
    fun toMap(): Map<String, Any?> = mapOf(
            "id" to id,
            "message" to message)
}