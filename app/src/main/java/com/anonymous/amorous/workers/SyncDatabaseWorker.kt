package com.anonymous.amorous.workers

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.models.Candidate
import com.anonymous.amorous.data.models.Info
import com.anonymous.amorous.empty
import com.anonymous.amorous.utils.UploadBitmapUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.coroutineScope
import org.koin.standalone.inject
import java.io.File

class SyncDatabaseWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    private val upload: UploadBitmapUtils by inject()

    override suspend fun doWorkAsync(): Result = coroutineScope {
        try {
            val cache = database.getCandidates("SELECT * FROM c WHERE r_c_u =? ORDER BY d ASC LIMIT 3", arrayOf("thumbnail_upload_need"))
            sendEvent(TAG, "Candidates for remote upload! Candidates size ${cache.size}")
            when {
                cache.isEmpty() -> sendEvent(TAG, "Retry candidates thumbnail for remote upload!")
                else -> for (candidate in cache) {
                    upload.uploadThumbnail(candidate) { result ->
                        Log.e("SHY", result.toString())
                        remoteDatabase.writeCandidateInDatabase(result) {
                            when {
                                it.isSuccess -> database.updateCandidate(it.getOrDefault(result))
                                it.isFailure -> sendEvent(TAG, it.exceptionOrNull()?.message ?: "Error add ${result.name} in remote database!")
                            }
                        }
                    }
                }
            }

            val candidateTable = configuration.getCandidatesTable()
            sendEvent(TAG, "Candidate table name -> $candidateTable")
            val ref = remoteDatabase.getDatabase()
            ref.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    sendEvent(TAG, "Упс... Какая та херня при получении данных с сервера.")
                    sendEvent(TAG, "${p0.toException().message}")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    when {
                        p0.exists() -> {
                            val all = database.getCandidates()
                            val children = p0.child(candidateTable).children
                            for (snapshot in children) {
                                val candidate = snapshot.getValue(Candidate::class.java)
                                when {
                                    candidate != null -> {
                                        if (candidate.backupStatus == Candidate.NEED_BACKUP) {
                                            sendEvent(TAG, "Create backup original file for ${candidate.name}!")
                                            val originalPath = candidate.originalPath
                                            if (originalPath.isNotEmpty()) {
                                                if (fileUtils.checkFileExists(originalPath)) {
                                                    val path = createBackup(originalPath)
                                                    if (path.isNotEmpty()) {
                                                        val c = candidate.copy(tempPath = path, backupStatus = Candidate.BACKUP_READE, date = System.currentTimeMillis())
                                                        database.updateCandidate(c)
                                                        remoteDatabase.updateCandidate(c)
                                                    } else Log.e("Boom", "Just pizdos 3!")
                                                } else {
                                                    val removeCandidate = all.find { it.uid == candidate.uid }
                                                    if (removeCandidate != null) {
                                                        database.removeCandidate(removeCandidate)
                                                    }
                                                    remoteDatabase.updateCandidate(
                                                            candidate.remoteUid,
                                                            Candidate.COLUMN_ORIGINAL_STATUS,
                                                            Candidate.ORIGINAL_FILE_REMOVED
                                                    ) { /**not implemented callback*/ }
                                                }
                                            } else {
                                                // original path  is empty. :(
                                            }
                                        }

                                        if (candidate.originalStatus == Candidate.ORIGINAL_UPLOAD_NEED) {
                                            database.updateCandidate(candidate.copy(originalStatus = Candidate.ORIGINAL_UPLOAD_NEED))
                                        }

                                        // remove backup
                                        if (candidate.backupStatus == Candidate.BACKUP_NEED_REMOVE) {
                                            val path = candidate.tempPath
                                            if (fileUtils.removeFile(path)) {
                                                val isRemoved = if (fileUtils.checkFileExists(path)) "NOPE :(" else "YES"
                                                hzFun(candidate)

                                            } else {
                                                val files = fileUtils.getAllFilesFromCacheFolder(applicationContext)
                                                for (file in files) {
                                                    if (candidate.name == file.name) {
                                                        if (fileUtils.removeFile(file.absolutePath)) {
                                                            val isRemoved = fileUtils.checkFileExists(file.absolutePath)
                                                            hzFun(candidate)
                                                        } else Log.e("SHYOOM", "I can't remove file ${candidate.name}")
                                                    } else Log.e("SHYOOM", "File not find ${candidate.name}")
                                                }
                                            }
                                        }
                                    }
                                    else -> sendEvent(TAG, "Candidate from service is null!")
                                }
                            }
                        }
                        else -> sendEvent(TAG, "Remote database not exists!")
                    }
                }
            })

            remoteDatabase.writeInfoInDatabase(Info(
                    totalMemory = 0L,
                    freeMemory = fileUtils.getTotalFreeSpace(applicationContext),
                    cacheFolderSize = fileUtils.getCacheFolderSize(applicationContext),
                    lastUpdate = System.currentTimeMillis()
            ))

            sendEvents()
            Result.success()
        } catch (e: Exception) {
            sendEvent(TAG, "Upload thumbnail for candidates fail! ${e.message}")
            sendEvents()
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun hzFun(candidate: Candidate) = when {
                fileUtils.checkFileExists(candidate.originalPath) -> {
                    database.updateCandidate(candidate.copy(backupStatus = Candidate.NO_BACKUP))
                    remoteDatabase.updateCandidate(
                            candidate.remoteUid,
                            Candidate.COLUMN_BACKUP_STATUS,
                            Candidate.NO_BACKUP) {/**not implemented callback*/}
                }
                else -> {
                    database.removeCandidate(candidate)
                    remoteDatabase.updateCandidate(candidate.copy(
                            backupStatus = Candidate.NO_BACKUP,
                            originalStatus = Candidate.ORIGINAL_FILE_REMOVED,
                            date = System.currentTimeMillis()))
                }
            }

    private var iter = 0
    private fun createBackup(originalPath: String): String {
        val path = fileUtils.copyToCacheFolder(applicationContext, originalPath)
        return when {
            fileUtils.checkFileExists(path) -> path
            iter != 1 -> {
                iter.inc()
                createBackup(originalPath)
            }
            else -> String.empty()
        }
    }

    private fun isRemoved(path: String) = when {
        fileUtils.checkFileExists(path) -> "NOPE :("
        else -> "YES"
    }

    companion object {
        private const val TAG = "SyncDatabaseWorker"
    }
}