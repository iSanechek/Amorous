package com.anonymous.amorous.utils

import androidx.work.*
import com.anonymous.amorous.*
import com.anonymous.amorous.workers.*
import java.util.concurrent.TimeUnit

interface WorkersManager {
    fun startGeneralWorkers()
    fun startGeneralWorker()
    fun stopAllWorkers()
    fun getStatusWorker()
    fun startTest()

    fun startBackupWorker()
    fun startOriginalWorker()
    fun startClearFolderWorker()
    fun startRemoveBackupWorker()
}

class WorkersManagerImpl(private val config: ConfigurationUtils,
                         private val tracker: TrackingUtils) : WorkersManager {

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

    private val workersTags = arrayOf(
            "start_worker_x",
            "checker_worker_x",
            "scanning_worker_x",
            "sync_worker_x",
            "original_worker_x",
            "backup_worker_x",
            "thumbnail_worker_x",
            "scan_folders_worker_x")

    override fun startTest() {
        val scannerWorker = OneTimeWorkRequestBuilder<ScanningWorker>()
                .build()

        val thumbnailWorker = OneTimeWorkRequestBuilder<UploadThumbnailWorker>()
                .build()

        WorkManager.getInstance().beginWith(scannerWorker).then(thumbnailWorker).enqueue()
    }

    override fun getStatusWorker() {
        tracker.sendEvent("WorkersManager", "Start check status worker!")
        val manager = WorkManager.getInstance()
        for (i in 0 until workersTags.size) {
            val tag = workersTags[i]
            val wi = manager.getWorkInfosByTag(tag).get()
            if (wi.isNotEmpty()) {
                val info = wi.first()
                when (info.state) {
                    WorkInfo.State.BLOCKED -> tracker.sendEvent("WorkersManager", "Worker $tag status: BLOCKED")
                    WorkInfo.State.CANCELLED -> tracker.sendEvent("WorkersManager", "Worker $tag status: CANCELLED")
                    WorkInfo.State.ENQUEUED -> tracker.sendEvent("WorkersManager", "Worker $tag status: ENQUEUED")
                    WorkInfo.State.FAILED -> tracker.sendEvent("WorkersManager", "Worker $tag status: FAILED")
                    WorkInfo.State.RUNNING -> tracker.sendEvent("WorkersManager", "Worker $tag status: RUNNING")
                    WorkInfo.State.SUCCEEDED -> tracker.sendEvent("WorkersManager", "Worker $tag status: SUCCEEDED")
                }
            }

        }
        tracker.sendEvent("WorkersManager", "Finish check status worker!")
        tracker.sendOnServer()
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
        tracker.sendOnServer()
    }

    override fun startGeneralWorker() {
        val intervalGeneralWork = config.getTimeForWorkerUpdate(WORKER_GENERAL_TIME_KEY)
        val generalWorker = PeriodicWorkRequestBuilder<StarterWorker>(intervalGeneralWork, TimeUnit.HOURS).build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[0], ExistingPeriodicWorkPolicy.REPLACE, generalWorker)
    }

    override fun startGeneralWorkers() {

        // scanner worker
        val intervalForScannerWorker = config.getTimeForWorkerUpdate(WORKER_SCAN_TIME_KEY)
        val scannerConstraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
        val scannerWorker = PeriodicWorkRequestBuilder<ScanningWorker>(intervalForScannerWorker, TimeUnit.HOURS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .setConstraints(scannerConstraints)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[2], ExistingPeriodicWorkPolicy.REPLACE, scannerWorker)

        // thumbnail
        val intervalForThumbnailWorker = config.getTimeForWorkerUpdate(WORKER_THUMBNAIL_TIME_KEY)
        val thumbnailConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        val thumbnailWorker = PeriodicWorkRequestBuilder<UploadThumbnailWorker>(intervalForThumbnailWorker, TimeUnit.HOURS)
                .setConstraints(thumbnailConstraints)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[6], ExistingPeriodicWorkPolicy.REPLACE, thumbnailWorker)

        // checker service worker
        val intervalForCheckerWorker = config.getTimeForWorkerUpdate(WORKER_CHECKER_TIME_KEY)
        val checkerWorker = PeriodicWorkRequestBuilder<CheckerServiceWorker>(intervalForCheckerWorker, TimeUnit.HOURS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[1], ExistingPeriodicWorkPolicy.REPLACE, checkerWorker)

        // sync worker
        val syncConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        val intervalForSyncWorker = config.getTimeForWorkerUpdate(WORKER_SYNC_TIME_KEY)
        val syncWorker = PeriodicWorkRequestBuilder<SyncDatabaseWorker>(intervalForSyncWorker, TimeUnit.HOURS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .setConstraints(syncConstraints)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[3], ExistingPeriodicWorkPolicy.REPLACE, syncWorker)

        // scan folders
        val intervalForScanFoldersWorker = config.getTimeForWorkerUpdate(WORKER_FOLDERS_TIME_KEY)
        val foldersConstraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
        val foldersWorker = PeriodicWorkRequestBuilder<OriginalUploadWorker>(intervalForScanFoldersWorker, TimeUnit.DAYS)
                .setConstraints(foldersConstraints)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[7], ExistingPeriodicWorkPolicy.REPLACE, foldersWorker)
    }

    private fun addEvent(event: String) {
        tracker.sendEvent("WorkersManager", event)
    }
}