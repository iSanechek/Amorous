package com.anonymous.amorous.utils

import android.util.Log
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
    fun writeInfoInDatabase(info: Info)
    fun writeFolderInDatabase(folder: Folder, callback: (Result<Folder>) -> Unit)
    fun writeMessage(msg: Message, callback: (Result<Message>) -> Unit)
}

class DatabaseUtilsImpl : RemoteDatabase {

    private val dateFormat by lazy { SimpleDateFormat("MMM_d_yyyy", Locale.US) }

    private val db: DatabaseReference
        get() = FirebaseDatabase.getInstance().reference

    private val userUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: String.empty()

    override fun getDatabase(): DatabaseReference = db

    override fun createNewUser(uid: String, user: User) {
        db.child(DB_T_U).child(uid).setValue(user)
    }

    override fun writeFolderInDatabase(folder: Folder, callback: (Result<Folder>) -> Unit) {
        when {
            folder.remoteKey.isEmpty() -> {
                val key = db.child(DB_T_F).push().key
                when (key) {
                    null -> callback(Result.failure(IllegalArgumentException("Не удалось создать ключ для $folder")))
                    else -> updateFolder(key, folder, callback)
                }
            }
            else -> updateFolder(folder.remoteKey, folder, callback)
        }
    }

    private fun updateFolder(key: String, folder: Folder, callback: (Result<Folder>) -> Unit) {
        val copyFolder = folder.copy(remoteKey = key)
        val folderUpdates = HashMap<String, Any>()
        folderUpdates["/$DB_T_U/$userUid/$DB_T_F/$key"] = copyFolder.toMap()
        db.updateChildren(folderUpdates)
                .addOnSuccessListener { callback(Result.success(copyFolder)) }
                .addOnFailureListener { callback(Result.failure(it)) }
    }

    override fun writeCandidateInDatabase(candidate: Candidate, callback: (Result<Candidate>) -> Unit) {
        val date = dateFormat.format(Date())
        val table = "/$DB_T_U/$userUid/$date/$DB_T_C"
        when {
            candidate.remoteUid.isEmpty() -> {
                val key = db.child(table).push().key
                when (key) {
                    null -> callback(Result.failure(IllegalArgumentException("Не удалось создать ключ для $candidate")))
                    else -> updateCandidate(key, table, candidate, callback)
                }
            }
            else -> updateCandidate(candidate.remoteUid, table, candidate, callback)
        }
    }

    private fun updateCandidate(key: String, table: String, candidate: Candidate, callback: (Result<Candidate>) -> Unit) {
        val copyCandidate = candidate.copy(remoteUid = key)
        val candidateUpdates = HashMap<String, Any>()
        candidateUpdates["/$table/$key"] = copyCandidate.toMap()
        db.updateChildren(candidateUpdates)
                .addOnSuccessListener { callback(Result.success(copyCandidate)) }
                .addOnFailureListener { callback(Result.failure(it)) }
    }

    override fun writeInfoInDatabase(info: Info) {
        db.child(DB_T_I).setValue(info)
    }

    override fun writeMessage(msg: Message, callback: (Result<Message>) -> Unit) {
        val key = db.child(DB_T_M).push().key
        if (key == null) {
            callback(Result.failure(IllegalArgumentException("Не удалось создать ключ для $msg")))
            return
        }

        val msgCopy = msg.copy(id = key)
        val msgValue = msgCopy.toMap()
        val msgUpdates = HashMap<String, Any>()
        val date = dateFormat.format(Date())
        msgUpdates["/$DB_T_M/$userUid/$date/$key"] = msgValue
        db.updateChildren(msgUpdates)
                .addOnSuccessListener { callback(Result.success(msgCopy)) }
                .addOnFailureListener { callback(Result.failure(it)) }
    }

    override fun userUid(): String = userUid
}