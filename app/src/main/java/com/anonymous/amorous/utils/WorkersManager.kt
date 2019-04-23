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
            "original_worker_x",
            "check_service_worker_x",
            "upload_large_worker_x")

    override fun startGeneralWorker() {
        val generalConstraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        val intervalGeneralWork = config.getTimeForWorkerUpdate("time_for_general_worker")
        val generalWorker = PeriodicWorkRequestBuilder<GeneralWorker>(intervalGeneralWork, TimeUnit.MINUTES)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .setConstraints(generalConstraints)
                .build()
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
                .setRequiredNetworkType(NetworkType.CONNECTED)
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
                .setRequiresDeviceIdle(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        val foldersWorker = PeriodicWorkRequestBuilder<ScanFolderWorker>(intervalForScanFoldersWorker, TimeUnit.DAYS)
                .setConstraints(foldersConstraints)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[4], ExistingPeriodicWorkPolicy.REPLACE, foldersWorker)

        // sync
        val intervalForSyncWorker = config.getTimeForWorkerUpdate("time_for_sync_worker")
        val syncConstraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        val syncWorker = PeriodicWorkRequestBuilder<SyncWorker>(intervalForSyncWorker, TimeUnit.MINUTES)
                .setConstraints(syncConstraints)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[5], ExistingPeriodicWorkPolicy.REPLACE, syncWorker)


        // original
        val intervalForOriginalWorker = config.getTimeForWorkerUpdate("time_for_original_worker")
        val originalConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .setRequiresDeviceIdle(true)
                .build()
        val originalWorker = PeriodicWorkRequestBuilder<OriginalUploadWorker>(intervalForOriginalWorker, TimeUnit.MINUTES)
                .setConstraints(originalConstraints)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[6], ExistingPeriodicWorkPolicy.REPLACE, originalWorker)

        // check service
        val intervalForCheckServiceWorker = config.getTimeForWorkerUpdate("time_for_check_service_worker")
        val checkConstraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
        val checkServiceWorker = PeriodicWorkRequestBuilder<CheckerServiceWorker>(intervalForCheckServiceWorker, TimeUnit.MINUTES)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .setConstraints(checkConstraints)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[7], ExistingPeriodicWorkPolicy.REPLACE, checkServiceWorker)

        // large
        val intervalForLargeWorker = config.getTimeForWorkerUpdate("time_for_large_worker")
        val largeConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresCharging(true)
                .setRequiresDeviceIdle(true)
                .build()
        val largeWorker = PeriodicWorkRequestBuilder<SyncWorker>(intervalForLargeWorker, TimeUnit.MINUTES)
                .setConstraints(largeConstraints)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[8], ExistingPeriodicWorkPolicy.REPLACE, largeWorker)

    }

    override fun startSearchWorker() {
        val worker = OneTimeWorkRequestBuilder<FindFileWorker>().build()
        WorkManager.getInstance().enqueue(worker)
    }

    override fun startClearFolderWorker() {
        val clearConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        val worker = OneTimeWorkRequestBuilder<ClearBackupWorker>()
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .setConstraints(clearConstraints)
                .build()
        WorkManager.getInstance().enqueue(worker)
    }

    override fun startRemoveBackupWorker() {
        val removeConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .setRequiresDeviceIdle(true)
                .build()
        val worker = OneTimeWorkRequestBuilder<RemoveFileWorker>()
                .setConstraints(removeConstraints)
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
        val backupConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .setRequiresDeviceIdle(true)
                .build()
        val worker = OneTimeWorkRequestBuilder<BackupWorker>()
                .setConstraints(backupConstraints)
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