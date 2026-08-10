package org.sedentaryapp.ui.schedule

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalTime
import org.sedentaryapp.data.ScheduleSettings
import org.sedentaryapp.engine.schedule.NextOpportunityPlanner
import org.sedentaryapp.engine.schedule.ScheduleValidator
import org.sedentaryapp.engine.schedule.SlotValidation

@Composable
fun ScheduleScreen(
    settings: ScheduleSettings,
    minimumGapMinutes: Long,
    onWeekdayChanged: (DayOfWeek, Boolean) -> Unit,
    onWorkStartChanged: (LocalTime) -> Unit,
    onWorkEndChanged: (LocalTime) -> Unit,
    onLunchEnabledChanged: (Boolean) -> Unit,
    onLunchStartChanged: (LocalTime) -> Unit,
    onLunchEndChanged: (LocalTime) -> Unit,
    onSlotEnabledChanged: (Int, Boolean) -> Unit,
    onSlotTimeChanged: (Int, LocalTime) -> Unit,
    onValidate: () -> Unit,
) {

    val workdayValid =
        ScheduleValidator.isWorkdayValid(
            workStart = settings.workStart,
            workEnd = settings.workEnd,
        )

    val lunchValid =
        ScheduleValidator.isLunchValid(
            workStart = settings.workStart,
            workEnd = settings.workEnd,
            lunchEnabled = settings.lunchEnabled,
            lunchStart = settings.lunchStart,
            lunchEnd = settings.lunchEnd,
        )

    val activeSlots =
        settings.slots.filter { it.enabled }

    val activeTimes =
        activeSlots.map { it.time }

    val spacingValid =
        NextOpportunityPlanner.hasValidSpacing(
            activeSlotTimes = activeTimes,
            minimumGapMinutes = minimumGapMinutes,
        )

    val slotsValid =
        activeSlots.all { slot ->
            ScheduleValidator.validateSlot(
                slotTime = slot.time,
                workStart = settings.workStart,
                workEnd = settings.workEnd,
                lunchEnabled = settings.lunchEnabled,
                lunchStart = settings.lunchStart,
                lunchEnd = settings.lunchEnd,
            ) == SlotValidation.VALID
        }

    val canValidate =
        workdayValid &&
                lunchValid &&
                spacingValid &&
                slotsValid &&
                activeSlots.isNotEmpty()

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(24.dp),
        ) {

            Text(
                text = "Tes horaires",
                style = MaterialTheme.typography.headlineMedium,
            )

            Spacer(
                modifier = Modifier.height(24.dp),
            )

            Text(
                text = "Jours travaillés",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(
                modifier = Modifier.height(8.dp),
            )

            WeekdayRow(
                days = listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                ),
                selected = settings.selectedWeekdays,
                onChanged = onWeekdayChanged,
            )

            Spacer(
                modifier = Modifier.height(8.dp),
            )

            WeekdayRow(
                days = listOf(
                    DayOfWeek.FRIDAY,
                    DayOfWeek.SATURDAY,
                    DayOfWeek.SUNDAY,
                ),
                selected = settings.selectedWeekdays,
                onChanged = onWeekdayChanged,
            )

            Spacer(
                modifier = Modifier.height(28.dp),
            )

            Text(
                text = "Journée de travail",
                style = MaterialTheme.typography.titleMedium,
            )

            TimeSettingRow(
                label = "Début",
                time = settings.workStart,
                onTimeChanged = onWorkStartChanged,
            )

            TimeSettingRow(
                label = "Fin",
                time = settings.workEnd,
                onTimeChanged = onWorkEndChanged,
            )

            if (!workdayValid) {
                ErrorText(
                    "L'heure de fin doit être après l'heure de début."
                )
            }

            Spacer(
                modifier = Modifier.height(20.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically,
            ) {
                Text(
                    text = "Pause déjeuner",
                    style = MaterialTheme.typography.titleMedium,
                )

                Switch(
                    checked = settings.lunchEnabled,
                    onCheckedChange = onLunchEnabledChanged,
                )
            }

            if (settings.lunchEnabled) {

                TimeSettingRow(
                    label = "Début",
                    time = settings.lunchStart,
                    onTimeChanged = onLunchStartChanged,
                )

                TimeSettingRow(
                    label = "Fin",
                    time = settings.lunchEnd,
                    onTimeChanged = onLunchEndChanged,
                )

                if (!lunchValid) {
                    ErrorText(
                        "La pause déjeuner doit être comprise dans ta journée de travail."
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(28.dp),
            )

            Text(
                text = "Créneaux",
                style = MaterialTheme.typography.titleMedium,
            )

            activeOrInactiveSlots(
                settings = settings,
                onSlotEnabledChanged =
                    onSlotEnabledChanged,
                onSlotTimeChanged =
                    onSlotTimeChanged,
            )

            settings.slots
                .filter { it.enabled }
                .forEach { slot ->

                    when (
                        ScheduleValidator.validateSlot(
                            slotTime = slot.time,
                            workStart = settings.workStart,
                            workEnd = settings.workEnd,
                            lunchEnabled = settings.lunchEnabled,
                            lunchStart = settings.lunchStart,
                            lunchEnd = settings.lunchEnd,
                        )
                    ) {
                        SlotValidation.OUTSIDE_WORK_HOURS ->
                            ErrorText(
                                "Le créneau ${formatTime(slot.time)} est en dehors de tes heures de travail."
                            )

                        SlotValidation.DURING_LUNCH ->
                            ErrorText(
                                "Le créneau ${formatTime(slot.time)} tombe pendant ta pause déjeuner."
                            )

                        SlotValidation.VALID ->
                            Unit
                    }
                }

            if (!spacingValid) {
                ErrorText(
                    "Laisse au moins $minimumGapMinutes min entre deux créneaux."
                )
            }

            Spacer(
                modifier = Modifier.height(32.dp),
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = canValidate,
                onClick = onValidate,
            ) {
                Text("Valider")
            }
        }
    }
}

@Composable
private fun activeOrInactiveSlots(
    settings: ScheduleSettings,
    onSlotEnabledChanged: (Int, Boolean) -> Unit,
    onSlotTimeChanged: (Int, LocalTime) -> Unit,
) {
    settings.slots.forEach { slot ->

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically,
        ) {

            Switch(
                checked = slot.enabled,
                onCheckedChange = { enabled ->
                    onSlotEnabledChanged(
                        slot.index,
                        enabled,
                    )
                },
            )

            TimeButton(
                time = slot.time,
                onTimeChanged = { time ->
                    onSlotTimeChanged(
                        slot.index,
                        time,
                    )
                },
            )
        }
    }
}

