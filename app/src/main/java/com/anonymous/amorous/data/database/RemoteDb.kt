package com.anonymous.amorous.data.database

import com.anonymous.amorous.*
import com.anonymous.amorous.data.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume

interface RemoteDb {
    suspend fun saveUser(user: User)
    suspend fun saveFolder(folder: Folder)
    suspend fun saveCandidate(candidate: Candidate)
    suspend fun updateCandidate(uid: String, column: String, value: Any)
    suspend fun updateCandidate(uid: String, column1: String, column2: String, value1: Any, value2: Any)
    suspend fun getCandidates(column: String, value: Any, limit: Long): List<Candidate>
    suspend fun getCandidates(column: String, value: Any): List<Candidate>
    suspend fun getCandidates(): List<Candidate>
    suspend fun saveInfo(info: Info)
    suspend fun getUser(isAuth: Boolean): User
    suspend fun saveEvent(event: Event)
    suspend fun getCommand(): Command
    suspend fun updateCommand(date: Long)
}

class RemoteDbImpl : RemoteDb {

    private val userUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: String.empty()

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val db2 by lazy { FirebaseDatabase.getInstance().reference }
    private val dateFormat by lazy { SimpleDateFormat("d_MMM_yyyy", Locale.US) }

    override suspend fun saveFolder(folder: Folder): Unit = suspendCancellableCoroutine { c ->
        val fc = folder.copy(userUid = userUid)
        db2.child(DB_T_F).child(fc.uid).setValue(fc.toMap())
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        c.resume(Unit)
                    } else {
                        c.resume(Unit)
                    }
                }.addOnFailureListener {
                    c.resume(Unit)
                }
    }

    override suspend fun saveEvent(event: Event): Unit = suspendCancellableCoroutine { c ->
        val ec = event.copy(userUid = userUid)
        val date = dateFormat.format(Date())
        val table = "$date/$DB_T_E"
        val key = db2.child(table).push().key
        if (key == null) {
            c.resume(Unit)
        } else {
            db2.child(table).child(key).setValue(ec.toMap())
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            c.resume(Unit)
                        } else {
                            c.resume(Unit)
                        }
                    }.addOnFailureListener {
                        c.resume(Unit)
                    }
        }
    }

    override suspend fun getCommand(): Command = withContext(Dispatchers.IO) {
        db.collection(DB_T_CS).document(userUid).awaitAsync().toObject(Command::class.java) ?: Command(command = "command_upload_original", date = System.currentTimeMillis())
    }

    override suspend fun updateCommand(date: Long) = withContext(Dispatchers.IO) {
        val command = db.collection(DB_T_CS).document(userUid).awaitAsync()
        when {
            !command.exists() -> db.collection(DB_T_CS).document(userUid).setAwaitAsync(Command(date = 0L, userUid = userUid, haveNewCommand = 0L))
            else -> db.collection(DB_T_CS).document(userUid).updateAwaitAsync(mapOf("date" to date, "haveNewCommand" to 0))
        }
    }

    override suspend fun saveCandidate(candidate: Candidate) = withContext(Dispatchers.IO) {
        val cc = candidate.copy(remoteUid = userUid)
        db.collection(DB_T_C)
                .document(candidate.uid)
                .setAwaitAsync(cc.toMap())
    }

    override suspend fun saveInfo(info: Info) = withContext(Dispatchers.IO) {
        val i = db.collection(DB_T_I).document(userUid).awaitAsync()
        when {
            i.exists() -> db.collection(DB_T_I).document(userUid).updateAwaitAsync(info.toMapUpdate())
            else -> db.collection(DB_T_I).document(userUid).setAwaitAsync(info.copy(userKey = userUid).toMap())
        }
    }

    override suspend fun updateCandidate(uid: String, column: String, value: Any) = withContext(Dispatchers.IO) {
        db.collection(DB_T_C)
                .document(uid)
                .updateAwaitAsync(mapOf(column to value, "date" to System.currentTimeMillis()))
    }

    override suspend fun updateCandidate(uid: String, column1: String, column2: String, value1: Any, value2: Any) = withContext(Dispatchers.IO) {
        db.collection(DB_T_C)
                .document(uid)
                .updateAwaitAsync(mapOf(column1 to value1, column2 to value2, "date" to System.currentTimeMillis()))
    }

    override suspend fun getCandidates(column: String, value: Any, limit: Long): List<Candidate> = withContext(Dispatchers.IO) {
        val result = db.collection(DB_T_C)
                .whereEqualTo("remoteUid", userUid)
                .whereEqualTo(column, value)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(limit)
                .awaitAsync()
        returnResult(result)
    }

    override suspend fun getCandidates(column: String, value: Any): List<Candidate> = withContext(Dispatchers.IO) {
        val result = db.collection(DB_T_C)
                .whereEqualTo("remoteUid", userUid)
                .whereEqualTo(column, value)
                .awaitAsync()
        returnResult(result)
    }

    override suspend fun getCandidates(): List<Candidate> = withContext(Dispatchers.IO) {
        returnResult(db.collection(DB_T_C).whereEqualTo("remoteUid", userUid).awaitAsync())
    }

    override suspend fun saveUser(user: User) {
        db.collection(DB_T_U)
                .document(userUid)
                .setAwaitAsync(user)
    }

    override suspend fun getUser(isAuth: Boolean): User = withContext(context = Dispatchers.IO) {
        if (!isAuth) {
            User(email = USER_EMAIL, message = USER_MESSAGE)

        } else {
            val result = db.collection(DB_T_U).document(userUid).awaitAsync()
            when {
                result.exists() -> result.toObject(User::class.java) ?: User(email = USER_EMAIL, message = USER_MESSAGE)
                else -> User(email = USER_EMAIL, message = USER_MESSAGE)
            }
        }
    }

    private fun returnResult(snapshot: QuerySnapshot): List<Candidate> = when {
        snapshot.isEmpty -> emptyList()
        else -> snapshot.documents
                .mapNotNull { it.toObject(Candidate::class.java) }
                .toList()
    }
}