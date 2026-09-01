package org.sedentaryapp.scheduling

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.sedentaryapp.data.ScheduleSettings
import org.sedentaryapp.data.StandardSlotSetting

class SnoozePlannerTest {

    private val now =
        LocalDateTime.of(
            2026,
            8,
            31,
            10,
            0,
        )

    @Test
    fun exactlyTenMinutesFromAnotherOpportunity_isAllowed() {
        assertTrue(
            canSnooze(
                target = now.plusMinutes(5),
                slotTimes =
                    listOf(
                        "10:00",
                        "10:15",
                    ),
            )
        )
    }

    @Test
    fun lessThanTenMinutesFromAnotherOpportunity_isDenied() {
        assertFalse(
            canSnooze(
                target = now.plusMinutes(5),
                slotTimes =
                    listOf(
                        "10:00",
                        "10:14",
                    ),
            )
        )
    }

    @Test
    fun currentProposal_doesNotConflictWithItsOwnSnooze() {
        assertTrue(
            canSnooze(
                target = now.plusMinutes(5),
                slotTimes =
                    listOf(
                        "10:00",
                    ),
            )
        )
    }

    @Test
    fun laterOpportunityIsChecked_evenWhenAnEarlierOneIsFarEnough() {
        assertFalse(
            canSnooze(
                target = now.plusMinutes(30),
                slotTimes =
                    listOf(
                        "10:00",
                        "10:15",
                        "10:35",
                    ),
            )
        )
    }

    @Test
    fun consumedOpportunity_doesNotBlockSnooze() {
        val settings =
            settings(
                "10:00",
                "10:35",
            )

        assertTrue(
            SnoozePlanner.canSchedule(
                target = now.plusMinutes(30),
                currentProposalKey =
                    now.toString(),
                now = now,
                settings = settings,
                consumedEarlyOpportunityKeys =
                    setOf(
                        "2026-08-31T10:35"
                    ),
                nextProposalOverride = null,
                minimumGapMinutes = 10,
            )
        )
    }

    private fun canSnooze(
        target: LocalDateTime,
        slotTimes: List<String>,
    ): Boolean {
        return SnoozePlanner.canSchedule(
            target = target,
            currentProposalKey =
                now.toString(),
            now = now,
            settings =
                settings(
                    *slotTimes.toTypedArray()
                ),
            consumedEarlyOpportunityKeys =
                emptySet(),
            nextProposalOverride = null,
            minimumGapMinutes = 10,
        )
    }

    private fun settings(
        vararg slotTimes: String,
    ): ScheduleSettings {
        return ScheduleSettings(
            selectedWeekdays =
                setOf(
                    DayOfWeek.MONDAY
                ),
            workStart =
                LocalTime.of(
                    8,
                    30,
                ),
            workEnd =
                LocalTime.of(
                    17,
                    30,
                ),
            lunchEnabled = true,
            lunchStart =
                LocalTime.of(
                    12,
                    30,
                ),
            lunchEnd =
                LocalTime.of(
                    13,
                    30,
                ),
            slots =
                slotTimes.mapIndexed {
                        index,
                        rawTime,
                    ->
                    StandardSlotSetting(
                        index = index,
                        enabled = true,
                        time =
                            LocalTime.parse(
                                rawTime
                            ),
                    )
                },
        )
    }
}
