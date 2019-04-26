package com.anonymous.amorous.utils

import android.content.SharedPreferences
import androidx.core.content.edit

interface PrefUtils {
    fun updateWorkerRetryCountValue(key: String, value: Int)
    fun getWorkerRetryCountValue(key: String): Int
    fun getTimeUpdate(key: String): Long
    fun setTimeUpdate(key: String,value: Long)
}

class PrefUtilsImpl(private val preferences: SharedPreferences) : PrefUtils {

    override fun getTimeUpdate(key: String): Long = preferences.getLong(key, 0L)

    override fun setTimeUpdate(key: String, value: Long) {
        preferences.edit {
            putLong(key, value)
        }
    }

    override fun updateWorkerRetryCountValue(key: String, value: Int) {
        preferences.edit {
            putInt(key, value)
        }
    }

    override fun getWorkerRetryCountValue(key: String): Int = preferences.getInt(key, 0)

}