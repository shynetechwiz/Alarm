package com.example.studentalarm

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object Notifications {
    private const val CHANNEL_ID = "student_alarms"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(NotificationManager::class.java)
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val channel = NotificationChannel(
            CHANNEL_ID, "Student alarms", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Wake-up, class, homework and study alarms"
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), attrs)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 300, 500, 300, 500)
        }
        nm.createNotificationChannel(channel)
    }

    fun show(context: Context, alarm: StudentAlarm) {
        ensureChannel(context)
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val tap = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val title = alarm.label.ifBlank { alarm.category.title }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText("${alarm.category.title} • ${alarm.timeString}")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(tap)
            .build()
        // Keep sounding until the notification is tapped or swiped away.
        notification.flags = notification.flags or Notification.FLAG_INSISTENT

        NotificationManagerCompat.from(context).notify(alarm.id.hashCode(), notification)
    }
}
