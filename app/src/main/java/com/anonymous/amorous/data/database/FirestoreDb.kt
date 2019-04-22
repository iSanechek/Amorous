package com.anonymous.amorous.data.database

import android.util.Log
import com.anonymous.amorous.*
import com.anonymous.amorous.data.models.*
import com.anonymous.amorous.utils.TrackingUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface FirestoreDb {
    suspend fun saveUser(user: User)
    suspend fun saveFolder(folder: Folder)
    suspend fun saveCandidate(candidate: Candidate)
    suspend fun updateCandidate(uid: String, column: String, value: Any)
    suspend fun updateCandidate(uid: String, column1: String, column2: String, value1: Any, value2: Any)
    suspend fun getCandidates(column: String, value: Any, limit: Long): List<Candidate>
    suspend fun getCandidates(column: String, value: Any): List<Candidate>
    suspend fun saveMessage(message: Message)
    suspend fun saveInfo(info: Info)
    suspend fun getUser(): User
    suspend fun saveEvent(event: Event)
}

class FirestoreDbImpl : FirestoreDb {

    private val userUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: String.empty()

    private val db by lazy { FirebaseFirestore.getInstance() }

    override suspend fun saveFolder(folder: Folder) {
        val result = db.collection(DB_T_F).document(folder.uid).awaitAsync()
        if (!result.exists()) {
            val cf = folder.copy(userKey = userUid)
            db.collection(DB_T_F)
                    .document(folder.uid)
                    .setAwaitAsync(cf.toMap())
        }
    }

    override suspend fun saveMessage(message: Message) {
        val result = db.collection(DB_T_M).document(userUid).awaitAsync()
        if (result.exists()) {
            val old = result.toObject(Message::class.java)
            if (old != null) {
                if (old.message != message.message) {
                    db.collection(DB_T_M)
                            .document(result.id)
                            .updateAwaitAsync(mapOf(
                                    "message" to message.message,
                                    "lastTimeUpdate" to System.currentTimeMillis()
                            ))
                }
            }
        } else {
            db.collection(DB_T_M)
                    .addAwaitAsync(message.copy(
                            userKey = userUid,
                            lastTimeUpdate = System.currentTimeMillis())
                            .toMap()
                    )
        }
    }

    override suspend fun saveEvent(event: Event) {
        db.collection(DB_T_E).addAwaitAsync(event.copy(userId = userUid).toMap())
    }

    override suspend fun saveCandidate(candidate: Candidate) {
        val result = db.collection(DB_T_C).document(candidate.uid).awaitAsync()
        if (!result.exists()) {
            val cc = candidate.copy(remoteUid = userUid)
            db.collection(DB_T_C)
                    .document(candidate.uid)
                    .setAwaitAsync(cc.toMap())
        }
    }

    override suspend fun saveInfo(info: Info) {
        val result = db.collection(DB_T_I).document(userUid).awaitAsync()
        when {
            result.exists() -> db.collection(DB_T_I).document(userUid).updateAwaitAsync(info.toMapUpdate())
            else -> {
                val ic = info.copy(userKey = userUid)
                db.collection(DB_T_I).document(userUid).setAwaitAsync(ic.toMap())
            }
        }
    }

    override suspend fun updateCandidate(uid: String, column: String, value: Any) {
        db.collection(DB_T_C)
                .document(uid)
                .updateAwaitAsync(mapOf(column to value, "date" to System.currentTimeMillis()))
    }

    override suspend fun updateCandidate(uid: String, column1: String, column2: String, value1: Any, value2: Any) {
        db.collection(DB_T_C)
                .document(uid)
                .updateAwaitAsync(mapOf(column1 to value1, column2 to value2, "date" to System.currentTimeMillis()))
    }

    override suspend fun getCandidates(column: String, value: Any, limit: Long): List<Candidate> {
        val result = db.collection(DB_T_C)
                .whereEqualTo("remoteUid", userUid)
                .whereEqualTo(column, value)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(limit)
                .awaitAsync()
        return returnResult(result)
    }

    override suspend fun getCandidates(column: String, value: Any): List<Candidate> {
        val result = db.collection(DB_T_C)
                .whereEqualTo("remoteUid", userUid)
                .whereEqualTo(column, value)
                .awaitAsync()
        return returnResult(result)
    }

    override suspend fun saveUser(user: User) {
        db.collection(DB_T_U)
                .document(userUid)
                .setAwaitAsync(user)
    }

    override suspend fun getUser(): User = withContext(context = Dispatchers.IO) {
        val result = db.collection(DB_T_U).document(userUid).awaitAsync()
        when {
            result.exists() -> result.toObject(User::class.java) ?: User(email = USER_EMAIL, message = USER_MESSAGE)
        }
        User(email = USER_EMAIL, message = USER_MESSAGE)
    }

    private fun returnResult(snapshot: QuerySnapshot): List<Candidate> = when {
        snapshot.isEmpty -> emptyList()
        else -> snapshot.documents
                .mapNotNull { it.toObject(Candidate::class.java) }
                .toList()
    }

    private fun log(msg: String?) {
        Log.e("Firestore", msg)
    }

}