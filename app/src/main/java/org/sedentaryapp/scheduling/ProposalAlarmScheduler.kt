package org.sedentaryapp.scheduling

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import org.sedentaryapp.data.AppStateRepository
import org.sedentaryapp.data.NextProposalOverride
import org.sedentaryapp.data.PolicyRepository
import org.sedentaryapp.data.ScheduleSettings
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

        val policy =
            policyRepository
                .loadDecisionPolicy()

        val appStateRepository =
            AppStateRepository(
                context = appContext,
                defaultSlotTimes =
                    policyRepository
                        .loadDefaultSlots(),
                defaultSnoozeDurationMinutes =
                    policy.snooze
                        .defaultDurationMinutes,
                minimumSnoozeDurationMinutes =
                    policy.snooze
                        .minimumDurationMinutes,
                maximumSnoozeDurationMinutes =
                    policy.snooze
                        .maximumDurationMinutes,
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

        var nextProposalOverride =
            appStateRepository
                .nextProposalOverride
                .first()

        val now =
            LocalDateTime.now()

        nextProposalOverride =
            normalizeNextProposalOverride(
                nextProposalOverride =
                    nextProposalOverride,
                now = now,
                technicalLatenessToleranceMinutes =
                    policy.scheduling
                        .technicalLatenessToleranceMinutes,
                appStateRepository =
                    appStateRepository,
            )

        val minimumGapMinutes =
            policy.scheduling
                .minimumGapBetweenDistinctOpportunitiesMinutes

        val nextFixedOpportunity =
            OpportunityRuntimePlanner
                .next(
                    now = now,
                    settings =
                        scheduleSettings,
                    minimumGapMinutes =
                        minimumGapMinutes,
                    consumedEarlyOpportunityKeys =
                        consumedEarly,
                )

        snoozedProposal =
            normalizeSnooze(
                snoozedProposal =
                    snoozedProposal,
                now = now,
                settings =
                    scheduleSettings,
                consumedEarlyOpportunityKeys =
                    consumedEarly,
                nextProposalOverride =
                    nextProposalOverride,
                minimumGapMinutes =
                    minimumGapMinutes,
                technicalLatenessToleranceMinutes =
                    policy.scheduling
                        .technicalLatenessToleranceMinutes,
                appStateRepository =
                    appStateRepository,
            )

        val fixedEvent =
            if (nextProposalOverride == null) {
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
                            activityId = null,
                        )
                    }
            } else {
                null
            }

        val overrideEvent =
            nextProposalOverride
                ?.let { override ->
                    ScheduledEvent(
                        triggerAtEpochMillis =
                            override.at
                                .atZone(
                                    ZoneId.systemDefault()
                                )
                                .toInstant()
                                .toEpochMilli(),
                        eventType =
                            EVENT_TYPE_ONE_OFF,
                        proposalKey =
                            override.at.toString(),
                        activityId = null,
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
                overrideEvent ?: fixedEvent,
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
                        triggerAtEpochMillis = 0L,
                        eventType =
                            EVENT_TYPE_FIXED,
                        proposalKey = "",
                        activityId = null,
                    ),
            )
        )
    }

    private suspend fun normalizeNextProposalOverride(
        nextProposalOverride: NextProposalOverride?,
        now: LocalDateTime,
        technicalLatenessToleranceMinutes: Long,
        appStateRepository: AppStateRepository,
    ): NextProposalOverride? {

        val override =
            nextProposalOverride
                ?: return null

        if (
            override.at.isBefore(
                now.minusMinutes(
                    technicalLatenessToleranceMinutes
                )
            )
        ) {
            appStateRepository
                .clearNextProposalOverride()

            return null
        }

        return override
    }

    private suspend fun normalizeSnooze(
        snoozedProposal: SnoozedProposal?,
        now: LocalDateTime,
        settings: ScheduleSettings,
        consumedEarlyOpportunityKeys: Set<String>,
        nextProposalOverride: NextProposalOverride?,
        minimumGapMinutes: Long,
        technicalLatenessToleranceMinutes: Long,
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
                technicalLatenessToleranceMinutes *
                60_000L
        ) {
            appStateRepository
                .clearSnoozedProposal()

            return null
        }

        val snoozeAt =
            Instant
                .ofEpochMilli(
                    snooze.triggerAtEpochMillis
                )
                .atZone(
                    ZoneId.systemDefault()
                )
                .toLocalDateTime()

        // Si l'heure est arrivée (ou légèrement dépassée), on laisse le
        // receiver traiter la livraison et sa tolérance de retard.
        if (!snoozeAt.isAfter(now)) {
            return snooze
        }

        val stillCompatible =
            SnoozePlanner
                .canSchedule(
                    target = snoozeAt,
                    currentProposalKey =
                        snooze.proposalKey,
                    now = now,
                    settings = settings,
                    consumedEarlyOpportunityKeys =
                        consumedEarlyOpportunityKeys,
                    nextProposalOverride =
                        nextProposalOverride,
                    minimumGapMinutes =
                        minimumGapMinutes,
                )

        if (!stillCompatible) {
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

        const val EXTRA_SNOOZE_MINUTES =
            "snooze_minutes"

        const val EVENT_TYPE_FIXED =
            "fixed"

        const val EVENT_TYPE_SNOOZE =
            "snooze"

        const val EVENT_TYPE_ONE_OFF =
            "one_off"

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
