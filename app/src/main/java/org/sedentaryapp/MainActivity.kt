package org.sedentaryapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.launch
import org.sedentaryapp.data.AppStateRepository
import org.sedentaryapp.data.CatalogActivity
import org.sedentaryapp.data.CatalogRepository
import org.sedentaryapp.data.PolicyRepository
import org.sedentaryapp.data.ScheduleSettings
import org.sedentaryapp.engine.schedule.NextOpportunityPlanner
import org.sedentaryapp.notification.NotificationPublisher
import org.sedentaryapp.ui.activity.ActivityScreen
import org.sedentaryapp.ui.activity.ActivityViewModel
import org.sedentaryapp.ui.activity.GuidedSessionState
import org.sedentaryapp.ui.home.HomeScreen
import org.sedentaryapp.ui.schedule.ScheduleScreen
import org.sedentaryapp.ui.theme.SedentaryAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)

        var pendingNotificationActivity:
                CatalogActivity? = null

        val notificationPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts
                    .RequestPermission()
            ) { granted ->

                val activityToShow =
                    pendingNotificationActivity

                pendingNotificationActivity =
                    null

                if (
                    granted &&
                    activityToShow != null
                ) {
                    NotificationPublisher.show(
                        context = this,
                        activity = activityToShow,
                    )
                }
            }

        val activities =
            CatalogRepository(this)
                .loadActivities()

        val policyRepository =
            PolicyRepository(this)

        val policy =
            policyRepository
                .loadDecisionPolicy()

        val defaultSlots =
            policyRepository
                .loadDefaultSlots()

        val minimumGapMinutes =
            policyRepository
                .loadMinimumGapMinutes()

        val appStateRepository =
            AppStateRepository(
                context = applicationContext,
                defaultSlotTimes = defaultSlots,
            )

        val factory =
            ActivityViewModel.factory(
                activities = activities,
                policy = policy,
                appStateRepository =
                    appStateRepository,
            )

        val viewModel =
            ViewModelProvider(
                this,
                factory,
            )[ActivityViewModel::class.java]

        setContent {
            SedentaryAppTheme {

                val activityState by
                viewModel.uiState
                    .collectAsState()

                val scheduleSettings by
                appStateRepository
                    .scheduleSettings
                    .collectAsState(
                        initial =
                            appStateRepository
                                .defaultScheduleSettings
                    )

                val plannedActivityOverride by
                appStateRepository
                    .plannedActivityOverride
                    .collectAsState(
                        initial = null
                    )

                val consumedEarlyOpportunityKeys by
                appStateRepository
                    .consumedEarlyOpportunityKeys
                    .collectAsState(
                        initial =
                            emptySet()
                    )

                val coroutineScope =
                    rememberCoroutineScope()

                var screen by
                rememberSaveable {
                    mutableStateOf("home")
                }

                val nextOpportunity =
                    nextOpportunity(
                        settings =
                            scheduleSettings,
                        minimumGapMinutes =
                            minimumGapMinutes,
                        consumedEarlyOpportunityKeys =
                            consumedEarlyOpportunityKeys,
                    )

                val nextOpportunityKey =
                    nextOpportunity
                        ?.toString()

                val matchingOverrideActivityId =
                    if (
                        nextOpportunityKey != null &&
                        plannedActivityOverride
                            ?.opportunityKey ==
                        nextOpportunityKey
                    ) {
                        plannedActivityOverride
                            ?.activityId
                    } else {
                        null
                    }

                val nextActivity =
                    if (
                        nextOpportunityKey != null
                    ) {
                        viewModel
                            .plannedFutureActivity(
                                opportunityKey =
                                    nextOpportunityKey,
                                overrideActivityId =
                                    matchingOverrideActivityId,
                            )
                    } else {
                        null
                    }

                val activity =
                    activityState.activity

                when {

                    screen == "activity" &&
                            activity != null -> {

                        BackHandler {

                            if (
                                activityState
                                    .guidedSessionState ==
                                GuidedSessionState
                                    .TIME_ELAPSED
                            ) {
                                viewModel
                                    .closeGuidedSession()
                            }

                            screen = "home"
                        }

                        ActivityScreen(
                            activity =
                                activity,

                            postureLabel =
                                postureLabel(
                                    activity
                                ),

                            canGoPrevious =
                                activityState
                                    .canGoPrevious,

                            canGoNext =
                                activityState
                                    .canGoNext,

                            canRequestAnother =
                                activityState
                                    .canRequestAnother,

                            showNotNow =
                                activityState
                                    .showNotNow,

                            guidedSessionState =
                                activityState
                                    .guidedSessionState,

                            remainingSeconds =
                                activityState
                                    .remainingSeconds,

                            shouldPlayCompletionHaptic =
                                activityState
                                    .shouldPlayCompletionHaptic,

                            onPreviousActivity =
                                viewModel::onPreviousActivity,

                            onNextActivity =
                                viewModel::onNextActivity,

                            onOtherActivity =
                                viewModel::onOtherActivity,

                            onNotNow = {
                                viewModel.onNotNow()
                                screen = "home"
                            },

                            onStartGuidedSession =
                                viewModel::startGuidedSession,

                            onPauseGuidedSession =
                                viewModel::pauseGuidedSession,

                            onResumeGuidedSession =
                                viewModel::resumeGuidedSession,

                            onEndGuidedSession = {
                                viewModel
                                    .endGuidedSessionEarly()

                                finish()
                            },

                            onCloseGuidedSession = {
                                viewModel
                                    .closeGuidedSession()

                                finish()
                            },

                            onCompletionHapticPlayed =
                                viewModel::markCompletionHapticDelivered,
                        )
                    }

                    screen == "schedule" -> {

                        BackHandler {
                            screen = "home"
                        }

                        ScheduleScreen(
                            settings =
                                scheduleSettings,

                            minimumGapMinutes =
                                minimumGapMinutes,

                            onWeekdayChanged = {
                                    day,
                                    enabled,
                                ->

                                coroutineScope.launch {
                                    appStateRepository
                                        .setWeekdayEnabled(
                                            day,
                                            enabled,
                                        )
                                }
                            },

                            onSlotTimeChanged = {
                                    index,
                                    time,
                                ->

                                coroutineScope.launch {
                                    appStateRepository
                                        .setSlotTime(
                                            index,
                                            time,
                                        )
                                }
                            },

                            onAddSlot = { time ->

                                coroutineScope.launch {
                                    appStateRepository
                                        .addSlot(
                                            time
                                        )
                                }
                            },

                            onRemoveSlot = { index ->

                                coroutineScope.launch {
                                    appStateRepository
                                        .removeSlot(
                                            index
                                        )
                                }
                            },

                            onBack = {
                                screen = "home"
                            },
                        )
                    }

                    else -> {

                        HomeScreen(
                            nextActivity =
                                nextActivity,

                            nextActivityWhenText =
                                nextOpportunity
                                    ?.let {
                                        opportunityLabel(
                                            it
                                        )
                                    },

                            lastActivity =
                                activityState
                                    .lastProposedActivity,

                            onOpenNextActivity = {

                                if (
                                    nextOpportunityKey != null &&
                                    nextActivity != null
                                ) {
                                    viewModel
                                        .openFuturePreview(
                                            opportunityKey =
                                                nextOpportunityKey,
                                            plannedActivityId =
                                                nextActivity
                                                    .definition
                                                    .id,
                                        )

                                    screen =
                                        "activity"
                                }
                            },

                            onOpenLastActivity = {
                                viewModel
                                    .openLastActivity()

                                screen = "activity"
                            },

                            onOpenSettings = {
                                screen = "schedule"
                            },

                            onSimulateProposal = {
                                viewModel
                                    .startDemoProposal()

                                screen = "activity"
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun nextOpportunity(
    settings: ScheduleSettings,
    minimumGapMinutes: Long,
    consumedEarlyOpportunityKeys:
    Set<String>,
): LocalDateTime? {

    if (
        settings.selectedWeekdays
            .isEmpty()
    ) {
        return null
    }

    val activeTimes =
        settings.slots
            .map {
                it.time
            }

    if (activeTimes.isEmpty()) {
        return null
    }

    val spacingValid =
        NextOpportunityPlanner
            .hasValidSpacing(
                activeSlotTimes =
                    activeTimes,
                minimumGapMinutes =
                    minimumGapMinutes,
            )

    if (!spacingValid) {
        return null
    }

    var cursor =
        LocalDateTime.now()

    repeat(
        consumedEarlyOpportunityKeys
            .size + 1
    ) {

        val candidate =
            NextOpportunityPlanner
                .next(
                    now =
                        cursor,
                    selectedWeekdays =
                        settings.selectedWeekdays,
                    activeSlotTimes =
                        activeTimes,
                )
                ?: return null

        if (
            candidate.toString() !in
            consumedEarlyOpportunityKeys
        ) {
            return candidate
        }

        cursor =
            candidate
    }

    return null
}

private fun opportunityLabel(
    opportunity: LocalDateTime,
): String {

    val time =
        "%02d:%02d".format(
            opportunity.hour,
            opportunity.minute,
        )

    val date =
        opportunity.toLocalDate()

    if (
        date ==
        LocalDate.now()
    ) {
        return "Aujourd’hui · $time"
    }

    val dayName =
        weekdayName(
            opportunity.dayOfWeek
        ).replaceFirstChar {
            it.uppercase()
        }

    return "$dayName ${date.dayOfMonth} ${monthName(date.monthValue)} · $time"
}

private fun weekdayName(
    day: DayOfWeek,
): String {
    return when (day) {
        DayOfWeek.MONDAY -> "lundi"
        DayOfWeek.TUESDAY -> "mardi"
        DayOfWeek.WEDNESDAY -> "mercredi"
        DayOfWeek.THURSDAY -> "jeudi"
        DayOfWeek.FRIDAY -> "vendredi"
        DayOfWeek.SATURDAY -> "samedi"
        DayOfWeek.SUNDAY -> "dimanche"
    }
}

private fun monthName(
    month: Int,
): String {
    return when (month) {
        1 -> "janvier"
        2 -> "février"
        3 -> "mars"
        4 -> "avril"
        5 -> "mai"
        6 -> "juin"
        7 -> "juillet"
        8 -> "août"
        9 -> "septembre"
        10 -> "octobre"
        11 -> "novembre"
        12 -> "décembre"
        else -> ""
    }
}

private fun postureLabel(
    activity: CatalogActivity,
): String? {
    return when {

        activity.definition
            .standingRequired ->
            "Debout nécessaire"

        activity.preferredPosture ==
                "standing" ->
            "Debout conseillé"

        else ->
            null
    }
}