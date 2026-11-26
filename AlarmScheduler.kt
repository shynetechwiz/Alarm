package com.example.studentalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object AlarmScheduler {
    const val EXTRA_ID = "alarm_id"

    /** Next time (millis) this alarm should ring, strictly after [from]. */
    fun nextTrigger(alarm: StudentAlarm, from: Calendar = Calendar.getInstance()): Long {
        for (offset in 0..7) {
            val c = from.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, offset)
            c.set(Calendar.HOUR_OF_DAY, alarm.hour)
            c.set(Calendar.MINUTE, alarm.minute)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
            val dayOk = alarm.weekdays.isEmpty() || c.get(Calendar.DAY_OF_WEEK) in alarm.weekdays
            if (dayOk && c.timeInMillis > from.timeInMillis) return c.timeInMillis
        }
        return from.timeInMillis + 24 * 60 * 60 * 1000
    }

    fun schedule(context: Context, alarm: StudentAlarm) {
        cancel(context, alarm)
        if (!alarm.enabled) return
        val am = context.getSystemService(AlarmManager::class.java)
        val showIntent = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        // setAlarmClock fires on time even in Doze mode and needs no special permission.
        am.setAlarmClock(
            AlarmManager.AlarmClockInfo(nextTrigger(alarm), showIntent),
            pendingIntent(context, alarm)
        )
    }

    fun cancel(context: Context, alarm: StudentAlarm) {
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent(context, alarm))
    }

    private fun pendingIntent(context: Context, alarm: StudentAlarm): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).putExtra(EXTRA_ID, alarm.id)
        return PendingIntent.getBroadcast(
            context,
            alarm.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
