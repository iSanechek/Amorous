package com.anonymous.amorous.data.models

import com.anonymous.amorous.empty

data class Message(val userKey: String = String.empty(), val message: String = String.empty(), val lastTimeUpdate: Long = 0L) {

    fun toMap() = mapOf("userKey" to userKey, "message" to message, "lastTimeUpdate" to lastTimeUpdate)
}