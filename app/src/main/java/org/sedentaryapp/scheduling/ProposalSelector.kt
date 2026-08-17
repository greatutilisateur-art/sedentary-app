package org.sedentaryapp.scheduling

import org.sedentaryapp.data.ActivityPreferences
import org.sedentaryapp.data.CatalogActivity
import org.sedentaryapp.data.PlannedActivityOverride
import org.sedentaryapp.data.ProposalHistoryEntry
import org.sedentaryapp.engine.decision.CandidateFilter
import org.sedentaryapp.engine.decision.DecisionEngine
import org.sedentaryapp.engine.model.CandidateFilterRequest
import org.sedentaryapp.engine.model.DecisionPolicy

object ProposalSelector {

    fun select(
        opportunityKey: String,
        activities: List<CatalogActivity>,
        policy: DecisionPolicy,
        activityPreferences: ActivityPreferences,
        proposalHistory: List<ProposalHistoryEntry>,
        plannedOverride: PlannedActivityOverride?,
    ): CatalogActivity? {

        val definitions =
            activities.map {
                it.definition
            }

        val filterRequest =
            standardFilterRequest(
                activityPreferences
            )

        val eligible =
            CandidateFilter.filter(
                activities =
                    definitions,
                request =
                    filterRequest,
            )

        val overrideActivityId =
            plannedOverride
                ?.takeIf {
                    it.opportunityKey ==
                        opportunityKey
                }
                ?.activityId

        if (
            overrideActivityId != null &&
            eligible.any {
                it.id ==
                    overrideActivityId
            }
        ) {
            return activities
                .firstOrNull {
                    it.definition.id ==
                        overrideActivityId
                }
        }

        val historyDefinitions =
            proposalHistory
                .mapNotNull { entry ->
                    definitions
                        .firstOrNull {
                            it.id ==
                                entry.activityId
                        }
                }
                .takeLast(
                    policy.scoring
                        .historyWindowProposals
                )

        val selected =
            DecisionEngine
                .chooseActivity(
                    activities =
                        definitions,
                    history =
                        historyDefinitions,
                    policy =
                        policy,
                    seed =
                        "future-$opportunityKey-0",
                    filterRequest =
                        filterRequest,
                )
                ?: return null

        return activities
            .firstOrNull {
                it.definition.id ==
                    selected.id
            }
    }

    private fun standardFilterRequest(
        preferences: ActivityPreferences,
    ): CandidateFilterRequest {

        val requiredWalking =
            when {
                preferences.stationaryEnabled &&
                    preferences.movingEnabled ->
                    null

                preferences.movingEnabled ->
                    true

                else ->
                    false
            }

        return CandidateFilterRequest(
            enabledIntensities =
                preferences.enabledIntensities,
            requiredWalking =
                requiredWalking,
            opportunityType =
                "standard",
        )
    }
}
