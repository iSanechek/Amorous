package com.anonymous.amorous.data.models

import com.anonymous.amorous.empty

data class Info(
        val userKey: String? = String.empty(),
        val totalMemory: Long? = 0L,
        val freeMemory: Long? = 0L,
        val cacheFolderSize: Long? = 0L,
        val lastUpdate: Long? = 0L
) {
    fun toMap() = mapOf(
            "userKey" to userKey,
            "totalMemory" to totalMemory,
            "freeMemory" to freeMemory,
            "cacheFolderSize" to cacheFolderSize,
            "lastUpdate" to lastUpdate
    )

    fun toMapUpdate() = mapOf(
            "totalMemory" to totalMemory,
            "freeMemory" to freeMemory,
            "cacheFolderSize" to cacheFolderSize,
            "lastUpdate" to lastUpdate
    )
}