package com.gnaanaa.mtimer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gnaanaa.mtimer.widget.worker.WidgetUpdateWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            WidgetUpdateWorker.enqueue(context)
        }
    }
}
