package org.sedentaryapp.ui.schedule

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalTime
import org.sedentaryapp.R
import org.sedentaryapp.data.ActivityPreferences
import org.sedentaryapp.data.ScheduleSettings
import org.sedentaryapp.engine.schedule.NextOpportunityPlanner

@Composable
fun ScheduleScreen(
    settings: ScheduleSettings,
    activityPreferences: ActivityPreferences,
    proposalsPaused: Boolean,
    completionVibrationEnabled: Boolean,
    notificationSoundEnabled: Boolean,
    notificationVibrationEnabled: Boolean,
    minimumGapMinutes: Long,
    title: String = "Tes réglages",
    showPauseControl: Boolean = true,
    bottomActionLabel: String = "Fermer",
    onWeekdayChanged: (DayOfWeek, Boolean) -> Unit,
    onSlotTimeChanged: (Int, LocalTime) -> Unit,
    onAddSlot: (LocalTime) -> Unit,
    onRemoveSlot: (Int) -> Unit,
    onIntensityChanged: (String, Boolean) -> Unit,
    onStationaryChanged: (Boolean) -> Unit,
    onMovingChanged: (Boolean) -> Unit,
    onPauseChanged: (Boolean) -> Unit,
    onCompletionVibrationChanged: (Boolean) -> Unit,
    onNotificationSoundChanged: (Boolean) -> Unit,
    onNotificationVibrationChanged: (Boolean) -> Unit,
    showInfoLinks: Boolean = true,
    onOpenWhy: () -> Unit,
    onOpenAbout: () -> Unit,
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
        color =
            MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        horizontal = 20.dp,
                        vertical = 20.dp,
                    ),
        ) {
            if (showInfoLinks) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick =
                            onBack,
                    ) {
                        Icon(
                            painter =
                                painterResource(
                                    R.drawable.ic_arrow_back
                                ),
                            contentDescription =
                                "Retour",
                        )
                    }

                    Text(
                        text = "Paramètres",
                        style =
                            MaterialTheme.typography
                                .headlineMedium,
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(20.dp),
                )
            }

            if (showPauseControl) {
                FilledTonalButton(
                    modifier =
                        Modifier.fillMaxWidth(),
                    onClick = {
                        onPauseChanged(
                            !proposalsPaused
                        )
                    },
                    shape =
                        RoundedCornerShape(14.dp),
                ) {
                    Text(
                        if (proposalsPaused) {
                            "Réactiver"
                        } else {
                            "Mettre en pause"
                        }
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(26.dp),
                )
            }

            SectionTitle(
                text =
                    "Jours"
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp),
            )

            WeekdayRows(
                selected =
                    settings.selectedWeekdays,
                onChanged =
                    onWeekdayChanged,
            )

            Spacer(
                modifier =
                    Modifier.height(24.dp),
            )

            SectionTitle(
                text = "Créneaux"
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp),
            )

            Card(
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(18.dp),
                border =
                    BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme
                            .outlineVariant,
                    ),
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme
                                .surface,
                    ),
                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 0.dp
                    ),
            ) {
                Column {
                    settings.slots
                        .forEachIndexed {
                                rowIndex,
                                slot,
                            ->

                            SlotRow(
                                time =
                                    slot.time,
                                canRemove =
                                    settings.slots.size > 1,
                                onEdit = {
                                    showTimePicker(
                                        context =
                                            context,
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
                                onRemove = {
                                    slotError = null
                                    onRemoveSlot(
                                        slot.index
                                    )
                                },
                            )

                            if (
                                rowIndex <
                                settings.slots.lastIndex
                            ) {
                                HorizontalDivider(
                                    color =
                                        MaterialTheme.colorScheme
                                            .outlineVariant,
                                )
                            }
                        }
                }
            }

            slotError?.let {
                Spacer(
                    modifier =
                        Modifier.height(8.dp),
                )

                Text(
                    text = it,
                    color =
                        MaterialTheme.colorScheme
                            .error,
                    style =
                        MaterialTheme.typography
                            .bodySmall,
                )
            }

            Spacer(
                modifier =
                    Modifier.height(6.dp),
            )

            if (settings.slots.size < 6) {
                TextButton(
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
                            context =
                                context,
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
                    Text("+ Ajouter un créneau")
                }
            }

            Spacer(
                modifier =
                    Modifier.height(24.dp),
            )

            SectionTitle(
                text = "Intensité"
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp),
            )

            PreferenceRows(
                activityPreferences =
                    activityPreferences,
                onIntensityChanged =
                    onIntensityChanged,
            )

            Spacer(
                modifier =
                    Modifier.height(24.dp),
            )

            SectionTitle(
                text =
                    "Type de mouvement"
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp),
            )

            MovementRows(
                activityPreferences =
                    activityPreferences,
                onStationaryChanged =
                    onStationaryChanged,
                onMovingChanged =
                    onMovingChanged,
            )

            if (showInfoLinks) {
                Spacer(
                    modifier =
                        Modifier.height(24.dp),
                )

                SectionTitle(
                    text = "Notifications"
                )

                Spacer(
                    modifier =
                        Modifier.height(10.dp),
                )

                SettingSwitchRow(
                    title = "Son",
                    subtitle =
                        "Joue le son de notification Android.",
                    checked =
                        notificationSoundEnabled,
                    onCheckedChange =
                        onNotificationSoundChanged,
                )

                SettingSwitchRow(
                    title = "Vibration",
                    subtitle =
                        "Vibre brièvement à chaque proposition.",
                    checked =
                        notificationVibrationEnabled,
                    onCheckedChange =
                        onNotificationVibrationChanged,
                )

                Spacer(
                    modifier =
                        Modifier.height(24.dp),
                )

                SectionTitle(
                    text = "Chrono"
                )

                Spacer(
                    modifier =
                        Modifier.height(10.dp),
                )

                SettingSwitchRow(
                    title =
                        "Vibration à la fin",
                    subtitle =
                        "Une vibration courte quand le chrono atteint 0:00.",
                    checked =
                        completionVibrationEnabled,
                    onCheckedChange =
                        onCompletionVibrationChanged,
                )

                Spacer(
                    modifier =
                        Modifier.height(28.dp),
                )

                Spacer(
                    modifier =
                        Modifier.height(28.dp),
                )

                InfoRow(
                    label =
                        "Pourquoi ces pauses ?",
                    onClick =
                        onOpenWhy,
                )

                HorizontalDivider(
                    color =
                        MaterialTheme.colorScheme
                            .outlineVariant,
                )

                InfoRow(
                    label =
                        "À propos",
                    onClick =
                        onOpenAbout,
                )
            } else {
                Spacer(
                    modifier =
                        Modifier.height(28.dp),
                )

                OutlinedButton(
                    modifier =
                        Modifier.fillMaxWidth(),
                    onClick =
                        onBack,
                    shape =
                        RoundedCornerShape(14.dp),
                ) {
                    Text(bottomActionLabel)
                }
            }

            Spacer(
                modifier =
                    Modifier.height(20.dp),
            )
        }
    }
}