@Composable
private fun WeekdayRow(
    days: List<DayOfWeek>,
    selected: Set<DayOfWeek>,
    onChanged: (DayOfWeek, Boolean) -> Unit,
) {
    Row(
        horizontalArrangement =
            Arrangement.spacedBy(8.dp),
    ) {
        days.forEach { day ->
            FilterChip(
                selected = day in selected,
                onClick = {
                    onChanged(
                        day,
                        day !in selected,
                    )
                },
                label = {
                    Text(
                        weekdayShortName(day)
                    )
                },
            )
        }
    }
}

@Composable
private fun TimeSettingRow(
    label: String,
    time: LocalTime,
    onTimeChanged: (LocalTime) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween,
        verticalAlignment =
            Alignment.CenterVertically,
    ) {
        Text(label)

        TimeButton(
            time = time,
            onTimeChanged = onTimeChanged,
        )
    }
}

@Composable
private fun TimeButton(
    time: LocalTime,
    onTimeChanged: (LocalTime) -> Unit,
) {
    val context =
        LocalContext.current

    TextButton(
        onClick = {
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    onTimeChanged(
                        LocalTime.of(
                            hour,
                            minute,
                        )
                    )
                },
                time.hour,
                time.minute,
                true,
            ).show()
        },
    ) {
        Text(
            formatTime(time)
        )
    }
}

@Composable
private fun ErrorText(
    text: String,
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
    )
}

private fun formatTime(
    time: LocalTime,
): String {
    return "%02d h %02d".format(
        time.hour,
        time.minute,
    )
}

private fun weekdayShortName(
    day: DayOfWeek,
): String {
    return when (day) {
        DayOfWeek.MONDAY -> "Lun"
        DayOfWeek.TUESDAY -> "Mar"
        DayOfWeek.WEDNESDAY -> "Mer"
        DayOfWeek.THURSDAY -> "Jeu"
        DayOfWeek.FRIDAY -> "Ven"
        DayOfWeek.SATURDAY -> "Sam"
        DayOfWeek.SUNDAY -> "Dim"
    }
}