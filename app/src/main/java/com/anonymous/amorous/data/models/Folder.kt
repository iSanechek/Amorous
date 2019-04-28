package com.anonymous.amorous.data.models

import com.anonymous.amorous.empty

data class Folder(var uid: String = String.empty(),
                  var name: String = String.empty(),
                  var lastModification: Long = 0L,
                  var userKey: String = String.empty(),
                  var parentPath: String = String.empty(),
                  var userUid: String = String.empty()) {

    fun toMap(): Map<String, Any?> = mapOf(
            "uid" to uid,
            "name" to name,
            "lastModification" to lastModification,
            "userKey" to userKey,
            "parentPath" to parentPath,
            "userUid" to userUid)
}