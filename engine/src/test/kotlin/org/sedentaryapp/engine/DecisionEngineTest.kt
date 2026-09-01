package org.sedentaryapp.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sedentaryapp.engine.decision.CandidateFilter
import org.sedentaryapp.engine.decision.CandidateScorer
import org.sedentaryapp.engine.decision.DecisionEngine
import org.sedentaryapp.engine.decision.WeightedSelector
import org.sedentaryapp.engine.model.ActivityDefinition
import org.sedentaryapp.engine.model.CandidateFilterRequest
import org.sedentaryapp.engine.model.DecisionPolicy
import org.sedentaryapp.engine.model.RepetitionCoefficients
import org.sedentaryapp.engine.model.RepetitionRule
import org.sedentaryapp.engine.model.ScoringPolicy
import org.sedentaryapp.engine.model.SchedulingPolicy
import org.sedentaryapp.engine.model.SnoozePolicy

class DecisionEngineTest {

    private val policy =
        DecisionPolicy(
            scoring =
                ScoringPolicy(
                    minWeight = 0.05,
                    maxWeight = 20.0,
                    historyWindowProposals = 4,
                    repetitionCoefficients =
                        RepetitionCoefficients(
                            sameAsPreviousActivity = 0.15,
                            sameActivityInRecentWindow = 0.55,
                            noRepetition = 1.0,
                        ),
                ),
            scheduling =
                SchedulingPolicy(
                    technicalLatenessToleranceMinutes = 10L,
                    minimumGapBetweenDistinctOpportunitiesMinutes = 10L,
                ),
            snooze =
                SnoozePolicy(
                    defaultDurationMinutes = 30,
                    minimumDurationMinutes = 5,
                    maximumDurationMinutes = 60,
                ),
        )

    private fun activity(
        id: String,
        intensity: String = "gentle",
        walking: Boolean = false,
        baseWeight: Double = 1.0,
        internalFamily: String = "family_$id",
        status: String = "published",
    ) =
        ActivityDefinition(
            id = id,
            status = status,
            userCategory = "mobilite",
            internalFamily = internalFamily,
            intensity = intensity,
            durationSecondsMin = 30,
            durationSecondsMax = 60,
            standingRequired = walking,
            walkingRequired = walking,
            baseWeight = baseWeight,
        )

    @Test
    fun samePreviousActivityUses015() {
        val a = activity("A")

        val result =
            CandidateScorer.repetitionRule(
                activity = a,
                history = listOf(a),
                policy = policy,
            )

        assertEquals(
            0.15,
            result.first,
            0.0,
        )
        assertEquals(
            RepetitionRule.SAME_AS_PREVIOUS_ACTIVITY,
            result.second,
        )
    }

    @Test
    fun recentActivityUses055() {
        val a = activity("A")
        val b = activity("B")

        val result =
            CandidateScorer.repetitionRule(
                activity = a,
                history = listOf(a, b),
                policy = policy,
            )

        assertEquals(
            0.55,
            result.first,
            0.0,
        )
        assertEquals(
            RepetitionRule.SAME_ACTIVITY_IN_RECENT_WINDOW,
            result.second,
        )
    }

    @Test
    fun internalFamilyHasNoScoringEffect() {
        val previous =
            activity(
                id = "B",
                internalFamily = "shared_family",
            )

        val current =
            activity(
                id = "A",
                internalFamily = "shared_family",
            )

        val result =
            CandidateScorer.repetitionRule(
                activity = current,
                history = listOf(previous),
                policy = policy,
            )

        assertEquals(
            1.0,
            result.first,
            0.0,
        )
        assertEquals(
            RepetitionRule.NO_REPETITION,
            result.second,
        )
    }

    @Test
    fun enabledIntensitiesFilterCandidates() {
        val result =
            CandidateFilter.filter(
                activities =
                    listOf(
                        activity(
                            id = "A",
                            intensity = "gentle",
                        ),
                        activity(
                            id = "B",
                            intensity = "dynamic",
                        ),
                    ),
                request =
                    CandidateFilterRequest(
                        enabledIntensities =
                            setOf("dynamic"),
                    ),
            )

        assertEquals(
            listOf("B"),
            result.map { it.id },
        )
    }

