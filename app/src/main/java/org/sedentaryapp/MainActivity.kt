package org.sedentaryapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import org.sedentaryapp.notification.NotificationPublisher
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
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
import org.sedentaryapp.ui.activity.ActivityScreen
import org.sedentaryapp.ui.activity.ActivityViewModel
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

                val coroutineScope =
                    rememberCoroutineScope()

                var screen by
                rememberSaveable {
                    mutableStateOf("home")
                }
                    val activity =
                    activityState.activity

                when {

                    screen == "activity" &&
                            activity != null -> {

                        BackHandler {
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
                                viewModel.endGuidedSessionEarly()

                                finish()
                            },

                            onCloseGuidedSession = {
                                viewModel.closeGuidedSession()

                                finish()
                            },
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
                            lastActivity =
                                activityState
                                    .lastProposedActivity,

                            scheduleStatusText =
                                scheduleStatus(
                                    settings =
                                        scheduleSettings,
                                    minimumGapMinutes =
                                        minimumGapMinutes,
                                ),

                            onOpenLastActivity = {
                                viewModel.openLastActivity()
                                screen = "activity"
                            },

                            onOpenSettings = {
                                screen = "schedule"
                            },

                            onSimulateProposal = {
                                viewModel.startDemoProposal()
                                screen = "activity"
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun scheduleStatus(
    settings: ScheduleSettings,
    minimumGapMinutes: Long,
): String {

    if (
        settings.selectedWeekdays
            .isEmpty()
    ) {
        return "Aucun jour de travail sélectionné.\nAucune proposition prévue."
    }

    val activeTimes =
        settings.slots
            .map {
                it.time
            }

    if (
        activeTimes.isEmpty()
    ) {
        return "Aucun créneau configuré."
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
        return "Certains créneaux doivent être ajustés."
    }

    val now =
        LocalDateTime.now()

    val next =
        NextOpportunityPlanner.next(
            now = now,
            selectedWeekdays =
                settings.selectedWeekdays,
            activeSlotTimes =
                activeTimes,
        ) ?: return "Aucune proposition prévue."

    val time =
        "%02d h %02d".format(
            next.hour,
            next.minute,
        )

    return if (
        next.toLocalDate() ==
        LocalDate.now()
    ) {
        "Prochaine proposition vers $time"
    } else {
        "Prochaine proposition ${weekdayName(next.dayOfWeek)} vers $time"
    }
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