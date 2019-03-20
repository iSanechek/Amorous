package com.anonymous.amorous.utils

import com.anonymous.amorous.BuildConfig
import com.anonymous.amorous.JOBS_SERVICE_STATUS_KEY
import com.anonymous.amorous.R
import com.anonymous.amorous.WORKER_RETRY_VALUE_KEY
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

interface ConfigurationUtils {
    fun getWorkerRetryCount(): Int
    fun getStartJobsServiceStatus(): Boolean
    fun getTimeForWorkerUpdate(workerKey: String): Int
}

class ConfigurationUtilsImpl : ConfigurationUtils {

    private var config: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        val setting = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()
        config.setConfigSettings(setting)
        config.setDefaults(R.xml.remote_config)
    }

    override fun getWorkerRetryCount(): Int = config.getLong(WORKER_RETRY_VALUE_KEY).toInt()

    override fun getStartJobsServiceStatus(): Boolean = config.getBoolean(JOBS_SERVICE_STATUS_KEY)

    override fun getTimeForWorkerUpdate(workerKey: String): Int = config.getLong(workerKey).toInt()

}