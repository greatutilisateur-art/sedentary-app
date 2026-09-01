package org.sedentaryapp.ui.activity

import android.os.SystemClock
import java.time.LocalDateTime
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.sedentaryapp.data.ActivityGuidanceMode
import org.sedentaryapp.data.ActivityPreferences
import org.sedentaryapp.data.AppStateRepository
import org.sedentaryapp.data.CatalogActivity
import org.sedentaryapp.engine.decision.CandidateFilter
import org.sedentaryapp.engine.decision.DecisionEngine
import org.sedentaryapp.engine.model.CandidateFilterRequest
import org.sedentaryapp.engine.model.DecisionPolicy

enum class GuidedSessionState {
    READY,
    RUNNING,
    PAUSED,
    TIME_ELAPSED,
}

enum class ActivityDisplayContext {
    CURRENT_PROPOSAL,
    SNOOZED_PROPOSAL,
    LAST_ACTIVITY,
    FUTURE_PREVIEW,
    MANUAL_ACTIVITY,
}

data class ActivityUiState(
    val activity: CatalogActivity? = null,
    val lastProposedActivity: CatalogActivity? = null,
    val context: ActivityDisplayContext? = null,
    val canGoPrevious: Boolean = false,
    val canGoNext: Boolean = false,
    val canRequestAnother: Boolean = false,
    val showNotNow: Boolean = false,
    val guidedSessionState: GuidedSessionState =
        GuidedSessionState.READY,
    val remainingSeconds: Int = 0,
    val shouldPlayCompletionHaptic: Boolean = false,
    val activityPreferences: ActivityPreferences =
        ActivityPreferences(),
    val recentProposalActivityIds: List<String> =
        emptyList(),
)

