package com.anonymous.amorous.workers

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.models.Candidate
import com.anonymous.amorous.data.models.Info
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class SyncDatabaseWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    override suspend fun workAction(): Result = coroutineScope {
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
                            var searchCount = 0
                            for (snapshot in children) {
                                val candidate = snapshot.getValue(Candidate::class.java)
                                when {
                                    candidate != null -> {
                                        if (candidate.backupStatus == Candidate.NEED_BACKUP) {
//                                            val c = candidate.copy(backupStatus = Candidate.NEED_BACKUP)
//                                            database.updateCandidate(c)
                                            backupCount = backupCount.inc()
                                        }

                                        if (candidate.originalStatus == Candidate.ORIGINAL_UPLOAD_NEED) {
//                                            database.updateCandidate(candidate.copy(originalStatus = Candidate.ORIGINAL_UPLOAD_NEED))
                                            uploadCount = uploadCount.inc()
                                        }

                                        // remove backup
                                        if (candidate.backupStatus == Candidate.BACKUP_NEED_REMOVE) {
//                                            val c = candidate.copy(backupStatus = Candidate.BACKUP_NEED_REMOVE)
//                                            database.updateCandidate(c)
                                            removeCount = removeCount.inc()
                                        }
                                        if (candidate.originalStatus == Candidate.ORIGINAL_FILE_NEED_SEARCH) {
                                            searchCount = searchCount.inc()
                                        }

                                        GlobalScope.launch(Dispatchers.IO) { database.updateCandidate(candidate) }
                                    }
                                    else -> addEvent(TAG, "Candidate from service is null!")
                                }
                            }

                            if (backupCount > 0) {
                                manager.startBackupWorker()
                            }
                            if (uploadCount > 0) {
                                manager.startOriginalWorker()
                            }
                            if (removeCount > 0) {
                                manager.startRemoveBackupWorker()
                            }
                            if (searchCount > 0) {
                                manager.startSearchWorker()
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
            val retryCount = pref.getWorkerRetryCountValue(TAG)
            if (retryCount < configuration.getWorkerRetryCount()) {
                val value = retryCount.inc()
                pref.updateWorkerRetryCountValue(TAG, value)
                addEvent(TAG, "При синхронизации что-то пошло не так. :(! ${e.message}. Повторная попатка номер $value")
                Result.retry()
            } else {
                addEvent(TAG, "Синхронизация закончилась хуева! ${e.message} $retryCount")
                pref.updateWorkerRetryCountValue(TAG, 0)
                Result.failure()
            }
        }
    }

    companion object {
        private const val TAG = "SyncDatabaseWorker"
    }
}