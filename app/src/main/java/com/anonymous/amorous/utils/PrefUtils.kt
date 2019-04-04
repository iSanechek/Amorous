package com.anonymous.amorous.utils

import android.content.SharedPreferences
import androidx.core.content.edit

interface PrefUtils {
    fun updateWorkerRetryCountValue(key: String, value: Int)
    fun getWorkerRetryCountValue(key: String): Int
    var scanFolders: Set<String>
}

class PrefUtilsImpl(private val preferences: SharedPreferences) : PrefUtils {
    override var scanFolders: Set<String>
        get() = preferences.getStringSet("default_folders", setOf("Movies", "Pictures", "Download", "DCIM")) ?: setOf("Movies", "Pictures", "Download", "DCIM")
        set(value) {
            preferences.edit {
                putStringSet("default_folders", value)
            }
        }

    override fun updateWorkerRetryCountValue(key: String, value: Int) {
        preferences.edit {
            putInt(key, value)
        }
    }

    override fun getWorkerRetryCountValue(key: String): Int = preferences.getInt(key, 0)

}