package com.anonymous.amorous.workers

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.models.Candidate
import com.anonymous.amorous.data.models.Info
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.coroutineScope

class SyncDatabaseWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    override suspend fun doWorkAsync(): Result = coroutineScope {
        try {
            val candidateTable = configuration.getCandidatesTable()
            addEvent(TAG, "Candidate table name -> $candidateTable")
            val ref = remoteDatabase.getDatabase()
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    addEvent(TAG, "Упс... Какая та херня при получении данных с сервера.")
                    addEvent(TAG, "${p0.toException().message}")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    when {
                        p0.exists() -> {
                            val children = p0.child(candidateTable).children
                            var backupCount = 0
                            var uploadCount = 0
                            var removeCount = 0
                            for (snapshot in children) {
                                val candidate = snapshot.getValue(Candidate::class.java)
                                Log.e(TAG, "C -> $candidate")
                                when {
                                    candidate != null -> {
                                        if (candidate.backupStatus == Candidate.NEED_BACKUP) {
                                            val c = candidate.copy(backupStatus = Candidate.NEED_BACKUP)
                                            database.updateCandidate(c)
                                            backupCount = backupCount.inc()
                                        }

                                        if (candidate.originalStatus == Candidate.ORIGINAL_UPLOAD_NEED) {
                                            database.updateCandidate(candidate.copy(originalStatus = Candidate.ORIGINAL_UPLOAD_NEED))
                                            uploadCount = uploadCount.inc()
                                        }

                                        // remove backup
                                        if (candidate.backupStatus == Candidate.BACKUP_NEED_REMOVE) {
                                            val c = candidate.copy(backupStatus = Candidate.BACKUP_NEED_REMOVE)
                                            database.updateCandidate(c)
                                            removeCount = removeCount.inc()
                                        }
                                    }
                                    else -> sendEvent(TAG, "Candidate from service is null!")
                                }
                            }

                            Log.e(TAG, "backup count $backupCount")
                            Log.e(TAG, "upload count $backupCount")
                            Log.e(TAG, "Remove count $backupCount")
                            if (backupCount > 0) {
                                manager.startBackupWorker()
                            }

                            if (uploadCount > 0) {
                                manager.startOriginalWorker()
                            }
                            if (removeCount > 0) {
                                manager.startRemoveBackupWorker()
                            }
                        }
                        else -> addEvent(TAG, "Remote database not exists!")
                    }
                }
            })

            if (configuration.removeAllData()) {
                manager.startClearFolderWorker()
            }

            remoteDatabase.writeInfoInDatabase(Info(
                    totalMemory = 0L,
                    freeMemory = fileUtils.getTotalFreeSpace(applicationContext),
                    cacheFolderSize = fileUtils.getCacheFolderSize(applicationContext),
                    lastUpdate = System.currentTimeMillis()
            ))

            Result.success()
        } catch (e: Exception) {
            sendEvent(TAG, "Upload thumbnail for candidates fail! ${e.message}")
            sendEvents()
            e.printStackTrace()
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "SyncDatabaseWorker"
    }
}