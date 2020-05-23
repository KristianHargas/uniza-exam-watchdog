package com.tinko.unizaexamwatchdog.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tinko.unizaexamwatchdog.R
import com.tinko.unizaexamwatchdog.domain.Subject
import com.tinko.unizaexamwatchdog.ui.MainActivity

private const val CHANNEL_ID = "watchdog"

/**
 * This method is used to create notification channel to broadcast watchdog notifications.
 *
 * Setting up the notification channel is required on devices running Android O and above.
 *
 * @param context
 */
fun createWatchdogNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = context.getString(R.string.watchdog_notification_channel_name)
        val descriptionText = context.getString(R.string.watchdog_notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        // create the notification channel
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

/**
 * This method is used to show watchdog notification when there are new detected exams for given subject.
 *
 * @param context application context.
 * @param subject subject which newly added exams were detected.
 * @param newExamsCount count of newly detected exams.
 */
fun showWatchdogNotification(context: Context, subject: Subject, newExamsCount: Int) {
    // intent which launches the app upon notification tap
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

    // build the notification
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_watchdog_on)
        .setContentTitle(subject.name)
        .setContentText(context.getString(R.string.watchdog_notification_message, newExamsCount))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)

    // show the notification
    with(NotificationManagerCompat.from(context)) {
        notify(subject.id.hashCode(), builder.build())
    }
}