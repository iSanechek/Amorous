package com.anonymous.amorous.debug

import android.util.Log
import com.anonymous.amorous.BuildConfig


private const val TAG = "TheFuckYouApp"

fun logDebug(message: () -> String) {
    if (BuildConfig.DEBUG) Log.d(TAG, message())
}

fun logInfo(message: () -> String) {
    if (BuildConfig.DEBUG) Log.i(TAG, message())
}
