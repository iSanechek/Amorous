package com.anonymous.amorous

import com.google.firebase.firestore.*
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun String.Companion.empty() = ""
fun String.toUid(): String = this.hashCode().toString()

const val DB_T_U = "users"
const val DB_T_C = "candidates"
const val DB_T_E = "events"
const val DB_T_M = "messages"
const val DB_T_I = "info"
const val DB_T_F = "folders"

const val WORKER_RETRY_VALUE_KEY = "worker_retry_value"
const val WORKER_FOLDERS_TIME_KEY = "time_for_folders_worker"
const val CANDIDATE_REMOTE_TABLE_KEY = "candidate_remote_table_key"
const val JOBS_SERVICE_STATUS_KEY = "jobs_server_start"

const val USER_EMAIL = "shellhellads012@gmail.com"
const val USER_MESSAGE = "hs888sa7gas9010sad"

suspend fun DocumentReference.awaitAsync(): DocumentSnapshot {
    return suspendCancellableCoroutine { continuation ->
        get().addOnCompleteListener {
            if (it.isSuccessful && it.result != null) {
                continuation.resume(it.result!!)
            } else {
                continuation.resumeWithException(it.exception ?: IllegalStateException())
            }
        }
    }
}

suspend fun DocumentReference.setAwaitAsync(var1: Any) {
    return suspendCancellableCoroutine { continuation ->
        set(var1).addOnCompleteListener {
            if (it.isSuccessful) {
                continuation.resumeWith(Result.success(Unit))
            } else {
                continuation.resumeWith(Result.failure(it.exception ?: IllegalStateException()))
            }
        }
    }
}

suspend fun Query.awaitAsync(): QuerySnapshot {
    return suspendCancellableCoroutine { continuation ->
        get().addOnCompleteListener {
            if (it.isSuccessful && it.result != null) {
                continuation.resume(it.result!!)
            }
        }
    }
}

suspend fun DocumentReference.updateAwaitAsync(var1: Map<String, Any?>) {
    return suspendCancellableCoroutine { continuation ->
        update(var1).addOnCompleteListener {
            if (it.isSuccessful) {
                continuation.resume(Unit)
            } else {
                continuation.resumeWithException(it.exception ?: IllegalStateException())
            }
        }
    }
}


suspend fun CollectionReference.addAwaitAsync(value: Map<String, Any>): DocumentReference {
    return suspendCancellableCoroutine { continuation ->
        add(value).addOnCompleteListener {
            if (it.isSuccessful && it.result != null) {
                continuation.resume(it.result!!)
            } else {
                continuation.resumeWithException(it.exception ?: IllegalStateException())
            }
        }
    }
}
