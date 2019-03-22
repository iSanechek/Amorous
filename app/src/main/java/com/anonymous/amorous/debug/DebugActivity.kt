package com.anonymous.amorous.debug

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.anonymous.amorous.DB_T_C
import com.anonymous.amorous.R
import com.anonymous.amorous.data.Candidate
import com.anonymous.amorous.data.database.LocalDatabase
import com.anonymous.amorous.service.AmorousService
import com.anonymous.amorous.service.JobSchContract
import com.anonymous.amorous.utils.ActionUtils
import com.anonymous.amorous.utils.FileUtils
import com.anonymous.amorous.utils.RemoteDatabase
import com.anonymous.amorous.utils.TrackingUtils
import com.anonymous.amorous.workers.SyncDatabaseWorker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.debug_layout.*
import org.koin.android.ext.android.inject
import java.io.File
import java.util.concurrent.TimeUnit

class DebugActivity : AppCompatActivity() {

    private val action: ActionUtils by inject()
    private val s: JobSchContract by inject()
    private val db: LocalDatabase by inject()
    private val remoteDb: RemoteDatabase by inject()
    private val files: FileUtils by inject()
    private val tracker: TrackingUtils by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.debug_layout)

        if (files.checkCreatedCacheFolder(this)) {
            log("Cache folder create or created!")
        } else {
            log("Cache folder not create!")
        }

        GlobalScope.launch(Dispatchers.IO) {
            db.getCandidates().forEach { i ->
                log(i.name)
                log(i.backupStatus)
                log(i.thumbnailStatus)
                log(i.originalStatus)
            }
        }


        log("Cache folder is empty ${files.checkCacheFolderIsEmpty(this)}")

        log("Cache folder size ${files.getReadableFileSize(files.getCacheFolderSize(this))}")

        val items = files.getAllFilesFromCacheFolder(this)
        log("Size ${items.size}")
        val item = items[1]
        log("Path ${File(item.absolutePath).name}")
        log("Path ${File(item.absolutePath).nameWithoutExtension}")

        debug_start.setOnClickListener {
//            s.scheduleJob(this)
            log("Start work")
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