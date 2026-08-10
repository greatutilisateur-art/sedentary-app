package org.sedentaryapp.ui.activity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sedentaryapp.data.CatalogActivity
import org.sedentaryapp.engine.decision.CandidateFilter
import org.sedentaryapp.engine.decision.DecisionEngine
import org.sedentaryapp.engine.model.CandidateFilterRequest
import org.sedentaryapp.engine.model.DecisionPolicy

data class ActivityUiState(
    val activity: CatalogActivity? = null,
    val hasAlternative: Boolean = false,
)

class ActivityViewModel(
    private val activities: List<CatalogActivity>,
    private val policy: DecisionPolicy,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val definitions =
        activities.map { it.definition }

    private val _uiState =
        MutableStateFlow(ActivityUiState())

    val uiState: StateFlow<ActivityUiState> =
        _uiState.asStateFlow()

    init {
        restoreOrCreateInteraction()
    }

    fun onOtherActivity() {

        val currentId =
            savedStateHandle.get<String>(
                KEY_CURRENT_ACTIVITY_ID
            ) ?: return

        val alreadyShown =
            readShownActivityIds() + currentId

        val newAttempt =
            savedStateHandle.get<Int>(
                KEY_ATTEMPT
            )?.plus(1) ?: 1

        val selected =
            selectActivity(
                excludedIds = alreadyShown,
                attempt = newAttempt,
            ) ?: run {
                publishState()
                return
            }

        val newShown =
            alreadyShown + selected.definition.id

        savedStateHandle[KEY_CURRENT_ACTIVITY_ID] =
            selected.definition.id

        savedStateHandle[KEY_ATTEMPT] =
            newAttempt

        writeShownActivityIds(
            newShown
        )

        publishState()
    }

    private fun restoreOrCreateInteraction() {

        val restoredId =
            savedStateHandle.get<String>(
                KEY_CURRENT_ACTIVITY_ID
            )

        val restoredActivity =
            activities.firstOrNull {
                it.definition.id == restoredId
            }

        if (restoredActivity != null) {

            val shown =
                readShownActivityIds() +
                        restoredActivity.definition.id

            writeShownActivityIds(shown)

            publishState()

            return
        }

        createNewInteraction()
    }

    private fun createNewInteraction() {

        val selected =
            selectActivity(
                excludedIds = emptySet(),
                attempt = 0,
            )

        savedStateHandle[KEY_ATTEMPT] = 0

        if (selected == null) {

            savedStateHandle
                .remove<String>(
                    KEY_CURRENT_ACTIVITY_ID
                )

            writeShownActivityIds(
                emptySet()
            )

            publishState()

            return
        }

        savedStateHandle[KEY_CURRENT_ACTIVITY_ID] =
            selected.definition.id

        writeShownActivityIds(
            setOf(
                selected.definition.id
            )
        )

        publishState()
    }

    private fun selectActivity(
        excludedIds: Set<String>,
        attempt: Int,
    ): CatalogActivity? {

        val selectedDefinition =
            DecisionEngine.chooseActivity(
                activities = definitions,
                history = emptyList(),
                policy = policy,
                seed = "activity-screen-$attempt",
                filterRequest =
                    CandidateFilterRequest(
                        opportunityType = "standard",
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

        val currentId =
            savedStateHandle.get<String>(
                KEY_CURRENT_ACTIVITY_ID
            )

        val currentActivity =
            activities.firstOrNull {
                it.definition.id == currentId
            }

        val shownIds =
            readShownActivityIds() +
                    listOfNotNull(currentId)

        val alternatives =
            CandidateFilter.filter(
                activities = definitions,
                request =
                    CandidateFilterRequest(
                        opportunityType = "standard",
                        excludedActivityIds =
                            shownIds,
                    ),
            )

        _uiState.value =
            ActivityUiState(
                activity = currentActivity,
                hasAlternative =
                    alternatives.isNotEmpty(),
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
                KEY_CURRENT_ACTIVITY_ID =
            "current_activity_id"

        private const val
                KEY_SHOWN_ACTIVITY_IDS =
            "shown_activity_ids"

        private const val
                KEY_ATTEMPT =
            "alternative_attempt"

        fun factory(
            activities: List<CatalogActivity>,
            policy: DecisionPolicy,
        ): ViewModelProvider.Factory =
            viewModelFactory {

                initializer {

                    ActivityViewModel(
                        activities = activities,
                        policy = policy,
                        savedStateHandle =
                            createSavedStateHandle(),
                    )
                }
            }
    }
}