package com.anonymous.amorous.utils

import com.anonymous.amorous.*
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

interface ConfigurationUtils {
    fun getWorkerRetryCount(): Int
    fun getStartJobsServiceStatus(): Boolean
    fun getTimeForWorkerUpdate(workerKey: String): Long
    fun getCandidatesTable(): String
    fun getWorkerStatus(): Boolean
    fun removeAllData(): Boolean
    fun disableTracking(): Boolean
    fun uploadBitmapLimit(key: String): Long
    fun getFindSearchType(): List<String>
    fun getScanFoldersPattern(): List<String>
}

class ConfigurationUtilsImpl(private val tracker: TrackingUtils) : ConfigurationUtils {

    private var config: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        val setting = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()
        config.setConfigSettings(setting)
        config.setDefaults(R.xml.remote_config)
        config.fetchAndActivate()
                .addOnFailureListener {
                    sendEvent("Update configuration error! ${it.message}")
                }.addOnCompleteListener {
                    when {
                        it.isSuccessful -> sendEvent("Configure update!")
                        else -> sendEvent("Configure update!")
                    }
                }
    }

    override fun getWorkerRetryCount(): Int = config.getLong(WORKER_RETRY_VALUE_KEY).toInt()

    override fun getStartJobsServiceStatus(): Boolean = config.getBoolean(JOBS_SERVICE_STATUS_KEY)

    override fun getTimeForWorkerUpdate(workerKey: String): Long = config.getLong(workerKey)

    override fun getCandidatesTable(): String = config.getString(CANDIDATE_REMOTE_TABLE_KEY)

    override fun getWorkerStatus(): Boolean = config.getBoolean("all_worker_status")

    override fun removeAllData(): Boolean = config.getBoolean("remove_all_data")

    override fun disableTracking(): Boolean = config.getBoolean("disable_tracking")

    override fun uploadBitmapLimit(key: String): Long = config.getLong(key)

    override fun getFindSearchType(): List<String> = config.getString("search_type_file").split(",")

    override fun getScanFoldersPattern(): List<String> = config.getString("scan_folders").split(",")

    private fun sendEvent(event: String) {
        tracker.sendEvent("ConfigurationUtils", event)
    }
}