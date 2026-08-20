package org.sedentaryapp.engine.schedule

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

object NextOpportunityPlanner {

    fun hasValidSpacing(
        activeSlotTimes: List<LocalTime>,
        minimumGapMinutes: Long,
    ): Boolean {

        if (
            activeSlotTimes.size !=
            activeSlotTimes.distinct().size
        ) {
            return false
        }

        val sorted =
            activeSlotTimes
                .sorted()

        return sorted
            .zipWithNext()
            .all { (first, second) ->
                Duration
                    .between(
                        first,
                        second,
                    )
                    .toMinutes() >=
                        minimumGapMinutes
            }
    }

    fun next(
        now: LocalDateTime,
        selectedWeekdays:
        Set<DayOfWeek>,
        activeSlotTimes:
        List<LocalTime>,
    ): LocalDateTime? {

        if (
            selectedWeekdays.isEmpty() ||
            activeSlotTimes.isEmpty()
        ) {
            return null
        }

        val sortedSlots =
            activeSlotTimes
                .distinct()
                .sorted()

        for (dayOffset in 0L..7L) {

            val date =
                now.toLocalDate()
                    .plusDays(dayOffset)

            if (
                date.dayOfWeek !in
                selectedWeekdays
            ) {
                continue
            }

            for (slotTime in sortedSlots) {

                val candidate =
                    LocalDateTime.of(
                        date,
                        slotTime,
                    )

                if (candidate.isAfter(now)) {
                    return candidate
                }
            }
        }

        return null
    }
}