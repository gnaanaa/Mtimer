package com.gnaanaa.mtimer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-schedule workers if needed, though WorkManager usually handles this.
            // This is required to be @AndroidEntryPoint as per spec.
        }
    }
}
