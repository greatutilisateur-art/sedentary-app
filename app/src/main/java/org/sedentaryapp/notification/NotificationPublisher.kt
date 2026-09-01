package org.sedentaryapp.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import org.sedentaryapp.MainActivity
import org.sedentaryapp.R
import org.sedentaryapp.data.ActivityGuidanceMode
import org.sedentaryapp.data.CatalogActivity
import org.sedentaryapp.scheduling.ProposalAlarmReceiver
import org.sedentaryapp.scheduling.ProposalAlarmScheduler

object NotificationPublisher {

    const val EXTRA_PROPOSAL_KEY =
        "notification_proposal_key"

    const val EXTRA_AUTO_START_GUIDED_SESSION =
        "notification_auto_start_guided_session"

    private const val LEGACY_CHANNEL_ID =
        "activity_proposals"

    private const val CHANNEL_SILENT =
        "activity_proposals_silent_v1"

    private const val CHANNEL_VIBRATION =
        "activity_proposals_vibration_v1"

    private const val CHANNEL_SOUND =
        "activity_proposals_sound_v1"

    private const val CHANNEL_SOUND_VIBRATION =
        "activity_proposals_sound_vibration_v1"

    private const val NOTIFICATION_ID =
        1001

    private const val OPEN_REQUEST_CODE =
        4201

    private const val SNOOZE_REQUEST_CODE =
        4202

    private const val DO_NOW_REQUEST_CODE =
        4203

    private const val DISMISS_REQUEST_CODE =
        4204

