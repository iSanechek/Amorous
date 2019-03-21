package com.anonymous.amorous.service

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import com.anonymous.amorous.BuildConfig
import com.anonymous.amorous.data.Candidate
import com.anonymous.amorous.data.database.LocalDatabase
import com.anonymous.amorous.utils.RemoteDatabase
import com.anonymous.amorous.utils.TrackingUtils
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class SyncDatabaseService : JobSchContract, JobService() {

    private val localDb: LocalDatabase by inject()
    private val remoteDb: RemoteDatabase by inject()
    private val tracker: TrackingUtils by inject()

    private var jobInfo: JobInfo? = null
    private var ref: DatabaseReference? = null
    private var params: JobParameters? = null

    private val events = hashSetOf<String>()
    private val jobsCache = hashMapOf<String, Job>()
    private val parentJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + parentJob)

    private val childEventsListener = object : ChildEventListener {

        override fun onCancelled(p0: DatabaseError) {
            addEvent("Какой то пиздец при синхронизации локальной бд с серверной! ${p0.toException().message}")
            sendEvents(TAG, events)
            params?.let {
                jobFinished(it, false)
            }
        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onChildChanged(p0: DataSnapshot, p1: String?) {

        }

        override fun onChildAdded(p0: DataSnapshot, p1: String?) {

        }

        override fun onChildRemoved(p0: DataSnapshot) {

        }
    }

    override fun scheduleJob(context: Context) {
        when {
            jobInfo != null -> a(context)
            else -> {
                val builder = JobInfo.Builder(SYNC_SERVICE_JOB_ID, ComponentName(BuildConfig.APPLICATION_ID, SyncDatabaseService::class.java.name))
                jobInfo = builder.build()
                jobInfo?.let {
                    bindService(context).schedule(it)
                }
            }
        }
    }

    override fun a(context: Context): Int = bindService(context).schedule(jobInfo!!)

    override fun serviceIsRun(context: Context): Boolean {
        val jobs= bindService(context).allPendingJobs
        var isRunning = jobs.isNotEmpty()
        for (i in 0 until jobs.size) { if (jobs[i].id == SYNC_SERVICE_JOB_ID) isRunning = true }
        return isRunning
    }

    override fun cancelJob(context: Context) {
        scope.coroutineContext.cancelChildren()
        ref?.removeEventListener(childEventsListener)
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        scope.coroutineContext.cancelChildren()
        ref?.removeEventListener(childEventsListener)
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        this.params = params
        ref = remoteDb.getDatabase()
        ref?.addChildEventListener(childEventsListener)

        return true
    }

    private suspend fun doWorkAsync(candidate: Candidate): Deferred<Unit> = coroutineScope {
        async {

        }
    }

    private fun finish() {

    }

    private fun addEvent(event: String) {
        events.add(event)
    }

    private fun sendEvents(tag: String, events: HashSet<String>) {
        tracker.sendEvent(tag, events)
        if (events.isNotEmpty()) {
            events.clear()
        }
    }

    private fun bindService(context: Context): JobScheduler = context.getSystemService(JobScheduler::class.java) as JobScheduler

    companion object {
        private const val SYNC_SERVICE_JOB_ID = 606
        private const val TAG = "SyncDatabaseService"
    }
}