package com.anonymous.amorous.utils

import android.content.SharedPreferences
import androidx.core.content.edit

interface PrefUtils {
    fun updateWorkerRetryCountValue(key: String, value: Int)
    fun getWorkerRetryCountValue(key: String): Int
}

class PrefUtilsImpl(private val preferences: SharedPreferences) : PrefUtils {

    override fun updateWorkerRetryCountValue(key: String, value: Int) {
        preferences.edit {
            putInt(key, value)
        }
    }

    override fun getWorkerRetryCountValue(key: String): Int = preferences.getInt(key, 0)

}