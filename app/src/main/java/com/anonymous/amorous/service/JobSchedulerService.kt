package com.anonymous.amorous.service

import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.anonymous.amorous.BuildConfig
import com.anonymous.amorous.data.database.FirestoreDb
import com.anonymous.amorous.data.models.Candidate
import com.anonymous.amorous.toUid
import com.anonymous.amorous.utils.FileUtils
import com.anonymous.amorous.utils.TrackingUtils
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class JobSchedulerService : JobSchContract, JobService() {

    private val db: FirestoreDb by inject()
    private val fileUtils: FileUtils by inject()
    private val tracker: TrackingUtils by inject()
    private var jobInfo: JobInfo? = null

    private val jobsCache = hashMapOf<String, Job>()
    private val parentJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + parentJob)

    override fun onStopJob(params: JobParameters?): Boolean {
        scope.coroutineContext.cancelChildren()
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        params ?: return false
        if (params.triggeredContentAuthorities != null) {
            if (params.triggeredContentUris != null) {
                val ids = mutableListOf<String>()
                params.triggeredContentUris?.let { uris ->
                    for (uri in uris) {
                        val path = uri.pathSegments
                        if (path.size == EXTERNAL_PATH_SEGMENTS.size + 1) ids.add(path[path.size - 1])
                        if (ids.isNotEmpty()) {
                            val selection = StringBuilder()
                            for (i in 0 until ids.size) {
                                if (selection.isNotEmpty()) selection.append(" OR ")

                                selection.append(MediaStore.Images.ImageColumns._ID)
                                selection.append("='")
                                selection.append(ids[i])
                                selection.append("'")

                                var cursor: Cursor? = null
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
//                                            val fileId = cursor.getInt(PROJECTION_ID)
                                            val fileName = dir.substring(dir.lastIndexOf("/") + 1)
                                            Log.e(TAG, "name $fileName")
                                            Log.e(TAG, "dir $dir")
                                            doAsyncJob(dir, fileName)
//                                            jobsCache[fileName]?.cancel()
//                                            jobsCache[fileName] = scope.launch {
//                                                try {
//
////                                                    doWorkAsync(
////                                                            dir = dir,
////                                                            name = fileName
////                                                    ).await()
//
//                                                } catch (e: Exception) {
//                                                    e.printStackTrace()
//                                                    addEvent("Error for $fileName ${e.message}")
//                                                    jobsCache[fileName]?.cancel()
//                                                }
//                                            }
                                        }
                                    }
                                } catch (e: SecurityException) {
                                    addEvent("Error: no access to media! ${e.message}")
                                } finally {
                                    cursor?.close()
                                }
                            }
                        }
                    }
                }

                jobFinished(params, true)
                scheduleJob(this@JobSchedulerService)
            }
        }
        return true
    }

    @SuppressLint("NewApi")
    override fun scheduleJob(context: Context) {
        when {
            jobInfo != null -> a(context)
            else -> {
                val builder = JobInfo.Builder(CHECKER_SERVICE_JOB_ID, ComponentName(BuildConfig.APPLICATION_ID, JobSchedulerService::class.java.name))

                builder.addTriggerContentUri(
                        JobInfo.TriggerContentUri(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS
                        )
                )
                builder.addTriggerContentUri(JobInfo.TriggerContentUri(MEDIA_URI, 0))
                builder.setTriggerContentMaxDelay(500)
                jobInfo = builder.build()
                jobInfo?.let {
                    addEvent("Service schedule!")
                    bindService(context).schedule(it)
                }
            }
        }
    }

    override fun cancelJob(context: Context) {
        scope.coroutineContext.cancelChildren()
    }

    override fun serviceIsRun(context: Context): Boolean {
        val jobs = bindService(context).allPendingJobs
        var isRunning = jobs.isNotEmpty()
        for (i in 0 until jobs.size) {
            if (jobs[i].id == CHECKER_SERVICE_JOB_ID) isRunning = true
        }
        addEvent("Check service run $isRunning")
        return isRunning
    }

    @SuppressLint("NewApi")
    override fun a(context: Context): Int = bindService(context).schedule(jobInfo!!)

    private fun doAsyncJob(dir: String, name: String) = GlobalScope.launch(Dispatchers.IO) {
        if (fileUtils.checkCreatedCacheFolder(this@JobSchedulerService)) {
            val tempPath = fileUtils.copyToCacheFolder(this@JobSchedulerService, dir)
            Log.e("MDA", "Temp path $tempPath")
            db.saveCandidate(Candidate(
                        uid = name.toUid(),
                        name = name,
                        thumbnailStatus = Candidate.THUMBNAIL_UPLOAD_NEED,
                        tempPath = tempPath,
                        originalStatus = Candidate.ORIGINAL_UPLOAD_READE,
                        type = Candidate.IMAGE_TYPE,
                        size = fileUtils.getLongSizeFromPath(dir),
                        originalPath = dir,
                        backupStatus = Candidate.BACKUP_READE
                ))

        }
    }

    private fun bindService(context: Context): JobScheduler = context.getSystemService(JobScheduler::class.java) as JobScheduler

    private fun addEvent(event: String) {
        tracker.sendEvent(TAG, event)
    }

    companion object {
        private const val TAG = "JobSchedulerService"
        private const val CHECKER_SERVICE_JOB_ID = 999
        val MEDIA_URI = Uri.parse("content://${MediaStore.AUTHORITY}/")
        val EXTERNAL_PATH_SEGMENTS = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.pathSegments
        val PROJECTION = arrayOf(MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA)
        val PROJECTION_ID = 0
        val PROJECTION_DATA = 1
        val DCIM_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).path
    }
}