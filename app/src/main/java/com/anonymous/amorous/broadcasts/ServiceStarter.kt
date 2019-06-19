package com.anonymous.amorous.broadcasts

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.anonymous.amorous.service.AmorousService

class ServiceStarter : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
        val i = Intent("com.anonymous.amorous.service.AmorousService")
        context?.let {
            i.setClass(it, AmorousService::class.java)
            it.startService(i)
        }
    }
}