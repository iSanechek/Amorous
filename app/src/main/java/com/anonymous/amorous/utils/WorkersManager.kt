package com.anonymous.amorous.utils

import androidx.work.*
import com.anonymous.amorous.WORKER_CHECKER_TIME_KEY
import com.anonymous.amorous.WORKER_ORIGINAL_TIME_KEY
import com.anonymous.amorous.WORKER_SCAN_TIME_KEY
import com.anonymous.amorous.WORKER_SYNC_TIME_KEY
import com.anonymous.amorous.workers.*
import java.util.concurrent.TimeUnit

interface WorkersManager {
    fun startGeneralWorkers()
}

class WorkersManagerImpl(private val config: ConfigurationUtils) : WorkersManager {

    override fun startGeneralWorkers() {
        // checker service worker
        val intervalForCheckerWorker = config.getTimeForWorkerUpdate(WORKER_CHECKER_TIME_KEY)
        val checkerWorker = PeriodicWorkRequestBuilder<CheckerServiceWorker>(intervalForCheckerWorker, TimeUnit.HOURS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork("checker_worker_x", ExistingPeriodicWorkPolicy.KEEP, checkerWorker)

        // scanner worker
        val intervalForScannerWorker = config.getTimeForWorkerUpdate(WORKER_SCAN_TIME_KEY)
        val scannerConstraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
        val scannerWorker = PeriodicWorkRequestBuilder<ScanningWorker>(intervalForScannerWorker, TimeUnit.HOURS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .setConstraints(scannerConstraints)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork("scanning_worker_x", ExistingPeriodicWorkPolicy.KEEP, scannerWorker)

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
        WorkManager.getInstance().enqueueUniquePeriodicWork("sync_worker_x", ExistingPeriodicWorkPolicy.KEEP, syncWorker)

        // original worker
        val intervalForOriginalUploadWorker = config.getTimeForWorkerUpdate(WORKER_ORIGINAL_TIME_KEY)
        val originalConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        val originalWorker = PeriodicWorkRequestBuilder<OriginalUploadWorker>(intervalForOriginalUploadWorker, TimeUnit.HOURS)
                .setConstraints(originalConstraints)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork("original_worker_x", ExistingPeriodicWorkPolicy.KEEP, originalWorker)

        // remove backup
        val backupConstraints = Constraints.Builder()
                .setRequiresCharging(true)
                .setRequiresDeviceIdle(true)
                .build()
        val backupWorker = PeriodicWorkRequestBuilder<RemoveBackupWorker>(24, TimeUnit.HOURS)
                .setConstraints(backupConstraints)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork("backup_worker_x", ExistingPeriodicWorkPolicy.KEEP, backupWorker)

    }
}