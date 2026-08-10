package org.sedentaryapp.engine.schedule

import java.time.LocalTime

enum class SlotValidation {
    VALID,
    OUTSIDE_WORK_HOURS,
    DURING_LUNCH,
}

object ScheduleValidator {

    fun isWorkdayValid(
        workStart: LocalTime,
        workEnd: LocalTime,
    ): Boolean {
        return workStart.isBefore(workEnd)
    }

    fun isLunchValid(
        workStart: LocalTime,
        workEnd: LocalTime,
        lunchEnabled: Boolean,
        lunchStart: LocalTime,
        lunchEnd: LocalTime,
    ): Boolean {

        if (!lunchEnabled) {
            return true
        }

        if (!isWorkdayValid(workStart, workEnd)) {
            return false
        }

        return lunchStart.isBefore(lunchEnd) &&
                !lunchStart.isBefore(workStart) &&
                !lunchEnd.isAfter(workEnd)
    }

    fun validateSlot(
        slotTime: LocalTime,
        workStart: LocalTime,
        workEnd: LocalTime,
        lunchEnabled: Boolean,
        lunchStart: LocalTime,
        lunchEnd: LocalTime,
    ): SlotValidation {

        if (
            !isWorkdayValid(
                workStart,
                workEnd,
            )
        ) {
            return SlotValidation.OUTSIDE_WORK_HOURS
        }

        val insideWorkHours =
            !slotTime.isBefore(workStart) &&
                    !slotTime.isAfter(workEnd)

        if (!insideWorkHours) {
            return SlotValidation.OUTSIDE_WORK_HOURS
        }

        if (
            lunchEnabled &&
            isLunchValid(
                workStart = workStart,
                workEnd = workEnd,
                lunchEnabled = true,
                lunchStart = lunchStart,
                lunchEnd = lunchEnd,
            )
        ) {
            val duringLunch =
                !slotTime.isBefore(lunchStart) &&
                        slotTime.isBefore(lunchEnd)

            if (duringLunch) {
                return SlotValidation.DURING_LUNCH
            }
        }

        return SlotValidation.VALID
    }
}