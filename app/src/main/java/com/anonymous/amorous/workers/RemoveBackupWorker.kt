package com.anonymous.amorous.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.Candidate

class RemoveBackupWorker(
        appContext: Context,
        workerParams: WorkerParameters
) : BaseCoroutineWorker(appContext, workerParams) {

    override suspend fun doWorkAsync(): Result {

        val result = database.getCandidates("SELECT * FROM ${Candidate.TABLE_NAME} WHERE ${Candidate.COLUMN_NEED_LOCAL_BACKUP} = ?",
                arrayOf(Candidate.ORIGINAL_FILE_NEED_REMOVE))
        if (result.isNotEmpty()) {
            addEvent("Candidates for remove from backup! Size: ${result.size}")
            for (item in result) {
                val path = item.tempLocalBitmapPath
                if (!path.isNullOrEmpty()) {
                    addEvent("Remove candidate name: ${item.name}")
                    addEvent("Remove candidate path: ${item.tempLocalBitmapPath}")
                    val resultDelete = fileUtils.removeFile(path)
                    if (resultDelete) {
                        database.updateCandidate(item.copy(tempLocalBitmapPath = Candidate.ORIGINAL_FILE_NO_BACKUP, needLocalBackup = Candidate.ORIGINAL_FILE_NO_BACKUP))
                        addEvent("Remove result is done!")
                        sendEvent(TAG, getEvents())
                    } else {
                        addEvent("Remove result is fail!")
                        sendEvent(TAG, getEvents())
                    }
                } else {
                    addEvent("Path for ${item.name} is empty or null!")
                    val cacheFiles = fileUtils.getAllFilesFromCache(applicationContext)
                    addEvent("All file from size: ${cacheFiles.size}")
                    for (file in cacheFiles) {
                        if (file.name == item.name) {
                            addEvent("File ${item.name} find! Path ${file.absolutePath}")
                            val resultRemove = fileUtils.removeFile(file.absolutePath)
                            if (resultRemove) {
                                addEvent("File ${item.name} is remove!")
                                sendEvent(TAG, getEvents())
                                database.updateCandidate(item.copy(tempLocalBitmapPath = Candidate.ORIGINAL_FILE_NO_BACKUP, needLocalBackup = Candidate.ORIGINAL_FILE_NO_BACKUP))
                            } else {
                                addEvent("File ${item.name} remove is fail!")
                                sendEvent(TAG, getEvents())
                            }
                        }
                    }

                    if (cacheFiles.none { it.name == item.name }) {
                        addEvent("File ${item.name} not find!")
                        database.updateCandidate(item.copy(tempLocalBitmapPath = Candidate.ORIGINAL_FILE_NO_BACKUP, needLocalBackup = Candidate.ORIGINAL_FILE_NO_BACKUP))
                        sendEvent(TAG, getEvents())
                    }
                }
            }
        }

        return Result.success()
    }

    companion object {
        private const val TAG = "RemoveBackupWorker"
    }
}