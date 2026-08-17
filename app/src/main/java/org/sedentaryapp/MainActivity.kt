package org.sedentaryapp

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.sedentaryapp.data.AppStateRepository
import org.sedentaryapp.data.CatalogActivity
import org.sedentaryapp.data.CatalogRepository
import org.sedentaryapp.data.PolicyRepository
import org.sedentaryapp.data.ScheduleSettings
import org.sedentaryapp.notification.NotificationPublisher
import org.sedentaryapp.scheduling.OpportunityRuntimePlanner
import org.sedentaryapp.scheduling.ProposalAlarmScheduler
import org.sedentaryapp.ui.activity.ActivityScreen
import org.sedentaryapp.ui.activity.ActivityViewModel
import org.sedentaryapp.ui.activity.GuidedSessionState
import org.sedentaryapp.ui.home.HomeScreen
import org.sedentaryapp.ui.info.AboutScreen
import org.sedentaryapp.ui.info.WhyAppScreen
import org.sedentaryapp.ui.onboarding.ExactAlarmOnboardingScreen
import org.sedentaryapp.ui.onboarding.NotificationOnboardingScreen
import org.sedentaryapp.ui.onboarding.WelcomeScreen
import org.sedentaryapp.ui.schedule.ScheduleScreen
import org.sedentaryapp.ui.theme.SedentaryAppTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionAllowedState =
        MutableStateFlow(
            true
        )

    private val exactAlarmAllowedState =
        MutableStateFlow(
            true
        )

    private val advanceToExactAlarmOnboardingState =
        MutableStateFlow(
            false
        )

    private val onboardingCompletedState =
        MutableStateFlow<Boolean?>(
            null
        )

    private val pendingNotificationProposalKeyState =
        MutableStateFlow<String?>(
            null
        )

    private lateinit var proposalAlarmScheduler:
        ProposalAlarmScheduler

    private var completeOnboardingAfterPermissionResult =
        false

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)

        pendingNotificationProposalKeyState.value =
            intent.getStringExtra(
                NotificationPublisher
                    .EXTRA_PROPOSAL_KEY
            )

        notificationPermissionAllowedState.value =
            areNotificationsAllowed()

        exactAlarmAllowedState.value =
            canScheduleExactAlarms()

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

        proposalAlarmScheduler =
            ProposalAlarmScheduler(
                applicationContext
            )

        lifecycleScope.launch {
            onboardingCompletedState.value =
                appStateRepository
                    .onboardingCompleted
                    .first()
        }

        val notificationPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts
                    .RequestPermission()
            ) {
                notificationPermissionAllowedState.value =
                    areNotificationsAllowed()

                lifecycleScope.launch {
                    proposalAlarmScheduler
                        .reconcile()
                }

                if (
                    completeOnboardingAfterPermissionResult
                ) {
                    completeOnboardingAfterPermissionResult =
                        false

                    if (
                        notificationPermissionAllowedState.value
                    ) {
                        advanceToExactAlarmOnboardingState.value =
                            true
                    } else {
                        lifecycleScope.launch {
                            appStateRepository
                                .markOnboardingCompleted()

                            onboardingCompletedState.value =
                                true
                        }
                    }
                }
            }

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

                val proposalsPaused by
                    appStateRepository
                        .proposalsPaused
                        .collectAsState(
                            initial = false
                        )

                val notificationsAllowed by
                    notificationPermissionAllowedState
                        .collectAsState()

                val exactAlarmsAllowed by
                    exactAlarmAllowedState
                        .collectAsState()

                val advanceToExactAlarmOnboarding by
                    advanceToExactAlarmOnboardingState
                        .collectAsState()

                val onboardingCompleted by
                    onboardingCompletedState
                        .collectAsState()

                val pendingNotificationProposalKey by
                    pendingNotificationProposalKeyState
                        .collectAsState()

                val notificationPermissionRequested by
                    appStateRepository
                        .notificationPermissionRequested
                        .collectAsState(
                            initial = false
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

                val lastProposalAtEpochMillis by
                    appStateRepository
                        .lastProposalAtEpochMillis
                        .collectAsState(
                            initial = null
                        )

                val coroutineScope =
                    rememberCoroutineScope()

                var screen by
                    rememberSaveable {
                        mutableStateOf("home")
                    }

                var lastActivityClockMillis by
                    remember {
                        mutableStateOf(
                            System.currentTimeMillis()
                        )
                    }

                LaunchedEffect(
                    lastProposalAtEpochMillis
                ) {
                    lastActivityClockMillis =
                        System.currentTimeMillis()

                    val proposedAt =
                        lastProposalAtEpochMillis
                            ?: return@LaunchedEffect

                    val remainingMillis =
                        LAST_ACTIVITY_VISIBILITY_MILLIS -
                            (
                                System.currentTimeMillis() -
                                    proposedAt
                            )

                    if (remainingMillis > 0L) {
                        delay(
                            remainingMillis +
                                250L
                        )

                        lastActivityClockMillis =
                            System.currentTimeMillis()
                    }
                }

                LaunchedEffect(
                    pendingNotificationProposalKey,
                    onboardingCompleted,
                ) {
                    val proposalKey =
                        pendingNotificationProposalKey

                    if (
                        onboardingCompleted == true &&
                        proposalKey != null
                    ) {
                        viewModel
                            .startNewProposal(
                                proposalKey
                            )

                        screen =
                            "activity"

                        pendingNotificationProposalKeyState.value =
                            null
                    }
                }

                var onboardingStep by
                    rememberSaveable {
                        mutableStateOf(0)
                    }

                LaunchedEffect(
                    advanceToExactAlarmOnboarding,
                    onboardingCompleted,
                ) {
                    if (
                        onboardingCompleted == false &&
                        advanceToExactAlarmOnboarding
                    ) {
                        onboardingStep = 3
                        advanceToExactAlarmOnboardingState.value =
                            false
                    }
                }

                LaunchedEffect(
                    exactAlarmsAllowed,
                    onboardingCompleted,
                    onboardingStep,
                ) {
                    if (
                        onboardingCompleted == false &&
                        onboardingStep == 3 &&
                        exactAlarmsAllowed
                    ) {
                        appStateRepository
                            .markOnboardingCompleted()

                        onboardingCompletedState.value =
                            true
                    }
                }

                val nextOpportunity =
                    if (proposalsPaused) {
                        null
                    } else {
                        nextOpportunity(
                            settings =
                                scheduleSettings,
                            minimumGapMinutes =
                                minimumGapMinutes,
                            consumedEarlyOpportunityKeys =
                                consumedEarlyOpportunityKeys,
                        )
                    }

                LaunchedEffect(
                    scheduleSettings,
                    proposalsPaused,
                    consumedEarlyOpportunityKeys,
                    notificationsAllowed,
                    exactAlarmsAllowed,
                    onboardingCompleted,
                ) {
                    proposalAlarmScheduler
                        .reconcile()
                }

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

                val matchingOverrideIsEligible =
                    matchingOverrideActivityId
                        ?.let {
                            viewModel
                                .isActivityEligible(
                                    it
                                )
                        } == true

                LaunchedEffect(
                    nextOpportunityKey,
                    matchingOverrideActivityId,
                    activityState.activityPreferences,
                ) {
                    if (
                        matchingOverrideActivityId != null &&
                        !matchingOverrideIsEligible
                    ) {
                        appStateRepository
                            .clearPlannedActivityOverride()
                    }
                }

                val effectiveOverrideActivityId =
                    if (
                        matchingOverrideIsEligible
                    ) {
                        matchingOverrideActivityId
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
                                    effectiveOverrideActivityId,
                            )
                    } else {
                        null
                    }

                val activity =
                    activityState.activity

                val lastActivityAgeMillis =
                    lastProposalAtEpochMillis
                        ?.let { proposedAt ->
                            lastActivityClockMillis -
                                proposedAt
                        }

                val lastActivityIsRecent =
                    lastActivityAgeMillis != null &&
                        lastActivityAgeMillis >= 0L &&
                        lastActivityAgeMillis <=
                            LAST_ACTIVITY_VISIBILITY_MILLIS

                val visibleLastActivity =
                    if (lastActivityIsRecent) {
                        activityState
                            .lastProposedActivity
                    } else {
                        null
                    }

                val visibleLastActivityWhenText =
                    if (
                        visibleLastActivity != null &&
                        lastProposalAtEpochMillis != null
                    ) {
                        lastActivityLabel(
                            lastProposalAtEpochMillis!!
                        )
                    } else {
                        null
                    }

                val showingOnboardingConfig =
                    onboardingCompleted == false &&
                        onboardingStep == 1

                when {

                    onboardingCompleted == null -> {
                        Unit
                    }

                    onboardingCompleted == false &&
                        onboardingStep == 0 -> {

                        WelcomeScreen(
                            onConfigure = {
                                onboardingStep = 1
                            },
                        )
                    }

                    onboardingCompleted == false &&
                        onboardingStep == 2 -> {

                        BackHandler {
                            onboardingStep = 1
                        }

                        NotificationOnboardingScreen(
                            notificationsAlreadyAllowed =
                                notificationsAllowed,

                            onAllowNotifications = {

                                if (notificationsAllowed) {
                                    if (exactAlarmsAllowed) {
                                        coroutineScope.launch {
                                            appStateRepository
                                                .markOnboardingCompleted()

                                            onboardingCompletedState.value =
                                                true
                                        }
                                    } else {
                                        onboardingStep = 3
                                    }
                                } else {

                                    val runtimePermissionMissing =
                                        Build.VERSION.SDK_INT >=
                                            Build.VERSION_CODES.TIRAMISU &&
                                            checkSelfPermission(
                                                Manifest.permission
                                                    .POST_NOTIFICATIONS
                                            ) !=
                                            PackageManager.PERMISSION_GRANTED

                                    if (
                                        runtimePermissionMissing &&
                                        (
                                            !notificationPermissionRequested ||
                                                shouldShowRequestPermissionRationale(
                                                    Manifest.permission
                                                        .POST_NOTIFICATIONS
                                                )
                                        )
                                    ) {
                                        coroutineScope.launch {
                                            appStateRepository
                                                .markNotificationPermissionRequested()
                                        }

                                        completeOnboardingAfterPermissionResult =
                                            true

                                        notificationPermissionLauncher
                                            .launch(
                                                Manifest.permission
                                                    .POST_NOTIFICATIONS
                                            )
                                    } else {
                                        coroutineScope.launch {
                                            appStateRepository
                                                .markOnboardingCompleted()

                                            onboardingCompletedState.value =
                                                true
                                        }

                                        openNotificationSettings()
                                    }
                                }
                            },

                            onLater = {
                                coroutineScope.launch {
                                    appStateRepository
                                        .markOnboardingCompleted()

                                    onboardingCompletedState.value =
                                        true
                                }
                            },
                        )
                    }

                    onboardingCompleted == false &&
                        onboardingStep == 3 -> {

                        BackHandler {
                            onboardingStep = 2
                        }

                        ExactAlarmOnboardingScreen(
                            exactAlarmsAlreadyAllowed =
                                exactAlarmsAllowed,

                            onAllowExactAlarms = {
                                if (exactAlarmsAllowed) {
                                    coroutineScope.launch {
                                        appStateRepository
                                            .markOnboardingCompleted()

                                        onboardingCompletedState.value =
                                            true
                                    }
                                } else {
                                    openExactAlarmSettings()
                                }
                            },

                            onLater = {
                                coroutineScope.launch {
                                    appStateRepository
                                        .markOnboardingCompleted()

                                    onboardingCompletedState.value =
                                        true
                                }
                            },
                        )
                    }

                    screen == "why" -> {

                        BackHandler {
                            screen = "schedule"
                        }

                        WhyAppScreen(
                            onBack = {
                                screen = "schedule"
                            },
                        )
                    }

                    screen == "about" -> {

                        BackHandler {
                            screen = "schedule"
                        }

                        AboutScreen(
                            onBack = {
                                screen = "schedule"
                            },
                        )
                    }

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

                    showingOnboardingConfig ||
                        screen == "schedule" -> {

                        BackHandler {
                            if (showingOnboardingConfig) {
                                onboardingStep = 0
                            } else {
                                screen = "home"
                            }
                        }

                        ScheduleScreen(
                            settings =
                                scheduleSettings,

                            activityPreferences =
                                activityState
                                    .activityPreferences,

                            proposalsPaused =
                                proposalsPaused,

                            minimumGapMinutes =
                                minimumGapMinutes,

                            title =
                                if (showingOnboardingConfig) {
                                    "Configure tes pauses"
                                } else {
                                    "Tes réglages"
                                },

                            showPauseControl =
                                !showingOnboardingConfig,

                            bottomActionLabel =
                                if (showingOnboardingConfig) {
                                    "Continuer"
                                } else {
                                    "Fermer"
                                },

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

                            onIntensityChanged = {
                                    intensity,
                                    enabled,
                                ->

                                coroutineScope.launch {
                                    appStateRepository
                                        .setIntensityEnabled(
                                            intensity =
                                                intensity,
                                            enabled =
                                                enabled,
                                        )
                                }
                            },

                            onStationaryChanged = {
                                    enabled,
                                ->

                                coroutineScope.launch {
                                    appStateRepository
                                        .setStationaryEnabled(
                                            enabled
                                        )
                                }
                            },

                            onMovingChanged = {
                                    enabled,
                                ->

                                coroutineScope.launch {
                                    appStateRepository
                                        .setMovingEnabled(
                                            enabled
                                        )
                                }
                            },

                            onPauseChanged = {
                                    paused,
                                ->

                                coroutineScope.launch {
                                    appStateRepository
                                        .setProposalsPaused(
                                            paused
                                        )
                                }
                            },

                            showInfoLinks =
                                !showingOnboardingConfig,

                            onOpenWhy = {
                                screen = "why"
                            },

                            onOpenAbout = {
                                screen = "about"
                            },

                            onBack = {
                                if (showingOnboardingConfig) {
                                    onboardingStep = 2
                                } else {
                                    screen = "home"
                                }
                            },
                        )
                    }

                    else -> {

                        HomeScreen(
                            proposalsPaused =
                                proposalsPaused,

                            notificationPermissionRequired =
                                !proposalsPaused &&
                                    !notificationsAllowed &&
                                    nextOpportunity != null &&
                                    nextActivity != null,

                            exactAlarmPermissionRequired =
                                !proposalsPaused &&
                                    notificationsAllowed &&
                                    !exactAlarmsAllowed &&
                                    nextOpportunity != null &&
                                    nextActivity != null,

                            nextActivity =
                                nextActivity,

                            noEligibleActivity =
                                nextOpportunity != null &&
                                    nextActivity == null,

                            nextActivityWhenText =
                                nextOpportunity
                                    ?.let {
                                        opportunityLabel(
                                            it
                                        )
                                    },

                            lastActivity =
                                visibleLastActivity,

                            lastActivityWhenText =
                                visibleLastActivityWhenText,

                            onPauseProposals = {
                                coroutineScope.launch {
                                    appStateRepository
                                        .setProposalsPaused(
                                            true
                                        )
                                }
                            },

                            onResumeProposals = {
                                coroutineScope.launch {
                                    appStateRepository
                                        .setProposalsPaused(
                                            false
                                        )
                                }
                            },

                            onRequestNotificationPermission = {

                                val runtimePermissionMissing =
                                    Build.VERSION.SDK_INT >=
                                        Build.VERSION_CODES.TIRAMISU &&
                                        checkSelfPermission(
                                            Manifest.permission
                                                .POST_NOTIFICATIONS
                                        ) !=
                                        PackageManager.PERMISSION_GRANTED

                                if (
                                    runtimePermissionMissing &&
                                    (
                                        !notificationPermissionRequested ||
                                            shouldShowRequestPermissionRationale(
                                                Manifest.permission
                                                    .POST_NOTIFICATIONS
                                            )
                                    )
                                ) {
                                    coroutineScope.launch {
                                        appStateRepository
                                            .markNotificationPermissionRequested()
                                    }

                                    completeOnboardingAfterPermissionResult =
                                        false

                                    notificationPermissionLauncher
                                        .launch(
                                            Manifest.permission
                                                .POST_NOTIFICATIONS
                                        )
                                } else {
                                    openNotificationSettings()
                                }
                            },

                            onRequestExactAlarmPermission = {
                                openExactAlarmSettings()
                            },

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
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        notificationPermissionAllowedState.value =
            areNotificationsAllowed()

        exactAlarmAllowedState.value =
            canScheduleExactAlarms()

        if (
            ::proposalAlarmScheduler
                .isInitialized
        ) {
            lifecycleScope.launch {
                proposalAlarmScheduler
                    .reconcile()
            }
        }
    }

    override fun onNewIntent(
        intent: Intent,
    ) {
        super.onNewIntent(
            intent
        )

        setIntent(
            intent
        )

        pendingNotificationProposalKeyState.value =
            intent.getStringExtra(
                NotificationPublisher
                    .EXTRA_PROPOSAL_KEY
            )
    }

    private fun areNotificationsAllowed():
        Boolean {

        val runtimePermissionGranted =
            Build.VERSION.SDK_INT <
                Build.VERSION_CODES.TIRAMISU ||
                checkSelfPermission(
                    Manifest.permission
                        .POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED

        return runtimePermissionGranted &&
            NotificationManagerCompat
                .from(
                    this
                )
                .areNotificationsEnabled()
    }

    private fun canScheduleExactAlarms():
        Boolean {

        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.S
        ) {
            return true
        }

        return getSystemService(
            AlarmManager::class.java
        ).canScheduleExactAlarms()
    }

    private fun openExactAlarmSettings() {

        if (
            Build.VERSION.SDK_INT <
            Build.VERSION_CODES.S
        ) {
            exactAlarmAllowedState.value =
                true
            return
        }

        val intent =
            Intent(
                Settings
                    .ACTION_REQUEST_SCHEDULE_EXACT_ALARM
            ).apply {
                data =
                    Uri.parse(
                        "package:$packageName"
                    )
            }

        startActivity(
            intent
        )
    }

    private fun openNotificationSettings() {

        val intent =
            Intent(
                Settings
                    .ACTION_APP_NOTIFICATION_SETTINGS
            ).apply {
                putExtra(
                    Settings.EXTRA_APP_PACKAGE,
                    packageName,
                )
            }

        startActivity(
            intent
        )
    }
}

private const val
    LAST_ACTIVITY_VISIBILITY_MILLIS =
    45L * 60_000L

private fun lastActivityLabel(
    epochMillis: Long,
): String {

    val proposedAt =
        Instant
            .ofEpochMilli(
                epochMillis
            )
            .atZone(
                ZoneId.systemDefault()
            )
            .toLocalDateTime()

    val time =
        "%02d:%02d".format(
            proposedAt.hour,
            proposedAt.minute,
        )

    val date =
        proposedAt.toLocalDate()

    return when (date) {

        LocalDate.now() ->
            "Aujourd’hui · $time"

        LocalDate.now()
            .minusDays(1) ->
            "Hier · $time"

        else ->
            "${weekdayName(proposedAt.dayOfWeek).replaceFirstChar { it.uppercase() }} · $time"
    }
}

private fun nextOpportunity(
    settings: ScheduleSettings,
    minimumGapMinutes: Long,
    consumedEarlyOpportunityKeys:
        Set<String>,
): LocalDateTime? {

    return OpportunityRuntimePlanner
        .next(
            now =
                LocalDateTime.now(),
            settings =
                settings,
            minimumGapMinutes =
                minimumGapMinutes,
            consumedEarlyOpportunityKeys =
                consumedEarlyOpportunityKeys,
        )
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
