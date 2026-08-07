package org.sedentaryapp.engine.schedule

import kotlin.math.abs

enum class SlotValidationCode {
    OUTSIDE_WORK,
    DURING_LUNCH,
    GAP_TOO_SMALL,
}

data class SlotValidationError(
    val code: SlotValidationCode,
    val minute: Int? = null,
    val pair: Pair<Int, Int>? = null,
)

enum class OpportunityReason {
    SUSPENDED,
    ALREADY_PROCESSED,
    TOO_EARLY,
    MISSED_SLOT,
    TOO_CLOSE_TO_NEXT_FIXED_SLOT,
    ALLOWED,
}

data class OpportunityDecision(
    val allowed: Boolean,
    val reason: OpportunityReason,
)

data class Capabilities(
    val canStand: Boolean,
    val canWalk: Boolean,
)

object ScheduleRules {
    fun toMinutes(value: String): Int {
        val parts = value.split(":")
        require(parts.size == 2) { "invalid time: $value" }
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        require(hour in 0..23 && minute in 0..59) { "invalid time: $value" }
        return hour * 60 + minute
    }

    fun validateStandardSlots(
        slots: List<Int>,
        workStart: Int,
        lunchStart: Int,
        lunchEnd: Int,
        workEnd: Int,
        minGapMinutes: Int,
    ): List<SlotValidationError> {
        val sorted = slots.sorted()
        val errors = mutableListOf<SlotValidationError>()

        sorted.forEach { time ->
            if (time < workStart || time > workEnd) {
                errors += SlotValidationError(SlotValidationCode.OUTSIDE_WORK, minute = time)
            }
            if (time >= lunchStart && time < lunchEnd) {
                errors += SlotValidationError(SlotValidationCode.DURING_LUNCH, minute = time)
            }
        }

        sorted.zipWithNext().forEach { (first, second) ->
            if (second - first < minGapMinutes) {
                errors += SlotValidationError(SlotValidationCode.GAP_TOO_SMALL, pair = first to second)
            }
        }

        return errors
    }

    fun opportunityAllowed(
        targetMinute: Int,
        actualMinute: Int,
        nextFixedSlotMinute: Int?,
        latenessToleranceMinutes: Int,
        minGapMinutes: Int,
        processed: Boolean = false,
        suspended: Boolean = false,
    ): OpportunityDecision {
        if (suspended) return OpportunityDecision(false, OpportunityReason.SUSPENDED)
        if (processed) return OpportunityDecision(false, OpportunityReason.ALREADY_PROCESSED)

        val lateness = actualMinute - targetMinute
        if (lateness < 0) return OpportunityDecision(false, OpportunityReason.TOO_EARLY)
        if (lateness > latenessToleranceMinutes) return OpportunityDecision(false, OpportunityReason.MISSED_SLOT)

        if (
            nextFixedSlotMinute != null &&
            actualMinute < nextFixedSlotMinute &&
            nextFixedSlotMinute - actualMinute < minGapMinutes
        ) {
            return OpportunityDecision(false, OpportunityReason.TOO_CLOSE_TO_NEXT_FIXED_SLOT)
        }

        return OpportunityDecision(true, OpportunityReason.ALLOWED)
    }

    fun allowedSnoozeOptions(
        actionMinute: Int,
        fixedSlots: List<Int>,
        sourceSlotMinute: Int,
        optionsMinutes: List<Int>,
        minGapMinutes: Int,
    ): List<Int> = optionsMinutes.filter { delta ->
        val candidate = actionMinute + delta
        if (candidate >= MINUTES_PER_DAY) return@filter false

        fixedSlots.none { fixed ->
            fixed != sourceSlotMinute && abs(candidate - fixed) < minGapMinutes
        }
    }

    fun applyCapabilityChange(
        capabilities: Capabilities,
        field: String,
        value: Boolean,
    ): Capabilities = when (field) {
        "can_stand" -> if (value) {
            capabilities.copy(canStand = true)
        } else {
            Capabilities(canStand = false, canWalk = false)
        }
        "can_walk" -> capabilities.copy(canWalk = value && capabilities.canStand)
        else -> error("unknown capability field")
    }

    /** Monday=0 ... Sunday=6. Strictly after current weekday. */
    fun nextSelectedWorkday(currentWeekday: Int, selectedWeekdays: Set<Int>): Int? {
        require(currentWeekday in 0..6)
        if (selectedWeekdays.isEmpty()) return null

        for (delta in 1..7) {
            if ((currentWeekday + delta) % 7 in selectedWeekdays) return delta
        }
        return null
    }

    fun scheduleHasOpportunities(selectedWeekdays: Set<Int>, activeSlots: List<Int>): Boolean =
        selectedWeekdays.isNotEmpty() && activeSlots.isNotEmpty()

    fun validateSameDayWorkWindow(workStart: Int, workEnd: Int): Boolean = workStart < workEnd

    private const val MINUTES_PER_DAY = 24 * 60
}
