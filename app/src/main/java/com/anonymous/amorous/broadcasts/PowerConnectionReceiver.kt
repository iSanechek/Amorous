package com.anonymous.amorous.broadcasts

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.BatteryManager.BATTERY_PLUGGED_AC
import com.anonymous.amorous.service.AmorousService

class PowerConnectionReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(p0: Context?, p1: Intent?) {
        p1 ?: return
        val chargePlug = p1.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        if (chargePlug == BATTERY_PLUGGED_AC) {
            AmorousService.startService(p0)
        }
    }
}