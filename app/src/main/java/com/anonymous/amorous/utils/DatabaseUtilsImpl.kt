package com.anonymous.amorous.utils

import com.anonymous.amorous.DB_T_C
import com.anonymous.amorous.DB_T_U
import com.anonymous.amorous.data.Candidate
import com.anonymous.amorous.data.User
import com.anonymous.amorous.debug.logDebug
import com.google.firebase.database.*

interface DatabaseUtils {
    fun createNewUser(uid: String, user: User)
    fun getDatabase(): DatabaseReference
    fun writeCandidateInDatabase(uid: String, candidate: Candidate)
    fun getCandidates(uid: String): List<Candidate>
}

class DatabaseUtilsImpl : DatabaseUtils {

    private val db: DatabaseReference
        get() = FirebaseDatabase.getInstance().reference

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
        childUpdates["/$DB_T_U/$uid/$key"] = candidateValue
        db.updateChildren(childUpdates)
    }

    override fun getCandidates(uid: String): List<Candidate> {
        val temp = mutableListOf<Candidate>()


        return temp
    }
}