package com.anonymous.amorous.utils

import com.anonymous.amorous.*
import com.anonymous.amorous.data.models.Candidate
import com.anonymous.amorous.data.models.Event
import com.anonymous.amorous.data.models.Info
import com.anonymous.amorous.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

interface RemoteDatabase {
    fun createNewUser(uid: String, user: User)
    fun getDatabase(): DatabaseReference
    fun writeCandidateInDatabase(uid: String, candidate: Candidate)
    fun userUid(): String
    fun writeEventInDatabase(event: Event)
    fun writeInfoInDatabase(info: Info)
    fun updateCandidate(candidate: Candidate)
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
        val key = db.child(DB_T_C).push().key
        key ?: return
        val candidateValue = candidate.toMap()
        val childUpdates = HashMap<String, Any>()
        childUpdates["/$DB_T_C/$key"] = candidateValue
        db.updateChildren(childUpdates)
    }

    override fun writeEventInDatabase(event: Event) {
        val key = db.child(DB_T_E).push().key
        key ?: return
        val eventValue = event.toMap()
        val eventUpdates = HashMap<String, Any>()
        eventUpdates["/$DB_T_E/$key"] = eventValue
        db.updateChildren(eventUpdates)
    }

    override fun writeInfoInDatabase(info: Info) {
        db.child(DB_T_I).setValue(info)
    }

    override fun updateCandidate(candidate: Candidate) {

    }

    override fun userUid(): String = userUid
}