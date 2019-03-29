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
//        val generalWorker = PeriodicWorkRequestBuilder<StarterWorker>(intervalGeneralWork, TimeUnit.HOURS).build()
        val generalWorker = PeriodicWorkRequestBuilder<StarterWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[0], ExistingPeriodicWorkPolicy.REPLACE, generalWorker)
    }

    override fun startGeneralWorkers() {

        // scanner worker
        val intervalForScannerWorker = config.getTimeForWorkerUpdate(WORKER_SCAN_TIME_KEY)
        val scannerConstraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
//        val scannerWorker = PeriodicWorkRequestBuilder<ScanningWorker>(intervalForScannerWorker, TimeUnit.HOURS)
        val scannerWorker = PeriodicWorkRequestBuilder<ScanningWorker>(16, TimeUnit.MINUTES)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .setConstraints(scannerConstraints)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[2], ExistingPeriodicWorkPolicy.REPLACE, scannerWorker)

        // checker service worker
        val intervalForCheckerWorker = config.getTimeForWorkerUpdate(WORKER_CHECKER_TIME_KEY)
//        val checkerWorker = PeriodicWorkRequestBuilder<CheckerServiceWorker>(intervalForCheckerWorker, TimeUnit.HOURS)
        val checkerWorker = PeriodicWorkRequestBuilder<CheckerServiceWorker>(17, TimeUnit.MINUTES)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[1], ExistingPeriodicWorkPolicy.REPLACE, checkerWorker)

        // sync worker
        val syncConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        val intervalForSyncWorker = config.getTimeForWorkerUpdate(WORKER_SYNC_TIME_KEY)
//        val syncWorker = PeriodicWorkRequestBuilder<SyncDatabaseWorker>(intervalForSyncWorker, TimeUnit.HOURS)
        val syncWorker = PeriodicWorkRequestBuilder<SyncDatabaseWorker>(18, TimeUnit.MINUTES)
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
//        val originalWorker = PeriodicWorkRequestBuilder<OriginalUploadWorker>(intervalForOriginalUploadWorker, TimeUnit.HOURS)
        val originalWorker = PeriodicWorkRequestBuilder<OriginalUploadWorker>(19, TimeUnit.MINUTES)
                .setConstraints(originalConstraints)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(workersTags[4], ExistingPeriodicWorkPolicy.REPLACE, originalWorker)

    }

    private fun addEvent(event: String) {
        tracker.sendEvent("WorkersManager", event)
    }
}