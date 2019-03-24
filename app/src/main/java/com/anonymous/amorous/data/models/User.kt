package com.anonymous.amorous.data.models

import com.anonymous.amorous.empty
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
        var uid: String? = String.empty(),
        var email: String? = String.empty()
)