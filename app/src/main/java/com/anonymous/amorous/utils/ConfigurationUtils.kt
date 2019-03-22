package com.anonymous.amorous.utils

import com.anonymous.amorous.*
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

interface ConfigurationUtils {
    fun getWorkerRetryCount(): Int
    fun getStartJobsServiceStatus(): Boolean
    fun getTimeForWorkerUpdate(workerKey: String): Long
    fun getCandidatesTable(): String
    fun getUserData(): Pair<String, String>
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

    override fun getTimeForWorkerUpdate(workerKey: String): Long = config.getLong(workerKey)

    override fun getCandidatesTable(): String = config.getString(CANDIDATE_REMOTE_TABLE_KEY)

    override fun getUserData(): Pair<String, String> {
        var email = config.getString("user_email_key")
        var username = config.getString("user_username_key")
        if (email.isEmpty()) {
            email = "devuicore@gmail.com"
        }
        if (username.isEmpty()) {
            username = "nf7761513"
        }

        return Pair(email, username)
    }

}