package org.sedentaryapp.engine.model

data class ActivityDefinition(
    val id: String,
    val status: String,
    val userCategory: String,
    val internalFamily: String,
    val intensity: String,
    val durationSecondsMin: Int,
    val durationSecondsMax: Int,
    val standingRequired: Boolean,
    val walkingRequired: Boolean,
    val eligibleOpportunityTypes: Set<String>,
    val baseWeight: Double,
)

data class RepetitionCoefficients(
    val sameAsPreviousActivity: Double,
    val sameActivityInRecentWindow: Double,
    val noRepetition: Double,
)

data class ScoringPolicy(
    val minWeight: Double,
    val maxWeight: Double,
    val historyWindowProposals: Int,
    val repetitionCoefficients: RepetitionCoefficients,
)

data class DecisionPolicy(
    val scoring: ScoringPolicy,
)

data class CandidateFilterRequest(
    val enabledCategories: Set<String>? = null,
    val enabledIntensities: Set<String>? = null,
    val canStand: Boolean = true,
    val canWalk: Boolean = true,
    val opportunityType: String = "standard",
    val availableSeconds: Int? = null,
    val currentActivityId: String? = null,
    val excludedActivityIds: Set<String> = emptySet(),
)

enum class RepetitionRule {
    SAME_AS_PREVIOUS_ACTIVITY,
    SAME_ACTIVITY_IN_RECENT_WINDOW,
    NO_REPETITION,
}

data class ScoredCandidate(
    val activity: ActivityDefinition,
    val weight: Double,
    val rule: RepetitionRule,
)
