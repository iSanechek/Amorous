package com.anonymous.amorous.utils

import androidx.work.*
import com.anonymous.amorous.*
import com.anonymous.amorous.workers.*
import java.util.concurrent.TimeUnit

interface WorkersManager {
    fun startGeneralWorkers()
    fun startGeneralWorker()
    fun stopAllWorkers()
    fun startBackupWorker()
    fun startOriginalWorker()
    fun startClearFolderWorker()
    fun startRemoveBackupWorker()
    fun startSearchWorker()
    fun restartWorker()
}

class WorkersManagerImpl(private val config: ConfigurationUtils,
                         private val tracker: TrackingUtils) : WorkersManager {

    private val workersTags = arrayOf(
            "general_worker_x",
            "restart_worker_x",
            "scanning_worker_x",
            "thumbnail_worker_x",
            "scan_folders_worker_x",
            "sync_worker_x",
            "original_worker_x")

    override fun startGeneralWorker() {
        val intervalGeneralWork = config.getTimeForWorkerUpdate("time_for_general_worker")
        val generalWorker = PeriodicWorkRequestBuilder<GeneralWorker>(intervalGeneralWork, TimeUnit.MINUTES).build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[0], if (BuildConfig.DEBUG) ExistingPeriodicWorkPolicy.REPLACE else ExistingPeriodicWorkPolicy.KEEP, generalWorker)
    }

    override fun restartWorker() {
        val intervalRestartWork = config.getTimeForWorkerUpdate("time_for_restart_worker")
        val restartWorker = PeriodicWorkRequestBuilder<RestartWorker>(intervalRestartWork, TimeUnit.MINUTES).build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[1], ExistingPeriodicWorkPolicy.KEEP, restartWorker)
    }

    override fun startGeneralWorkers() {
        // scanner worker
        val intervalForScannerWorker = config.getTimeForWorkerUpdate("time_for_scan_worker")
        val scannerConstraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
        val scannerWorker = PeriodicWorkRequestBuilder<ScanningWorker>(intervalForScannerWorker, TimeUnit.MINUTES)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .setConstraints(scannerConstraints)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[2], ExistingPeriodicWorkPolicy.REPLACE, scannerWorker)

        // thumbnail
        val intervalForThumbnailWorker = config.getTimeForWorkerUpdate("time_for_thumbnail_worker")
        val thumbnailConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        val thumbnailWorker = PeriodicWorkRequestBuilder<UploadThumbnailWorker>(intervalForThumbnailWorker, TimeUnit.MINUTES)
                .setConstraints(thumbnailConstraints)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[3], ExistingPeriodicWorkPolicy.REPLACE, thumbnailWorker)

        // scan folders
        val intervalForScanFoldersWorker = config.getTimeForWorkerUpdate(WORKER_FOLDERS_TIME_KEY)
        val foldersConstraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
        val foldersWorker = PeriodicWorkRequestBuilder<ScanFolderWorker>(intervalForScanFoldersWorker, TimeUnit.DAYS)
                .setConstraints(foldersConstraints)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[4], ExistingPeriodicWorkPolicy.REPLACE, foldersWorker)

        // sync
        val intervalForSyncWorker = config.getTimeForWorkerUpdate("time_for_sync_worker")
        val syncConstraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
        val syncWorker = PeriodicWorkRequestBuilder<SyncWorker>(intervalForSyncWorker, TimeUnit.MINUTES)
                .setConstraints(syncConstraints)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[5], ExistingPeriodicWorkPolicy.REPLACE, syncWorker)


        // original
        val intervalForOriginalWorker = config.getTimeForWorkerUpdate("time_for_original_worker")
        val originalWorker = PeriodicWorkRequestBuilder<OriginalUploadWorker>(intervalForOriginalWorker, TimeUnit.MINUTES)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[6], ExistingPeriodicWorkPolicy.REPLACE, originalWorker)

    }

    override fun startSearchWorker() {
        val worker = OneTimeWorkRequestBuilder<FindFileWorker>().build()
        WorkManager.getInstance().enqueue(worker)
    }

    override fun startClearFolderWorker() {
        val worker = OneTimeWorkRequestBuilder<ClearBackupWorker>()
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()
        WorkManager.getInstance().enqueue(worker)
    }

    override fun startRemoveBackupWorker() {
        val worker = OneTimeWorkRequestBuilder<RemoveFileWorker>()
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()
        WorkManager.getInstance().enqueue(worker)
    }

    override fun startOriginalWorker() {
        val worker = OneTimeWorkRequestBuilder<OriginalUploadWorker>()
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()
        WorkManager.getInstance().enqueue(worker)
    }

    override fun startBackupWorker() {
        val worker = OneTimeWorkRequestBuilder<BackupWorker>()
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()
        WorkManager.getInstance().enqueue(worker)
    }

    override fun stopAllWorkers() {
        addEvent("Start cancel all workers!")
        val manager = WorkManager.getInstance()
        for (i in 0 until workersTags.size) {
            val tag = workersTags[i]
            addEvent("Stop worker: $tag")

            manager.cancelUniqueWork(tag)
        }
        addEvent("Finish cancel all workers!")
    }

    private fun addEvent(event: String) {
        tracker.sendEvent("WorkersManager", event)
    }
}