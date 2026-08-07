package org.sedentaryapp.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
import org.sedentaryapp.engine.schedule.Capabilities
import org.sedentaryapp.engine.schedule.OpportunityReason
import org.sedentaryapp.engine.schedule.ScheduleRules
import org.sedentaryapp.engine.schedule.SlotValidationCode

class DecisionEngineTest {
    private val policy = DecisionPolicy(
        scoring = ScoringPolicy(
            minWeight = 0.05,
            maxWeight = 20.0,
            historyWindowProposals = 4,
            repetitionCoefficients = RepetitionCoefficients(
                sameAsPreviousActivity = 0.15,
                sameActivityInRecentWindow = 0.55,
                noRepetition = 1.0,
            ),
        ),
    )

    private fun activity(
        id: String,
        category: String = "mobilite",
        intensity: String = "gentle",
        standing: Boolean = false,
        walking: Boolean = false,
        baseWeight: Double = 1.0,
        opportunityTypes: Set<String> = setOf("standard"),
    ) = ActivityDefinition(
        id = id,
        status = "published",
        userCategory = category,
        internalFamily = "family_$id",
        intensity = intensity,
        durationSecondsMin = 30,
        durationSecondsMax = 60,
        standingRequired = standing,
        walkingRequired = walking,
        eligibleOpportunityTypes = opportunityTypes,
        baseWeight = baseWeight,
    )

    @Test fun samePreviousActivityUses015() {
        val a = activity("A")
        val result = CandidateScorer.repetitionRule(a, listOf(a), policy)
        assertEquals(0.15, result.first, 0.0)
        assertEquals(RepetitionRule.SAME_AS_PREVIOUS_ACTIVITY, result.second)
    }

    @Test fun recentActivityUses055() {
        val a = activity("A")
        val b = activity("B")
        val result = CandidateScorer.repetitionRule(a, listOf(a, b), policy)
        assertEquals(0.55, result.first, 0.0)
    }

    @Test fun internalFamilyHasNoScoringEffect() {
        val previous = activity("B")
        val current = activity("A").copy(internalFamily = previous.internalFamily)
        assertEquals(1.0, CandidateScorer.repetitionRule(current, listOf(previous), policy).first, 0.0)
    }

    @Test fun canNotStandFiltersStandingActivities() {
        val result = CandidateFilter.filter(
            listOf(activity("A", standing = true), activity("B")),
            CandidateFilterRequest(canStand = false),
        )
        assertEquals(listOf("B"), result.map { it.id })
    }

    @Test fun alternativeExcludesCurrentActivity() {
        val result = CandidateFilter.filter(
            listOf(activity("A"), activity("B")),
            CandidateFilterRequest(currentActivityId = "A"),
        )
        assertEquals(listOf("B"), result.map { it.id })
    }

    @Test fun alternativeExcludesAllActivitiesSeenInInteraction() {
        val result = CandidateFilter.filter(
            listOf(activity("A"), activity("B"), activity("C")),
            CandidateFilterRequest(excludedActivityIds = setOf("A", "B")),
        )
        assertEquals(listOf("C"), result.map { it.id })
    }

    @Test fun alternativeExclusionsResetWithNewRequest() {
        val result = CandidateFilter.filter(
            listOf(activity("A"), activity("B"), activity("C")),
            CandidateFilterRequest(),
        )
        assertEquals(listOf("A", "B", "C"), result.map { it.id })
    }

    @Test fun emptyCategoriesProduceNoCandidate() {
        val result = CandidateFilter.filter(
            listOf(activity("A")),
            CandidateFilterRequest(enabledCategories = emptySet()),
        )
        assertTrue(result.isEmpty())
    }

    @Test fun weightedSelectionIsOrderIndependent() {
        val activities = listOf(activity("C"), activity("A"), activity("B"))
        val scored = CandidateScorer.score(activities, emptyList(), policy)
        val reversed = CandidateScorer.score(activities.reversed(), emptyList(), policy)
        assertEquals(
            WeightedSelector.select(scored, "same-seed")?.id,
            WeightedSelector.select(reversed, "same-seed")?.id,
        )
    }

    @Test fun chooseActivityReturnsNullWhenNothingEligible() {
        assertNull(
            DecisionEngine.chooseActivity(
                activities = listOf(activity("A")),
                history = emptyList(),
                policy = policy,
                seed = "seed",
                filterRequest = CandidateFilterRequest(enabledCategories = emptySet()),
            )
        )
    }

    @Test fun exact20MinuteSlotGapIsValid() {
        val errors = ScheduleRules.validateStandardSlots(
            slots = listOf(610, 630),
            workStart = 540,
            lunchStart = 750,
            lunchEnd = 810,
            workEnd = 1050,
            minGapMinutes = 20,
        )
        assertTrue(errors.isEmpty())
    }

    @Test fun fifteenMinuteSlotGapIsInvalid() {
        val errors = ScheduleRules.validateStandardSlots(
            slots = listOf(610, 625),
            workStart = 540,
            lunchStart = 750,
            lunchEnd = 810,
            workEnd = 1050,
            minGapMinutes = 20,
        )
        assertTrue(errors.any { it.code == SlotValidationCode.GAP_TOO_SMALL })
    }

    @Test fun lateOpportunityBeyond15MinutesIsMissed() {
        val decision = ScheduleRules.opportunityAllowed(
            targetMinute = 610,
            actualMinute = 630,
            nextFixedSlotMinute = 710,
            latenessToleranceMinutes = 15,
            minGapMinutes = 20,
        )
        assertFalse(decision.allowed)
        assertEquals(OpportunityReason.MISSED_SLOT, decision.reason)
    }

    @Test fun snoozeCannotCrossMidnight() {
        assertTrue(
            ScheduleRules.allowedSnoozeOptions(
                actionMinute = 23 * 60 + 50,
                fixedSlots = listOf(17 * 60),
                sourceSlotMinute = 17 * 60,
                optionsMinutes = listOf(30),
                minGapMinutes = 20,
            ).isEmpty()
        )
    }

    @Test fun disablingStandingAlsoDisablesWalking() {
        assertEquals(
            Capabilities(false, false),
            ScheduleRules.applyCapabilityChange(Capabilities(true, true), "can_stand", false),
        )
    }

    @Test fun reenablingStandingDoesNotReenableWalking() {
        assertEquals(
            Capabilities(true, false),
            ScheduleRules.applyCapabilityChange(Capabilities(false, false), "can_stand", true),
        )
    }

    @Test fun noSelectedWorkdayHasNoNextDay() {
        assertNull(ScheduleRules.nextSelectedWorkday(2, emptySet()))
    }

    @Test fun fridayToMondayIsThreeDays() {
        assertEquals(3, ScheduleRules.nextSelectedWorkday(4, setOf(0, 1, 2, 3, 4)))
    }
}
