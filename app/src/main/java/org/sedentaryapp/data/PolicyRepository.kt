package org.sedentaryapp.data

import android.content.Context
import java.time.LocalTime
import org.json.JSONObject
import org.sedentaryapp.engine.model.DecisionPolicy
import org.sedentaryapp.engine.model.RepetitionCoefficients
import org.sedentaryapp.engine.model.SchedulingPolicy
import org.sedentaryapp.engine.model.ScoringPolicy
import org.sedentaryapp.engine.model.SnoozePolicy

class PolicyRepository(
    context: Context,
) {

    private val appContext =
        context.applicationContext

    fun loadDecisionPolicy(): DecisionPolicy =
        parsedPolicy().decisionPolicy

    fun loadDefaultSlots(): List<LocalTime> =
        parsedPolicy().defaultSlots

    fun loadMinimumGapMinutes(): Long =
        parsedPolicy()
            .decisionPolicy
            .scheduling
            .minimumGapBetweenDistinctOpportunitiesMinutes

    private fun parsedPolicy(): ParsedPolicy {
        cachedPolicy?.let {
            return it
        }

        return synchronized(
            CACHE_LOCK
        ) {
            cachedPolicy
                ?: parsePolicy(
                    appContext
                ).also {
                    cachedPolicy = it
                }
        }
    }

    private fun parsePolicy(
        context: Context,
    ): ParsedPolicy {
        val json =
            context.assets
                .open(
                    POLICY_ASSET_NAME
                )
                .bufferedReader()
                .use {
                    it.readText()
                }

        val root =
            JSONObject(
                json
            )

        require(
            root.getString(
                "policy_version"
            ) == SUPPORTED_POLICY_VERSION
        ) {
            "Unsupported decision policy version."
        }

        require(
            root.getString(
                "selector_version"
            ) == SUPPORTED_SELECTOR_VERSION
        ) {
            "Unsupported selector version."
        }

        val defaultSlotsArray =
            root.getJSONArray(
                "default_slots"
            )

        val defaultSlots =
            List(
                defaultSlotsArray.length()
            ) { index ->
                LocalTime.parse(
                    defaultSlotsArray
                        .getString(
                            index
                        )
                )
            }

        require(
            defaultSlots.isNotEmpty() &&
                defaultSlots.size <= 6 &&
                defaultSlots.distinct().size ==
                defaultSlots.size
        ) {
            "Default slots must contain 1 to 6 unique times."
        }

        val scheduling =
            root.getJSONObject(
                "scheduling"
            )

        val latenessMinutes =
            root.getLong(
                "technical_lateness_tolerance_minutes"
            )

        val minimumGapMinutes =
            scheduling.getLong(
                "minimum_gap_between_distinct_opportunities_minutes"
            )

        require(
            latenessMinutes > 0L
        ) {
            "Technical lateness tolerance must be positive."
        }

        require(
            minimumGapMinutes > 0L
        ) {
            "Minimum opportunity gap must be positive."
        }

        val snooze =
            root.getJSONObject(
                "snooze"
            )

        val defaultSnoozeMinutes =
            snooze.getInt(
                "default_duration_minutes"
            )

        val minimumSnoozeMinutes =
            snooze.getInt(
                "minimum_duration_minutes"
            )

        val maximumSnoozeMinutes =
            snooze.getInt(
                "maximum_duration_minutes"
            )

        require(
            minimumSnoozeMinutes > 0 &&
                maximumSnoozeMinutes >=
                minimumSnoozeMinutes &&
                defaultSnoozeMinutes in
                minimumSnoozeMinutes..maximumSnoozeMinutes
        ) {
            "Invalid snooze duration policy."
        }

        val scoring =
            root.getJSONObject(
                "scoring"
            )

        val minWeight =
            scoring.getDouble(
                "min_weight"
            )

        val maxWeight =
            scoring.getDouble(
                "max_weight"
            )

        val historyWindow =
            scoring.getInt(
                "history_window_proposals"
            )

        require(
            minWeight > 0.0 &&
                maxWeight >= minWeight &&
                historyWindow > 0
        ) {
            "Invalid scoring policy."
        }

        val repetition =
            scoring.getJSONObject(
                "repetition_coefficients"
            )

        val repetitionCoefficients =
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
            )

        require(
            repetitionCoefficients.sameAsPreviousActivity > 0.0 &&
                repetitionCoefficients.sameActivityInRecentWindow > 0.0 &&
                repetitionCoefficients.noRepetition > 0.0
        ) {
            "Repetition coefficients must be positive."
        }

        return ParsedPolicy(
            defaultSlots =
                defaultSlots,
            decisionPolicy =
                DecisionPolicy(
                    scoring =
                        ScoringPolicy(
                            minWeight =
                                minWeight,
                            maxWeight =
                                maxWeight,
                            historyWindowProposals =
                                historyWindow,
                            repetitionCoefficients =
                                repetitionCoefficients,
                        ),
                    scheduling =
                        SchedulingPolicy(
                            technicalLatenessToleranceMinutes =
                                latenessMinutes,
                            minimumGapBetweenDistinctOpportunitiesMinutes =
                                minimumGapMinutes,
                        ),
                    snooze =
                        SnoozePolicy(
                            defaultDurationMinutes =
                                defaultSnoozeMinutes,
                            minimumDurationMinutes =
                                minimumSnoozeMinutes,
                            maximumDurationMinutes =
                                maximumSnoozeMinutes,
                        ),
                ),
        )
    }

    private data class ParsedPolicy(
        val defaultSlots: List<LocalTime>,
        val decisionPolicy: DecisionPolicy,
    )

    private companion object {
        const val POLICY_ASSET_NAME =
            "decision_policy_v1.json"

        const val SUPPORTED_POLICY_VERSION =
            "decision-policy-v1.0.5"

        const val SUPPORTED_SELECTOR_VERSION =
            "weighted-selector-v1.1.0"

        val CACHE_LOCK =
            Any()

        @Volatile
        var cachedPolicy:
            ParsedPolicy? = null
    }
}
