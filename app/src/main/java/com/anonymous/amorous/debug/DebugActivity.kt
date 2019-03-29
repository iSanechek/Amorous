package com.anonymous.amorous.debug

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.anonymous.amorous.R
import com.anonymous.amorous.data.database.LocalDatabase
import com.anonymous.amorous.data.models.Event
import com.anonymous.amorous.service.AmorousService
import com.anonymous.amorous.utils.*
import com.anonymous.amorous.workers.OriginalUploadWorker
import com.anonymous.amorous.workers.ScanningWorker
import com.anonymous.amorous.workers.SyncDatabaseWorker
import kotlinx.android.synthetic.main.debug_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class DebugActivity : AppCompatActivity() {

    private val db: LocalDatabase by inject()
    private val remoteDb: RemoteDatabase by inject()
    private val files: FileUtils by inject()
    private val manager: WorkersManager by inject()
    private val action: ActionUtils by inject()
    private val scan: ScanContract by inject()
    private val tracker: TrackingUtils by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.debug_layout)

//        if (files.checkCreatedCacheFolder(this)) {
//            log("Cache folder create or created!")
//        } else {
//            log("Cache folder not create!")
//        }


//        log("Cache folder is empty ${files.checkCacheFolderIsEmpty(this)}")
//
//        log("Cache folder size ${files.getReadableFileSize(files.getCacheFolderSize(this))}")
//
        val items = files.getAllFilesFromCacheFolder(this)
        log("Size ${items.size}")

//        action.startAction {
//            val scannerWorker = OneTimeWorkRequestBuilder<ScanningWorker>()
//                    .build()
//            val syncWorker = OneTimeWorkRequestBuilder<SyncDatabaseWorker>()
//                    .build()
//            WorkManager.getInstance().beginWith(scannerWorker).then(syncWorker).enqueue()
//
//        }
//        val scannerWorker = OneTimeWorkRequestBuilder<ScanningWorker>()
//                .build()
//        val syncWorker = OneTimeWorkRequestBuilder<SyncDatabaseWorker>()
//                .build()
//        WorkManager.getInstance().beginWith(scannerWorker).then(syncWorker).enqueue()

        val syncWorker = OneTimeWorkRequestBuilder<TestWorker>().build()
        WorkManager.getInstance().enqueue(syncWorker)
        manager.getStatusWorker()

        debug_start.setOnClickListener {

            manager.stopAllWorkers()

            manager.getStatusWorker()

//            val syncWorker = OneTimeWorkRequestBuilder<OriginalUploadWorker>()
//                    .build()
//
//            WorkManager.getInstance().enqueue(syncWorker)

//            GlobalScope.launch(Dispatchers.IO) {
//
//            }


//            s.scheduleJob(this)
//            action.startAction {
//                manager.startGeneralWorker()
//                manager.startGeneralWorkers()
//                log("Is ok")
//            }

//            manager.stopAllWorkers()

//            val uploadCandidates = remoteDb.getDatabase().child(DB_T_O)
//            uploadCandidates.addListenerForSingleValueEvent(object: ValueEventListener {
//                override fun onCancelled(p0: DatabaseError) {
//                    log("Error ${p0.toException().message}")
//                }
//
//                override fun onDataChange(p0: DataSnapshot) {
//                    val children = p0.children
//                    children.forEach { child ->
//
//                        val item = child.getValue(Candidate::class.java)
//                        log("Name ${item?.name}")
//                        log("uid ${item?.uid}")
//                        log("uid ${item?.originalStatus}")
//                    }
//
//
//                }
//            })
        }
    }

    private fun testStartService() {
        startService(Intent(this, AmorousService::class.java))
    }

    private fun log(msg: String?) {
        Log.e("DebugA", msg)
//        debug_info.text = msg
    }

    override fun onDestroy() {
//        WorkManager.getInstance().cancelUniqueWork("test_worker")
        super.onDestroy()
    }
}