package org.sedentaryapp.data

import android.content.Context
import java.time.LocalTime
import org.json.JSONObject
import org.sedentaryapp.engine.model.DecisionPolicy
import org.sedentaryapp.engine.model.RepetitionCoefficients
import org.sedentaryapp.engine.model.ScoringPolicy

class PolicyRepository(
    private val context: Context,
) {

    private fun loadRoot(): JSONObject {
        val json = context.assets
            .open("decision_policy_v1.json")
            .bufferedReader()
            .use { it.readText() }

        return JSONObject(json)
    }

    fun loadDecisionPolicy(): DecisionPolicy {
        val root = loadRoot()

        val scoring =
            root.getJSONObject("scoring")

        val repetition =
            scoring.getJSONObject(
                "repetition_coefficients"
            )

        return DecisionPolicy(
            scoring = ScoringPolicy(
                minWeight =
                    scoring.getDouble(
                        "min_weight"
                    ),
                maxWeight =
                    scoring.getDouble(
                        "max_weight"
                    ),
                historyWindowProposals =
                    scoring.getInt(
                        "history_window_proposals"
                    ),
                repetitionCoefficients =
                    RepetitionCoefficients(
                        sameAsPreviousActivity =
                            repetition.getDouble(
                                "same_as_previous_activity"
                            ),
                        sameActivityInRecentWindow =
                            repetition.getDouble(
                                "same_activity_in_recent_window"
                            ),
                        noRepetition =
                            repetition.getDouble(
                                "no_repetition"
                            ),
                    ),
            )
        )
    }

    fun loadDefaultSlots(): List<LocalTime> {
        val array =
            loadRoot().getJSONArray(
                "default_slots"
            )

        return List(array.length()) { index ->
            LocalTime.parse(
                array.getString(index)
            )
        }
    }

    fun loadMinimumGapMinutes(): Long {
        return loadRoot()
            .getJSONObject("scheduling")
            .getLong(
                "minimum_gap_between_distinct_opportunities_minutes"
            )
    }
}