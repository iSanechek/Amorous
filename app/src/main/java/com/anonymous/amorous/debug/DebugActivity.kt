package com.anonymous.amorous.debug

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.anonymous.amorous.DB_T_C
import com.anonymous.amorous.R
import com.anonymous.amorous.data.Candidate
import com.anonymous.amorous.data.database.LocalDatabase
import com.anonymous.amorous.service.AmorousService
import com.anonymous.amorous.service.JobSchContract
import com.anonymous.amorous.utils.*
import com.anonymous.amorous.workers.ScanningWorker
import com.anonymous.amorous.workers.SyncDatabaseWorker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.debug_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File
import java.util.concurrent.TimeUnit

class DebugActivity : AppCompatActivity() {

    private val db: LocalDatabase by inject()
    private val remoteDb: RemoteDatabase by inject()
    private val files: FileUtils by inject()
    private val manager: WorkersManager by inject()
    private val action: ActionUtils by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.debug_layout)

        if (files.checkCreatedCacheFolder(this)) {
            log("Cache folder create or created!")
        } else {
            log("Cache folder not create!")
        }

        GlobalScope.launch(Dispatchers.IO) {

            val o = db.getCandidates("SELECT * FROM c WHERE n_o_u =? ORDER BY d ASC LIMIT 5", arrayOf("original_upload_need"))
            log("Original size ${o.size}")
        }


        log("Cache folder is empty ${files.checkCacheFolderIsEmpty(this)}")

        log("Cache folder size ${files.getReadableFileSize(files.getCacheFolderSize(this))}")

        val items = files.getAllFilesFromCacheFolder(this)
        log("Size ${items.size}")


        debug_start.setOnClickListener {
//            s.scheduleJob(this)
//            action.startAction {
//                manager.startGeneralWorker()
//                manager.startGeneralWorkers()
//                log("Is ok")
//            }

            val scannerWorker = PeriodicWorkRequestBuilder<ScanningWorker>(2, TimeUnit.HOURS)
                    .build()
            WorkManager.getInstance().enqueueUniquePeriodicWork("scanning_worker_x", ExistingPeriodicWorkPolicy.REPLACE, scannerWorker)
        }
    }

    private fun testStartService() {
        startService(Intent(this, AmorousService::class.java))
    }

    private fun log(msg: String?) {
        Log.e("DebugA", msg)
//        debug_info.text = msg
    }
}