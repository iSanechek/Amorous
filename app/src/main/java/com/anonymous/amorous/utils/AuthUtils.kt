package com.anonymous.amorous.utils

import com.anonymous.amorous.data.models.User
import com.anonymous.amorous.empty
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface AuthUtils {
    suspend fun isAuth(): Boolean
    suspend fun auth(u: User): User
}

class AuthUtilsImpl(
        private val tracker: TrackingUtils
) : AuthUtils {

    private val authInstance by lazy { FirebaseAuth.getInstance() }

    override suspend fun auth(u: User): User = suspendCoroutine { c ->
        val e = u.email ?: String.empty()
        val p = u.message ?: String.empty()
        addEvent("User data $e -- $p")
        authInstance.signInWithEmailAndPassword(e, p)
                .addOnCompleteListener { task ->
                    when {
                        task.isSuccessful -> {
                            val user = task.result?.user
                            addEvent("Auth is done! $user")
                            c.resume(User(uid = user?.uid, email = user?.email, message = p, timeAuth = System.currentTimeMillis()))
                        }
                        else -> c.resume(User())
                    }
                }.addOnFailureListener {
                    addEvent("Event fail! Error ${it.message ?: "Какое-то говно"}")
                    c.resume(User())
                }
    }

    override suspend fun isAuth(): Boolean = withContext(Dispatchers.IO) {
        authInstance.currentUser != null
    }

    private fun addEvent(msg: String) {
        tracker.sendEvent("AuthUtils", msg)
    }
}