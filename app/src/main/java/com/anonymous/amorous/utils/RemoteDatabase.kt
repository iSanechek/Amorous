package com.anonymous.amorous.utils

import com.anonymous.amorous.*
import com.anonymous.amorous.data.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

interface RemoteDatabase {
    fun createNewUser(uid: String, user: User)
    fun getDatabase(): DatabaseReference
    fun writeCandidateInDatabase(candidate: Candidate, callback: (Result<Candidate>) -> Unit)
    fun userUid(): String
    fun writeEventInDatabase(event: Event)
    fun writeInfoInDatabase(info: Info)
    fun updateCandidate(candidate: Candidate)
    fun updateCandidate(remoteUid: String?, column: String, value: Any, callback: (String) -> Unit)
    fun writeMessageInDatabase(message: Message, callback: () -> Unit)
    fun writeFolderInDatabase(folder: Folder, callback: (Result<Folder>) -> Unit)
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

    override fun writeFolderInDatabase(folder: Folder, callback: (Result<Folder>) -> Unit) {
        val key = db.child(DB_T_F).push().key
        key ?: return
        val copyFolder = folder.copy(remoteKey = key)
        val childUpdates = HashMap<String, Any>()
        childUpdates["/$DB_T_F/$key"] = copyFolder.toMap()
        db.updateChildren(childUpdates)
                .addOnCompleteListener { callback(Result.success(copyFolder)) }
                .addOnFailureListener { callback(Result.failure(it)) }
    }

    override fun writeCandidateInDatabase(candidate: Candidate, callback: (Result<Candidate>) -> Unit) {
        val key = db.child(DB_T_C).push().key
        key ?: return
        val copyCandidate = candidate.copy(remoteUid = key)
        val childUpdates = HashMap<String, Any>()
        childUpdates["/$DB_T_C/$key"] = copyCandidate.toMap()
        db.updateChildren(childUpdates)
                .addOnSuccessListener {
                    callback(Result.success(copyCandidate))
                }
                .addOnFailureListener {
                    callback(Result.failure(it))
                }
    }

    override fun writeEventInDatabase(event: Event) {
        val key = db.child(DB_T_E).push().key
        key ?: return
        val eventValue = event.copy(id = key).toMap()
        val eventUpdates = HashMap<String, Any>()
        val date = dateFormat.format(Date())
        eventUpdates["/$DB_T_E/$date/$key"] = eventValue
        db.updateChildren(eventUpdates)
    }

    override fun writeMessageInDatabase(message: Message, callback: () -> Unit) {
        val key = db.child(DB_T_M).push().key
        key ?: return
        val value = message.copy(id = key).toMap()
        val updates = HashMap<String, Any>()
        val date = dateFormat.format(Date())
        updates["/$DB_T_M/$date/$key"] = value
        db.updateChildren(updates)
                .addOnSuccessListener { callback() }
    }

    override fun writeInfoInDatabase(info: Info) {
        db.child(DB_T_I).setValue(info)
    }

    override fun updateCandidate(candidate: Candidate) {
        db.child(DB_T_C).child(candidate.remoteUid).setValue(candidate)
    }

    override fun updateCandidate(remoteUid: String?,
                                 column: String,
                                 value: Any,
                                 callback: (String) -> Unit) {
        val id = remoteUid ?: db.child(DB_T_C).push().key
        id ?: return
        db.child(DB_T_C)
                .child(id)
                .child(column)
                .setValue(value)
                .addOnSuccessListener { callback(String.empty()) }
                .addOnFailureListener { callback(it.message ?: "Хуйня какая-та при обновление значения!") }
    }

    private val dateFormat by lazy { SimpleDateFormat("MMM d, yyyy", Locale.US) }

    override fun userUid(): String = userUid
}