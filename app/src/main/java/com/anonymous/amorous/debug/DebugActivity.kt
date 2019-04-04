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
import com.anonymous.amorous.workers.*
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


        GlobalScope.launch(Dispatchers.IO) {


            try {
                val done = db.getCandidates("SELECT * FROM candidate WHERE thumbnailstatus =? ORDER BY date ASC", arrayOf("thumbnail_upload_done"))
                val error = db.getCandidates("SELECT * FROM candidate WHERE thumbnailstatus =? ORDER BY date ASC", arrayOf("thumbnail_upload_fail"))
                val need = db.getCandidates("SELECT * FROM candidate WHERE thumbnailstatus =? ORDER BY date ASC", arrayOf("thumbnail_upload_need"))
                val back = db.getCandidates("SELECT * FROM candidate WHERE backupstatus =?", arrayOf("original_reade_backup"))
                val remove = db.getCandidates("SELECT * FROM candidate WHERE backupstatus =?", arrayOf("original_remove_backup"))
                log("Done size ${done.size}")
                log("Need size ${need.size}")
                log("Error size ${error.size}")
                log("Backup size ${back.size}")
                log("Remove size ${remove.size}")
            } catch (e: Exception) {

            }
        }

        val items = files.getAllFilesFromCacheFolder(this)
        log("Size ${items.size}")

//        action.startAction {
////            val scannerWorker = OneTimeWorkRequestBuilder<ScanningWorker>()
////                    .build()
////            val syncWorker = OneTimeWorkRequestBuilder<UploadThumbnailWorker>()
////                    .build()
////            WorkManager.getInstance().beginWith(scannerWorker).then(syncWorker).enqueue()
//
//        }
//        val scannerWorker = OneTimeWorkRequestBuilder<ScanningWorker>()
//                .build()
//        val syncWorker = OneTimeWorkRequestBuilder<SyncDatabaseWorker>()
//                .build()
//        WorkManager.getInstance().beginWith(scannerWorker).then(syncWorker).enqueue()

        val syncWorker = OneTimeWorkRequestBuilder<SyncDatabaseWorker>().build()
        WorkManager.getInstance().enqueue(syncWorker)
//        manager.getStatusWorker()

        debug_start.setOnClickListener {

//            scan.scanRoot {  }

//            manager.stopAllWorkers()
//
//            manager.getStatusWorker()

            val sync = OneTimeWorkRequestBuilder<UploadThumbnailWorker>()
                    .build()

            WorkManager.getInstance().enqueue(sync)

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