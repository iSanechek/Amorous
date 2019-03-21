package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.DB_T_U
import com.anonymous.amorous.data.Candidate
import com.anonymous.amorous.utils.UploadBitmapUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.coroutineScope
import org.koin.standalone.inject

class SyncDatabaseWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    private val upload: UploadBitmapUtils by inject()

    override suspend fun doWorkAsync(): Result = coroutineScope {
        val cache = database.getCandidates("SELECT * FROM c WHERE r_c_u =? ORDER BY d ASC LIMIT 10", arrayOf("thumbnail_upload_need"))
        addEvent("Candidates for remote upload! Candidates size ${cache.size}")
        try {
            when {
                cache.isEmpty() -> addEvent("Retry candidates thumbnail for remote upload!")
                else -> for (candidate in cache) {
                    addEvent("Upload thumbnail for candidate ${candidate.name}")
                    upload.uploadBitmap(candidate) {
                        database.updateCandidate(it)
                        remoteDatabase.writeCandidateInDatabase("", candidate)
                    }
                }
            }

            val userUid = remoteDatabase.userUid()
            if (userUid.isEmpty()) {

                Result.success()
            }
            val ref = remoteDatabase.getDatabase().child(DB_T_U).child(userUid)
            ref.addValueEventListener(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    addEvent("Упс... Какая та херня при получении данных с сервера.")
                    addEvent("${p0.toException().message}")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    val all = database.getCandidates()
                    for (snapshot in p0.children) {
                        val candidate = snapshot.getValue(Candidate::class.java)
                        when {
                            candidate != null -> when {
                                all.isEmpty() -> database.saveCandidate(candidate)
                                else -> {
                                    val item = all.find { it.uid == candidate.uid }
                                    when {
                                        item != null -> database.updateCandidate(item)
                                        else -> database.saveCandidate(candidate)
                                    }
                                }
                            }
                            else -> addEvent("Candidate from service is null!")
                        }
                    }
                }
            })
            sendEvent(TAG, getEvents())
            Result.success()
        } catch (e: Exception) {
            addEvent("Upload thumbnail for candidates fail! ${e.message}")
            sendEvent(TAG, getEvents())
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "SyncDatabaseWorker"
    }
}