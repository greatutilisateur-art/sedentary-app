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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalTime
import org.sedentaryapp.data.ActivityPreferences
import org.sedentaryapp.data.ScheduleSettings
import org.sedentaryapp.engine.schedule.NextOpportunityPlanner

@Composable
fun ScheduleScreen(
    settings: ScheduleSettings,
    activityPreferences: ActivityPreferences,
    minimumGapMinutes: Long,
    onWeekdayChanged: (DayOfWeek, Boolean) -> Unit,
    onSlotTimeChanged: (Int, LocalTime) -> Unit,
    onAddSlot: (LocalTime) -> Unit,
    onRemoveSlot: (Int) -> Unit,
    onIntensityChanged: (String, Boolean) -> Unit,
    onStationaryChanged: (Boolean) -> Unit,
    onMovingChanged: (Boolean) -> Unit,
    onBack: () -> Unit,
) {

    val context =
        LocalContext.current

    var slotError by
    remember {
        mutableStateOf<String?>(
            null
        )
    }

    Surface(
        modifier =
            Modifier.fillMaxSize(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(24.dp),
        ) {

            Text(
                text = "Tes réglages",
                style =
                    MaterialTheme.typography
                        .headlineMedium,
            )

            Spacer(
                modifier =
                    Modifier.height(24.dp),
            )

            Text(
                text = "Jours travaillés",
                style =
                    MaterialTheme.typography
                        .titleMedium,
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp),
            )

            WeekdayRow(
                days = listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                ),
                selected =
                    settings.selectedWeekdays,
                onChanged =
                    onWeekdayChanged,
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp),
            )

            WeekdayRow(
                days = listOf(
                    DayOfWeek.FRIDAY,
                    DayOfWeek.SATURDAY,
                    DayOfWeek.SUNDAY,
                ),
                selected =
                    settings.selectedWeekdays,
                onChanged =
                    onWeekdayChanged,
            )

            Spacer(
                modifier =
                    Modifier.height(28.dp),
            )

            Text(
                text = "Créneaux",
                style =
                    MaterialTheme.typography
                        .titleMedium,
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp),
            )

            settings.slots.forEach { slot ->

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically,
                ) {

                    TextButton(
                        onClick = {

                            showTimePicker(
                                context = context,
                                initialTime =
                                    slot.time,
                            ) { newTime ->

                                val candidateTimes =
                                    settings.slots
                                        .map {
                                                currentSlot ->

                                            if (
                                                currentSlot.index ==
                                                slot.index
                                            ) {
                                                newTime
                                            } else {
                                                currentSlot.time
                                            }
                                        }

                                val valid =
                                    NextOpportunityPlanner
                                        .hasValidSpacing(
                                            activeSlotTimes =
                                                candidateTimes,
                                            minimumGapMinutes =
                                                minimumGapMinutes,
                                        )

                                if (valid) {
                                    slotError = null

                                    onSlotTimeChanged(
                                        slot.index,
                                        newTime,
                                    )
                                } else {
                                    slotError =
                                        "Espace les créneaux d’au moins $minimumGapMinutes min."
                                }
                            }
                        },
                    ) {
                        Text(
                            formatTime(
                                slot.time
                            )
                        )
                    }

                    TextButton(
                        modifier =
                            Modifier.semantics {
                                contentDescription =
                                    "Supprimer le créneau de ${formatTimeForTalkBack(slot.time)}"
                            },
                        enabled =
                            settings.slots.size > 1,
                        onClick = {
                            slotError = null

                            onRemoveSlot(
                                slot.index
                            )
                        },
                    ) {
                        Text("−")
                    }
                }
            }

            slotError?.let {
                Text(
                    text = it,
                    color =
                        MaterialTheme
                            .colorScheme
                            .error,
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,
                )
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp),
            )

            OutlinedButton(
                modifier =
                    Modifier.fillMaxWidth(),
                enabled =
                    settings.slots.size < 6,
                onClick = {

                    val suggestedTime =
                        settings.slots
                            .maxByOrNull {
                                it.time
                            }
                            ?.time
                            ?.plusMinutes(30)
                            ?: LocalTime.of(
                                10,
                                0,
                            )

                    showTimePicker(
                        context = context,
                        initialTime =
                            suggestedTime,
                    ) { newTime ->

                        val candidateTimes =
                            settings.slots
                                .map {
                                    it.time
                                } +
                                    newTime

                        val valid =
                            NextOpportunityPlanner
                                .hasValidSpacing(
                                    activeSlotTimes =
                                        candidateTimes,
                                    minimumGapMinutes =
                                        minimumGapMinutes,
                                )

                        if (valid) {
                            slotError = null

                            onAddSlot(
                                newTime
                            )
                        } else {
                            slotError =
                                "Espace les créneaux d’au moins $minimumGapMinutes min."
                        }
                    }
                },
            ) {
                Text(
                    "+ Ajouter un créneau"
                )
            }

            if (
                settings.slots.size >= 6
            ) {
                Spacer(
                    modifier =
                        Modifier.height(8.dp),
                )

                Text(
                    text =
                        "Maximum 6 créneaux.",
                    style =
                        MaterialTheme.typography
                            .bodySmall,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,
                )
            }

            Spacer(
                modifier =
                    Modifier.height(28.dp),
            )

            Text(
                text = "Intensité",
                style =
                    MaterialTheme.typography
                        .titleMedium,
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp),
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp),
            ) {
                PreferenceChip(
                    label = "Douce",
                    selected =
                        "gentle" in
                                activityPreferences
                                    .enabledIntensities,
                    canDisable =
                        activityPreferences
                            .enabledIntensities
                            .size > 1,
                    onChanged = {
                        onIntensityChanged(
                            "gentle",
                            it,
                        )
                    },
                )

                PreferenceChip(
                    label = "Intermédiaire",
                    selected =
                        "active" in
                                activityPreferences
                                    .enabledIntensities,
                    canDisable =
                        activityPreferences
                            .enabledIntensities
                            .size > 1,
                    onChanged = {
                        onIntensityChanged(
                            "active",
                            it,
                        )
                    },
                )
            }

            Spacer(
                modifier =
                    Modifier.height(8.dp),
            )

            Row {
                PreferenceChip(
                    label = "Dynamique",
                    selected =
                        "dynamic" in
                                activityPreferences
                                    .enabledIntensities,
                    canDisable =
                        activityPreferences
                            .enabledIntensities
                            .size > 1,
                    onChanged = {
                        onIntensityChanged(
                            "dynamic",
                            it,
                        )
                    },
                )
            }

            Spacer(
                modifier =
                    Modifier.height(28.dp),
            )

            Text(
                text = "Type de mouvement",
                style =
                    MaterialTheme.typography
                        .titleMedium,
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp),
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp),
            ) {
                PreferenceChip(
                    label = "Sur place",
                    selected =
                        activityPreferences
                            .stationaryEnabled,
                    canDisable =
                        activityPreferences
                            .movingEnabled,
                    onChanged =
                        onStationaryChanged,
                )

                PreferenceChip(
                    label = "En mouvement",
                    selected =
                        activityPreferences
                            .movingEnabled,
                    canDisable =
                        activityPreferences
                            .stationaryEnabled,
                    onChanged =
                        onMovingChanged,
                )
            }

            Spacer(
                modifier =
                    Modifier.height(32.dp),
            )

            TextButton(
                modifier =
                    Modifier.fillMaxWidth(),
                onClick =
                    onBack,
            ) {
                Text("Fermer")
            }
        }
    }
}

