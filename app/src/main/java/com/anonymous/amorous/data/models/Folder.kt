package com.anonymous.amorous.data.models

import com.anonymous.amorous.empty
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Folder(var name: String = String.empty(),
                  var lastModification: Long? = 0L,
                  var remoteKey: String = String.empty()) {

    @Exclude
    fun toMap(): Map<String, Any?> = mapOf(
            "name" to name,
            "lastModification" to lastModification,
            "remoteKey" to remoteKey)
}