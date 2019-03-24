package com.anonymous.amorous.workers

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.models.Candidate
import com.anonymous.amorous.empty
import com.anonymous.amorous.utils.UploadBitmapUtils
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
        try {
            val cache = database.getCandidates("SELECT * FROM c WHERE r_c_u =? ORDER BY d ASC LIMIT 10", arrayOf("thumbnail_upload_need"))
            addEvent("Candidates for remote upload! Candidates size ${cache.size}")
//            when {
//                cache.isEmpty() -> addEvent("Retry candidates thumbnail for remote upload!")
//                else -> for (candidate in cache) {
//                    addEvent("Upload thumbnail for candidate ${candidate.name}")
//                    upload.uploadBitmap(candidate) {
//                        val c = it.copy(thumbnailStatus = Candidate.THUMBNAIL_UPLOAD_DONE)
//                        database.updateCandidate(c)
//                        remoteDatabase.writeCandidateInDatabase("", c)
//                    }
//                }
//            }

            val candidateTable = configuration.getCandidatesTable()
            addEvent("Candidate table $candidateTable")
            val ref = remoteDatabase.getDatabase()
            ref.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    addEvent("Упс... Какая та херня при получении данных с сервера.")
                    addEvent("${p0.toException().message}")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    when {
                        p0.exists() -> {
                            val all = database.getCandidates()
                            for (snapshot in p0.child(candidateTable).children) {
                                val candidate = snapshot.getValue(Candidate::class.java)
                                when {
                                    candidate != null -> when {
                                        all.isEmpty() -> database.saveCandidate(candidate)
                                        else -> {
                                            Log.d("Boom", "${candidate.originalStatus}")
                                            Log.d("Boom", "${candidate.name}")
                                            database.updateCandidate(candidate.uid!!, Candidate.COLUMN_ORIGINAL_STATUS, candidate.originalStatus!!)
                                            when {
                                                candidate.backupStatus == Candidate.NEED_BACKUP -> {
                                                    addEvent("Create backup original file!")
                                                    val originalPath = candidate.tempPath
                                                    if (originalPath != null) {
                                                        if (fileUtils.checkFileExists(originalPath)) {
                                                            val path = createBackup(originalPath)
                                                            if (path.isNotEmpty()) {
                                                                database.updateCandidate(candidate.copy(tempPath = path, date = System.currentTimeMillis()))
                                                                addEvent("Create backup is done!")
                                                                addEvent("Create backup path: $path")
                                                            } else {
                                                                addEvent("Create backup for ${candidate.name} fail!")
                                                            }
                                                        } else {
                                                            addEvent("Original file not exists!")
                                                            val removeCandidate = all.find { it.uid == candidate.uid }
                                                            if (removeCandidate != null) {
                                                                database.removeCandidate(candidate)
                                                            }
                                                            remoteDatabase.updateCandidate(candidate.copy(originalStatus = Candidate.ORIGINAL_FILE_REMOVED))
                                                        }
                                                    } else {
                                                        addEvent("Original path is null or empty!")
                                                        addEvent("Search file from name!")
                                                        val cacheFiles = fileUtils.getAllFilesFromCacheFolder(applicationContext)
                                                        addEvent("All file from cache folder! Size: ${cacheFiles.size}")
                                                        for (file in cacheFiles) {
                                                            if (file.name == candidate.name) {
                                                                addEvent("File ${candidate.name} find! Path ${file.absolutePath}")
                                                                val resultRemove = fileUtils.removeFile(file.absolutePath)
                                                                if (resultRemove) {
                                                                    addEvent("File ${candidate.name} is remove!")
                                                                    sendEvent(TAG, getEvents())
                                                                    database.updateCandidate(candidate.copy(tempPath = Candidate.NO_BACKUP, backupStatus = Candidate.NO_BACKUP))
                                                                } else {
                                                                    addEvent("File ${candidate.name} remove is fail!")
                                                                    sendEvent(TAG, getEvents())
                                                                }
                                                            }
                                                        }
                                                        if (cacheFiles.none { it.name == candidate.name }) {
                                                            addEvent("File ${candidate.name} not find!")
                                                            database.updateCandidate(candidate.copy(tempPath = Candidate.NO_BACKUP, backupStatus = Candidate.NO_BACKUP))
                                                            sendEvent(TAG, getEvents())
                                                        }
                                                    }

                                                }
                                                candidate.backupStatus == Candidate.BACKUP_NEED_REMOVE -> {
                                                    addEvent("Remove backup actions!")
                                                    val path = candidate.tempPath ?: String.empty()
                                                    when {
                                                        path.isNotEmpty() -> if (fileUtils.checkFileExists(path)) {
                                                            if (fileUtils.removeFile(path)) {
                                                                addEvent("Remove backup for ${candidate.name} is done!")
                                                                if (!fileUtils.checkFileExists(candidate.originalPath ?: String.empty())) {
                                                                    val removeCandidate = all.find { it.uid == candidate.uid }
                                                                    if (removeCandidate != null) {
                                                                        database.removeCandidate(candidate)
                                                                    }
                                                                    remoteDatabase.updateCandidate(candidate.copy(originalStatus = Candidate.ORIGINAL_FILE_REMOVED, date = System.currentTimeMillis()))
                                                                } else {
                                                                    remoteDatabase.updateCandidate(candidate.copy(backupStatus = Candidate.NO_BACKUP))
                                                                }
                                                            }
                                                        }
                                                        else -> addEvent("Backup path for ${candidate.name} is empty or null!")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    else -> addEvent("Candidate from service is null!")
                                }
                            }
                        }
                        else -> addEvent("Remote database not exists!")
                    }
                }
            })

            //

            sendEvent(TAG, getEvents())
            Result.success()
        } catch (e: Exception) {
            addEvent("Upload thumbnail for candidates fail! ${e.message}")
            sendEvent(TAG, getEvents())
            Result.failure()
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

    companion object {
        private const val TAG = "SyncDatabaseWorker"
    }
}