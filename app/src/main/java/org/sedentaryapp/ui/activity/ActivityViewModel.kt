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

data class ActivityUiState(
    val activity: CatalogActivity? = null,
    val isActiveProposal: Boolean = false,
    val hasAlternative: Boolean = false,
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
        MutableStateFlow(ActivityUiState())

    val uiState: StateFlow<ActivityUiState> =
        _uiState.asStateFlow()

    init {
        restoreState()
        restorePersistentLastActivity()
    }

    fun startDemoProposal() {
        val number =
            (savedStateHandle.get<Int>(
                KEY_DEMO_PROPOSAL_NUMBER
            ) ?: 0) + 1

        savedStateHandle[
            KEY_DEMO_PROPOSAL_NUMBER
        ] = number

        startNewProposal(
            proposalId = "demo-proposal-$number"
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
            KEY_ATTEMPT
        ] = 0

        writeShownActivityIds(
            emptySet()
        )

        val selected =
            selectActivity(
                excludedIds = emptySet(),
                attempt = 0,
            )

        if (selected == null) {
            clearActiveProposal()
            publishState()
            return
        }

        savedStateHandle[
            KEY_LAST_ACTIVITY_ID
        ] = selected.definition.id

        persistLastActivity(
            selected.definition.id
        )

        writeShownActivityIds(
            setOf(selected.definition.id)
        )

        publishState()
    }

    fun onOtherActivity() {
        if (!isProposalActive()) {
            return
        }

        val currentId =
            savedStateHandle.get<String>(
                KEY_LAST_ACTIVITY_ID
            ) ?: return

        val alreadyShown =
            readShownActivityIds() +
                    currentId

        val newAttempt =
            (savedStateHandle.get<Int>(
                KEY_ATTEMPT
            ) ?: 0) + 1

        val selected =
            selectActivity(
                excludedIds = alreadyShown,
                attempt = newAttempt,
            ) ?: run {
                publishState()
                return
            }

        savedStateHandle[
            KEY_LAST_ACTIVITY_ID
        ] = selected.definition.id

        savedStateHandle[
            KEY_ATTEMPT
        ] = newAttempt

        persistLastActivity(
            selected.definition.id
        )

        writeShownActivityIds(
            alreadyShown +
                    selected.definition.id
        )

        publishState()
    }

    fun onNotNow() {
        clearActiveProposal()
        publishState()
    }

    private fun restoreState() {
        val lastActivityId =
            savedStateHandle.get<String>(
                KEY_LAST_ACTIVITY_ID
            )

        val lastActivityExists =
            activities.any {
                it.definition.id ==
                        lastActivityId
            }

        if (!lastActivityExists) {
            savedStateHandle
                .remove<String>(
                    KEY_LAST_ACTIVITY_ID
                )

            clearActiveProposal()
            publishState()
            return
        }

        if (isProposalActive()) {
            writeShownActivityIds(
                readShownActivityIds() +
                        lastActivityId!!
            )
        } else {
            clearActiveProposal()
        }

        publishState()
    }

    private fun restorePersistentLastActivity() {
        if (
            savedStateHandle.get<String>(
                KEY_LAST_ACTIVITY_ID
            ) != null
        ) {
            return
        }

        viewModelScope.launch {
            val persistedId =
                appStateRepository
                    .lastActivityId
                    .first()

            val validId =
                persistedId != null &&
                        activities.any {
                            it.definition.id ==
                                    persistedId
                        }

            if (
                validId &&
                savedStateHandle.get<String>(
                    KEY_LAST_ACTIVITY_ID
                ) == null
            ) {
                savedStateHandle[
                    KEY_LAST_ACTIVITY_ID
                ] = persistedId

                clearActiveProposal()
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
            savedStateHandle.get<String>(
                KEY_PROPOSAL_ID
            ) ?: return null

        val selectedDefinition =
            DecisionEngine.chooseActivity(
                activities = definitions,
                history = emptyList(),
                policy = policy,
                seed = "$proposalId-$attempt",
                filterRequest =
                    CandidateFilterRequest(
                        opportunityType =
                            "standard",
                        excludedActivityIds =
                            excludedIds,
                    ),
            )

        return activities.firstOrNull {
            it.definition.id ==
                    selectedDefinition?.id
        }
    }

    private fun publishState() {
        val lastActivityId =
            savedStateHandle.get<String>(
                KEY_LAST_ACTIVITY_ID
            )

        val lastActivity =
            activities.firstOrNull {
                it.definition.id ==
                        lastActivityId
            }

        if (lastActivity == null) {
            _uiState.value =
                ActivityUiState()
            return
        }

        val active =
            isProposalActive() &&
                    savedStateHandle.get<String>(
                        KEY_PROPOSAL_ID
                    ) != null

        val alternativesRemain =
            if (active) {
                val shownIds =
                    readShownActivityIds() +
                            lastActivity.definition.id

                CandidateFilter.filter(
                    activities = definitions,
                    request =
                        CandidateFilterRequest(
                            opportunityType =
                                "standard",
                            excludedActivityIds =
                                shownIds,
                        ),
                ).isNotEmpty()
            } else {
                false
            }

        _uiState.value =
            ActivityUiState(
                activity = lastActivity,
                isActiveProposal = active,
                hasAlternative =
                    alternativesRemain,
            )
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

        writeShownActivityIds(
            emptySet()
        )
    }

    private fun readShownActivityIds():
            Set<String> {
        return savedStateHandle
            .get<ArrayList<String>>(
                KEY_SHOWN_ACTIVITY_IDS
            )
            ?.toSet()
            ?: emptySet()
    }

    private fun writeShownActivityIds(
        ids: Set<String>,
    ) {
        savedStateHandle[
            KEY_SHOWN_ACTIVITY_IDS
        ] = ArrayList(ids)
    }

    companion object {
        private const val
                KEY_LAST_ACTIVITY_ID =
            "last_activity_id"

        private const val
                KEY_SHOWN_ACTIVITY_IDS =
            "shown_activity_ids"

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
                KEY_DEMO_PROPOSAL_NUMBER =
            "demo_proposal_number"

        fun factory(
            activities: List<CatalogActivity>,
            policy: DecisionPolicy,
            appStateRepository:
            AppStateRepository,
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    ActivityViewModel(
                        activities = activities,
                        policy = policy,
                        appStateRepository =
                            appStateRepository,
                        savedStateHandle =
                            createSavedStateHandle(),
                    )
                }
            }
    }
}