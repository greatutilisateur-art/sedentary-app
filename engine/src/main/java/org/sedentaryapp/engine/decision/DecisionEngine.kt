package org.sedentaryapp.engine.decision

import java.security.MessageDigest
import kotlin.math.max
import kotlin.math.min
import org.sedentaryapp.engine.model.ActivityDefinition
import org.sedentaryapp.engine.model.CandidateFilterRequest
import org.sedentaryapp.engine.model.DecisionPolicy
import org.sedentaryapp.engine.model.RepetitionRule
import org.sedentaryapp.engine.model.ScoredCandidate

object CandidateFilter {
    fun filter(
        activities: List<ActivityDefinition>,
        request: CandidateFilterRequest = CandidateFilterRequest(),
    ): List<ActivityDefinition> {
        val excludedActivityIds = request.excludedActivityIds + listOfNotNull(request.currentActivityId)
        return activities.filter activityFilter@ { activity ->
        if (activity.status != "published") return@activityFilter false
        if (request.enabledCategories != null && activity.userCategory !in request.enabledCategories) return@activityFilter false
        if (request.enabledIntensities != null && activity.intensity !in request.enabledIntensities) return@activityFilter false
        if (activity.standingRequired && !request.canStand) return@activityFilter false
        if (activity.walkingRequired && !request.canWalk) return@activityFilter false
        if (request.opportunityType !in activity.eligibleOpportunityTypes) return@activityFilter false
        if (request.availableSeconds != null && activity.durationSecondsMin > request.availableSeconds) return@activityFilter false
        if (activity.id in excludedActivityIds) return@activityFilter false
        true
        }
    }
}

object CandidateScorer {
    fun repetitionRule(
        activity: ActivityDefinition,
        history: List<ActivityDefinition>,
        policy: DecisionPolicy,
    ): Pair<Double, RepetitionRule> {
        val scoring = policy.scoring
        val coeff = scoring.repetitionCoefficients
        val recent = history.takeLast(scoring.historyWindowProposals)

        if (recent.isEmpty()) {
            return coeff.noRepetition to RepetitionRule.NO_REPETITION
        }

        if (activity.id == recent.last().id) {
            return coeff.sameAsPreviousActivity to RepetitionRule.SAME_AS_PREVIOUS_ACTIVITY
        }

        if (recent.any { it.id == activity.id }) {
            return coeff.sameActivityInRecentWindow to RepetitionRule.SAME_ACTIVITY_IN_RECENT_WINDOW
        }

        return coeff.noRepetition to RepetitionRule.NO_REPETITION
    }

    fun validateCatalogForSelection(activities: List<ActivityDefinition>) {
        require(activities.map { it.id }.distinct().size == activities.size) {
            "duplicate activity id"
        }
        activities.forEach { activity ->
            require(activity.baseWeight.isFinite() && activity.baseWeight > 0.0) {
                "invalid baseWeight for ${activity.id}"
            }
        }
    }

    fun score(
        candidates: List<ActivityDefinition>,
        history: List<ActivityDefinition>,
        policy: DecisionPolicy,
    ): List<ScoredCandidate> {
        validateCatalogForSelection(candidates)
        val scoring = policy.scoring

        return candidates.map { activity ->
            val (coefficient, rule) = repetitionRule(activity, history, policy)
            val weight = max(scoring.minWeight, min(scoring.maxWeight, activity.baseWeight * coefficient))
            ScoredCandidate(activity = activity, weight = weight, rule = rule)
        }
    }
}

object WeightedSelector {
    fun deterministicFraction(seed: String): Double {
        val digest = MessageDigest.getInstance("SHA-256").digest(seed.toByteArray(Charsets.UTF_8))
        var value = 0UL
        for (index in 0 until 8) {
            value = (value shl 8) or digest[index].toUByte().toULong()
        }
        return value.toDouble() / TWO_POW_64
    }

    fun select(scored: List<ScoredCandidate>, seed: String): ActivityDefinition? {
        if (scored.isEmpty()) return null

        require(scored.map { it.activity.id }.distinct().size == scored.size) {
            "duplicate activity id"
        }
        scored.forEach { candidate ->
            require(candidate.weight.isFinite() && candidate.weight > 0.0) {
                "invalid selection weight"
            }
        }

        val ordered = scored.sortedBy { it.activity.id }
        val total = ordered.sumOf { it.weight }
        val cursor = deterministicFraction(seed) * total
        var cumulative = 0.0

        for (candidate in ordered) {
            cumulative += candidate.weight
            if (cursor < cumulative) return candidate.activity
        }

        return ordered.last().activity
    }

    private const val TWO_POW_64 = 18446744073709551616.0
}

object DecisionEngine {
    fun chooseActivity(
        activities: List<ActivityDefinition>,
        history: List<ActivityDefinition>,
        policy: DecisionPolicy,
        seed: String,
        filterRequest: CandidateFilterRequest = CandidateFilterRequest(),
    ): ActivityDefinition? {
        val candidates = CandidateFilter.filter(activities, filterRequest)
        if (candidates.isEmpty()) return null
        val scored = CandidateScorer.score(candidates, history, policy)
        return WeightedSelector.select(scored, seed)
    }
}
