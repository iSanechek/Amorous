package com.anonymous.amorous.debug

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.anonymous.amorous.R
import com.anonymous.amorous.data.database.FirestoreDb
import com.anonymous.amorous.service.AmorousService
import com.anonymous.amorous.utils.FileUtils
import com.anonymous.amorous.utils.WorkersManager
import com.anonymous.amorous.workers.BaseCoroutineWorker
import com.anonymous.amorous.workers.ScanFolderWorker
import com.anonymous.amorous.workers.SyncWorker
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.debug_layout.*
import org.koin.android.ext.android.inject

class DebugActivity : AppCompatActivity() {

    private val files: FileUtils by inject()
    private val manager: WorkersManager by inject()
    private val f: FirestoreDb by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.debug_layout)
       testStartService()



//        val testWorker = PeriodicWorkRequestBuilder<DebugWorker>(15, TimeUnit.MINUTES).build()
//        WorkManager.getInstance().enqueueUniquePeriodicWork("debug_worker", ExistingPeriodicWorkPolicy.REPLACE, testWorker)




//        val items = files.getAllFilesFromCacheFolder(this)
//        log("Size ${items.size}")

        debug_start.setOnClickListener {
            log("Boom")

            val t = OneTimeWorkRequestBuilder<ScanFolderWorker>().build()
            WorkManager.getInstance().enqueue(t)

//            FirebaseInstanceId.getInstance().instanceId
//                    .addOnCompleteListener(OnCompleteListener { task ->
//                        if (!task.isSuccessful) {
//                            log("Erro ${task.exception}")
//                            return@OnCompleteListener
//                        }
//
//                        // Get new Instance ID token
//                        val token = task.result?.token
//
//                        // Log and toast
//                        log(token)
//                    })

        }
    }

    private fun testStartService() {
        startService(Intent(this, AmorousService::class.java))
    }

    private fun log(msg: String?) {
        Log.e("DebugA", msg)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}