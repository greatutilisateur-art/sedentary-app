package org.sedentaryapp.ui.activity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.sedentaryapp.data.AppStateRepository
import org.sedentaryapp.data.CatalogActivity
import org.sedentaryapp.engine.decision.CandidateFilter
import org.sedentaryapp.engine.decision.DecisionEngine
import org.sedentaryapp.engine.model.CandidateFilterRequest
import org.sedentaryapp.engine.model.DecisionPolicy

enum class ActivityDisplayContext {
    CURRENT_PROPOSAL,
    LAST_ACTIVITY,
    FUTURE_PREVIEW,
}

data class ActivityUiState(
    val activity: CatalogActivity? = null,
    val lastProposedActivity: CatalogActivity? = null,
    val context: ActivityDisplayContext? = null,
    val canGoPrevious: Boolean = false,
    val canGoNext: Boolean = false,
    val canRequestAnother: Boolean = false,
    val showNotNow: Boolean = false,
)

class ActivityViewModel(
    private val activities: List<CatalogActivity>,
    private val policy: DecisionPolicy,
    private val appStateRepository: AppStateRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

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
        restorePersistentLastActivity()
    }

    fun startDemoProposal() {
        val number =
            (
                    savedStateHandle.get<Int>(
                        KEY_DEMO_PROPOSAL_NUMBER
                    ) ?: 0
                    ) + 1

        savedStateHandle[
            KEY_DEMO_PROPOSAL_NUMBER
        ] = number

        startNewProposal(
            proposalId =
                "demo-proposal-$number"
        )
    }

    fun startNewProposal(
        proposalId: String,
    ) {
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

        val selected =
            selectActivity(
                excludedIds =
                    emptySet(),
                attempt = 0,
            )

        if (selected == null) {
            clearActiveProposal()
            publishState()
            return
        }

        val selectedId =
            selected.definition.id

        savedStateHandle[
            KEY_LAST_PROPOSED_ACTIVITY_ID
        ] = selectedId

        persistLastActivity(
            selectedId
        )

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

    fun onOtherActivity() {
        val context =
            readDisplayContext()

        if (
            context !=
            ActivityDisplayContext
                .CURRENT_PROPOSAL &&
            context !=
            ActivityDisplayContext
                .FUTURE_PREVIEW
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

        /*
         * Important :
         * on ne modifie PAS
         * KEY_LAST_PROPOSED_ACTIVITY_ID.
         *
         * Explorer une alternative
         * n'est pas une nouvelle proposition
         * automatique.
         *
         * Le FUTURE_PREVIEW ajoutera plus tard
         * ici la persistance de l'override.
         */

        publishState()
    }

    fun onPreviousActivity() {
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

        savedStateHandle[
            KEY_CURRENT_INDEX
        ] =
            current - 1

        /*
         * FUTURE_PREVIEW :
         * l'override persistant sera
         * synchronisé ici dans une
         * prochaine étape.
         */

        publishState()
    }

    fun onNextActivity() {
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

        savedStateHandle[
            KEY_CURRENT_INDEX
        ] =
            current + 1

        /*
         * FUTURE_PREVIEW :
         * idem, l'override sera
         * synchronisé plus tard.
         */

        publishState()
    }

    fun openLastActivity() {
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

    fun onNotNow() {
        if (
            readDisplayContext() !=
            ActivityDisplayContext
                .CURRENT_PROPOSAL
        ) {
            return
        }

        clearActiveProposal()

        savedStateHandle
            .remove<String>(
                KEY_LAST_PROPOSED_ACTIVITY_ID
            )

        writeExploredActivityIds(
            emptyList()
        )

        viewModelScope.launch {
            appStateRepository
                .clearLastActivityId()
        }

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

    private fun restorePersistentLastActivity() {
        if (
            savedStateHandle.get<String>(
                KEY_LAST_PROPOSED_ACTIVITY_ID
            ) != null
        ) {
            return
        }

        viewModelScope.launch {

            val persistedId =
                appStateRepository
                    .lastActivityId
                    .first()

            val valid =
                persistedId != null &&
                        activities.any {
                            it.definition.id ==
                                    persistedId
                        }

            if (
                valid &&
                savedStateHandle.get<String>(
                    KEY_LAST_PROPOSED_ACTIVITY_ID
                ) == null
            ) {
                savedStateHandle[
                    KEY_LAST_PROPOSED_ACTIVITY_ID
                ] =
                    persistedId

                if (!isProposalActive()) {
                    savedStateHandle[
                        KEY_DISPLAY_CONTEXT
                    ] =
                        ActivityDisplayContext
                            .LAST_ACTIVITY
                            .name

                    writeExploredActivityIds(
                        listOf(
                            persistedId!!
                        )
                    )

                    savedStateHandle[
                        KEY_CURRENT_INDEX
                    ] = 0
                }

                publishState()
            }
        }
    }

    private fun persistLastActivity(
        activityId: String,
    ) {
        viewModelScope.launch {
            appStateRepository
                .saveLastActivityId(
                    activityId
                )
        }
    }

    private fun selectActivity(
        excludedIds: Set<String>,
        attempt: Int,
    ): CatalogActivity? {

        val proposalId =
            savedStateHandle
                .get<String>(
                    KEY_PROPOSAL_ID
                )
                ?: return null

        val selectedDefinition =
            DecisionEngine.chooseActivity(
                activities =
                    definitions,
                history =
                    emptyList(),
                policy =
                    policy,
                seed =
                    "$proposalId-$attempt",
                filterRequest =
                    CandidateFilterRequest(
                        opportunityType =
                            "standard",
                        excludedActivityIds =
                            excludedIds,
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

        val currentId =
            explored
                .getOrNull(
                    currentIndex
                )

        val currentActivity =
            activities
                .firstOrNull {
                    it.definition.id ==
                            currentId
                }

        val context =
            readDisplayContext()

        val atEnd =
            explored.isNotEmpty() &&
                    currentIndex ==
                    explored.lastIndex

        val alternativesRemain =
            if (
                atEnd &&
                (
                        context ==
                                ActivityDisplayContext
                                    .CURRENT_PROPOSAL ||
                                context ==
                                ActivityDisplayContext
                                    .FUTURE_PREVIEW
                        )
            ) {

                CandidateFilter.filter(
                    activities =
                        definitions,
                    request =
                        CandidateFilterRequest(
                            opportunityType =
                                "standard",
                            excludedActivityIds =
                                explored.toSet(),
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
                    currentIndex > 0,
                canGoNext =
                    explored.isNotEmpty() &&
                            currentIndex <
                            explored.lastIndex,
                canRequestAnother =
                    alternativesRemain,
                showNotNow =
                    context ==
                            ActivityDisplayContext
                                .CURRENT_PROPOSAL,
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
            ArrayList(ids)
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
                KEY_DEMO_PROPOSAL_NUMBER =
            "demo_proposal_number"

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