package com.mayra.assistant

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

/**
 * Restores Mayra's persisted state after device boot.
 *
 * Android 14+ does not allow a microphone foreground service to be launched
 * directly from BOOT_COMPLETED, so the receiver restores the Master/Voice
 * preference and posts a notification asking the owner to tap Mayra to resume
 * background microphone listening.
 */
class MayraBootReceiver : BroadcastReceiver() {
    companion object {
        private const val CHANNEL_ID = "mayra_boot"
        private const val NOTIFICATION_ID = 9402
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED &&
            intent?.action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            intent?.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        val prefs = context.getSharedPreferences("mayra_secure", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("setup_complete", false)) return

        val masterOn = prefs.getBoolean("master_on", false)
        val voiceOn = FeatureToggleRegistry.isEnabled(
            prefs, FeatureToggleRegistry.VOICE_COMMAND
        )

        if (!masterOn) return

        val manager = context.getSystemService(NotificationManager::class.java)
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Mayra Startup",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }

        val launchIntent =
            context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = launchIntent?.let {
            PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val text = if (voiceOn) {
            "Mayra চালু আছে। Background Voice পুনরায় চালু করতে Mayra খুলুন।"
        } else {
            "Mayra Master চালু আছে।"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("Mayra প্রস্তুত")
            .setContentText(text)
            .setAutoCancel(true)
            .apply {
                if (pendingIntent != null) setContentIntent(pendingIntent)
            }
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }
}
