package org.sedentaryapp.scheduling

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.app.NotificationManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import org.sedentaryapp.data.AppStateRepository
import org.sedentaryapp.data.PolicyRepository
import org.sedentaryapp.data.SnoozedProposal

class ProposalAlarmScheduler(
    private val context: Context,
) {

    private val appContext =
        context.applicationContext

    suspend fun reconcile() {

        val policyRepository =
            PolicyRepository(
                appContext
            )

        val appStateRepository =
            AppStateRepository(
                context =
                    appContext,
                defaultSlotTimes =
                    policyRepository
                        .loadDefaultSlots(),
            )

        val onboardingCompleted =
            appStateRepository
                .onboardingCompleted
                .first()

        val proposalsPaused =
            appStateRepository
                .proposalsPaused
                .first()

        if (
            !onboardingCompleted ||
            proposalsPaused ||
            !notificationsAllowed(
                appContext
            ) ||
            !exactAlarmsAllowed(
                appContext
            )
        ) {
            if (proposalsPaused) {
                appStateRepository
                    .clearSnoozedProposal()
            }

            cancel()
            return
        }

        val scheduleSettings =
            appStateRepository
                .scheduleSettings
                .first()

        val consumedEarly =
            appStateRepository
                .consumedEarlyOpportunityKeys
                .first()

        var snoozedProposal =
            appStateRepository
                .snoozedProposal
                .first()

        val now =
            LocalDateTime.now()

        val nextFixedOpportunity =
            OpportunityRuntimePlanner
                .next(
                    now =
                        now,
                    settings =
                        scheduleSettings,
                    minimumGapMinutes =
                        policyRepository
                            .loadMinimumGapMinutes(),
                    consumedEarlyOpportunityKeys =
                        consumedEarly,
                )

        snoozedProposal =
            normalizeSnooze(
                snoozedProposal =
                    snoozedProposal,
                nextFixedOpportunity =
                    nextFixedOpportunity,
                appStateRepository =
                    appStateRepository,
            )

        val fixedEvent =
            nextFixedOpportunity
                ?.let { opportunity ->
                    ScheduledEvent(
                        triggerAtEpochMillis =
                            opportunity
                                .atZone(
                                    ZoneId.systemDefault()
                                )
                                .toInstant()
                                .toEpochMilli(),
                        eventType =
                            EVENT_TYPE_FIXED,
                        proposalKey =
                            opportunity.toString(),
                        activityId =
                            null,
                    )
                }

        val snoozeEvent =
            snoozedProposal
                ?.let { snooze ->
                    ScheduledEvent(
                        triggerAtEpochMillis =
                            snooze
                                .triggerAtEpochMillis,
                        eventType =
                            EVENT_TYPE_SNOOZE,
                        proposalKey =
                            snooze.proposalKey,
                        activityId =
                            snooze.activityId,
                    )
                }

        val nextEvent =
            listOfNotNull(
                fixedEvent,
                snoozeEvent,
            )
                .minByOrNull {
                    it.triggerAtEpochMillis
                }

        if (nextEvent == null) {
            cancel()
            return
        }

        schedule(
            nextEvent
        )
    }

    fun cancel() {

        val alarmManager =
            appContext
                .getSystemService(
                    AlarmManager::class.java
                )

        alarmManager.cancel(
            alarmPendingIntent(
                event =
                    ScheduledEvent(
                        triggerAtEpochMillis =
                            0L,
                        eventType =
                            EVENT_TYPE_FIXED,
                        proposalKey =
                            "",
                        activityId =
                            null,
                    ),
            )
        )
    }

    private suspend fun normalizeSnooze(
        snoozedProposal: SnoozedProposal?,
        nextFixedOpportunity: LocalDateTime?,
        appStateRepository: AppStateRepository,
    ): SnoozedProposal? {

        val snooze =
            snoozedProposal
                ?: return null

        val nowMillis =
            System.currentTimeMillis()

        if (
            snooze.triggerAtEpochMillis <
            nowMillis -
                MAX_LATE_DELIVERY_MINUTES *
                60_000L
        ) {
            appStateRepository
                .clearSnoozedProposal()

            return null
        }

        if (nextFixedOpportunity == null) {
            return snooze
        }

        val fixedMillis =
            nextFixedOpportunity
                .atZone(
                    ZoneId.systemDefault()
                )
                .toInstant()
                .toEpochMilli()

        val gapMinutes =
            Duration
                .ofMillis(
                    kotlin.math.abs(
                        fixedMillis -
                            snooze.triggerAtEpochMillis
                    )
                )
                .toMinutes()

        if (
            gapMinutes <
            MIN_FIXED_SNOOZE_GAP_MINUTES
        ) {
            appStateRepository
                .clearSnoozedProposal()

            return null
        }

        return snooze
    }

    private fun schedule(
        event: ScheduledEvent,
    ) {

        val alarmManager =
            appContext
                .getSystemService(
                    AlarmManager::class.java
                )

        val safeTriggerAt =
            event
                .triggerAtEpochMillis
                .coerceAtLeast(
                    System.currentTimeMillis() +
                        MIN_FUTURE_DELAY_MILLIS
                )

        if (
            !exactAlarmsAllowed(
                appContext
            )
        ) {
            cancel()
            return
        }

        alarmManager
            .setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                safeTriggerAt,
                alarmPendingIntent(
                    event
                ),
            )
    }

    private fun alarmPendingIntent(
        event: ScheduledEvent,
    ): PendingIntent {

        val intent =
            Intent(
                appContext,
                ProposalAlarmReceiver::class.java,
            ).apply {
                action =
                    ACTION_DELIVER

                putExtra(
                    EXTRA_EVENT_TYPE,
                    event.eventType,
                )

                putExtra(
                    EXTRA_PROPOSAL_KEY,
                    event.proposalKey,
                )

                event.activityId
                    ?.let {
                        putExtra(
                            EXTRA_ACTIVITY_ID,
                            it,
                        )
                    }
            }

        return PendingIntent
            .getBroadcast(
                appContext,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE,
            )
    }

    private data class ScheduledEvent(
        val triggerAtEpochMillis: Long,
        val eventType: String,
        val proposalKey: String,
        val activityId: String?,
    )

    companion object {

        const val ACTION_DELIVER =
            "org.sedentaryapp.action.DELIVER_PROPOSAL"

        const val ACTION_SNOOZE =
            "org.sedentaryapp.action.SNOOZE_PROPOSAL"

        const val ACTION_NOT_NOW =
            "org.sedentaryapp.action.NOT_NOW_PROPOSAL"

        const val ACTION_DO_NOW =
            "org.sedentaryapp.action.DO_NOW_PROPOSAL"

        const val ACTION_DISMISS =
            "org.sedentaryapp.action.DISMISS_PROPOSAL"

        const val EXTRA_EVENT_TYPE =
            "proposal_event_type"

        const val EXTRA_PROPOSAL_KEY =
            "proposal_key"

        const val EXTRA_ACTIVITY_ID =
            "activity_id"

        const val EVENT_TYPE_FIXED =
            "fixed"

        const val EVENT_TYPE_SNOOZE =
            "snooze"

        const val SNOOZE_MINUTES =
            2L

        const val MAX_LATE_DELIVERY_MINUTES =
            10L

        private const val
            MIN_FIXED_SNOOZE_GAP_MINUTES = 20L

        private const val
            MIN_FUTURE_DELAY_MILLIS = 1_000L

        private const val
            ALARM_REQUEST_CODE = 4101

        fun exactAlarmsAllowed(
            context: Context,
        ): Boolean {

            if (
                Build.VERSION.SDK_INT <
                Build.VERSION_CODES.S
            ) {
                return true
            }

            return context
                .getSystemService(
                    AlarmManager::class.java
                )
                .canScheduleExactAlarms()
        }

        fun notificationsAllowed(
            context: Context,
        ): Boolean {

            val runtimePermissionGranted =
                Build.VERSION.SDK_INT <
                    Build.VERSION_CODES.TIRAMISU ||
                    context.checkSelfPermission(
                        Manifest.permission
                            .POST_NOTIFICATIONS
                    ) ==
                    PackageManager
                        .PERMISSION_GRANTED

            if (!runtimePermissionGranted) {
                return false
            }

            return context
                .getSystemService(
                    NotificationManager::class.java
                )
                .areNotificationsEnabled()
        }
    }
}
