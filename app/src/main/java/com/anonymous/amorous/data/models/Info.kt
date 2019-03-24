package com.anonymous.amorous.data.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Info(
        val totalMemory: Long? = 0L,
        val freeMemory: Long? = 0L,
        val cacheFolderSize: Long? = 0L,
        val lastUpdate: Long? = 0L
)