@Composable
private fun PreferenceChip(
    label: String,
    selected: Boolean,
    canDisable: Boolean,
    onChanged: (Boolean) -> Unit,
) {
    FilterChip(
        selected =
            selected,
        onClick = {
            if (
                !selected ||
                canDisable
            ) {
                onChanged(
                    !selected
                )
            }
        },
        label = {
            Text(
                label
            )
        },
    )
}

@Composable
private fun WeekdayRow(
    days: List<DayOfWeek>,
    selected: Set<DayOfWeek>,
    onChanged:
        (DayOfWeek, Boolean) -> Unit,
) {
    Row(
        horizontalArrangement =
            Arrangement.spacedBy(8.dp),
    ) {
        days.forEach { day ->

            FilterChip(
                selected =
                    day in selected,
                onClick = {
                    onChanged(
                        day,
                        day !in selected,
                    )
                },
                label = {
                    Text(
                        weekdayShortName(
                            day
                        )
                    )
                },
            )
        }
    }
}

private fun showTimePicker(
    context: android.content.Context,
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
) {
    TimePickerDialog(
        context,
        { _, hour, minute ->
            onTimeSelected(
                LocalTime.of(
                    hour,
                    minute,
                )
            )
        },
        initialTime.hour,
        initialTime.minute,
        true,
    ).show()
}
private fun formatTime(
    time: LocalTime,
): String {
    return "%02d h %02d".format(
        time.hour,
        time.minute,
    )
}

private fun formatTimeForTalkBack(
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
        DayOfWeek.MONDAY ->
            "Lun"

        DayOfWeek.TUESDAY ->
            "Mar"

        DayOfWeek.WEDNESDAY ->
            "Mer"

        DayOfWeek.THURSDAY ->
            "Jeu"

        DayOfWeek.FRIDAY ->
            "Ven"

        DayOfWeek.SATURDAY ->
            "Sam"

        DayOfWeek.SUNDAY ->
            "Dim"
    }
}