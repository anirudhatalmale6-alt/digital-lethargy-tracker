package com.dld.tracker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dld.tracker.DLTApplication

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val app = context.applicationContext as? DLTApplication ?: return
        if (app.securePrefs.isTrackingEnabled()) {
            UsagePollingService.start(context)
        }
    }
}
