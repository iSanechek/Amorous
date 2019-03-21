package com.anonymous.amorous.service

import android.content.Context

interface JobSchContract {
    fun scheduleJob(context: Context)
    fun a(context: Context): Int
    fun serviceIsRun(context: Context): Boolean
    fun cancelJob(context: Context)
}