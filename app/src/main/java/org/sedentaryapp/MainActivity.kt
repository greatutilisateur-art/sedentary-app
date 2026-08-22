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
import org.sedentaryapp.data.NotificationAlertSettings
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
import org.sedentaryapp.ui.onboarding.PermissionsOnboardingScreen
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

    private val onboardingCompletedState =
        MutableStateFlow<Boolean?>(
            null
        )

    private val pendingNotificationProposalKeyState =
        MutableStateFlow<String?>(
            null
        )

    private val pendingAutoStartGuidedProposalKeyState =
        MutableStateFlow<String?>(
            null
        )

    private lateinit var proposalAlarmScheduler:
        ProposalAlarmScheduler

    private var continuePermissionSetupAfterNotificationResult =
        false

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)

        val initialNotificationProposalKey =
            intent.getStringExtra(
                NotificationPublisher
                    .EXTRA_PROPOSAL_KEY
            )

        pendingNotificationProposalKeyState.value =
            initialNotificationProposalKey

        pendingAutoStartGuidedProposalKeyState.value =
            initialNotificationProposalKey
                ?.takeIf {
                    intent.getBooleanExtra(
                        NotificationPublisher
                            .EXTRA_AUTO_START_GUIDED_SESSION,
                        false,
                    )
                }

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
                    continuePermissionSetupAfterNotificationResult
                ) {
                    continuePermissionSetupAfterNotificationResult =
                        false

                    if (
                        notificationPermissionAllowedState.value &&
                        !canScheduleExactAlarms()
                    ) {
                        openExactAlarmSettings()
                    } else if (
                        notificationPermissionAllowedState.value &&
                        canScheduleExactAlarms()
                    ) {
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

                val completionVibrationEnabled by
                    appStateRepository
                        .completionVibrationEnabled
                        .collectAsState(
                            initial = true
                        )

                val notificationAlertSettings by
                    appStateRepository
                        .notificationAlertSettings
                        .collectAsState(
                            initial =
                                NotificationAlertSettings()
                        )

                val activeProposal by
                    appStateRepository
                        .activeProposal
                        .collectAsState(
                            initial = null
                        )

                val snoozedProposal by
                    appStateRepository
                        .snoozedProposal
                        .collectAsState(
                            initial = null
                        )

                val notificationsAllowed by
                    notificationPermissionAllowedState
                        .collectAsState()

                val exactAlarmsAllowed by
                    exactAlarmAllowedState
                        .collectAsState()

                val onboardingCompleted by
                    onboardingCompletedState
                        .collectAsState()

                val pendingNotificationProposalKey by
                    pendingNotificationProposalKeyState
                        .collectAsState()

                val pendingAutoStartGuidedProposalKey by
                    pendingAutoStartGuidedProposalKeyState
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

                val nextProposalOverride by
                    appStateRepository
                        .nextProposalOverride
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

                var activeProposalClockMillis by
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
                    activeProposal
                        ?.proposedAtEpochMillis
                ) {
                    activeProposalClockMillis =
                        System.currentTimeMillis()

                    val proposedAt =
                        activeProposal
                            ?.proposedAtEpochMillis
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

                        activeProposalClockMillis =
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
                        val deliveredProposal =
                            appStateRepository
                                .activeProposal
                                .first()
                                ?.takeIf {
                                    it.proposalKey ==
                                        proposalKey
                                }

                        if (deliveredProposal != null) {
                            viewModel
                                .startNewProposal(
                                    proposalId =
                                        proposalKey,
                                    preferredActivityId =
                                        deliveredProposal
                                            .activityId,
                                )

                            NotificationPublisher
                                .cancel(
                                    this@MainActivity
                                )

                            screen =
                                "activity"
                        } else {
                            pendingAutoStartGuidedProposalKeyState.value =
                                null
                        }

                        pendingNotificationProposalKeyState.value =
                            null
                    }
                }

                LaunchedEffect(
                    pendingAutoStartGuidedProposalKey,
                    activityState.activity
                        ?.definition
                        ?.id,
                    activityState.context,
                    activityState.guidedSessionState,
                    screen,
                    activeProposal,
                ) {
                    val proposalKey =
                        pendingAutoStartGuidedProposalKey
                            ?: return@LaunchedEffect

                    val active =
                        activeProposal
                            ?.takeIf {
                                it.proposalKey ==
                                    proposalKey
                            }
                            ?: return@LaunchedEffect

                    val readyToStart =
                        screen == "activity" &&
                            activityState.context ==
                                org.sedentaryapp.ui.activity
                                    .ActivityDisplayContext
                                    .CURRENT_PROPOSAL &&
                            activityState.guidedSessionState ==
                                GuidedSessionState.READY &&
                            activityState.activity
                                ?.definition
                                ?.id ==
                                active.activityId

                    if (readyToStart) {
                        viewModel
                            .startGuidedSession()

                        NotificationPublisher
                            .cancel(
                                this@MainActivity
                            )

                        proposalAlarmScheduler
                            .reconcile()

                        pendingAutoStartGuidedProposalKeyState.value =
                            null
                    }
                }

                var onboardingStep by
                    rememberSaveable {
                        mutableStateOf(0)
                    }

                LaunchedEffect(
                    notificationsAllowed,
                    exactAlarmsAllowed,
                    onboardingCompleted,
                    onboardingStep,
                ) {
                    if (
                        onboardingCompleted == false &&
                        onboardingStep == 2 &&
                        notificationsAllowed &&
                        exactAlarmsAllowed
                    ) {
                        appStateRepository
                            .markOnboardingCompleted()

                        onboardingCompletedState.value =
                            true
                    }
                }

                val regularNextOpportunity =
                    nextOpportunity(
                        settings =
                            scheduleSettings,
                        minimumGapMinutes =
                            minimumGapMinutes,
                        consumedEarlyOpportunityKeys =
                            consumedEarlyOpportunityKeys,
                    )

                val nextProposalOverrideAt =
                    nextProposalOverride
                        ?.at

                LaunchedEffect(
                    nextProposalOverrideAt,
                    notificationsAllowed,
                    exactAlarmsAllowed,
                ) {
                    val overrideAt =
                        nextProposalOverrideAt
                            ?: return@LaunchedEffect

                    val now =
                        LocalDateTime.now()

                    val tooLate =
                        overrideAt.isBefore(
                            now.minusMinutes(
                                ProposalAlarmScheduler
                                    .MAX_LATE_DELIVERY_MINUTES
                            )
                        )

                    val cannotBeDeliveredAndAlreadyPassed =
                        (
                            !notificationsAllowed ||
                                !exactAlarmsAllowed
                        ) &&
                            !overrideAt.isAfter(
                                now
                            )

                    if (
                        tooLate ||
                        cannotBeDeliveredAndAlreadyPassed
                    ) {
                        appStateRepository
                            .clearNextProposalOverride()
                    }
                }

                val nextOpportunity =
                    if (proposalsPaused) {
                        null
                    } else {
                        nextProposalOverrideAt
                            ?: regularNextOpportunity
                    }

                val settingsNextProposal =
                    nextProposalOverrideAt
                        ?: regularNextOpportunity

                LaunchedEffect(
                    scheduleSettings,
                    proposalsPaused,
                    consumedEarlyOpportunityKeys,
                    notificationsAllowed,
                    exactAlarmsAllowed,
                    onboardingCompleted,
                    nextProposalOverrideAt,
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

                val fixedNextActivity =
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

                val activeActivity =
                    activeProposal
                        ?.activityId
                        ?.let { activityId ->
                            activities
                                .firstOrNull {
                                    it.definition.id ==
                                        activityId
                                }
                        }

                val activeProposalAgeMillis =
                    activeProposal
                        ?.proposedAtEpochMillis
                        ?.let { proposedAt ->
                            activeProposalClockMillis -
                                proposedAt
                        }

                val activeProposalIsRecent =
                    activeProposalAgeMillis != null &&
                        activeProposalAgeMillis >= 0L &&
                        activeProposalAgeMillis <=
                            LAST_ACTIVITY_VISIBILITY_MILLIS

                val activeProposalIsNext =
                    !proposalsPaused &&
                        activeProposalIsRecent &&
                        activeActivity != null

                val activeAt =
                    activeProposal
                        ?.proposedAtEpochMillis
                        ?.let { proposedAt ->
                            Instant
                                .ofEpochMilli(
                                    proposedAt
                                )
                                .atZone(
                                    ZoneId.systemDefault()
                                )
                                .toLocalDateTime()
                        }

                val snoozedActivity =
                    snoozedProposal
                        ?.activityId
                        ?.let { activityId ->
                            activities
                                .firstOrNull {
                                    it.definition.id ==
                                        activityId
                                }
                        }

                val snoozedAt =
                    snoozedProposal
                        ?.triggerAtEpochMillis
                        ?.let { triggerAt ->
                            Instant
                                .ofEpochMilli(
                                    triggerAt
                                )
                                .atZone(
                                    ZoneId.systemDefault()
                                )
                                .toLocalDateTime()
                        }

                val fixedAtEpochMillis =
                    nextOpportunity
                        ?.atZone(
                            ZoneId.systemDefault()
                        )
                        ?.toInstant()
                        ?.toEpochMilli()

                val snoozeIsNext =
                    !activeProposalIsNext &&
                        !proposalsPaused &&
                        snoozedProposal != null &&
                        snoozedActivity != null &&
                        (
                            fixedAtEpochMillis == null ||
                                snoozedProposal!!
                                    .triggerAtEpochMillis <=
                                fixedAtEpochMillis
                        )

                val homeNextActivity =
                    when {
                        activeProposalIsNext ->
                            activeActivity

                        snoozeIsNext ->
                            snoozedActivity

                        else ->
                            fixedNextActivity
                    }

                val homeNextWhenText =
                    when {
                        activeProposalIsNext &&
                            activeAt != null ->
                            opportunityLabel(
                                activeAt
                            )

                        snoozeIsNext &&
                            snoozedAt != null ->
                            opportunityLabel(
                                snoozedAt
                            )

                        else ->
                            nextOpportunity
                                ?.let {
                                    opportunityLabel(
                                        it
                                    )
                                }
                    }

                val snoozedProposalOpen =
                    screen == "activity" &&
                        activityState.context ==
                            org.sedentaryapp.ui.activity
                                .ActivityDisplayContext
                                .SNOOZED_PROPOSAL

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

                        PermissionsOnboardingScreen(
                            notificationsAllowed =
                                notificationsAllowed,

                            exactAlarmsAllowed =
                                exactAlarmsAllowed,

                            soundEnabled =
                                notificationAlertSettings
                                    .soundEnabled,

                            vibrationEnabled =
                                notificationAlertSettings
                                    .vibrationEnabled,

                            onSoundChanged = { enabled ->
                                coroutineScope.launch {
                                    appStateRepository
                                        .setNotificationSoundEnabled(
                                            enabled
                                        )
                                }
                            },

                            onVibrationChanged = { enabled ->
                                coroutineScope.launch {
                                    appStateRepository
                                        .setNotificationVibrationEnabled(
                                            enabled
                                        )
                                }
                            },

                            onConfigurePermissions = {

                                if (!notificationsAllowed) {

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

                                        continuePermissionSetupAfterNotificationResult =
                                            true

                                        notificationPermissionLauncher
                                            .launch(
                                                Manifest.permission
                                                    .POST_NOTIFICATIONS
                                            )
                                    } else {
                                        openNotificationSettings()
                                    }

                                } else if (
                                    !exactAlarmsAllowed
                                ) {
                                    openExactAlarmSettings()

                                } else {
                                    coroutineScope.launch {
                                        appStateRepository
                                            .markOnboardingCompleted()

                                        onboardingCompletedState.value =
                                            true
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

                            when (
                                activityState
                                    .guidedSessionState
                            ) {
                                GuidedSessionState
                                    .RUNNING,
                                GuidedSessionState
                                    .PAUSED ->
                                    viewModel
                                        .abandonGuidedSession()

                                GuidedSessionState
                                    .TIME_ELAPSED ->
                                    viewModel
                                        .closeGuidedSession()

                                GuidedSessionState
                                    .READY -> Unit
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

                            completionVibrationEnabled =
                                completionVibrationEnabled,

                            onBack = {

                                when (
                                    activityState
                                        .guidedSessionState
                                ) {
                                    GuidedSessionState
                                        .RUNNING,
                                    GuidedSessionState
                                        .PAUSED ->
                                        viewModel
                                            .abandonGuidedSession()

                                    GuidedSessionState
                                        .TIME_ELAPSED ->
                                        viewModel
                                            .closeGuidedSession()

                                    GuidedSessionState
                                        .READY -> Unit
                                }

                                screen = "home"
                            },

                            onPreviousActivity =
                                viewModel::onPreviousActivity,

                            onNextActivity =
                                viewModel::onNextActivity,

                            onOtherActivity =
                                viewModel::onOtherActivity,

                            onNotNow = {
                                if (snoozedProposalOpen) {
                                    viewModel
                                        .cancelPendingSnoozedActivityPersistence()
                                }

                                viewModel.onNotNow()

                                NotificationPublisher
                                    .cancel(
                                        this@MainActivity
                                    )

                                coroutineScope.launch {
                                    proposalAlarmScheduler
                                        .reconcile()
                                }

                                screen = "home"
                            },

                            onStartGuidedSession = {
                                if (snoozedProposalOpen) {
                                    viewModel
                                        .cancelPendingSnoozedActivityPersistence()
                                }

                                viewModel
                                    .startGuidedSession()

                                NotificationPublisher
                                    .cancel(
                                        this@MainActivity
                                    )

                                coroutineScope.launch {
                                    proposalAlarmScheduler
                                        .reconcile()
                                }
                            },

                            onCompleteSimpleActivity = {
                                if (snoozedProposalOpen) {
                                    viewModel
                                        .cancelPendingSnoozedActivityPersistence()
                                }

                                viewModel
                                    .completeSimpleActivity()

                                NotificationPublisher
                                    .cancel(
                                        this@MainActivity
                                    )

                                coroutineScope.launch {
                                    proposalAlarmScheduler
                                        .reconcile()
                                }

                                screen = "home"
                            },

                            onPauseGuidedSession =
                                viewModel::pauseGuidedSession,

                            onResumeGuidedSession =
                                viewModel::resumeGuidedSession,

                            onEndGuidedSession = {
                                viewModel
                                    .completeGuidedSessionEarly()

                                coroutineScope.launch {
                                    proposalAlarmScheduler
                                        .reconcile()
                                }

                                screen = "home"
                            },

                            onCloseGuidedSession = {
                                viewModel
                                    .closeGuidedSession()

                                coroutineScope.launch {
                                    proposalAlarmScheduler
                                        .reconcile()
                                }

                                screen = "home"
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

                            completionVibrationEnabled =
                                completionVibrationEnabled,

                            notificationSoundEnabled =
                                notificationAlertSettings
                                    .soundEnabled,

                            notificationVibrationEnabled =
                                notificationAlertSettings
                                    .vibrationEnabled,

                            minimumGapMinutes =
                                minimumGapMinutes,

                            nextProposalAt =
                                settingsNextProposal,

                            nextProposalOverrideActive =
                                nextProposalOverrideAt != null,

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

                            onCompletionVibrationChanged = {
                                    enabled,
                                ->

                                coroutineScope.launch {
                                    appStateRepository
                                        .setCompletionVibrationEnabled(
                                            enabled
                                        )
                                }
                            },

                            onNotificationSoundChanged = {
                                    enabled,
                                ->

                                coroutineScope.launch {
                                    appStateRepository
                                        .setNotificationSoundEnabled(
                                            enabled
                                        )
                                }
                            },

                            onNotificationVibrationChanged = {
                                    enabled,
                                ->

                                coroutineScope.launch {
                                    appStateRepository
                                        .setNotificationVibrationEnabled(
                                            enabled
                                        )
                                }
                            },

                            onNextProposalChanged = {
                                    at,
                                ->

                                coroutineScope.launch {
                                    appStateRepository
                                        .saveNextProposalOverride(
                                            at
                                        )

                                    proposalAlarmScheduler
                                        .reconcile()
                                }
                            },

                            onClearNextProposalOverride = {
                                coroutineScope.launch {
                                    appStateRepository
                                        .clearNextProposalOverride()

                                    proposalAlarmScheduler
                                        .reconcile()
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
                                    homeNextActivity != null,

                            exactAlarmPermissionRequired =
                                !proposalsPaused &&
                                    notificationsAllowed &&
                                    !exactAlarmsAllowed &&
                                    homeNextActivity != null,

                            nextActivity =
                                homeNextActivity,

                            noEligibleActivity =
                                nextOpportunity != null &&
                                    fixedNextActivity == null &&
                                    !snoozeIsNext &&
                                    !activeProposalIsNext,

                            nextActivityWhenText =
                                homeNextWhenText,

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

                                    continuePermissionSetupAfterNotificationResult =
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
                                    activeProposalIsNext &&
                                    activeProposal != null
                                ) {
                                    NotificationPublisher
                                        .cancel(
                                            this@MainActivity
                                        )

                                    viewModel
                                        .startNewProposal(
                                            proposalId =
                                                activeProposal!!
                                                    .proposalKey,
                                            preferredActivityId =
                                                activeProposal!!
                                                    .activityId,
                                        )

                                    screen = "activity"

                                } else if (
                                    snoozeIsNext &&
                                    snoozedProposal != null
                                ) {
                                    viewModel
                                        .openSnoozedProposal(
                                            proposalId =
                                                snoozedProposal!!
                                                    .proposalKey,
                                            activityId =
                                                snoozedProposal!!
                                                    .activityId,
                                        )

                                    screen = "activity"

                                } else if (
                                    nextOpportunityKey != null &&
                                    fixedNextActivity != null
                                ) {
                                    viewModel
                                        .openFuturePreview(
                                            opportunityKey =
                                                nextOpportunityKey,
                                            plannedActivityId =
                                                fixedNextActivity
                                                    .definition
                                                    .id,
                                        )

                                    screen =
                                        "activity"
                                }
                            },

                            onMoveNow = {
                                if (
                                    viewModel
                                        .openManualActivity()
                                ) {
                                    screen = "activity"
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

        val proposalKey =
            intent.getStringExtra(
                NotificationPublisher
                    .EXTRA_PROPOSAL_KEY
            )

        pendingNotificationProposalKeyState.value =
            proposalKey

        pendingAutoStartGuidedProposalKeyState.value =
            proposalKey
                ?.takeIf {
                    intent.getBooleanExtra(
                        NotificationPublisher
                            .EXTRA_AUTO_START_GUIDED_SESSION,
                        false,
                    )
                }
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

    if (
        date ==
        LocalDate.now().plusDays(1)
    ) {
        return "Demain · $time"
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
            "Si tu peux, lève-toi"

        else ->
            null
    }
}