class ActivityViewModel(
    private val activities: List<CatalogActivity>,
    private val policy: DecisionPolicy,
    private val appStateRepository: AppStateRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private var timerJob: Job? = null
    private var futureOverrideJob: Job? = null
    private var snoozedActivityPersistenceJob: Job? = null
    private var proposalSelectionJob: Job? = null
    private var activityPreferences =
        ActivityPreferences()
    private var proposalHistoryIds:
        List<String> = emptyList()

    private val definitions =
        activities.map { it.definition }

    private val _uiState =
        MutableStateFlow(
            ActivityUiState()
        )

    val uiState: StateFlow<ActivityUiState> =
        _uiState.asStateFlow()

    init {
        restoreState()
        restoreGuidedSession()
        observePersistentLastActivity()
        observeActivityPreferences()
        observeProposalHistory()
    }

    fun startNewProposal(
        proposalId: String,
        preferredActivityId: String? = null,
    ) {

        clearGuidedSession()

        proposalSelectionJob?.cancel()

        savedStateHandle
            .remove<String>(
                KEY_FUTURE_OPPORTUNITY_KEY
            )

        savedStateHandle
            .remove<String>(
                KEY_MANUAL_SESSION_ID
            )

        savedStateHandle[
            KEY_PROPOSAL_ID
        ] = proposalId

        savedStateHandle[
            KEY_PROPOSAL_ACTIVE
        ] = true

        savedStateHandle[
            KEY_DISPLAY_CONTEXT
        ] =
            ActivityDisplayContext
                .CURRENT_PROPOSAL
                .name

        savedStateHandle[
            KEY_ATTEMPT
        ] = 0

        writeExploredActivityIds(
            emptyList()
        )

        savedStateHandle[
            KEY_CURRENT_INDEX
        ] = 0

        publishState()

        proposalSelectionJob =
            viewModelScope.launch {

                val persistedHistory =
                    appStateRepository
                        .proposalHistory
                        .first()

                proposalHistoryIds =
                    persistedHistory
                        .mapNotNull { entry ->
                            entry.activityId
                                .takeIf { id ->
                                    activities.any { activity ->
                                        activity.definition.id ==
                                            id
                                    }
                                }
                        }
                        .takeLast(
                            policy.scoring
                                .historyWindowProposals
                        )

                val preferredActivity =
                    preferredActivityId
                        ?.let { id ->
                            activities
                                .firstOrNull { activity ->
                                    activity.definition.id ==
                                        id
                                }
                        }

                val historyActivityId =
                    persistedHistory
                        .lastOrNull { entry ->
                            entry.proposalKey ==
                                proposalId
                        }
                        ?.activityId

                val historyActivity =
                    historyActivityId
                        ?.let { id ->
                            activities
                                .firstOrNull { activity ->
                                    activity.definition.id ==
                                        id
                                }
                        }

                val existingActivity =
                    preferredActivity
                        ?: historyActivity

                val selected =
                    existingActivity
                        ?: selectActivity(
                            excludedIds =
                                emptySet(),
                            attempt = 0,
                        )

                if (selected == null) {
                    clearActiveProposal()
                    publishState()
                    return@launch
                }

                val selectedId =
                    selected.definition.id

                if (existingActivity == null) {
                    appStateRepository
                        .recordProposal(
                            proposalKey =
                                proposalId,
                            activityId =
                                selectedId,
                            maxEntries =
                                policy.scoring
                                    .historyWindowProposals,
                        )

                    proposalHistoryIds =
                        (
                            proposalHistoryIds +
                                selectedId
                        ).takeLast(
                            policy.scoring
                                .historyWindowProposals
                        )
                }

                writeExploredActivityIds(
                    listOf(
                        selectedId
                    )
                )

                savedStateHandle[
                    KEY_CURRENT_INDEX
                ] = 0

                publishState()
            }
    }

    fun onOtherActivity() {

        if (
            readGuidedSessionState() !=
            GuidedSessionState.READY
        ) {
            return
        }

        val context =
            readDisplayContext()

        if (
            context !=
            ActivityDisplayContext.CURRENT_PROPOSAL &&
            context !=
            ActivityDisplayContext.SNOOZED_PROPOSAL &&
            context !=
            ActivityDisplayContext.FUTURE_PREVIEW &&
            context !=
            ActivityDisplayContext.MANUAL_ACTIVITY
        ) {
            return
        }

        val explored =
            readExploredActivityIds()

        if (explored.isEmpty()) {
            return
        }

        val currentIndex =
            readCurrentIndex()

        if (
            currentIndex !=
            explored.lastIndex
        ) {
            return
        }

        val newAttempt =
            (
                    savedStateHandle.get<Int>(
                        KEY_ATTEMPT
                    ) ?: 0
                    ) + 1

        val selected =
            selectActivity(
                excludedIds =
                    explored.toSet(),
                attempt =
                    newAttempt,
            ) ?: run {
                publishState()
                return
            }

        val updated =
            explored +
                    selected.definition.id

        writeExploredActivityIds(
            updated
        )

        savedStateHandle[
            KEY_CURRENT_INDEX
        ] =
            updated.lastIndex

        savedStateHandle[
            KEY_ATTEMPT
        ] =
            newAttempt

        when (context) {

            ActivityDisplayContext.FUTURE_PREVIEW ->
                persistFutureOverride(
                    updated.last()
                )

            ActivityDisplayContext.SNOOZED_PROPOSAL ->
                persistSnoozedActivity(
                    updated.last()
                )

            else -> Unit
        }

        publishState()
    }

    fun onPreviousActivity() {

        if (
            readGuidedSessionState() !=
            GuidedSessionState.READY
        ) {
            return
        }

        val context =
            readDisplayContext()

        val explored =
            readExploredActivityIds()

        if (explored.isEmpty()) {
            return
        }

        val current =
            readCurrentIndex()

        if (current <= 0) {
            return
        }

        val newIndex =
            current - 1

        savedStateHandle[
            KEY_CURRENT_INDEX
        ] =
            newIndex

        when (context) {

            ActivityDisplayContext.FUTURE_PREVIEW ->
                persistFutureOverride(
                    explored[newIndex]
                )

            ActivityDisplayContext.SNOOZED_PROPOSAL ->
                persistSnoozedActivity(
                    explored[newIndex]
                )

            else -> Unit
        }

        publishState()
    }

    fun onNextActivity() {

        if (
            readGuidedSessionState() !=
            GuidedSessionState.READY
        ) {
            return
        }

        val context =
            readDisplayContext()

        val explored =
            readExploredActivityIds()

        if (explored.isEmpty()) {
            return
        }

        val current =
            readCurrentIndex()

        if (
            current >=
            explored.lastIndex
        ) {
            return
        }

        val newIndex =
            current + 1

        savedStateHandle[
            KEY_CURRENT_INDEX
        ] =
            newIndex

        when (context) {

            ActivityDisplayContext.FUTURE_PREVIEW ->
                persistFutureOverride(
                    explored[newIndex]
                )

            ActivityDisplayContext.SNOOZED_PROPOSAL ->
                persistSnoozedActivity(
                    explored[newIndex]
                )

            else -> Unit
        }

        publishState()
    }

    fun openLastActivity() {

        savedStateHandle
            .remove<String>(
                KEY_MANUAL_SESSION_ID
            )

        val lastId =
            savedStateHandle.get<String>(
                KEY_LAST_PROPOSED_ACTIVITY_ID
            ) ?: return

        val exists =
            activities.any {
                it.definition.id ==
                        lastId
            }

        if (!exists) {
            return
        }

        savedStateHandle
            .remove<String>(
                KEY_FUTURE_OPPORTUNITY_KEY
            )

        savedStateHandle[
            KEY_DISPLAY_CONTEXT
        ] =
            ActivityDisplayContext
                .LAST_ACTIVITY
                .name

        writeExploredActivityIds(
            listOf(
                lastId
            )
        )

        savedStateHandle[
            KEY_CURRENT_INDEX
        ] = 0

        publishState()
    }

    fun isActivityEligible(
        activityId: String,
    ): Boolean {

        return CandidateFilter.filter(
            activities =
                definitions,
            request =
                standardFilterRequest(
                    excludedIds =
                        emptySet()
                ),
        ).any {
            it.id ==
                activityId
        }
    }

    fun plannedFutureActivity(
        opportunityKey: String,
        overrideActivityId: String?,
    ): CatalogActivity? {

        val eligibleDefinitions =
            CandidateFilter.filter(
                activities =
                    definitions,
                request =
                    standardFilterRequest(
                        excludedIds =
                            emptySet()
                    ),
            )

        if (overrideActivityId != null) {

            val overrideEligible =
                eligibleDefinitions.any {
                    it.id ==
                        overrideActivityId
                }

            if (overrideEligible) {
                return activities
                    .firstOrNull {
                        it.definition.id ==
                            overrideActivityId
                    }
            }
        }

        val selectedDefinition =
            DecisionEngine.chooseActivity(
                activities =
                    definitions,
                history =
                    currentHistoryDefinitions(),
                policy =
                    policy,
                seed =
                    "future-$opportunityKey-0",
                filterRequest =
                    standardFilterRequest(
                        excludedIds =
                            emptySet()
                    ),
            )
                ?: return null

        return activities
            .firstOrNull {
                it.definition.id ==
                    selectedDefinition.id
            }
    }
    fun openSnoozedProposal(
        proposalId: String,
        activityId: String,
    ) {

        val activityExists =
            activities.any {
                it.definition.id ==
                    activityId
            }

        if (!activityExists) {
            return
        }

        savedStateHandle
            .remove<String>(
                KEY_MANUAL_SESSION_ID
            )

        clearGuidedSession()

        proposalSelectionJob?.cancel()

        savedStateHandle
            .remove<String>(
                KEY_FUTURE_OPPORTUNITY_KEY
            )

        val existingProposalId =
            savedStateHandle
                .get<String>(
                    KEY_PROPOSAL_ID
                )

        val existingExplored =
            readExploredActivityIds()

        val canReuseExploration =
            readDisplayContext() ==
                ActivityDisplayContext
                    .SNOOZED_PROPOSAL &&
                existingProposalId ==
                    proposalId &&
                activityId in
                    existingExplored

        savedStateHandle[
            KEY_PROPOSAL_ID
        ] = proposalId

        savedStateHandle[
            KEY_PROPOSAL_ACTIVE
        ] = true

        savedStateHandle[
            KEY_DISPLAY_CONTEXT
        ] =
            ActivityDisplayContext
                .SNOOZED_PROPOSAL
                .name

        if (canReuseExploration) {
            savedStateHandle[
                KEY_CURRENT_INDEX
            ] =
                existingExplored
                    .indexOf(
                        activityId
                    )

            publishState()
            return
        }

        savedStateHandle[
            KEY_ATTEMPT
        ] = 0

        writeExploredActivityIds(
            listOf(activityId)
        )

        savedStateHandle[
            KEY_CURRENT_INDEX
        ] = 0

        publishState()
    }

    fun openFuturePreview(
        opportunityKey: String,
        plannedActivityId: String,
    ) {

        val plannedActivity =
            plannedFutureActivity(
                opportunityKey =
                    opportunityKey,
                overrideActivityId =
                    plannedActivityId,
            ) ?: return

        if (
            plannedActivity.definition.id !=
            plannedActivityId
        ) {
            return
        }

        savedStateHandle
            .remove<String>(
                KEY_MANUAL_SESSION_ID
            )

        clearGuidedSession()

        val existingOpportunityKey =
            savedStateHandle
                .get<String>(
                    KEY_FUTURE_OPPORTUNITY_KEY
                )

        val existingExplored =
            readExploredActivityIds()

        val canReuseExploration =
            readDisplayContext() ==
                ActivityDisplayContext
                    .FUTURE_PREVIEW &&
                existingOpportunityKey ==
                    opportunityKey &&
                plannedActivityId in
                    existingExplored

        savedStateHandle[
            KEY_PROPOSAL_ACTIVE
        ] = false

        savedStateHandle
            .remove<String>(
                KEY_PROPOSAL_ID
            )

        savedStateHandle[
            KEY_FUTURE_OPPORTUNITY_KEY
        ] =
            opportunityKey

        savedStateHandle[
            KEY_DISPLAY_CONTEXT
        ] =
            ActivityDisplayContext
                .FUTURE_PREVIEW
                .name

        if (canReuseExploration) {
            savedStateHandle[
                KEY_CURRENT_INDEX
            ] =
                existingExplored
                    .indexOf(
                        plannedActivityId
                    )

            publishState()
            return
        }

        savedStateHandle[
            KEY_ATTEMPT
        ] = 0

        writeExploredActivityIds(
            listOf(
                plannedActivityId
            )
        )

        savedStateHandle[
            KEY_CURRENT_INDEX
        ] = 0

        publishState()
    }

    fun openManualActivity(
        preferredActivityId: String? = null,
    ): Boolean {

        clearGuidedSession()

        proposalSelectionJob?.cancel()
        futureOverrideJob?.cancel()
        futureOverrideJob = null
        snoozedActivityPersistenceJob?.cancel()
        snoozedActivityPersistenceJob = null

        clearActiveProposal()

        savedStateHandle
            .remove<String>(
                KEY_FUTURE_OPPORTUNITY_KEY
            )

        val sessionId =
            "manual-${System.currentTimeMillis()}"

        savedStateHandle[
            KEY_MANUAL_SESSION_ID
        ] = sessionId

        savedStateHandle[
            KEY_DISPLAY_CONTEXT
        ] =
            ActivityDisplayContext
                .MANUAL_ACTIVITY
                .name

        savedStateHandle[
            KEY_ATTEMPT
        ] = 0

        writeExploredActivityIds(
            emptyList()
        )

        savedStateHandle[
            KEY_CURRENT_INDEX
        ] = 0

        val preferredActivity =
            preferredActivityId
                ?.let { id ->
                    activities
                        .firstOrNull { activity ->
                            activity.definition.id ==
                                id
                        }
                }

        val selected =
            preferredActivity
                ?: selectActivity(
                    excludedIds =
                        emptySet(),
                    attempt = 0,
                )
                ?: run {
                    savedStateHandle
                        .remove<String>(
                            KEY_MANUAL_SESSION_ID
                        )

                    savedStateHandle
                        .remove<String>(
                            KEY_DISPLAY_CONTEXT
                        )

                    publishState()
                    return false
                }

        writeExploredActivityIds(
            listOf(
                selected.definition.id
            )
        )

        publishState()
        return true
    }

    fun markCurrentActivityDone() {

        if (
            readGuidedSessionState() !=
                GuidedSessionState.READY
        ) {
            return
        }

        val activity =
            currentDisplayedActivity()
                ?: return

        // Utilisé par :
        // - `Fait` pour une activité ponctuelle SIMPLE ;
        // - `Déjà fait` pour une activité TIMER réalisée sans lancer le chrono.
        //
        // La sémantique de réalisation reste centralisée dans
        // recordActivityAsDone(), qui traite correctement les contextes
        // proposition courante, reportée, future et manuelle.
        recordActivityAsDone(
            activity.definition.id
        )

        clearInteractionAfterExit()
        publishState()
    }

    fun startGuidedSession() {

        if (
            readGuidedSessionState() !=
            GuidedSessionState.READY
        ) {
            return
        }

        val activity =
            currentDisplayedActivity()
                ?: return

        if (
            activity.guidanceMode !=
            ActivityGuidanceMode.TIMER
        ) {
            return
        }

        val durationMillis =
            activity.definition
                .durationSecondsMax
                .toLong() *
                    1000L

        if (durationMillis <= 0L) {
            return
        }

        claimOpportunityForGuidedSession()

        savedStateHandle[
            KEY_COMPLETION_HAPTIC_DELIVERED
        ] = false

        savedStateHandle[
            KEY_GUIDED_COMPLETION_RECORDED
        ] = false

        savedStateHandle[
            KEY_GUIDED_ACTIVITY_ID
        ] =
            activity.definition.id

        savedStateHandle[
            KEY_GUIDED_STATE
        ] =
            GuidedSessionState
                .RUNNING
                .name

        savedStateHandle[
            KEY_GUIDED_END_ELAPSED_REALTIME
        ] =
            SystemClock.elapsedRealtime() +
                    durationMillis

        savedStateHandle
            .remove<Long>(
                KEY_GUIDED_PAUSED_REMAINING_MILLIS
            )

        startTimerTicker()
        publishState()
    }

    fun pauseGuidedSession() {

        if (
            readGuidedSessionState() !=
            GuidedSessionState.RUNNING
        ) {
            return
        }

        val remainingMillis =
            calculateRunningRemainingMillis()

        if (remainingMillis <= 0L) {
            finishGuidedSessionNaturally()
            return
        }

        timerJob?.cancel()
        timerJob = null

        savedStateHandle[
            KEY_GUIDED_PAUSED_REMAINING_MILLIS
        ] =
            remainingMillis

        savedStateHandle[
            KEY_GUIDED_STATE
        ] =
            GuidedSessionState
                .PAUSED
                .name

        savedStateHandle
            .remove<Long>(
                KEY_GUIDED_END_ELAPSED_REALTIME
            )

        publishState()
    }

    fun resumeGuidedSession() {

        if (
            readGuidedSessionState() !=
            GuidedSessionState.PAUSED
        ) {
            return
        }

        val remainingMillis =
            savedStateHandle
                .get<Long>(
                    KEY_GUIDED_PAUSED_REMAINING_MILLIS
                )
                ?: return

        if (remainingMillis <= 0L) {
            finishGuidedSessionNaturally()
            return
        }

        savedStateHandle[
            KEY_GUIDED_END_ELAPSED_REALTIME
        ] =
            SystemClock.elapsedRealtime() +
                    remainingMillis

        savedStateHandle[
            KEY_GUIDED_STATE
        ] =
            GuidedSessionState
                .RUNNING
                .name

        savedStateHandle
            .remove<Long>(
                KEY_GUIDED_PAUSED_REMAINING_MILLIS
            )

        startTimerTicker()
        publishState()
    }

    fun abandonGuidedSession() {

        val state =
            readGuidedSessionState()

        if (
            state != GuidedSessionState.RUNNING &&
            state != GuidedSessionState.PAUSED
        ) {
            return
        }

        clearGuidedSession()
        clearInteractionAfterExit()
        publishState()
    }

    // Alias conservé pour éviter une régression si un ancien appel existe encore.
    fun endGuidedSessionEarly() {
        abandonGuidedSession()
    }

    fun completeGuidedSessionEarly() {

        val state =
            readGuidedSessionState()

        if (
            state != GuidedSessionState.RUNNING &&
            state != GuidedSessionState.PAUSED
        ) {
            return
        }

        recordGuidedActivityAsDone()
        clearGuidedSession()
        clearInteractionAfterExit()
        publishState()
    }

    fun closeGuidedSession() {

        if (
            readGuidedSessionState() !=
            GuidedSessionState.TIME_ELAPSED
        ) {
            return
        }

        clearGuidedSession()
        clearInteractionAfterExit()
        publishState()
    }

    fun markCompletionHapticDelivered() {

        if (
            readGuidedSessionState() !=
            GuidedSessionState.TIME_ELAPSED
        ) {
            return
        }

        savedStateHandle[
            KEY_COMPLETION_HAPTIC_DELIVERED
        ] = true

        publishState()
    }
    fun cancelPendingSnoozedActivityPersistence() {
        snoozedActivityPersistenceJob?.cancel()
        snoozedActivityPersistenceJob = null
    }

    fun onNotNow() {

        if (
            readGuidedSessionState() !=
            GuidedSessionState.READY
        ) {
            return
        }

        val context =
            readDisplayContext()

        if (
            context !=
            ActivityDisplayContext
                .CURRENT_PROPOSAL &&
            context !=
            ActivityDisplayContext
                .SNOOZED_PROPOSAL
        ) {
            return
        }

        val proposalId =
            savedStateHandle
                .get<String>(
                    KEY_PROPOSAL_ID
                )

        if (proposalId != null) {
            viewModelScope.launch {
                appStateRepository
                    .clearSnoozedProposalIfProposalKey(
                        proposalId
                    )

                appStateRepository
                    .clearActiveProposalIfProposalKey(
                        proposalId
                    )
            }
        }

        clearInteractionAfterExit()
        publishState()
    }

    private fun restoreState() {

        val lastProposedId =
            savedStateHandle.get<String>(
                KEY_LAST_PROPOSED_ACTIVITY_ID
            )

        val lastExists =
            lastProposedId != null &&
                    activities.any {
                        it.definition.id ==
                                lastProposedId
                    }

        if (
            lastProposedId != null &&
            !lastExists
        ) {
            savedStateHandle
                .remove<String>(
                    KEY_LAST_PROPOSED_ACTIVITY_ID
                )
        }

        val explored =
            readExploredActivityIds()
                .filter { id ->
                    activities.any {
                        it.definition.id ==
                                id
                    }
                }

        val restoredContext =
            readDisplayContext()

        val futureOpportunityKey =
            savedStateHandle
                .get<String>(
                    KEY_FUTURE_OPPORTUNITY_KEY
                )

        val guidedState =
            readGuidedSessionState()

        val futureOpportunityStillValid =
            if (
                restoredContext ==
                ActivityDisplayContext.FUTURE_PREVIEW &&
                futureOpportunityKey != null
            ) {
                val opportunityStillFuture =
                    runCatching {
                        LocalDateTime.parse(
                            futureOpportunityKey
                        )
                    }
                        .getOrNull()
                        ?.isAfter(
                            LocalDateTime.now()
                        ) == true

                opportunityStillFuture ||
                    guidedState !=
                    GuidedSessionState.READY
            } else {
                false
            }

        if (
            futureOpportunityStillValid &&
            explored.isNotEmpty()
        ) {

            writeExploredActivityIds(
                explored
            )

            val safeIndex =
                readCurrentIndex()
                    .coerceIn(
                        0,
                        explored.lastIndex,
                    )

            savedStateHandle[
                KEY_CURRENT_INDEX
            ] =
                safeIndex

            savedStateHandle[
                KEY_PROPOSAL_ACTIVE
            ] = false

            savedStateHandle
                .remove<String>(
                    KEY_PROPOSAL_ID
                )

            publishState()
            return
        }

        val manualSessionId =
            savedStateHandle
                .get<String>(
                    KEY_MANUAL_SESSION_ID
                )

        if (
            restoredContext ==
                ActivityDisplayContext.MANUAL_ACTIVITY &&
            !manualSessionId.isNullOrBlank() &&
            explored.isNotEmpty()
        ) {

            writeExploredActivityIds(
                explored
            )

            val safeIndex =
                readCurrentIndex()
                    .coerceIn(
                        0,
                        explored.lastIndex,
                    )

            savedStateHandle[
                KEY_CURRENT_INDEX
            ] =
                safeIndex

            savedStateHandle[
                KEY_PROPOSAL_ACTIVE
            ] = false

            savedStateHandle
                .remove<String>(
                    KEY_PROPOSAL_ID
                )

            savedStateHandle
                .remove<String>(
                    KEY_FUTURE_OPPORTUNITY_KEY
                )

            publishState()
            return
        }

        savedStateHandle
            .remove<String>(
                KEY_FUTURE_OPPORTUNITY_KEY
            )

        savedStateHandle
            .remove<String>(
                KEY_MANUAL_SESSION_ID
            )

        if (
            isProposalActive() &&
            savedStateHandle.get<String>(
                KEY_PROPOSAL_ID
            ) != null &&
            explored.isNotEmpty()
        ) {

            writeExploredActivityIds(
                explored
            )

            val safeIndex =
                readCurrentIndex()
                    .coerceIn(
                        0,
                        explored.lastIndex,
                    )

            savedStateHandle[
                KEY_CURRENT_INDEX
            ] =
                safeIndex

            savedStateHandle[
                KEY_DISPLAY_CONTEXT
            ] =
                ActivityDisplayContext
                    .CURRENT_PROPOSAL
                    .name

            publishState()
            return
        }

        clearActiveProposal()

        if (lastExists) {

            savedStateHandle[
                KEY_DISPLAY_CONTEXT
            ] =
                ActivityDisplayContext
                    .LAST_ACTIVITY
                    .name

            writeExploredActivityIds(
                listOf(
                    lastProposedId!!
                )
            )

            savedStateHandle[
                KEY_CURRENT_INDEX
            ] = 0

        } else {

            writeExploredActivityIds(
                emptyList()
            )

            savedStateHandle
                .remove<String>(
                    KEY_DISPLAY_CONTEXT
                )
        }

        publishState()
    }

    private fun restoreGuidedSession() {

        val state =
            readGuidedSessionState()

        if (
            state ==
            GuidedSessionState.READY
        ) {
            return
        }

        val guidedActivityId =
            savedStateHandle
                .get<String>(
                    KEY_GUIDED_ACTIVITY_ID
                )

        val guidedActivity =
            activities.firstOrNull {
                it.definition.id ==
                        guidedActivityId
            }

        if (guidedActivity == null) {
            clearGuidedSession()
            publishState()
            return
        }

        when (state) {

            GuidedSessionState.RUNNING -> {

                val remainingMillis =
                    calculateRunningRemainingMillis()

                val maximumExpectedMillis =
                    guidedActivity.definition
                        .durationSecondsMax
                        .toLong() *
                            1000L

                /*
                 * elapsedRealtime repart de zéro
                 * après un redémarrage complet
                 * du téléphone.
                 *
                 * Si l'échéance restaurée donne
                 * un temps absurde supérieur à
                 * la durée de l'activité, on
                 * abandonne simplement cette
                 * restauration plutôt que
                 * relancer un faux chrono.
                 */
                if (
                    remainingMillis >
                    maximumExpectedMillis +
                    2000L
                ) {
                    clearGuidedSession()
                    publishState()
                    return
                }

                if (
                    remainingMillis <= 0L
                ) {
                    finishGuidedSessionNaturally()
                } else {
                    startTimerTicker()
                    publishState()
                }
            }

            GuidedSessionState.PAUSED -> {

                val remainingMillis =
                    savedStateHandle
                        .get<Long>(
                            KEY_GUIDED_PAUSED_REMAINING_MILLIS
                        )
                        ?: 0L

                if (
                    remainingMillis <= 0L
                ) {
                    finishGuidedSessionNaturally()
                } else {
                    publishState()
                }
            }

            GuidedSessionState.TIME_ELAPSED -> {

                recordGuidedActivityAsDone()
                publishState()
            }

            GuidedSessionState.READY -> {
                Unit
            }
        }
    }

    private fun startTimerTicker() {

        timerJob?.cancel()

        timerJob =
            viewModelScope.launch {

                while (
                    isActive &&
                    readGuidedSessionState() ==
                    GuidedSessionState.RUNNING
                ) {

                    val remainingMillis =
                        calculateRunningRemainingMillis()

                    if (
                        remainingMillis <= 0L
                    ) {
                        finishGuidedSessionNaturally()
                        break
                    }

                    publishState()

                    delay(
                        250L
                    )
                }
            }
    }

    private fun finishGuidedSessionNaturally() {

        timerJob?.cancel()
        timerJob = null

        savedStateHandle[
            KEY_GUIDED_STATE
        ] =
            GuidedSessionState
                .TIME_ELAPSED
                .name

        savedStateHandle
            .remove<Long>(
                KEY_GUIDED_END_ELAPSED_REALTIME
            )

        savedStateHandle
            .remove<Long>(
                KEY_GUIDED_PAUSED_REMAINING_MILLIS
            )

        recordGuidedActivityAsDone()
        publishState()
    }

    private fun clearGuidedSession() {

        timerJob?.cancel()
        timerJob = null

        savedStateHandle
            .remove<String>(
                KEY_GUIDED_STATE
            )

        savedStateHandle
            .remove<String>(
                KEY_GUIDED_ACTIVITY_ID
            )

        savedStateHandle
            .remove<Long>(
                KEY_GUIDED_END_ELAPSED_REALTIME
            )

        savedStateHandle
            .remove<Long>(
                KEY_GUIDED_PAUSED_REMAINING_MILLIS
            )

        savedStateHandle
            .remove<Boolean>(
                KEY_COMPLETION_HAPTIC_DELIVERED
            )

        savedStateHandle
            .remove<Boolean>(
                KEY_GUIDED_COMPLETION_RECORDED
            )
    }

    private fun calculateRunningRemainingMillis():
            Long {

        val end =
            savedStateHandle
                .get<Long>(
                    KEY_GUIDED_END_ELAPSED_REALTIME
                )
                ?: return 0L

        return (
                end -
                        SystemClock.elapsedRealtime()
                )
            .coerceAtLeast(
                0L
            )
    }

    private fun calculateRemainingSeconds():
            Int {

        val remainingMillis =
            when (
                readGuidedSessionState()
            ) {

                GuidedSessionState.RUNNING ->
                    calculateRunningRemainingMillis()

                GuidedSessionState.PAUSED ->
                    savedStateHandle
                        .get<Long>(
                            KEY_GUIDED_PAUSED_REMAINING_MILLIS
                        )
                        ?: 0L

                GuidedSessionState.TIME_ELAPSED ->
                    0L

                GuidedSessionState.READY ->
                    0L
            }

        if (remainingMillis <= 0L) {
            return 0
        }

        return (
                (
                        remainingMillis +
                                999L
                        ) /
                        1000L
                )
            .coerceAtMost(
                Int.MAX_VALUE
                    .toLong()
            )
            .toInt()
    }

    private fun readGuidedSessionState():
            GuidedSessionState {

        val value =
            savedStateHandle
                .get<String>(
                    KEY_GUIDED_STATE
                )
                ?: return GuidedSessionState.READY

        return runCatching {
            GuidedSessionState
                .valueOf(
                    value
                )
        }.getOrDefault(
            GuidedSessionState.READY
        )
    }

    private fun currentDisplayedActivity():
            CatalogActivity? {

        val explored =
            readExploredActivityIds()

        if (explored.isEmpty()) {
            return null
        }

        val index =
            readCurrentIndex()
                .coerceIn(
                    0,
                    explored.lastIndex,
                )

        val id =
            explored[index]

        return activities
            .firstOrNull {
                it.definition.id ==
                        id
            }
    }

    private fun observeActivityPreferences() {

        viewModelScope.launch {
            appStateRepository
                .activityPreferences
                .collect { preferences ->

                    if (
                        preferences ==
                        activityPreferences
                    ) {
                        return@collect
                    }

                    activityPreferences =
                        preferences

                    publishState()
                }
        }
    }

    private fun observeProposalHistory() {

        viewModelScope.launch {
            appStateRepository
                .proposalHistory
                .collect { history ->

                    val updatedIds =
                        history
                            .mapNotNull { entry ->
                                entry.activityId
                                    .takeIf { id ->
                                        activities.any { activity ->
                                            activity.definition.id ==
                                                id
                                        }
                                    }
                            }
                            .takeLast(
                                policy.scoring
                                    .historyWindowProposals
                            )

                    if (
                        updatedIds ==
                        proposalHistoryIds
                    ) {
                        return@collect
                    }

                    proposalHistoryIds =
                        updatedIds

                    publishState()
                }
        }
    }

    private fun currentHistoryDefinitions():
            List<org.sedentaryapp.engine.model.ActivityDefinition> {

        return proposalHistoryIds
            .mapNotNull { id ->
                definitions
                    .firstOrNull { definition ->
                        definition.id ==
                            id
                    }
            }
    }

    private fun observePersistentLastActivity() {

        viewModelScope.launch {
            appStateRepository
                .lastActivityId
                .collect { persistedId ->

                    val validId =
                        persistedId
                            ?.takeIf { id ->
                                activities.any { activity ->
                                    activity.definition.id ==
                                        id
                                }
                            }

                    val currentId =
                        savedStateHandle
                            .get<String>(
                                KEY_LAST_PROPOSED_ACTIVITY_ID
                            )

                    if (
                        currentId ==
                        validId
                    ) {
                        return@collect
                    }

                    if (validId == null) {
                        savedStateHandle
                            .remove<String>(
                                KEY_LAST_PROPOSED_ACTIVITY_ID
                            )
                    } else {
                        savedStateHandle[
                            KEY_LAST_PROPOSED_ACTIVITY_ID
                        ] =
                            validId
                    }

                    publishState()
                }
        }
    }

    private fun claimOpportunityForGuidedSession() {

        val context =
            readDisplayContext()

        when (context) {

            ActivityDisplayContext.FUTURE_PREVIEW -> {
                val opportunityKey =
                    savedStateHandle
                        .get<String>(
                            KEY_FUTURE_OPPORTUNITY_KEY
                        )
                        ?: return

                viewModelScope.launch {
                    appStateRepository
                        .markOpportunityConsumedEarly(
                            opportunityKey
                        )
                }
            }

            ActivityDisplayContext.CURRENT_PROPOSAL,
            ActivityDisplayContext.SNOOZED_PROPOSAL -> {
                val proposalId =
                    savedStateHandle
                        .get<String>(
                            KEY_PROPOSAL_ID
                        )
                        ?: return

                snoozedActivityPersistenceJob?.cancel()
                snoozedActivityPersistenceJob = null

                viewModelScope.launch {
                    appStateRepository
                        .clearSnoozedProposalIfProposalKey(
                            proposalId
                        )

                    appStateRepository
                        .clearActiveProposalIfProposalKey(
                            proposalId
                        )
                }
            }

            else -> Unit
        }
    }

    private fun recordGuidedActivityAsDone() {

        if (
            savedStateHandle
                .get<Boolean>(
                    KEY_GUIDED_COMPLETION_RECORDED
                ) == true
        ) {
            return
        }

        val activityId =
            savedStateHandle
                .get<String>(
                    KEY_GUIDED_ACTIVITY_ID
                )
                ?: return

        savedStateHandle[
            KEY_GUIDED_COMPLETION_RECORDED
        ] = true

        recordActivityAsDone(
            activityId
        )
    }

    private fun recordActivityAsDone(
        activityId: String,
    ) {

        val context =
            readDisplayContext()

        val completedAt =
            System.currentTimeMillis()

        savedStateHandle[
            KEY_LAST_PROPOSED_ACTIVITY_ID
        ] = activityId

        when (context) {

            ActivityDisplayContext.FUTURE_PREVIEW -> {
                val opportunityKey =
                    savedStateHandle
                        .get<String>(
                            KEY_FUTURE_OPPORTUNITY_KEY
                        )

                viewModelScope.launch {
                    if (opportunityKey != null) {
                        appStateRepository
                            .markOpportunityConsumedEarly(
                                opportunityKey
                            )

                        appStateRepository
                            .saveLastActivityFromCompletedFuture(
                                activityId =
                                    activityId,
                                opportunityKey =
                                    opportunityKey,
                                completedAtEpochMillis =
                                    completedAt,
                            )
                    } else {
                        appStateRepository
                            .saveLastActivityNow(
                                activityId =
                                    activityId,
                                completedAtEpochMillis =
                                    completedAt,
                            )
                    }
                }
            }

            ActivityDisplayContext.CURRENT_PROPOSAL,
            ActivityDisplayContext.SNOOZED_PROPOSAL -> {
                val proposalId =
                    savedStateHandle
                        .get<String>(
                            KEY_PROPOSAL_ID
                        )

                viewModelScope.launch {
                    if (proposalId != null) {
                        appStateRepository
                            .clearSnoozedProposalIfProposalKey(
                                proposalId
                            )

                        appStateRepository
                            .clearActiveProposalIfProposalKey(
                                proposalId
                            )

                        appStateRepository
                            .saveLastActivity(
                                activityId =
                                    activityId,
                                proposalKey =
                                    proposalId,
                                proposedAtEpochMillis =
                                    completedAt,
                            )
                    } else {
                        appStateRepository
                            .saveLastActivityNow(
                                activityId =
                                    activityId,
                                completedAtEpochMillis =
                                    completedAt,
                            )
                    }
                }
            }

            ActivityDisplayContext.MANUAL_ACTIVITY -> {
                val manualSessionId =
                    savedStateHandle
                        .get<String>(
                            KEY_MANUAL_SESSION_ID
                        )
                        ?: "manual-$completedAt"

                viewModelScope.launch {
                    appStateRepository
                        .saveLastActivityNow(
                            activityId =
                                activityId,
                            completedAtEpochMillis =
                                completedAt,
                        )

                    appStateRepository
                        .recordProposal(
                            proposalKey =
                                manualSessionId,
                            activityId =
                                activityId,
                            maxEntries =
                                policy.scoring
                                    .historyWindowProposals,
                        )
                }
            }

            ActivityDisplayContext.LAST_ACTIVITY,
            null -> {
                viewModelScope.launch {
                    appStateRepository
                        .saveLastActivityNow(
                            activityId =
                                activityId,
                            completedAtEpochMillis =
                                completedAt,
                        )
                }
            }
        }
    }

    private fun clearInteractionAfterExit() {

        clearActiveProposal()

        savedStateHandle
            .remove<String>(
                KEY_DISPLAY_CONTEXT
            )

        savedStateHandle
            .remove<String>(
                KEY_FUTURE_OPPORTUNITY_KEY
            )

        savedStateHandle
            .remove<String>(
                KEY_MANUAL_SESSION_ID
            )

        writeExploredActivityIds(
            emptyList()
        )

        savedStateHandle[
            KEY_CURRENT_INDEX
        ] = 0
    }

    private fun persistSnoozedActivity(
        activityId: String,
    ) {

        val proposalId =
            savedStateHandle
                .get<String>(
                    KEY_PROPOSAL_ID
                )
                ?: return

        snoozedActivityPersistenceJob?.cancel()

        snoozedActivityPersistenceJob =
            viewModelScope.launch {

                val snooze =
                    appStateRepository
                        .snoozedProposal
                        .first()
                        ?: return@launch

                if (
                    snooze.proposalKey !=
                    proposalId
                ) {
                    return@launch
                }

                appStateRepository
                    .saveSnoozedProposal(
                        proposalKey =
                            snooze.proposalKey,
                        activityId =
                            activityId,
                        triggerAtEpochMillis =
                            snooze.triggerAtEpochMillis,
                    )
            }
    }
    private fun persistFutureOverride(
        activityId: String,
    ) {

        val opportunityKey =
            savedStateHandle
                .get<String>(
                    KEY_FUTURE_OPPORTUNITY_KEY
                )
                ?: return

        futureOverrideJob?.cancel()

        futureOverrideJob =
            viewModelScope.launch {
                appStateRepository
                    .savePlannedActivityOverride(
                        opportunityKey =
                            opportunityKey,
                        activityId =
                            activityId,
                    )
            }
    }

    private fun standardFilterRequest(
        excludedIds: Set<String>,
    ): CandidateFilterRequest {

        val requiredWalking =
            when {
                activityPreferences
                    .stationaryEnabled &&
                    activityPreferences
                        .movingEnabled ->
                    null

                activityPreferences
                    .movingEnabled ->
                    true

                else ->
                    false
            }

        return CandidateFilterRequest(
            enabledIntensities =
                activityPreferences
                    .enabledIntensities,
            requiredWalking =
                requiredWalking,
            excludedActivityIds =
                excludedIds,
        )
    }

    private fun selectActivity(
        excludedIds: Set<String>,
        attempt: Int,
    ): CatalogActivity? {

        val seedBase =
            when (
                readDisplayContext()
            ) {

                ActivityDisplayContext.CURRENT_PROPOSAL,
                ActivityDisplayContext.SNOOZED_PROPOSAL ->
                    savedStateHandle
                        .get<String>(
                            KEY_PROPOSAL_ID
                        )

                ActivityDisplayContext.FUTURE_PREVIEW ->
                    savedStateHandle
                        .get<String>(
                            KEY_FUTURE_OPPORTUNITY_KEY
                        )
                        ?.let {
                            "future-$it"
                        }

                ActivityDisplayContext.MANUAL_ACTIVITY ->
                    savedStateHandle
                        .get<String>(
                            KEY_MANUAL_SESSION_ID
                        )

                else ->
                    null
            }
                ?: return null

        val selectedDefinition =
            DecisionEngine.chooseActivity(
                activities =
                    definitions,
                history =
                    currentHistoryDefinitions(),
                policy =
                    policy,
                seed =
                    "$seedBase-$attempt",
                filterRequest =
                    standardFilterRequest(
                        excludedIds =
                            excludedIds
                    ),
            )

        return activities
            .firstOrNull {
                it.definition.id ==
                        selectedDefinition?.id
            }
    }

    private fun publishState() {

        val lastProposedId =
            savedStateHandle
                .get<String>(
                    KEY_LAST_PROPOSED_ACTIVITY_ID
                )

        val lastProposedActivity =
            activities
                .firstOrNull {
                    it.definition.id ==
                            lastProposedId
                }

        val explored =
            readExploredActivityIds()

        val currentIndex =
            if (explored.isEmpty()) {
                0
            } else {
                readCurrentIndex()
                    .coerceIn(
                        0,
                        explored.lastIndex,
                    )
            }

        val guidedState =
            readGuidedSessionState()

        val currentId =
            if (
                guidedState ==
                GuidedSessionState.READY
            ) {
                explored.getOrNull(
                    currentIndex
                )
            } else {
                savedStateHandle
                    .get<String>(
                        KEY_GUIDED_ACTIVITY_ID
                    )
                    ?: explored.getOrNull(
                        currentIndex
                    )
            }

        val currentActivity =
            activities
                .firstOrNull {
                    it.definition.id ==
                            currentId
                }

        val context =
            readDisplayContext()

        val ready =
            guidedState ==
                    GuidedSessionState.READY

        val atEnd =
            explored.isNotEmpty() &&
                    currentIndex ==
                    explored.lastIndex

        val alternativesRemain =
            if (
                ready &&
                atEnd &&
                (
                        context ==
                                ActivityDisplayContext
                                    .CURRENT_PROPOSAL ||
                                context ==
                                ActivityDisplayContext
                                    .SNOOZED_PROPOSAL ||
                                context ==
                                ActivityDisplayContext
                                    .FUTURE_PREVIEW ||
                                context ==
                                ActivityDisplayContext
                                    .MANUAL_ACTIVITY
                        )
            ) {

                CandidateFilter.filter(
                    activities =
                        definitions,
                    request =
                        standardFilterRequest(
                            excludedIds =
                                explored.toSet()
                        ),
                ).isNotEmpty()

            } else {
                false
            }

        _uiState.value =
            ActivityUiState(
                activity =
                    currentActivity,
                lastProposedActivity =
                    lastProposedActivity,
                context =
                    context,
                canGoPrevious =
                    ready &&
                            currentIndex > 0,
                canGoNext =
                    ready &&
                            explored.isNotEmpty() &&
                            currentIndex <
                            explored.lastIndex,
                canRequestAnother =
                    alternativesRemain,
                showNotNow =
                    ready &&
                        (
                            context ==
                                ActivityDisplayContext
                                    .CURRENT_PROPOSAL ||
                            context ==
                                ActivityDisplayContext
                                    .SNOOZED_PROPOSAL
                        ),
                guidedSessionState =
                    guidedState,
                remainingSeconds =
                    calculateRemainingSeconds(),
                shouldPlayCompletionHaptic =
                    guidedState ==
                            GuidedSessionState.TIME_ELAPSED &&
                            savedStateHandle
                                .get<Boolean>(
                                    KEY_COMPLETION_HAPTIC_DELIVERED
                                ) != true,
                activityPreferences =
                    activityPreferences,
                recentProposalActivityIds =
                    proposalHistoryIds,
            )
    }

    private fun readDisplayContext():
            ActivityDisplayContext? {

        val value =
            savedStateHandle
                .get<String>(
                    KEY_DISPLAY_CONTEXT
                )
                ?: return null

        return runCatching {
            ActivityDisplayContext
                .valueOf(
                    value
                )
        }.getOrNull()
    }

    private fun isProposalActive():
            Boolean {

        return savedStateHandle
            .get<Boolean>(
                KEY_PROPOSAL_ACTIVE
            ) == true
    }

    private fun clearActiveProposal() {

        savedStateHandle[
            KEY_PROPOSAL_ACTIVE
        ] = false

        savedStateHandle
            .remove<String>(
                KEY_PROPOSAL_ID
            )

        savedStateHandle
            .remove<Int>(
                KEY_ATTEMPT
            )
    }

    private fun readExploredActivityIds():
            List<String> {

        return savedStateHandle
            .get<ArrayList<String>>(
                KEY_EXPLORED_ACTIVITY_IDS
            )
            ?.toList()
            ?: emptyList()
    }

    private fun writeExploredActivityIds(
        ids: List<String>,
    ) {

        savedStateHandle[
            KEY_EXPLORED_ACTIVITY_IDS
        ] =
            ArrayList(
                ids
            )
    }

    private fun readCurrentIndex():
            Int {

        return savedStateHandle
            .get<Int>(
                KEY_CURRENT_INDEX
            ) ?: 0
    }

    companion object {

        private const val
                KEY_LAST_PROPOSED_ACTIVITY_ID =
            "last_activity_id"

        private const val
                KEY_COMPLETION_HAPTIC_DELIVERED =
            "completion_haptic_delivered"
        private const val
                KEY_EXPLORED_ACTIVITY_IDS =
            "explored_activity_ids"

        private const val
                KEY_CURRENT_INDEX =
            "current_activity_index"

        private const val
                KEY_ATTEMPT =
            "alternative_attempt"

        private const val
                KEY_PROPOSAL_ID =
            "proposal_id"

        private const val
                KEY_PROPOSAL_ACTIVE =
            "proposal_active"

        private const val
                KEY_DISPLAY_CONTEXT =
            "activity_display_context"

        private const val
                KEY_FUTURE_OPPORTUNITY_KEY =
            "future_opportunity_key"

        private const val
                KEY_MANUAL_SESSION_ID =
            "manual_session_id"

        private const val
                KEY_GUIDED_STATE =
            "guided_session_state"

        private const val
                KEY_GUIDED_ACTIVITY_ID =
            "guided_activity_id"

        private const val
                KEY_GUIDED_COMPLETION_RECORDED =
            "guided_completion_recorded"

        private const val
                KEY_GUIDED_END_ELAPSED_REALTIME =
            "guided_end_elapsed_realtime"

        private const val
                KEY_GUIDED_PAUSED_REMAINING_MILLIS =
            "guided_paused_remaining_millis"

        fun factory(
            activities:
            List<CatalogActivity>,
            policy:
            DecisionPolicy,
            appStateRepository:
            AppStateRepository,
        ): ViewModelProvider.Factory =
            viewModelFactory {

                initializer {

                    ActivityViewModel(
                        activities =
                            activities,
                        policy =
                            policy,
                        appStateRepository =
                            appStateRepository,
                        savedStateHandle =
                            createSavedStateHandle(),
                    )
                }
            }
    }
}