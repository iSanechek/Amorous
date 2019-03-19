package com.anonymous.amorous.service

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import com.anonymous.amorous.BuildConfig
import com.anonymous.amorous.debug.logDebug

class TestService : JobService() {

    private var jobInfo: JobInfo? = null

    fun s() {
        logDebug {
            "Boom"
        }
        val builder = JobInfo.Builder(
                606,
                ComponentName(BuildConfig.APPLICATION_ID, TestService::class.java.name)
        )
        builder.addTriggerContentUri(
                JobInfo.TriggerContentUri(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS
                )
        )
        builder.addTriggerContentUri(
                JobInfo.TriggerContentUri(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS
                )
        )
        builder.addTriggerContentUri(JobInfo.TriggerContentUri(MEDIA_URI, 0))

        jobInfo = builder.build()
    }

    private var params: JobParameters? = null
    private val handler = Handler()
    private val worker = Runnable {
        scheduleJob(this@TestService)
        params?.let {
            logDebug {
                "Finish"
            }
            jobFinished(it, false)
        }
    }

    fun scheduleJob(ctx: Context) {
        val js = ctx.getSystemService(JobScheduler::class.java) as JobScheduler
        jobInfo?.let {
            js.schedule(it)
        }
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        logDebug {
            "onStopJob"
        }
        handler.removeCallbacks(worker)
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        logDebug {
            "onStartJob"
        }
        this.params = params
        val sb = StringBuilder()
        params ?: return false
        if (params.triggeredContentAuthorities != null) {
            var rescanNeeded = false
            if (params.triggeredContentUris != null) {
                val ids = mutableListOf<String>()
                for (uri in params.triggeredContentUris!!) {
                    val path = uri.pathSegments
                    if (path.size == EXTERNAL_PATH_SEGMENTS.size + 1) {
                        ids.add(path[path.size - 1])
                    } else {
                        rescanNeeded = true
                        logDebug {
                            "rescanNeeded -> $rescanNeeded"
                        }
                    }

                    if (ids.isNotEmpty()) {
                        val selection = StringBuilder()
                        for (i in 0 until ids.size) {
                            if (selection.isNotEmpty()) {
                                selection.append(" OR ")
                            }
                            selection.append(MediaStore.Images.ImageColumns._ID)
                            selection.append("='")
                            selection.append(ids[i])
                            selection.append("'")

                            var cursor: Cursor? = null
                            var haveFiles = false
                            try {
                                cursor = contentResolver.query(
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        PROJECTION,
                                        selection.toString(),
                                        null,
                                        null)
                                while (cursor.moveToNext()) {
                                    val dir = cursor.getString(PROJECTION_DATA)
                                    if (dir.startsWith(DCIM_DIR)) {
                                        if (!haveFiles) {
                                            haveFiles = true
                                            sb.append("New photos:\n")
                                        }
                                        sb.append(cursor.getInt(PROJECTION_ID))
                                        sb.append(": ")
                                        sb.append(dir)
                                        sb.append("\n")
                                    }
                                }
                            } catch (e: SecurityException) {
                                sb.append("Error: no access to media!")
                            } finally {
                                cursor?.close()
                            }
                        }
                    } else {
                        logDebug {
                            "ids is empty"
                        }
                    }
                }
            } else {
                rescanNeeded = true
                logDebug {
                    "triggeredContentUris is null! Rescan needed $rescanNeeded"
                }
            }

            if (rescanNeeded) {
                sb.append("Photos rescan needed!")
            }
        } else {
            logDebug {
                "No photos content"
            }
        }
        logDebug {
            "Result $sb"
        }
//        handler.postDelayed(worker, 10 * 1000)
        return true
    }

    companion object {
        val MEDIA_URI = Uri.parse("content://${MediaStore.AUTHORITY}/")
        val EXTERNAL_PATH_SEGMENTS = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.pathSegments
        val PROJECTION = arrayOf(MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA)
        val PROJECTION_ID = 0
        val PROJECTION_DATA = 1
        val DCIM_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).path
    }

}