package org.sedentaryapp

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
import androidx.compose.runtime.remember
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
import org.sedentaryapp.engine.schedule.ScheduleValidator
import org.sedentaryapp.engine.schedule.SlotValidation
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
                var scheduleDraft by
                remember {
                    mutableStateOf<ScheduleSettings?>(
                        null
                    )
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
                            activity = activity,
                            postureLabel =
                                postureLabel(activity),
                            isActiveProposal =
                                activityState
                                    .isActiveProposal,
                            hasAlternative =
                                activityState
                                    .hasAlternative,
                            onOtherActivity =
                                viewModel::onOtherActivity,
                            onNotNow = {
                                viewModel.onNotNow()
                                screen = "home"
                            },
                        )
                    }

                    screen == "schedule" -> {

                        val draft =
                            scheduleDraft ?: scheduleSettings

                        BackHandler {
                            scheduleDraft = null
                            screen = "home"
                        }

                        ScheduleScreen(
                            settings = draft,
                            minimumGapMinutes = minimumGapMinutes,

                            onWeekdayChanged = { day, enabled ->
                                scheduleDraft =
                                    draft.copy(
                                        selectedWeekdays =
                                            if (enabled) {
                                                draft.selectedWeekdays + day
                                            } else {
                                                draft.selectedWeekdays - day
                                            }
                                    )
                            },

                            onWorkStartChanged = { time ->
                                scheduleDraft =
                                    draft.copy(
                                        workStart = time
                                    )
                            },

                            onWorkEndChanged = { time ->
                                scheduleDraft =
                                    draft.copy(
                                        workEnd = time
                                    )
                            },

                            onLunchEnabledChanged = { enabled ->
                                scheduleDraft =
                                    draft.copy(
                                        lunchEnabled = enabled
                                    )
                            },

                            onLunchStartChanged = { time ->
                                scheduleDraft =
                                    draft.copy(
                                        lunchStart = time
                                    )
                            },

                            onLunchEndChanged = { time ->
                                scheduleDraft =
                                    draft.copy(
                                        lunchEnd = time
                                    )
                            },

                            onSlotEnabledChanged = { index, enabled ->

                                val currentlyActive =
                                    draft.slots.count { slot ->
                                        slot.enabled
                                    }

                                if (
                                    enabled ||
                                    currentlyActive > 1
                                ) {
                                    scheduleDraft =
                                        draft.copy(
                                            slots =
                                                draft.slots.map { slot ->
                                                    if (slot.index == index) {
                                                        slot.copy(
                                                            enabled = enabled
                                                        )
                                                    } else {
                                                        slot
                                                    }
                                                }
                                        )
                                }
                            },

                            onSlotTimeChanged = { index, time ->
                                scheduleDraft =
                                    draft.copy(
                                        slots =
                                            draft.slots.map { slot ->
                                                if (slot.index == index) {
                                                    slot.copy(
                                                        time = time
                                                    )
                                                } else {
                                                    slot
                                                }
                                            }
                                    )
                            },

                            onValidate = {
                                coroutineScope.launch {
                                    appStateRepository
                                        .saveScheduleSettings(
                                            draft
                                        )

                                    scheduleDraft = null
                                    screen = "home"
                                }
                            },
                        )
                    }
                    else -> {

                        HomeScreen(
                            lastActivity =
                                activity,
                            scheduleStatusText =
                                scheduleStatus(
                                    settings =
                                        scheduleSettings,
                                    minimumGapMinutes =
                                        minimumGapMinutes,
                                ),
                            onOpenLastActivity = {
                                screen = "activity"
                            },
                            onOpenSettings = {
                                scheduleDraft =
                                    scheduleSettings

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

    val workdayValid =
        ScheduleValidator.isWorkdayValid(
            workStart =
                settings.workStart,
            workEnd =
                settings.workEnd,
        )

    val lunchValid =
        ScheduleValidator.isLunchValid(
            workStart =
                settings.workStart,
            workEnd =
                settings.workEnd,
            lunchEnabled =
                settings.lunchEnabled,
            lunchStart =
                settings.lunchStart,
            lunchEnd =
                settings.lunchEnd,
        )

    if (
        !workdayValid ||
        !lunchValid
    ) {
        return "Tes horaires doivent être ajustés."
    }

    val activeSlots =
        settings.slots
            .filter { it.enabled }

    val invalidSlotExists =
        activeSlots.any { slot ->

            ScheduleValidator.validateSlot(
                slotTime = slot.time,
                workStart =
                    settings.workStart,
                workEnd =
                    settings.workEnd,
                lunchEnabled =
                    settings.lunchEnabled,
                lunchStart =
                    settings.lunchStart,
                lunchEnd =
                    settings.lunchEnd,
            ) != SlotValidation.VALID
        }

    val activeTimes =
        activeSlots.map {
            it.time
        }

    val spacingValid =
        NextOpportunityPlanner
            .hasValidSpacing(
                activeSlotTimes =
                    activeTimes,
                minimumGapMinutes =
                    minimumGapMinutes,
            )

    if (
        invalidSlotExists ||
        !spacingValid
    ) {
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