package com.anonymous.amorous.data.models

import com.anonymous.amorous.empty

data class User(
        var uid: String? = String.empty(),
        var email: String? = String.empty(),
        var timeAuth: Long? = 0L,
        var message: String? = String.empty()
)