    @Test
    fun requiredWalkingTrueKeepsWalkingActivities() {
        val result =
            CandidateFilter.filter(
                activities =
                    listOf(
                        activity(
                            id = "A",
                            walking = true,
                        ),
                        activity(
                            id = "B",
                            walking = false,
                        ),
                    ),
                request =
                    CandidateFilterRequest(
                        requiredWalking = true,
                    ),
            )

        assertEquals(
            listOf("A"),
            result.map { it.id },
        )
    }

    @Test
    fun requiredWalkingFalseKeepsNonWalkingActivities() {
        val result =
            CandidateFilter.filter(
                activities =
                    listOf(
                        activity(
                            id = "A",
                            walking = true,
                        ),
                        activity(
                            id = "B",
                            walking = false,
                        ),
                    ),
                request =
                    CandidateFilterRequest(
                        requiredWalking = false,
                    ),
            )

        assertEquals(
            listOf("B"),
            result.map { it.id },
        )
    }

    @Test
    fun requiredWalkingNullKeepsBothMovementTypes() {
        val result =
            CandidateFilter.filter(
                activities =
                    listOf(
                        activity(
                            id = "A",
                            walking = true,
                        ),
                        activity(
                            id = "B",
                            walking = false,
                        ),
                    ),
                request =
                    CandidateFilterRequest(
                        requiredWalking = null,
                    ),
            )

        assertEquals(
            listOf("A", "B"),
            result.map { it.id },
        )
    }

    @Test
    fun alternativeExcludesAllActivitiesSeenInInteraction() {
        val result =
            CandidateFilter.filter(
                activities =
                    listOf(
                        activity("A"),
                        activity("B"),
                        activity("C"),
                    ),
                request =
                    CandidateFilterRequest(
                        excludedActivityIds =
                            setOf("A", "B"),
                    ),
            )

        assertEquals(
            listOf("C"),
            result.map { it.id },
        )
    }

    @Test
    fun alternativeExclusionsResetWithNewRequest() {
        val result =
            CandidateFilter.filter(
                activities =
                    listOf(
                        activity("A"),
                        activity("B"),
                        activity("C"),
                    ),
                request =
                    CandidateFilterRequest(),
            )

        assertEquals(
            listOf("A", "B", "C"),
            result.map { it.id },
        )
    }

    @Test
    fun unpublishedActivitiesAreFiltered() {
        val result =
            CandidateFilter.filter(
                activities =
                    listOf(
                        activity("A"),
                        activity(
                            id = "B",
                            status = "deprecated",
                        ),
                    ),
            )

        assertEquals(
            listOf("A"),
            result.map { it.id },
        )
    }

    @Test
    fun weightedSelectionIsOrderIndependent() {
        val activities =
            listOf(
                activity("C"),
                activity("A"),
                activity("B"),
            )

        val scored =
            CandidateScorer.score(
                candidates = activities,
                history = emptyList(),
                policy = policy,
            )

        val reversed =
            CandidateScorer.score(
                candidates = activities.reversed(),
                history = emptyList(),
                policy = policy,
            )

        assertEquals(
            WeightedSelector
                .select(
                    scored = scored,
                    seed = "same-seed",
                )
                ?.id,
            WeightedSelector
                .select(
                    scored = reversed,
                    seed = "same-seed",
                )
                ?.id,
        )
    }

    @Test
    fun chooseActivityReturnsNullWhenNoIntensityIsEnabled() {
        val selected =
            DecisionEngine.chooseActivity(
                activities =
                    listOf(
                        activity("A")
                    ),
                history = emptyList(),
                policy = policy,
                seed = "seed",
                filterRequest =
                    CandidateFilterRequest(
                        enabledIntensities =
                            emptySet(),
                    ),
            )

        assertNull(selected)
    }

    @Test
    fun catalogValidationAcceptsValidUniqueActivities() {
        CandidateScorer.validateCatalogForSelection(
            listOf(
                activity(
                    id = "A",
                    baseWeight = 1.0,
                ),
                activity(
                    id = "B",
                    baseWeight = 2.0,
                ),
            )
        )

        assertTrue(true)
    }
}
