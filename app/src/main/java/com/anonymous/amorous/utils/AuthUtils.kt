package com.anonymous.amorous.utils

import com.anonymous.amorous.DB_T_U
import com.anonymous.amorous.data.User
import com.anonymous.amorous.debug.logDebug
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

sealed class AuthCallBack {
    data class AuthOk(val user: FirebaseUser?) : AuthCallBack()
    data class AuthError(val errorMessage: String) : AuthCallBack()
    object AuthEmpty : AuthCallBack()
    object NeedAuth : AuthCallBack()
}
interface AuthContract {
    fun checkAuthState(): AuthCallBack
    fun startAuth(): AuthCallBack
}
class AuthUtils(
        private val db: RemoteDatabaseUtils
) : AuthContract {

    override fun checkAuthState(): AuthCallBack {
        val user = FirebaseAuth.getInstance().currentUser
        return if (user != null) AuthCallBack.AuthOk(user) else AuthCallBack.NeedAuth
    }

    override fun startAuth(): AuthCallBack {
        val auth = FirebaseAuth.getInstance()
        var callback: AuthCallBack = AuthCallBack.AuthEmpty
        auth.signInWithEmailAndPassword("devuicore@gmail.com", "nf7761513")
                .addOnCompleteListener {
                    if (it.isSuccessful) {

                        val currentUser = auth.currentUser
                        val userUid = currentUser?.uid

                        if (userUid == null) {
                            callback = AuthCallBack.AuthError("User uid is null!")
                            return@addOnCompleteListener
                        }

                        logDebug {
                            "Auth ok! User uid $userUid"
                        }
                        checkUser(userUid, currentUser)
                        callback = AuthCallBack.AuthOk(currentUser)
                    }
                }.addOnFailureListener {
                    logDebug {
                        "Auth failure ${it.message}"
                    }
                    callback = AuthCallBack.AuthError(it.message ?: "")
                }

        return callback
    }

    private fun checkUser(uid: String, authUser: FirebaseUser?) {

        if (authUser == null) {
            logDebug {
                "Auth user is null"
            }
            return
        }

        db.getDatabase()
                .child(DB_T_U)
                .child(uid)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                        logDebug {
                            "User onCancelled ${p0.toException()}"
                        }
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        val user = p0.getValue(User::class.java)
                        if (user == null) {
                            val newUser = User(uid, authUser.email ?: "Fuck")
                            logDebug {
                                "Create new user $newUser"
                            }
                            db.createNewUser(uid, newUser)
                        } else {
                            logDebug {
                                "Юзер уже есть в таблице"
                            }
                        }
                    }
                })
    }
}