@Composable
private fun SectionTitle(
    text: String,
) {
    Text(
        text = text,
        style =
            MaterialTheme.typography
                .titleMedium,
    )
}

@Composable
private fun SlotRow(
    time: LocalTime,
    canRemove: Boolean,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp),
        verticalAlignment =
            Alignment.CenterVertically,
    ) {
        TextButton(
            modifier =
                Modifier.weight(1f),
            onClick =
                onEdit,
        ) {
            Text(
                modifier =
                    Modifier.fillMaxWidth(),
                text =
                    formatTime(
                        time
                    ),
                style =
                    MaterialTheme.typography
                        .bodyLarge,
            )
        }

        IconButton(
            modifier =
                Modifier.semantics {
                    contentDescription =
                        "Supprimer le créneau de ${formatTimeForTalkBack(time)}"
                },
            enabled =
                canRemove,
            onClick =
                onRemove,
        ) {
            Text(
                text = "−",
                style =
                    MaterialTheme.typography
                        .titleLarge,
            )
        }
    }
}

@Composable
private fun WeekdayRows(
    selected: Set<DayOfWeek>,
    onChanged:
        (DayOfWeek, Boolean) -> Unit,
) {
    val rows =
        listOf(
            listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
            ),
            listOf(
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY,
            ),
        )

    Column {
        rows.forEachIndexed {
                index,
                days,
            ->

            if (index > 0) {
                Spacer(
                    modifier =
                        Modifier.height(8.dp),
                )
            }

            Row {
                days.forEachIndexed {
                        dayIndex,
                        day,
                    ->

                    if (dayIndex > 0) {
                        Spacer(
                            modifier =
                                Modifier.size(8.dp),
                        )
                    }

                    FilterChip(
                        modifier =
                            Modifier.semantics {
                                contentDescription =
                                    weekdayFullName(day)
                            },
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
                        shape =
                            RoundedCornerShape(12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PreferenceRows(
    activityPreferences:
    ActivityPreferences,
    onIntensityChanged:
        (String, Boolean) -> Unit,
) {
    Column {
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

        Spacer(
            modifier =
                Modifier.height(8.dp),
        )

        PreferenceChip(
            label =
                "Intermédiaire",
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

        Spacer(
            modifier =
                Modifier.height(8.dp),
        )

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
}

@Composable
private fun MovementRows(
    activityPreferences:
    ActivityPreferences,
    onStationaryChanged:
        (Boolean) -> Unit,
    onMovingChanged:
        (Boolean) -> Unit,
) {
    Column {
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

        Spacer(
            modifier =
                Modifier.height(8.dp),
        )

        PreferenceChip(
            label =
                "En mouvement",
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
            Text(label)
        },
        shape =
            RoundedCornerShape(12.dp),
    )
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 6.dp,
                ),
        verticalAlignment =
            Alignment.CenterVertically,
    ) {
        Column(
            modifier =
                Modifier.weight(1f),
        ) {
            Text(
                text = title,
                style =
                    MaterialTheme.typography
                        .bodyLarge,
            )

            Text(
                text = subtitle,
                style =
                    MaterialTheme.typography
                        .bodySmall,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant,
            )
        }

        Switch(
            checked =
                checked,
            onCheckedChange =
                onCheckedChange,
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick =
                        onClick
                )
                .padding(
                    vertical = 16.dp,
                ),
        verticalAlignment =
            Alignment.CenterVertically,
    ) {
        Text(
            modifier =
                Modifier.weight(1f),
            text = label,
            style =
                MaterialTheme.typography
                    .bodyLarge,
        )

        Text(
            text = "›",
            style =
                MaterialTheme.typography
                    .titleLarge,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant,
        )
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
    return "%02d:%02d".format(
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
        DayOfWeek.MONDAY -> "L"
        DayOfWeek.TUESDAY -> "M"
        DayOfWeek.WEDNESDAY -> "M"
        DayOfWeek.THURSDAY -> "J"
        DayOfWeek.FRIDAY -> "V"
        DayOfWeek.SATURDAY -> "S"
        DayOfWeek.SUNDAY -> "D"
    }
}

private fun weekdayFullName(
    day: DayOfWeek,
): String {
    return when (day) {
        DayOfWeek.MONDAY -> "Lundi"
        DayOfWeek.TUESDAY -> "Mardi"
        DayOfWeek.WEDNESDAY -> "Mercredi"
        DayOfWeek.THURSDAY -> "Jeudi"
        DayOfWeek.FRIDAY -> "Vendredi"
        DayOfWeek.SATURDAY -> "Samedi"
        DayOfWeek.SUNDAY -> "Dimanche"
    }
}