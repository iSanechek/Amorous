package com.anonymous.amorous.utils

import android.content.Context
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
    fun startLargeUploadWorker()
    fun startClearFolderWorker()
    fun startRemoveBackupWorker()
    fun startSearchWorker()
    fun restartWorker()
}

class WorkersManagerImpl(private val ctx: Context,
                         private val config: ConfigurationUtils,
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
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(workersTags[0], if (BuildConfig.DEBUG) ExistingPeriodicWorkPolicy.REPLACE else ExistingPeriodicWorkPolicy.KEEP, generalWorker)
    }

    override fun restartWorker() {
        val intervalRestartWork = config.getTimeForWorkerUpdate("time_for_restart_worker")
        val restartWorker = PeriodicWorkRequestBuilder<RestartWorker>(intervalRestartWork, TimeUnit.MINUTES).build()
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(workersTags[1], ExistingPeriodicWorkPolicy.KEEP, restartWorker)
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
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(workersTags[2], ExistingPeriodicWorkPolicy.REPLACE, scannerWorker)

        // scan folders
        val intervalForScanFoldersWorker = config.getTimeForWorkerUpdate(WORKER_FOLDERS_TIME_KEY)
        val foldersConstraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        val foldersWorker = PeriodicWorkRequestBuilder<ScanFolderWorker>(intervalForScanFoldersWorker, TimeUnit.MINUTES)
                .setConstraints(foldersConstraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(workersTags[4], ExistingPeriodicWorkPolicy.REPLACE, foldersWorker)

        // thumbnail
        val intervalForThumbnailWorker = config.getTimeForWorkerUpdate("time_for_thumbnail_worker")
        val thumbnailConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        val thumbnailWorker = PeriodicWorkRequestBuilder<UploadThumbnailWorker>(intervalForThumbnailWorker, TimeUnit.MINUTES)
                .setConstraints(thumbnailConstraints)
                .build()
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(workersTags[3], ExistingPeriodicWorkPolicy.REPLACE, thumbnailWorker)

        // sync
        val intervalForSyncWorker = config.getTimeForWorkerUpdate("time_for_sync_worker")
        val syncConstraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        val syncWorker = PeriodicWorkRequestBuilder<SyncWorker>(intervalForSyncWorker, TimeUnit.MINUTES)
                .setConstraints(syncConstraints)
                .build()
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(workersTags[5], ExistingPeriodicWorkPolicy.REPLACE, syncWorker)

        // check service
        val intervalForCheckServiceWorker = config.getTimeForWorkerUpdate("time_for_check_service_worker")
        val checkConstraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
        val checkServiceWorker = PeriodicWorkRequestBuilder<CheckerServiceWorker>(intervalForCheckServiceWorker, TimeUnit.MINUTES)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .setConstraints(checkConstraints)
                .build()
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(workersTags[7], ExistingPeriodicWorkPolicy.REPLACE, checkServiceWorker)

    }

    override fun startSearchWorker() {
        val searchConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        val worker = OneTimeWorkRequestBuilder<FindFileWorker>()
                .setInitialDelay(3, TimeUnit.MINUTES)
                .setConstraints(searchConstraints)
                .build()
        WorkManager.getInstance(ctx).enqueue(worker)
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
        WorkManager.getInstance(ctx).enqueue(worker)
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
        WorkManager.getInstance(ctx).enqueue(worker)
    }

    override fun startOriginalWorker() {
        val originalConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        val worker = OneTimeWorkRequestBuilder<OriginalUploadWorker>()
                .setInitialDelay(3, TimeUnit.MINUTES)
                .setConstraints(originalConstraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()
        WorkManager.getInstance(ctx).enqueue(worker)
    }

    override fun startLargeUploadWorker() {
        val largeConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresCharging(true)
                .setRequiresDeviceIdle(true)
                .build()
        val largeWorker = OneTimeWorkRequestBuilder<UploadLargeWorker>()
                .setInitialDelay(3, TimeUnit.MINUTES)
                .setConstraints(largeConstraints)
                .build()
        WorkManager.getInstance(ctx).enqueue(largeWorker)
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
        WorkManager.getInstance(ctx).enqueue(worker)
    }

    override fun stopAllWorkers() {
        addEvent("Start cancel all workers!")
        val manager = WorkManager.getInstance(ctx)
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