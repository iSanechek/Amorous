package com.anonymous.amorous.utils

import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.anonymous.amorous.CJSWT
import com.anonymous.amorous.workers.CheckerServiceWorker
import java.util.concurrent.TimeUnit

interface WorkersManager {
    fun startJobsCheckerWorker()
    fun startGeneralWorkers()
}

class WorkersManagerImpl : WorkersManager {

    override fun startGeneralWorkers() {

    }

    override fun startJobsCheckerWorker() {
        val worker = PeriodicWorkRequestBuilder<CheckerServiceWorker>(5, TimeUnit.HOURS)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(CJSWT, ExistingPeriodicWorkPolicy.REPLACE, worker)
    }
}