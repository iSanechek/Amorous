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
    suspend fun authIn(u: User): User
    suspend fun authOut(u: User): User
    suspend fun reSignIn(u: User): User
}

class AuthUtilsImpl(
        private val tracker: TrackingUtils
) : AuthUtils {

    private val authInstance by lazy { FirebaseAuth.getInstance() }

    override suspend fun authIn(u: User): User = suspendCoroutine { c ->
        val e = u.email
        val p = u.message
        addEvent("User data $e -- $p")
        authInstance.signInWithEmailAndPassword(e, p)
                .addOnCompleteListener { task ->
                    when {
                        task.isSuccessful -> {
                            val user = task.result?.user
                            addEvent("Auth is done! $user")
                            c.resume(User(uid = user?.uid ?: "Хуй тебе, а не uid", email = e, message = p, timeAuth = System.currentTimeMillis(), authState = User.AUTH_DONE))
                        }
                        else -> c.resume(User())
                    }
                }.addOnFailureListener {
                    addEvent("Event fail! Error ${it.message ?: "Какое-то говно"}")
                    c.resume(User(authState = User.AUTH_FAIL))
                }
    }

    override suspend fun reSignIn(u: User): User {
        val user = authOut(u)
        if (user.authState == User.NEED_SIGN_IN) {
            return authIn(user)
        }
        return user
    }

    override suspend fun isAuth(): Boolean = withContext(Dispatchers.IO) {
        authInstance.currentUser != null
    }

    override suspend fun authOut(u: User): User {
        authInstance.signOut()
        return when {
            authInstance.currentUser == null -> u.copy(authState = User.NEED_SIGN_IN)
            else -> u.copy(authState = User.AUTH_FAIL)
        }
    }

    private fun addEvent(msg: String) {
        tracker.sendEvent("AuthUtils", msg)
    }
}