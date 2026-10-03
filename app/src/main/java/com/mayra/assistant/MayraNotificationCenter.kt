package com.mayra.assistant

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Owner-visible notification boundary for Mayra background events.
 *
 * Notifications are informational only and never perform financial or
 * external-account actions.
 */
object MayraNotificationCenter {
    private const val CHANNEL_ID = "mayra_owner_alerts"

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Mayra Owner Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Owner-visible alerts from Mayra background checks."
        }
        manager.createNotificationChannel(channel)
    }

    fun notifyOwner(context: Context, event: NotificationSchedulePolicy.Event, notificationId: Int) {
        if (!NotificationSchedulePolicy.mayNotify(event)) return

        ensureChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(event.title)
            .setContentText(event.message)
            .setPriority(
                if (event.important) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
