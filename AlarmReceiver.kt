package com.example.studentalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Runs when an alarm time arrives. */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra(AlarmScheduler.EXTRA_ID) ?: return
        val store = AlarmStore(context)
        val alarm = store.get(id) ?: return
        if (!alarm.enabled) return

        Notifications.show(context, alarm)

        if (alarm.weekdays.isEmpty()) {
            // One-time alarm: switch it off after it rings.
            store.upsert(alarm.copy(enabled = false))
        } else {
            AlarmScheduler.schedule(context, alarm) // set up the next repeat
        }
    }
}
