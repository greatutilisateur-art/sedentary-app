package org.sedentaryapp.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import org.sedentaryapp.MainActivity
import org.sedentaryapp.R
import org.sedentaryapp.data.CatalogActivity
import org.sedentaryapp.scheduling.ProposalAlarmReceiver
import org.sedentaryapp.scheduling.ProposalAlarmScheduler

object NotificationPublisher {

    const val EXTRA_PROPOSAL_KEY =
        "notification_proposal_key"

    private const val CHANNEL_ID =
        "activity_proposals"

    private const val NOTIFICATION_ID =
        1001

    private const val OPEN_REQUEST_CODE =
        4201

    private const val SNOOZE_REQUEST_CODE =
        4202

    private const val NOT_NOW_REQUEST_CODE =
        4203

    fun show(
        context: Context,
        activity: CatalogActivity,
        proposalKey: String,
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

                putExtra(
                    EXTRA_PROPOSAL_KEY,
                    proposalKey,
                )
            }

        val openAppPendingIntent =
            PendingIntent.getActivity(
                context,
                OPEN_REQUEST_CODE,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE,
            )

        val snoozePendingIntent =
            PendingIntent.getBroadcast(
                context,
                SNOOZE_REQUEST_CODE,
                actionIntent(
                    context =
                        context,
                    action =
                        ProposalAlarmScheduler
                            .ACTION_SNOOZE,
                    proposalKey =
                        proposalKey,
                    activityId =
                        activity.definition.id,
                ),
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE,
            )

        val notNowPendingIntent =
            PendingIntent.getBroadcast(
                context,
                NOT_NOW_REQUEST_CODE,
                actionIntent(
                    context =
                        context,
                    action =
                        ProposalAlarmScheduler
                            .ACTION_NOT_NOW,
                    proposalKey =
                        proposalKey,
                    activityId =
                        activity.definition.id,
                ),
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE,
            )

        val notification =
            Notification.Builder(
                context,
                CHANNEL_ID,
            )
                .setSmallIcon(
                    R.drawable
                        .ic_notification
                )
                .setContentTitle(
                    activity.title
                )
                .setContentText(
                    "${durationLabel(activity)} · Touche pour voir la consigne"
                )
                .setContentIntent(
                    openAppPendingIntent
                )
                .addAction(
                    Notification.Action.Builder(
                        null,
                        "+ 30 min",
                        snoozePendingIntent,
                    ).build()
                )
                .addAction(
                    Notification.Action.Builder(
                        null,
                        "Pas maintenant",
                        notNowPendingIntent,
                    ).build()
                )
                .setAutoCancel(true)
                .setCategory(
                    Notification
                        .CATEGORY_REMINDER
                )
                .build()

        notificationManager.notify(
            NOTIFICATION_ID,
            notification,
        )
    }

    fun cancel(
        context: Context,
    ) {
        context
            .getSystemService(
                NotificationManager::class.java
            )
            .cancel(
                NOTIFICATION_ID
            )
    }

    private fun actionIntent(
        context: Context,
        action: String,
        proposalKey: String,
        activityId: String,
    ): Intent {

        return Intent(
            context,
            ProposalAlarmReceiver::class.java,
        ).apply {
            this.action =
                action

            putExtra(
                ProposalAlarmScheduler
                    .EXTRA_PROPOSAL_KEY,
                proposalKey,
            )

            putExtra(
                ProposalAlarmScheduler
                    .EXTRA_ACTIVITY_ID,
                activityId,
            )
        }
    }

    private fun durationLabel(
        activity: CatalogActivity,
    ): String {

        val min =
            activity.definition
                .durationSecondsMin

        val max =
            activity.definition
                .durationSecondsMax

        if (min == max) {
            return formatDuration(
                min
            )
        }

        return "${formatDuration(min)}–${formatDuration(max)}"
    }

    private fun formatDuration(
        seconds: Int,
    ): String {

        return if (
            seconds < 60
        ) {
            "$seconds s"
        } else {
            val minutes =
                seconds / 60

            val remainingSeconds =
                seconds % 60

            if (
                remainingSeconds == 0
            ) {
                "$minutes min"
            } else {
                "$minutes min ${remainingSeconds} s"
            }
        }
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
                    .IMPORTANCE_HIGH,
            ).apply {
                description =
                    "Petites activités proposées pendant la journée"

                setSound(
                    null,
                    null,
                )

                enableVibration(
                    false
                )
            }

        notificationManager
            .createNotificationChannel(
                channel
            )
    }
}
