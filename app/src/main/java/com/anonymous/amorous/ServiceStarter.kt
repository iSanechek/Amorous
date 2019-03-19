package com.anonymous.amorous

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.anonymous.amorous.service.AmorousService

class ServiceStarter : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
        val intent = Intent("com.anonymous.amorous.service.AmorousService")
        context?.let {
            intent.setClass(it, AmorousService::class.java)
            it.startService(intent)
        }
    }
}