    fun show(
        context: Context,
        activity: CatalogActivity,
        proposalKey: String,
        allowSnooze: Boolean,
        snoozeDurationMinutes: Int,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
    ) {

        val notificationManager =
            context.getSystemService(
                NotificationManager::class.java
            )

        val channelId =
            ensureChannel(
                notificationManager =
                    notificationManager,
                soundEnabled =
                    soundEnabled,
                vibrationEnabled =
                    vibrationEnabled,
            )

        val openAppIntent =
            proposalActivityIntent(
                context = context,
                proposalKey = proposalKey,
                autoStartGuidedSession = false,
            )

        val openAppPendingIntent =
            PendingIntent.getActivity(
                context,
                OPEN_REQUEST_CODE,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE,
            )

        val doNowPendingIntent =
            when (activity.guidanceMode) {
                ActivityGuidanceMode.TIMER ->
                    PendingIntent.getActivity(
                        context,
                        DO_NOW_REQUEST_CODE,
                        proposalActivityIntent(
                            context = context,
                            proposalKey = proposalKey,
                            autoStartGuidedSession = true,
                        ),
                        PendingIntent.FLAG_UPDATE_CURRENT or
                            PendingIntent.FLAG_IMMUTABLE,
                    )

                ActivityGuidanceMode.SIMPLE ->
                    PendingIntent.getBroadcast(
                        context,
                        DO_NOW_REQUEST_CODE,
                        actionIntent(
                            context = context,
                            action =
                                ProposalAlarmScheduler
                                    .ACTION_DO_NOW,
                            proposalKey = proposalKey,
                            activityId =
                                activity.definition.id,
                        ),
                        PendingIntent.FLAG_UPDATE_CURRENT or
                            PendingIntent.FLAG_IMMUTABLE,
                    )
            }

        val dismissPendingIntent =
            PendingIntent.getBroadcast(
                context,
                DISMISS_REQUEST_CODE,
                actionIntent(
                    context = context,
                    action =
                        ProposalAlarmScheduler
                            .ACTION_DISMISS,
                    proposalKey = proposalKey,
                    activityId =
                        activity.definition.id,
                ),
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE,
            )

        val builder =
            Notification.Builder(
                context,
                channelId,
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
                .setDeleteIntent(
                    dismissPendingIntent
                )
                // On annule explicitement la notification après ouverture.
                // Cela évite de confondre un tap avec un swipe, qui a sa
                // propre sémantique d'abandon via deleteIntent.
                .setAutoCancel(false)
                .setCategory(
                    Notification
                        .CATEGORY_REMINDER
                )
                .setOnlyAlertOnce(true)

        if (allowSnooze) {

            val snoozePendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    SNOOZE_REQUEST_CODE,
                    actionIntent(
                        context = context,
                        action =
                            ProposalAlarmScheduler
                                .ACTION_SNOOZE,
                        proposalKey =
                            proposalKey,
                        activityId =
                            activity.definition.id,
                        snoozeDurationMinutes =
                            snoozeDurationMinutes,
                    ),
                    PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE,
                )

            builder.addAction(
                Notification.Action.Builder(
                    null,
                    "+ $snoozeDurationMinutes min",
                    snoozePendingIntent,
                ).build()
            )
        }

        builder.addAction(
            Notification.Action.Builder(
                null,
                "Commencer",
                doNowPendingIntent,
            ).build()
        )

        notificationManager.notify(
            NOTIFICATION_ID,
            builder.build(),
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

    private fun proposalActivityIntent(
        context: Context,
        proposalKey: String,
        autoStartGuidedSession: Boolean,
    ): Intent {

        return Intent(
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

            putExtra(
                EXTRA_AUTO_START_GUIDED_SESSION,
                autoStartGuidedSession,
            )
        }
    }

    private fun actionIntent(
        context: Context,
        action: String,
        proposalKey: String,
        activityId: String,
        snoozeDurationMinutes: Int? = null,
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

            snoozeDurationMinutes
                ?.let { minutes ->
                    putExtra(
                        ProposalAlarmScheduler
                            .EXTRA_SNOOZE_MINUTES,
                        minutes,
                    )
                }
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

        return if (min == max) {
            formatDuration(max)
        } else {
            "≈ ${formatDuration(max)}"
        }
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
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
    ): String {

        notificationManager
            .deleteNotificationChannel(
                LEGACY_CHANNEL_ID
            )

        val channelId =
            channelId(
                soundEnabled =
                    soundEnabled,
                vibrationEnabled =
                    vibrationEnabled,
            )

        if (
            notificationManager
                .getNotificationChannel(
                    channelId
                ) != null
        ) {
            return channelId
        }

        val channel =
            NotificationChannel(
                channelId,
                channelName(
                    soundEnabled =
                        soundEnabled,
                    vibrationEnabled =
                        vibrationEnabled,
                ),
                NotificationManager
                    .IMPORTANCE_HIGH,
            ).apply {

                description =
                    "Propositions Intervalles"

                if (soundEnabled) {
                    val attributes =
                        AudioAttributes.Builder()
                            .setUsage(
                                AudioAttributes
                                    .USAGE_NOTIFICATION
                            )
                            .setContentType(
                                AudioAttributes
                                    .CONTENT_TYPE_SONIFICATION
                            )
                            .build()

                    setSound(
                        RingtoneManager
                            .getDefaultUri(
                                RingtoneManager
                                    .TYPE_NOTIFICATION
                            ),
                        attributes,
                    )
                } else {
                    setSound(
                        null,
                        null,
                    )
                }

                enableVibration(
                    vibrationEnabled
                )

                if (vibrationEnabled) {
                    vibrationPattern =
                        longArrayOf(
                            0L,
                            120L,
                        )
                }
            }

        notificationManager
            .createNotificationChannel(
                channel
            )

        return channelId
    }

    private fun channelId(
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
    ): String {

        return when {
            soundEnabled &&
                vibrationEnabled ->
                CHANNEL_SOUND_VIBRATION

            soundEnabled ->
                CHANNEL_SOUND

            vibrationEnabled ->
                CHANNEL_VIBRATION

            else ->
                CHANNEL_SILENT
        }
    }

    private fun channelName(
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
    ): String {

        return when {
            soundEnabled &&
                vibrationEnabled ->
                "Propositions — Son et vibration"

            soundEnabled ->
                "Propositions — Son"

            vibrationEnabled ->
                "Propositions — Vibration"

            else ->
                "Propositions — Silencieuses"
        }
    }
}
