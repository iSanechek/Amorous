package com.anonymous.amorous.utils

import android.util.Log
import androidx.work.*
import com.anonymous.amorous.*
import com.anonymous.amorous.workers.*
import java.util.concurrent.TimeUnit

interface WorkersManager {
    fun startGeneralWorkers()
    fun startGeneralWorker()
    fun stopAllWorkers()
    fun getStatusWorker()
}

class WorkersManagerImpl(private val config: ConfigurationUtils,
                         private val tracker: TrackingUtils) : WorkersManager {

    private val workersTags = arrayOf(
            "start_worker_x",
            "checker_worker_x",
            "scanning_worker_x",
            "sync_worker_x",
            "original_worker_x",
            "backup_worker_x")

    override fun getStatusWorker() {
        val events = hashSetOf<String>()
        events.add("Start check status worker!")
//        val manager = WorkManager.getInstance()
//        for (i in 0 until workersTags.size) {
//            val tag = workersTags[i]
//            val info = manager.getWorkInfosByTag(tag).get()[0]
//            when (info.state) {
//                WorkInfo.State.BLOCKED -> events.add("Worker $tag status: BLOCKED")
//                WorkInfo.State.CANCELLED -> events.add("Worker $tag status: CANCELLED")
//                WorkInfo.State.ENQUEUED -> events.add("Worker $tag status: ENQUEUED")
//                WorkInfo.State.FAILED -> events.add("Worker $tag status: FAILED")
//                WorkInfo.State.RUNNING -> events.add("Worker $tag status: Running")
//                WorkInfo.State.SUCCEEDED -> events.add("Worker $tag status: SUCCEEDED")
//            }
//        }
//        events.add("Finish check status worker!")
//        tracker.sendEvent("WorkersManager", events)
    }

    override fun stopAllWorkers() {
        val events = hashSetOf<String>()
        events.add("Start cancel all workers!")
        val manager = WorkManager.getInstance()
        for (i in 0 until workersTags.size) {
            val tag = workersTags[i]
            events.add("Stop worker: $tag")
            manager.cancelAllWorkByTag(tag)
        }
        events.add("Finish cancel all workers!")
        tracker.sendEvent("WorkersManager", events)
    }

    override fun startGeneralWorker() {
        val intervalGeneralWork = config.getTimeForWorkerUpdate(WORKER_GENERAL_TIME_KEY)
        val generalWorker = PeriodicWorkRequestBuilder<StarterWorker>(intervalGeneralWork, TimeUnit.HOURS).build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[0], ExistingPeriodicWorkPolicy.REPLACE, generalWorker)
    }

    override fun startGeneralWorkers() {
        // checker service worker
        val intervalForCheckerWorker = config.getTimeForWorkerUpdate(WORKER_CHECKER_TIME_KEY)
        val checkerWorker = PeriodicWorkRequestBuilder<CheckerServiceWorker>(intervalForCheckerWorker, TimeUnit.HOURS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[1], ExistingPeriodicWorkPolicy.REPLACE, checkerWorker)

        // scanner worker
        val intervalForScannerWorker = config.getTimeForWorkerUpdate(WORKER_SCAN_TIME_KEY)
        val scannerConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        val scannerWorker = PeriodicWorkRequestBuilder<ScanningWorker>(intervalForScannerWorker, TimeUnit.HOURS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .setConstraints(scannerConstraints)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[2], ExistingPeriodicWorkPolicy.REPLACE, scannerWorker)

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

        // original worker
        val intervalForOriginalUploadWorker = config.getTimeForWorkerUpdate(WORKER_ORIGINAL_TIME_KEY)
        val originalConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        val originalWorker = PeriodicWorkRequestBuilder<OriginalUploadWorker>(intervalForOriginalUploadWorker, TimeUnit.HOURS)
                .setConstraints(originalConstraints)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[4], ExistingPeriodicWorkPolicy.REPLACE, originalWorker)

        // remove backup
//        val backupConstraints = Constraints.Builder()
//                .setRequiresCharging(true)
//                .setRequiresDeviceIdle(true)
//                .build()
//        val backupWorker = PeriodicWorkRequestBuilder<RemoveBackupWorker>(24, TimeUnit.HOURS)
//                .setConstraints(backupConstraints)
//                .build()
//        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[5], ExistingPeriodicWorkPolicy.REPLACE, backupWorker)

    }
}