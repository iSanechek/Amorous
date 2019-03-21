package com.anonymous.amorous.debug

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.anonymous.amorous.R
import com.anonymous.amorous.data.Candidate
import com.anonymous.amorous.data.database.LocalDatabase
import com.anonymous.amorous.service.AmorousService
import com.anonymous.amorous.service.JobSchContract
import com.anonymous.amorous.utils.ActionContract
import com.anonymous.amorous.utils.FileUtils
import com.anonymous.amorous.workers.SyncDatabaseWorker
import kotlinx.android.synthetic.main.debug_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.sql.Time
import java.util.concurrent.TimeUnit

class DebugActivity : AppCompatActivity() {

    private val action: ActionContract by inject()
    private val s: JobSchContract by inject()
    private val db: LocalDatabase by inject()
    private val files: FileUtils by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.debug_layout)




        if (files.checkCreatedCacheFolder(this)) {
            log("Cache folder create or created!")
        } else {
            log("Cache folder not create!")
        }

        log("Cache folder is empty ${files.checkCacheFolderIsEmpty(this)}")

        log("Cache folder size ${files.getReadableFileSize(files.getCacheFolderSize(this))}")

        val items = files.getAllFilesFromCacheFolder(this)
        log("Size ${items.size}")

        GlobalScope.launch(Dispatchers.IO) {
            val i = db.getCandidates("SELECT * FROM c WHERE r_c_u =? ORDER BY d ASC LIMIT 10", arrayOf("thumbnail_upload_need"))
            log("Size candidates ${i.size}")

        }

        action.prepareAction()
        debug_start.setOnClickListener {
//            s.scheduleJob(this)
            log("Start work")
            val work = PeriodicWorkRequestBuilder<SyncDatabaseWorker>(120, TimeUnit.SECONDS).build()
            WorkManager.getInstance().enqueue(work)
        }
    }

    private fun testStartService() {
        startService(Intent(this, AmorousService::class.java))
    }

    private fun log(msg: String) {
        Log.e("DebugA", msg)
//        debug_info.text = msg
    }
}