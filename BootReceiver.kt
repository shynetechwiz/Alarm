package com.example.studentalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Android forgets alarms on reboot, so this sets them up again. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            AlarmStore(context).load()
                .filter { it.enabled }
                .forEach { AlarmScheduler.schedule(context, it) }
        }
    }
}
