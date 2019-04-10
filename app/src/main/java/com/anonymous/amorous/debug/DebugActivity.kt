package com.anonymous.amorous.debug

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.anonymous.amorous.R
import com.anonymous.amorous.data.database.LocalDatabase
import com.anonymous.amorous.service.AmorousService
import com.anonymous.amorous.utils.*
import kotlinx.android.synthetic.main.debug_layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class DebugActivity : AppCompatActivity() {

    private val db: LocalDatabase by inject()
    private val files: FileUtils by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.debug_layout)

        GlobalScope.launch(Dispatchers.IO) {
            try {

                val error = db.getThumbnailsCandidate("thumbnail_upload_fail", 100)
                val back = db.getThumbnailsCandidate("original_reade_backup", 100)
                val done = db.getThumbnailsCandidate("thumbnail_upload_done", 100)

                log("Done size ${done.size}")
                log("Error size ${error.size}")
                log("Backup size ${back.size}")

            } catch (e: Exception) {

            }
        }

        val items = files.getAllFilesFromCacheFolder(this)
        log("Size ${items.size}")

        debug_start.setOnClickListener {

            startService(Intent(this@DebugActivity, AmorousService::class.java))

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