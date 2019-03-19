package com.anonymous.amorous.debug

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.anonymous.amorous.R
import com.anonymous.amorous.data.database.LocalDatabase
import com.anonymous.amorous.service.AmorousService
import com.anonymous.amorous.service.JobSchContract
import com.anonymous.amorous.utils.ActionContract
import kotlinx.android.synthetic.main.debug_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class DebugActivity : AppCompatActivity() {

    private val action: ActionContract by inject()
    private val s: JobSchContract by inject()
    private val db: LocalDatabase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.debug_layout)

        s.scheduleJob(this)

//        GlobalScope.launch(Dispatchers.IO) {
//            db.saveCandidates(listOf(
//                    Candidate(
//                            uid = 101,
//                            needLocalBackup = "",
//                            originalLocalBitmapPath = "",
//                            size = "256kb",
//                            type = "image",
//                            needOriginalUpload = "",
//                            tempLocalBitmapPath = "",
//                            remoteCoverUrl = "",
//                            name = "Vasi"
//                    )
//            ))
//        }

        debug_start.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val items = withContext(Dispatchers.IO) { db.getCandidates() }
                log("Items size ${items.size}")
                items.forEach { item ->
                    logDebug {
                        "Candidate id ${item.uid} \n" +
                                "Candidate name ${item.name} \n" +
                                "Candidate original path ${item.originalLocalBitmapPath} \n" +
                                "Candidate size ${item.size} \n" +
                                "Candidate temp path ${item.tempLocalBitmapPath} \n" +
                                "Candidate type ${item.type}\n" +
                                "========================="
                    }
                }
            }
        }
    }

    private fun testStartService() {
        startService(Intent(this, AmorousService::class.java))
    }

    private fun log(msg: String) {
        Log.e("DebugA", msg)
        debug_info.text = msg
    }
}