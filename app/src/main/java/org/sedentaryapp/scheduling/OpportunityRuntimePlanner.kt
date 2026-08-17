package org.sedentaryapp.scheduling

import java.time.LocalDateTime
import org.sedentaryapp.data.ScheduleSettings
import org.sedentaryapp.engine.schedule.NextOpportunityPlanner

object OpportunityRuntimePlanner {

    fun next(
        now: LocalDateTime,
        settings: ScheduleSettings,
        minimumGapMinutes: Long,
        consumedEarlyOpportunityKeys: Set<String>,
    ): LocalDateTime? {

        if (
            settings.selectedWeekdays.isEmpty() ||
            settings.slots.isEmpty()
        ) {
            return null
        }

        val slotTimes =
            settings.slots
                .map {
                    it.time
                }

        if (
            !NextOpportunityPlanner
                .hasValidSpacing(
                    activeSlotTimes =
                        slotTimes,
                    minimumGapMinutes =
                        minimumGapMinutes,
                )
        ) {
            return null
        }

        var cursor =
            now

        repeat(MAX_SKIPPED_OPPORTUNITIES) {

            val candidate =
                NextOpportunityPlanner
                    .next(
                        now =
                            cursor,
                        selectedWeekdays =
                            settings.selectedWeekdays,
                        activeSlotTimes =
                            slotTimes,
                    )
                    ?: return null

            if (
                candidate.toString() !in
                consumedEarlyOpportunityKeys
            ) {
                return candidate
            }

            cursor =
                candidate
        }

        return null
    }

    fun isConfiguredOpportunity(
        opportunity: LocalDateTime,
        settings: ScheduleSettings,
    ): Boolean {

        if (
            opportunity.dayOfWeek !in
            settings.selectedWeekdays
        ) {
            return false
        }

        return settings.slots
            .any { slot ->
                slot.time ==
                    opportunity.toLocalTime()
            }
    }

    private const val
        MAX_SKIPPED_OPPORTUNITIES = 64
}
