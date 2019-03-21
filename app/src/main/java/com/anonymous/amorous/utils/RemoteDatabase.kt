package com.anonymous.amorous.utils

import com.anonymous.amorous.DB_T_C
import com.anonymous.amorous.DB_T_U
import com.anonymous.amorous.data.Candidate
import com.anonymous.amorous.data.User
import com.anonymous.amorous.debug.logDebug
import com.anonymous.amorous.empty
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

interface RemoteDatabase {
    fun createNewUser(uid: String, user: User)
    fun getDatabase(): DatabaseReference
    fun writeCandidateInDatabase(uid: String, candidate: Candidate)
    fun userUid(): String
}

class DatabaseUtilsImpl : RemoteDatabase {

    private val db: DatabaseReference
        get() = FirebaseDatabase.getInstance().reference

    private val userUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: String.empty()

    override fun createNewUser(uid: String, user: User) {
        db.child(DB_T_U).child(uid).setValue(user)
    }

    override fun getDatabase(): DatabaseReference = db

    override fun writeCandidateInDatabase(uid: String, candidate: Candidate) {
        logDebug {
            "writeCandidateInDatabase $candidate"
        }


        val key = db.child(DB_T_C).push().key
        logDebug {
            "KEY $key"
        }

        key ?: return
        val candidateValue = candidate.toMap()
        val childUpdates = HashMap<String, Any>()
        childUpdates["/$DB_T_C/$key"] = candidateValue
        childUpdates["/$DB_T_U/$userUid/$key"] = candidateValue
        db.updateChildren(childUpdates)
    }

    override fun userUid(): String = userUid
}