package org.sedentaryapp.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import org.sedentaryapp.MainActivity
import org.sedentaryapp.data.CatalogActivity

object NotificationPublisher {

    private const val CHANNEL_ID =
        "activity_proposals"

    private const val NOTIFICATION_ID =
        1001

    fun show(
        context: Context,
        activity: CatalogActivity,
    ) {
        val notificationManager =
            context.getSystemService(
                NotificationManager::class.java
            )

        ensureChannel(
            notificationManager
        )

        val openAppIntent =
            Intent(
                context,
                MainActivity::class.java,
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

        val openAppPendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE,
            )

        val notification =
            Notification.Builder(
                context,
                CHANNEL_ID,
            )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle(
                    activity.title
                )
                .setContentText(
                    activity.instruction
                )
                .setStyle(
                    Notification.BigTextStyle()
                        .bigText(
                            activity.instruction
                        )
                )
                .setContentIntent(
                    openAppPendingIntent
                )
                .setAutoCancel(true)
                .setCategory(
                    Notification.CATEGORY_REMINDER
                )
                .build()

        notificationManager.notify(
            NOTIFICATION_ID,
            notification,
        )
    }

    private fun ensureChannel(
        notificationManager:
        NotificationManager,
    ) {
        if (
            notificationManager
                .getNotificationChannel(
                    CHANNEL_ID
                ) != null
        ) {
            return
        }

        val channel =
            NotificationChannel(
                CHANNEL_ID,
                "Propositions d'activité",
                NotificationManager
                    .IMPORTANCE_DEFAULT,
            ).apply {
                description =
                    "Petites activités proposées pendant la journée"

                setSound(
                    null,
                    null,
                )

                enableVibration(false)
            }

        notificationManager
            .createNotificationChannel(
                channel
            )
    }
}