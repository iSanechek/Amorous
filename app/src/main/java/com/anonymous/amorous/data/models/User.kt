package com.anonymous.amorous.data.models

import com.anonymous.amorous.empty

data class User(
        var uid: String = String.empty(),
        var email: String = String.empty(),
        var timeAuth: Long = 0L,
        var message: String = String.empty(),
        var phoneId: String = String.empty(),
        var authState: String = NEED_SIGN_IN
) {
    companion object {
        const val NEED_RE_AUTH = "need_re_auth"
        const val NEED_SIGN_OUT = "need_sign_out"
        const val NEED_SIGN_IN = "need_sign_in"
        const val AUTH_DONE = "auth_done"
        const val AUTH_FAIL = "auth_fail"
    }
}