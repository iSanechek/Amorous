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
import com.anonymous.amorous.utils.FileUtils
import kotlinx.android.synthetic.main.debug_layout.*
import org.koin.android.ext.android.inject

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
//        if (items.isNotEmpty()) {
//            for (i in 0 until items.size) {
//                val file = items[i]
//                log("File name ${file.name}")
//
//                if (files.getCheckFileExists(file.absolutePath)) {
//                    log("File ${file.name} exists")
//                } else log("File ${file.name} not exists")
//
//                log("File ${file.name} size ${files.getFileSizeFromPath(file.absolutePath)}")
//
//                if (i == i % 2) {
//                    val name = file.name
//                    val path = file.absolutePath
//                    if (files.removeFile(path)) {
//                        log("Remove file $name")
//                        if (files.getCheckFileExists(path)) {
//                            log("File $name not exists")
//                        } else log("File $name not exists")
//                    } else log("File $name not remove")
//                }
//            }
//        }
//
//        if (files.clearCacheFolder(this)) {
//            log("Clear cache folder")
//        } else {
//            log("Not clear cache folder")
//        }
//        log("Cache folder size ${files.getReadableFileSize(files.getCacheFolderSize(this))}")

        debug_start.setOnClickListener {
            s.scheduleJob(this)
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