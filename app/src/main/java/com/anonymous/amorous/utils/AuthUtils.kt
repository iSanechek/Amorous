package com.anonymous.amorous.utils

import android.util.Log
import com.anonymous.amorous.DB_T_U
import com.anonymous.amorous.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

sealed class AuthCallBack {
    data class AuthOk(val user: FirebaseUser?) : AuthCallBack()
    data class AuthError(val errorMessage: String) : AuthCallBack()
    object NeedAuth : AuthCallBack()
}
interface AuthUtils {
    fun checkAuthState(callback: (AuthCallBack) -> Unit)
    fun startAuth(callback: (AuthCallBack) -> Unit)
}
class AuthUtilsImpl(
        private val db: RemoteDatabase,
        private val config: ConfigurationUtils,
        private val tracker: TrackingUtils
) : AuthUtils {


    override fun checkAuthState(callback: (AuthCallBack) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) callback(AuthCallBack.AuthOk(user)) else callback(AuthCallBack.NeedAuth)
    }

    override fun startAuth(callback: (AuthCallBack) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val userData = config.getUserData()
        auth.signInWithEmailAndPassword(userData.first, userData.second)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d("TEST", "Auth comp")
                        val user = auth.currentUser
                        if (user != null) {
                            db.getDatabase()
                                    .child(DB_T_U)
                                    .child(user.uid)
                                    .addListenerForSingleValueEvent(object: ValueEventListener {
                                        override fun onCancelled(p0: DatabaseError) {
                                            addEvent("User onCancelled ${p0.toException()}")
                                            callback(AuthCallBack.AuthError(p0.toException().message ?: ""))
                                        }

                                        override fun onDataChange(p0: DataSnapshot) {
                                            val u = p0.getValue(User::class.java)
                                            when (u) {
                                                null -> {
                                                    val newUser = User(user.uid, user.email
                                                            ?: "Fuck")
                                                    addEvent("Create new user $newUser")
                                                    db.createNewUser(user.uid, newUser)
                                                    callback(AuthCallBack.AuthOk(auth.currentUser))
                                                }
                                                else -> {
                                                    callback(AuthCallBack.AuthOk(auth.currentUser))
                                                    addEvent("Юзер уже есть в таблице")
                                                }
                                            }
                                        }
                                    })
                        }


                        tracker.sendOnServer()

                    }
                }.addOnFailureListener {
                    addEvent("Auth failure ${it.message}")
                    tracker.sendOnServer()
                    callback(AuthCallBack.AuthError(it.message ?: ""))
                }
    }

    private fun addEvent(msg: String) {
        tracker.sendEvent("AuthUtils", msg)
    }
}