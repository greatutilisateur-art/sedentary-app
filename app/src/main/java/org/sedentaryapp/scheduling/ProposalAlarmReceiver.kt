package org.sedentaryapp.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.sedentaryapp.data.ActivityGuidanceMode
import org.sedentaryapp.data.AppStateRepository
import org.sedentaryapp.data.CatalogRepository
import org.sedentaryapp.data.PolicyRepository
import org.sedentaryapp.notification.NotificationPublisher

class ProposalAlarmReceiver :
    BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {

        val pendingResult =
            goAsync()

        val appContext =
            context.applicationContext

        CoroutineScope(
            SupervisorJob() +
                    Dispatchers.IO
        ).launch {
            try {
                when (intent.action) {

                    ProposalAlarmScheduler
                        .ACTION_DELIVER ->
                        handleDelivery(
                            context =
                                appContext,
                            intent =
                                intent,
                        )

                    ProposalAlarmScheduler
                        .ACTION_SNOOZE ->
                        handleSnoozeAction(
                            context =
                                appContext,
                            intent =
                                intent,
                        )

                    ProposalAlarmScheduler
                        .ACTION_NOT_NOW ->
                        handleNotNowAction(
                            context =
                                appContext,
                            intent =
                                intent,
                        )

                    ProposalAlarmScheduler
                        .ACTION_DO_NOW ->
                        handleDoNowAction(
                            context =
                                appContext,
                            intent =
                                intent,
                        )

                    ProposalAlarmScheduler
                        .ACTION_DISMISS ->
                        handleDismissAction(
                            context =
                                appContext,
                            intent =
                                intent,
                        )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleDelivery(
        context: Context,
        intent: Intent,
    ) {

        val eventType =
            intent.getStringExtra(
                ProposalAlarmScheduler
                    .EXTRA_EVENT_TYPE
            )
                ?: return

        when (eventType) {

            ProposalAlarmScheduler
                .EVENT_TYPE_FIXED ->
                deliverFixedProposal(
                    context =
                        context,
                    proposalKey =
                        intent.getStringExtra(
                            ProposalAlarmScheduler
                                .EXTRA_PROPOSAL_KEY
                        )
                            ?: return,
                )

            ProposalAlarmScheduler
                .EVENT_TYPE_SNOOZE ->
                deliverSnoozedProposal(
                    context =
                        context,
                    proposalKey =
                        intent.getStringExtra(
                            ProposalAlarmScheduler
                                .EXTRA_PROPOSAL_KEY
                        )
                            ?: return,
                )

            ProposalAlarmScheduler
                .EVENT_TYPE_ONE_OFF ->
                deliverOneOffProposal(
                    context =
                        context,
                    proposalKey =
                        intent.getStringExtra(
                            ProposalAlarmScheduler
                                .EXTRA_PROPOSAL_KEY
                        )
                            ?: return,
                )
        }
    }

    private suspend fun deliverFixedProposal(
        context: Context,
        proposalKey: String,
    ) {

        val dependencies =
            dependencies(
                context
            )

        if (
            !deliveryStillAllowed(
                dependencies
            )
        ) {
            dependencies.scheduler
                .reconcile()

            return
        }

        val opportunity =
            runCatching {
                LocalDateTime.parse(
                    proposalKey
                )
            }.getOrNull()
                ?: run {
                    dependencies.scheduler
                        .reconcile()

                    return
                }

        val settings =
            dependencies
                .appStateRepository
                .scheduleSettings
                .first()

        val consumedEarly =
            dependencies
                .appStateRepository
                .consumedEarlyOpportunityKeys
                .first()

        if (
            !OpportunityRuntimePlanner
                .isConfiguredOpportunity(
                    opportunity =
                        opportunity,
                    settings =
                        settings,
                ) ||
            proposalKey in
            consumedEarly
        ) {
            dependencies.scheduler
                .reconcile()

            return
        }

        val now =
            LocalDateTime.now()

        if (
            opportunity.isAfter(
                now.plusMinutes(1)
            )
        ) {
            dependencies.scheduler
                .reconcile()

            return
        }

        val lateMinutes =
            Duration
                .between(
                    opportunity,
                    now,
                )
                .toMinutes()

        if (
            lateMinutes >
            dependencies.policy
                .scheduling
                .technicalLatenessToleranceMinutes
        ) {
            dependencies.scheduler
                .reconcile()

            return
        }

        val history =
            dependencies
                .appStateRepository
                .proposalHistory
                .first()

        if (
            history.any {
                it.proposalKey ==
                        proposalKey
            }
        ) {
            dependencies.scheduler
                .reconcile()

            return
        }

        val activities =
            dependencies
                .catalogRepository
                .loadActivities()

        val activity =
            ProposalSelector
                .select(
                    opportunityKey =
                        proposalKey,
                    activities =
                        activities,
                    policy =
                        dependencies.policy,
                    activityPreferences =
                        dependencies
                            .appStateRepository
                            .activityPreferences
                            .first(),
                    proposalHistory =
                        history,
                    plannedOverride =
                        dependencies
                            .appStateRepository
                            .plannedActivityOverride
                            .first(),
                )
                ?: run {
                    dependencies.scheduler
                        .reconcile()

                    return
                }

        val activityId =
            activity.definition.id

        dependencies
            .appStateRepository
            .recordProposal(
                proposalKey =
                    proposalKey,
                activityId =
                    activityId,
                maxEntries =
                    dependencies
                        .policy
                        .scoring
                        .historyWindowProposals,
            )

        dependencies
            .appStateRepository
            .saveActiveProposal(
                activityId =
                    activityId,
                proposalKey =
                    proposalKey,
                proposedAtEpochMillis =
                    System.currentTimeMillis(),
            )

        dependencies
            .appStateRepository
            .clearPlannedActivityOverrideIfOpportunity(
                proposalKey
            )

        val alertSettings =
            dependencies
                .appStateRepository
                .notificationAlertSettings
                .first()

        val snoozeOption =
            snoozeOption(
                dependencies = dependencies,
                proposalKey = proposalKey,
            )

        NotificationPublisher
            .show(
                context =
                    context,
                activity =
                    activity,
                proposalKey =
                    proposalKey,
                allowSnooze =
                    snoozeOption.allowed,
                snoozeDurationMinutes =
                    snoozeOption.durationMinutes,
                soundEnabled =
                    alertSettings
                        .soundEnabled,
                vibrationEnabled =
                    alertSettings
                        .vibrationEnabled,
            )

        dependencies.scheduler
            .reconcile()
    }

    private suspend fun deliverOneOffProposal(
        context: Context,
        proposalKey: String,
    ) {

        val dependencies =
            dependencies(
                context
            )

        if (
            !deliveryStillAllowed(
                dependencies
            )
        ) {
            dependencies.scheduler
                .reconcile()

            return
        }

        val override =
            dependencies
                .appStateRepository
                .nextProposalOverride
                .first()

        if (
            override == null ||
            override.at.toString() !=
            proposalKey
        ) {
            dependencies.scheduler
                .reconcile()

            return
        }

        val opportunity =
            override.at

        val now =
            LocalDateTime.now()

        if (
            opportunity.isAfter(
                now.plusMinutes(1)
            )
        ) {
            dependencies.scheduler
                .reconcile()

            return
        }

        val lateMinutes =
            Duration
                .between(
                    opportunity,
                    now,
                )
                .toMinutes()

        if (
            lateMinutes >
            dependencies.policy
                .scheduling
                .technicalLatenessToleranceMinutes
        ) {
            dependencies
                .appStateRepository
                .clearNextProposalOverride()

            dependencies.scheduler
                .reconcile()

            return
        }

        val history =
            dependencies
                .appStateRepository
                .proposalHistory
                .first()

        if (
            history.any {
                it.proposalKey ==
                    proposalKey
            }
        ) {
            dependencies
                .appStateRepository
                .clearNextProposalOverride()

            dependencies.scheduler
                .reconcile()

            return
        }

        val activity =
            ProposalSelector
                .select(
                    opportunityKey =
                        proposalKey,
                    activities =
                        dependencies
                            .catalogRepository
                            .loadActivities(),
                    policy =
                        dependencies.policy,
                    activityPreferences =
                        dependencies
                            .appStateRepository
                            .activityPreferences
                            .first(),
                    proposalHistory =
                        history,
                    plannedOverride =
                        dependencies
                            .appStateRepository
                            .plannedActivityOverride
                            .first(),
                )
                ?: run {
                    dependencies
                        .appStateRepository
                        .clearNextProposalOverride()

                    dependencies.scheduler
                        .reconcile()

                    return
                }

        val activityId =
            activity.definition.id

        dependencies
            .appStateRepository
            .recordProposal(
                proposalKey =
                    proposalKey,
                activityId =
                    activityId,
                maxEntries =
                    dependencies
                        .policy
                        .scoring
                        .historyWindowProposals,
            )

        dependencies
            .appStateRepository
            .saveActiveProposal(
                activityId =
                    activityId,
                proposalKey =
                    proposalKey,
                proposedAtEpochMillis =
                    System.currentTimeMillis(),
            )

        dependencies
            .appStateRepository
            .clearPlannedActivityOverrideIfOpportunity(
                proposalKey
            )

        dependencies
            .appStateRepository
            .clearNextProposalOverride()

        val alertSettings =
            dependencies
                .appStateRepository
                .notificationAlertSettings
                .first()

        val snoozeOption =
            snoozeOption(
                dependencies = dependencies,
                proposalKey = proposalKey,
            )

        NotificationPublisher
            .show(
                context =
                    context,
                activity =
                    activity,
                proposalKey =
                    proposalKey,
                allowSnooze =
                    snoozeOption.allowed,
                snoozeDurationMinutes =
                    snoozeOption.durationMinutes,
                soundEnabled =
                    alertSettings
                        .soundEnabled,
                vibrationEnabled =
                    alertSettings
                        .vibrationEnabled,
            )

        dependencies.scheduler
            .reconcile()
    }

    private suspend fun deliverSnoozedProposal(
        context: Context,
        proposalKey: String,
    ) {

        val dependencies =
            dependencies(
                context
            )

        if (
            !deliveryStillAllowed(
                dependencies
            )
        ) {
            dependencies
                .appStateRepository
                .clearSnoozedProposal()

            dependencies.scheduler
                .reconcile()

            return
        }

        val snooze =
            dependencies
                .appStateRepository
                .snoozedProposal
                .first()

        if (
            snooze == null ||
            snooze.proposalKey !=
            proposalKey
        ) {
            dependencies.scheduler
                .reconcile()

            return
        }

        val effectiveActivityId =
            snooze.activityId

        val lateMillis =
            System.currentTimeMillis() -
                    snooze.triggerAtEpochMillis

        if (
            lateMillis >
            dependencies.policy
                .scheduling
                .technicalLatenessToleranceMinutes *
            60_000L
        ) {
            dependencies
                .appStateRepository
                .clearSnoozedProposal()

            dependencies.scheduler
                .reconcile()

            return
        }

        val activity =
            dependencies
                .catalogRepository
                .loadActivities()
                .firstOrNull {
                    it.definition.id ==
                            effectiveActivityId
                }
                ?: run {
                    dependencies
                        .appStateRepository
                        .clearSnoozedProposal()

                    dependencies.scheduler
                        .reconcile()

                    return
                }

        dependencies
            .appStateRepository
            .clearSnoozedProposal()

        dependencies
            .appStateRepository
            .saveActiveProposal(
                activityId =
                    effectiveActivityId,
                proposalKey =
                    proposalKey,
                proposedAtEpochMillis =
                    System.currentTimeMillis(),
            )

        val alertSettings =
            dependencies
                .appStateRepository
                .notificationAlertSettings
                .first()

        NotificationPublisher
            .show(
                context =
                    context,
                activity =
                    activity,
                proposalKey =
                    proposalKey,
                allowSnooze =
                    false,
                snoozeDurationMinutes =
                    dependencies.policy
                        .snooze
                        .defaultDurationMinutes,
                soundEnabled =
                    alertSettings
                        .soundEnabled,
                vibrationEnabled =
                    alertSettings
                        .vibrationEnabled,
            )

        dependencies.scheduler
            .reconcile()
    }

    private suspend fun handleSnoozeAction(
        context: Context,
        intent: Intent,
    ) {

        val proposalKey =
            intent.getStringExtra(
                ProposalAlarmScheduler
                    .EXTRA_PROPOSAL_KEY
            )
                ?: return

        val activityId =
            intent.getStringExtra(
                ProposalAlarmScheduler
                    .EXTRA_ACTIVITY_ID
            )
                ?: return

        val dependencies =
            dependencies(
                context
            )

        NotificationPublisher
            .cancel(
                context
            )

        if (
            !deliveryStillAllowed(
                dependencies
            )
        ) {
            dependencies
                .appStateRepository
                .clearSnoozedProposal()

            dependencies.scheduler
                .reconcile()

            return
        }

        val persistedDuration =
            dependencies
                .appStateRepository
                .snoozeDurationMinutes
                .first()

        val requestedDuration =
            intent.getIntExtra(
                ProposalAlarmScheduler
                    .EXTRA_SNOOZE_MINUTES,
                persistedDuration,
            )

        val snoozePolicy =
            dependencies.policy
                .snooze

        val durationMinutes =
            requestedDuration
                .takeIf { minutes ->
                    minutes >=
                        snoozePolicy
                            .minimumDurationMinutes &&
                        minutes <=
                        snoozePolicy
                            .maximumDurationMinutes
                }
                ?: persistedDuration

        val now =
            LocalDateTime.now()

        val target =
            now.plusMinutes(
                durationMinutes.toLong()
            )

        val canSnooze =
            SnoozePlanner
                .canSchedule(
                    target = target,
                    currentProposalKey =
                        proposalKey,
                    now = now,
                    settings =
                        dependencies
                            .appStateRepository
                            .scheduleSettings
                            .first(),
                    consumedEarlyOpportunityKeys =
                        dependencies
                            .appStateRepository
                            .consumedEarlyOpportunityKeys
                            .first(),
                    nextProposalOverride =
                        dependencies
                            .appStateRepository
                            .nextProposalOverride
                            .first(),
                    minimumGapMinutes =
                        dependencies.policy
                            .scheduling
                            .minimumGapBetweenDistinctOpportunitiesMinutes,
                )

        dependencies
            .appStateRepository
            .clearActiveProposalIfProposalKey(
                proposalKey
            )

        if (!canSnooze) {
            dependencies
                .appStateRepository
                .clearSnoozedProposalIfProposalKey(
                    proposalKey
                )

            dependencies.scheduler
                .reconcile()

            return
        }

        dependencies
            .appStateRepository
            .saveSnoozedProposal(
                proposalKey =
                    proposalKey,
                activityId =
                    activityId,
                triggerAtEpochMillis =
                    target
                        .atZone(
                            ZoneId.systemDefault()
                        )
                        .toInstant()
                        .toEpochMilli(),
            )

        dependencies.scheduler
            .reconcile()
    }

    private suspend fun handleDoNowAction(
        context: Context,
        intent: Intent,
    ) {

        val proposalKey =
            intent.getStringExtra(
                ProposalAlarmScheduler
                    .EXTRA_PROPOSAL_KEY
            )
                ?: return

        val activityId =
            intent.getStringExtra(
                ProposalAlarmScheduler
                    .EXTRA_ACTIVITY_ID
            )
                ?: return

        val dependencies =
            dependencies(
                context
            )

        val activeProposal =
            dependencies
                .appStateRepository
                .activeProposal
                .first()

        if (
            activeProposal == null ||
            activeProposal.proposalKey !=
                proposalKey ||
            activeProposal.activityId !=
                activityId
        ) {
            NotificationPublisher
                .cancel(
                    context
                )

            dependencies.scheduler
                .reconcile()

            return
        }

        val activity =
            dependencies
                .catalogRepository
                .loadActivities()
                .firstOrNull {
                    it.definition.id ==
                        activityId
                }
                ?: return

        if (
            activity.guidanceMode !=
            ActivityGuidanceMode.SIMPLE
        ) {
            return
        }

        val completedAt =
            System.currentTimeMillis()

        dependencies
            .appStateRepository
            .saveLastActivity(
                activityId =
                    activityId,
                proposalKey =
                    proposalKey,
                proposedAtEpochMillis =
                    completedAt,
            )

        dependencies
            .appStateRepository
            .clearActiveProposalIfProposalKey(
                proposalKey
            )

        dependencies
            .appStateRepository
            .clearSnoozedProposalIfProposalKey(
                proposalKey
            )

        NotificationPublisher
            .cancel(
                context
            )

        dependencies.scheduler
            .reconcile()
    }

    private suspend fun handleDismissAction(
        context: Context,
        intent: Intent,
    ) {

        val proposalKey =
            intent.getStringExtra(
                ProposalAlarmScheduler
                    .EXTRA_PROPOSAL_KEY
            )
                ?: return

        val dependencies =
            dependencies(
                context
            )

        dependencies
            .appStateRepository
            .clearSnoozedProposalIfProposalKey(
                proposalKey
            )

        dependencies
            .appStateRepository
            .clearActiveProposalIfProposalKey(
                proposalKey
            )

        dependencies.scheduler
            .reconcile()
    }

    private suspend fun handleNotNowAction(
        context: Context,
        intent: Intent,
    ) {

        val proposalKey =
            intent.getStringExtra(
                ProposalAlarmScheduler
                    .EXTRA_PROPOSAL_KEY
            )
                ?: return

        val dependencies =
            dependencies(
                context
            )

        NotificationPublisher
            .cancel(
                context
            )

        dependencies
            .appStateRepository
            .clearSnoozedProposalIfProposalKey(
                proposalKey
            )

        dependencies
            .appStateRepository
            .clearActiveProposalIfProposalKey(
                proposalKey
            )

        dependencies.scheduler
            .reconcile()
    }

    private suspend fun snoozeOption(
        dependencies: Dependencies,
        proposalKey: String,
    ): SnoozeOption {

        val durationMinutes =
            dependencies
                .appStateRepository
                .snoozeDurationMinutes
                .first()

        val now =
            LocalDateTime.now()

        val target =
            now.plusMinutes(
                durationMinutes.toLong()
            )

        val allowed =
            SnoozePlanner
                .canSchedule(
                    target = target,
                    currentProposalKey =
                        proposalKey,
                    now = now,
                    settings =
                        dependencies
                            .appStateRepository
                            .scheduleSettings
                            .first(),
                    consumedEarlyOpportunityKeys =
                        dependencies
                            .appStateRepository
                            .consumedEarlyOpportunityKeys
                            .first(),
                    nextProposalOverride =
                        dependencies
                            .appStateRepository
                            .nextProposalOverride
                            .first(),
                    minimumGapMinutes =
                        dependencies.policy
                            .scheduling
                            .minimumGapBetweenDistinctOpportunitiesMinutes,
                )

        return SnoozeOption(
            durationMinutes =
                durationMinutes,
            allowed = allowed,
        )
    }

    private suspend fun deliveryStillAllowed(
        dependencies: Dependencies,
    ): Boolean {

        return dependencies
            .appStateRepository
            .onboardingCompleted
            .first() &&
                !dependencies
                    .appStateRepository
                    .proposalsPaused
                    .first() &&
                ProposalAlarmScheduler
                    .notificationsAllowed(
                        dependencies.context
                    ) &&
                ProposalAlarmScheduler
                    .exactAlarmsAllowed(
                        dependencies.context
                    )
    }

    private fun dependencies(
        context: Context,
    ): Dependencies {

        val policyRepository =
            PolicyRepository(
                context
            )

        val policy =
            policyRepository
                .loadDecisionPolicy()

        val appStateRepository =
            AppStateRepository(
                context =
                    context,
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

        return Dependencies(
            context =
                context,
            appStateRepository =
                appStateRepository,
            catalogRepository =
                CatalogRepository(
                    context
                ),
            policy = policy,
            scheduler =
                ProposalAlarmScheduler(
                    context
                ),
        )
    }

    private data class SnoozeOption(
        val durationMinutes: Int,
        val allowed: Boolean,
    )

    private data class Dependencies(
        val context: Context,
        val appStateRepository:
        AppStateRepository,
        val catalogRepository:
        CatalogRepository,
        val policy:
        org.sedentaryapp.engine.model.DecisionPolicy,
        val scheduler:
        ProposalAlarmScheduler,
    )
}