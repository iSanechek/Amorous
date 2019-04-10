package com.anonymous.amorous.workers

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.anonymous.amorous.data.database.LocalDatabase
import com.anonymous.amorous.data.models.Candidate
import com.anonymous.amorous.service.UploadService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class TestWorker(appContext: Context,
                 workerParams: WorkerParameters) : Worker(appContext, workerParams), KoinComponent {

    private val database: LocalDatabase by inject()

    override fun doWork(): Result = try {

        applicationContext.startService(Intent(applicationContext, UploadService::class.java))

        Result.success()
    } catch (e: Exception) {
        Result.failure()
    }

     companion object {
         private const val TAG = "TEST"
     }
}