package org.sedentaryapp.ui.schedule

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
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
    snoozeDurationMinutes: Int,
    snoozeMinimumDurationMinutes: Int,
    snoozeMaximumDurationMinutes: Int,
    minimumGapMinutes: Long,
    nextProposalAt: LocalDateTime?,
    nextProposalOverrideActive: Boolean,
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
    onSnoozeDurationChanged: (Int) -> Unit,
    onNextProposalChanged: (LocalDateTime) -> Unit,
    onClearNextProposalOverride: () -> Unit,
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

    var nextProposalError by
        remember {
            mutableStateOf<String?>(
                null
            )
        }

    var infoDialog by
        remember {
            mutableStateOf<InfoDialogContent?>(
                null
            )
        }

    var customSnoozeDialogOpen by
        remember {
            mutableStateOf(false)
        }

    var customSnoozeDraftMinutes by
        remember(snoozeDurationMinutes) {
            mutableStateOf(
                snoozeDurationMinutes
                    .coerceIn(
                        snoozeMinimumDurationMinutes,
                        snoozeMaximumDurationMinutes,
                    )
            )
        }

    infoDialog
        ?.let { content ->
            AlertDialog(
                onDismissRequest = {
                    infoDialog = null
                },
                title = {
                    Text(
                        text =
                            content.title
                    )
                },
                text = {
                    Text(
                        text =
                            content.body
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            infoDialog = null
                        },
                    ) {
                        Text("OK")
                    }
                },
            )
        }

    if (customSnoozeDialogOpen) {
        AlertDialog(
            onDismissRequest = {
                customSnoozeDialogOpen = false
            },
            title = {
                Text(
                    text = "Autre durée"
                )
            },
            text = {
                Column {
                    Text(
                        text =
                            "Choisis entre $snoozeMinimumDurationMinutes et $snoozeMaximumDurationMinutes min, par pas de 5."
                    )

                    Spacer(
                        modifier =
                            Modifier.height(18.dp),
                    )

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(12.dp),
                        verticalAlignment =
                            Alignment.CenterVertically,
                    ) {
                        OutlinedButton(
                            modifier =
                                Modifier.weight(1f),
                            enabled =
                                customSnoozeDraftMinutes >
                                    snoozeMinimumDurationMinutes,
                            onClick = {
                                customSnoozeDraftMinutes =
                                    (customSnoozeDraftMinutes -
                                        SNOOZE_CUSTOM_STEP_MINUTES)
                                        .coerceAtLeast(
                                            snoozeMinimumDurationMinutes
                                        )
                            },
                        ) {
                            Text("−")
                        }

                        Text(
                            modifier =
                                Modifier.weight(1.2f),
                            text =
                                "$customSnoozeDraftMinutes min",
                            textAlign =
                                TextAlign.Center,
                            style =
                                MaterialTheme.typography
                                    .titleMedium,
                        )

                        OutlinedButton(
                            modifier =
                                Modifier.weight(1f),
                            enabled =
                                customSnoozeDraftMinutes <
                                    snoozeMaximumDurationMinutes,
                            onClick = {
                                customSnoozeDraftMinutes =
                                    (customSnoozeDraftMinutes +
                                        SNOOZE_CUSTOM_STEP_MINUTES)
                                        .coerceAtMost(
                                            snoozeMaximumDurationMinutes
                                        )
                            },
                        ) {
                            Text("+")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSnoozeDurationChanged(
                            customSnoozeDraftMinutes
                        )
                        customSnoozeDialogOpen = false
                    },
                ) {
                    Text("Utiliser")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        customSnoozeDialogOpen = false
                    },
                ) {
                    Text("Annuler")
                }
            },
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
                if (proposalsPaused) {
                    Text(
                        text =
                            "Propositions d'activités en pause",
                        style =
                            MaterialTheme.typography
                                .titleLarge,
                    )

                    Spacer(
                        modifier =
                            Modifier.height(16.dp),
                    )

                    Button(
                        modifier =
                            Modifier.fillMaxWidth(),
                        onClick = {
                            onPauseChanged(
                                false
                            )
                        },
                    ) {
                        Text("Réactiver")
                    }
                } else {
                    FilledTonalButton(
                        modifier =
                            Modifier.fillMaxWidth(),
                        onClick = {
                            onPauseChanged(
                                true
                            )
                        },
                        shape =
                            RoundedCornerShape(14.dp),
                    ) {
                        Text(
                            "Mettre les propositions d’activité en pause"
                        )
                    }
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

            val slotFontScale =
                LocalDensity.current
                    .fontScale

            val slotRows =
                if (slotFontScale < 1.3f) {
                    settings.slots
                        .chunked(2)
                } else {
                    settings.slots
                        .map {
                            listOf(it)
                        }
                }

            slotRows.forEachIndexed {
                    gridRowIndex,
                    rowSlots,
                ->

                if (gridRowIndex > 0) {
                    Spacer(
                        modifier =
                            Modifier.height(8.dp),
                    )
                }

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp),
                ) {
                    rowSlots.forEach { slot ->
                        SlotTile(
                            modifier =
                                Modifier.weight(1f),
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
                    }

                    if (
                        rowSlots.size == 1 &&
                        slotFontScale < 1.3f
                    ) {
                        Spacer(
                            modifier =
                                Modifier.weight(1f),
                        )
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

            if (showInfoLinks) {
                Spacer(
                    modifier =
                        Modifier.height(24.dp),
                )

                SectionTitleWithInfo(
                    text =
                        "Prochaine proposition d’activité",
                    onInfo = {
                        infoDialog =
                            InfoDialogContent(
                                title =
                                    "Prochaine proposition d’activité",
                                body =
                                    "Tu modifies la prochaine proposition. Ensuite, Intervalles reprend automatiquement le planning habituel à partir de cette date et de cette heure.",
                            )
                    },
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
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 10.dp,
                                ),
                        verticalAlignment =
                            Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier =
                                Modifier.weight(1f),
                            text =
                                if (proposalsPaused) {
                                    "En pause"
                                } else {
                                    nextProposalAt
                                        ?.let {
                                            formatNextProposalDateTime(
                                                it
                                            )
                                        }
                                        ?: "Aucune proposition prévue"
                                },
                            style =
                                MaterialTheme.typography
                                    .bodyLarge,
                        )

                        TextButton(
                            onClick = {
                                val initial =
                                    nextProposalAt
                                        ?.takeIf {
                                            it.isAfter(
                                                LocalDateTime.now()
                                            )
                                        }
                                        ?: LocalDateTime
                                            .now()
                                            .plusDays(1)
                                            .withHour(10)
                                            .withMinute(0)
                                            .withSecond(0)
                                            .withNano(0)

                                showDateTimePicker(
                                    context =
                                        context,
                                    initialDateTime =
                                        initial,
                                ) { selected ->
                                    if (
                                        !selected.isAfter(
                                            LocalDateTime.now()
                                        )
                                    ) {
                                        nextProposalError =
                                            "Choisis une date et une heure à venir."
                                    } else {
                                        nextProposalError = null
                                        onNextProposalChanged(
                                            selected
                                        )
                                    }
                                }
                            },
                        ) {
                            Text(
                                if (
                                    nextProposalAt == null
                                ) {
                                    "Planifier"
                                } else {
                                    "Modifier"
                                }
                            )
                        }
                    }

                    if (
                        proposalsPaused &&
                        nextProposalAt != null
                    ) {
                        Text(
                            modifier =
                                Modifier.padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = 14.dp,
                                ),
                            text =
                                "Planning prévu : ${formatNextProposalDateTime(nextProposalAt)}",
                            style =
                                MaterialTheme.typography
                                    .bodySmall,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant,
                        )
                    }
                }

                nextProposalError
                    ?.let {
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

                if (nextProposalOverrideActive) {
                    TextButton(
                        onClick = {
                            nextProposalError = null
                            onClearNextProposalOverride()
                        },
                    ) {
                        Text(
                            "Revenir au planning habituel"
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(24.dp),
            )

            SectionTitleWithInfo(
                text =
                    "Intensité",
                onInfo = {
                    infoDialog =
                        InfoDialogContent(
                            title =
                                "Intensité",
                            body =
                                "Douce, Modérée ou Dynamique indiquent le niveau de mouvement proposé. Tu peux en choisir plusieurs.",
                        )
                },
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

            SectionTitleWithInfo(
                text =
                    "Type de mouvement",
                onInfo = {
                    infoDialog =
                        InfoDialogContent(
                            title =
                                "Type de mouvement",
                            body =
                                "Sur place : sans te déplacer. En mouvement : activités qui impliquent de marcher ou de te déplacer.",
                        )
                },
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

                SectionTitleWithInfo(
                    text = "Report",
                    onInfo = {
                        infoDialog =
                            InfoDialogContent(
                                title = "Report",
                                body =
                                    "Une proposition peut être reportée une seule fois. Choisis ici la durée du report. Si ce report placerait la proposition à moins de $minimumGapMinutes minutes d’un autre créneau prévu, le bouton de report n’est pas proposé. Dans ce cas, la prochaine proposition arrive au prochain créneau prévu.",
                            )
                    },
                )

                Spacer(
                    modifier =
                        Modifier.height(10.dp),
                )

                SnoozeDurationRows(
                    selectedMinutes =
                        snoozeDurationMinutes,
                    onPresetSelected =
                        onSnoozeDurationChanged,
                    onCustomSelected = {
                        customSnoozeDraftMinutes =
                            snoozeDurationMinutes
                                .coerceIn(
                                    snoozeMinimumDurationMinutes,
                                    snoozeMaximumDurationMinutes,
                                )
                        customSnoozeDialogOpen = true
                    },
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
                        Modifier.height(24.dp),
                )

                SectionTitleWithInfo(
                    text = "Report",
                    onInfo = {
                        infoDialog =
                            InfoDialogContent(
                                title = "Report",
                                body =
                                    "Une proposition peut être reportée une seule fois. Choisis ici la durée du report. Si ce report placerait la proposition à moins de $minimumGapMinutes minutes d’un autre créneau prévu, le bouton de report n’est pas proposé. Dans ce cas, la prochaine proposition arrive au prochain créneau prévu.",
                            )
                    },
                )

                Spacer(
                    modifier =
                        Modifier.height(10.dp),
                )

                SnoozeDurationRows(
                    selectedMinutes =
                        snoozeDurationMinutes,
                    onPresetSelected =
                        onSnoozeDurationChanged,
                    onCustomSelected = {
                        customSnoozeDraftMinutes =
                            snoozeDurationMinutes
                                .coerceIn(
                                    snoozeMinimumDurationMinutes,
                                    snoozeMaximumDurationMinutes,
                                )
                        customSnoozeDialogOpen = true
                    },
                )

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

private data class InfoDialogContent(
    val title: String,
    val body: String,
)

@Composable
private fun SectionTitleWithInfo(
    text: String,
    onInfo: () -> Unit,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically,
    ) {
        Text(
            modifier =
                Modifier.weight(1f),
            text =
                text,
            style =
                MaterialTheme.typography
                    .titleMedium,
        )

        IconButton(
            modifier =
                Modifier.sizeIn(
                    minWidth = 48.dp,
                    minHeight = 48.dp,
                ),
            onClick =
                onInfo,
        ) {
            Text(
                text = "ⓘ",
                style =
                    MaterialTheme.typography
                        .titleMedium,
                color =
                    MaterialTheme.colorScheme
                        .primary,
            )
        }
    }
}

@Composable
private fun SlotTile(
    modifier: Modifier = Modifier,
    time: LocalTime,
    canRemove: Boolean,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        modifier =
            modifier,
        shape =
            RoundedCornerShape(14.dp),
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
                    textAlign =
                        TextAlign.Center,
                    style =
                        MaterialTheme.typography
                            .bodyLarge,
                )
            }

            IconButton(
                modifier =
                    Modifier
                        .sizeIn(
                            minWidth = 48.dp,
                            minHeight = 48.dp,
                        )
                        .semantics {
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
}

@Composable
private fun WeekdayRows(
    selected: Set<DayOfWeek>,
    onChanged:
        (DayOfWeek, Boolean) -> Unit,
) {
    val days =
        listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY,
        )

    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(4.dp),
    ) {
        days.forEach { day ->
            SelectionChip(
                modifier =
                    Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription =
                                weekdayFullName(day)
                        },
                label =
                    weekdayShortName(
                        day
                    ),
                selected =
                    day in selected,
                compact =
                    true,
                onClick = {
                    onChanged(
                        day,
                        day !in selected,
                    )
                },
            )
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
    val fontScale =
        LocalDensity.current
            .fontScale

    BoxWithConstraints(
        modifier =
            Modifier.fillMaxWidth(),
    ) {
        val effectiveWidth =
            maxWidth / fontScale

        val oneRow =
            effectiveWidth >= 300.dp

        if (oneRow) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(6.dp),
            ) {
                PreferenceChip(
                    modifier =
                        Modifier.weight(0.85f),
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
                    modifier =
                        Modifier.weight(1.0f),
                    label =
                        "Modérée",
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

                PreferenceChip(
                    modifier =
                        Modifier.weight(1.15f),
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
        } else {
            Column(
                modifier =
                    Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp),
                ) {
                    PreferenceChip(
                        modifier =
                            Modifier.weight(1f),
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
                        modifier =
                            Modifier.weight(1f),
                        label =
                            "Modérée",
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

                PreferenceChip(
                    modifier =
                        Modifier.fillMaxWidth(),
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
    }
}

@Composable
private fun SnoozeDurationRows(
    selectedMinutes: Int,
    onPresetSelected: (Int) -> Unit,
    onCustomSelected: () -> Unit,
) {
    val presets =
        listOf(
            15,
            30,
            60,
        )

    val customSelected =
        selectedMinutes !in presets

    val choices =
        listOf(
            Pair(
                "15 min",
                { onPresetSelected(15) },
            ),
            Pair(
                "30 min",
                { onPresetSelected(30) },
            ),
            Pair(
                "60 min",
                { onPresetSelected(60) },
            ),
            Pair(
                if (customSelected) {
                    "Autre · $selectedMinutes min"
                } else {
                    "Autre…"
                },
                onCustomSelected,
            ),
        )

    val fontScale =
        LocalDensity.current
            .fontScale

    val rows =
        if (fontScale < 1.3f) {
            choices.chunked(2)
        } else {
            choices.map { listOf(it) }
        }

    rows.forEachIndexed {
            rowIndex,
            rowChoices,
        ->

        if (rowIndex > 0) {
            Spacer(
                modifier =
                    Modifier.height(8.dp),
            )
        }

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(8.dp),
        ) {
            rowChoices.forEachIndexed {
                    choiceIndex,
                    choice,
                ->

                val absoluteIndex =
                    rowIndex *
                        (if (fontScale < 1.3f) 2 else 1) +
                        choiceIndex

                val selected =
                    when (absoluteIndex) {
                        0 -> selectedMinutes == 15
                        1 -> selectedMinutes == 30
                        2 -> selectedMinutes == 60
                        else -> customSelected
                    }

                SelectionChip(
                    modifier =
                        Modifier.weight(1f),
                    label = choice.first,
                    selected = selected,
                    onClick = choice.second,
                )
            }

            if (
                rowChoices.size == 1 &&
                fontScale < 1.3f
            ) {
                Spacer(
                    modifier =
                        Modifier.weight(1f),
                )
            }
        }
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
    val fontScale =
        LocalDensity.current
            .fontScale

    BoxWithConstraints(
        modifier =
            Modifier.fillMaxWidth(),
    ) {
        val effectiveWidth =
            maxWidth / fontScale

        val oneRow =
            effectiveWidth >= 260.dp

        if (oneRow) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp),
            ) {
                PreferenceChip(
                    modifier =
                        Modifier.weight(0.9f),
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
                    modifier =
                        Modifier.weight(1.1f),
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
        } else {
            Column(
                modifier =
                    Modifier.fillMaxWidth(),
            ) {
                PreferenceChip(
                    modifier =
                        Modifier.fillMaxWidth(),
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
                    modifier =
                        Modifier.fillMaxWidth(),
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
    }
}

@Composable
private fun PreferenceChip(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    canDisable: Boolean,
    onChanged: (Boolean) -> Unit,
) {
    SelectionChip(
        modifier =
            modifier,
        label =
            label,
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
    )
}

@Composable
private fun SelectionChip(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    compact: Boolean = false,
    onClick: () -> Unit,
) {
    FilterChip(
        modifier =
            modifier,
        selected =
            selected,
        onClick =
            onClick,
        label = {
            Text(
                modifier =
                    Modifier.fillMaxWidth(),
                text =
                    if (
                        selected &&
                        !compact
                    ) {
                        "✓ $label"
                    } else {
                        label
                    },
                textAlign =
                    TextAlign.Center,
                maxLines = 1,
                softWrap = false,
            )
        },
        colors =
            FilterChipDefaults
                .filterChipColors(
                    containerColor =
                        MaterialTheme.colorScheme
                            .surface,
                    labelColor =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,
                    selectedContainerColor =
                        MaterialTheme.colorScheme
                            .primaryContainer,
                    selectedLabelColor =
                        MaterialTheme.colorScheme
                            .onPrimaryContainer,
                ),
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

private fun showDateTimePicker(
    context: android.content.Context,
    initialDateTime: LocalDateTime,
    onDateTimeSelected: (LocalDateTime) -> Unit,
) {
    val dateDialog =
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate =
                    LocalDate.of(
                        year,
                        month + 1,
                        dayOfMonth,
                    )

                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        onDateTimeSelected(
                            LocalDateTime.of(
                                selectedDate,
                                LocalTime.of(
                                    hour,
                                    minute,
                                ),
                            )
                        )
                    },
                    initialDateTime.hour,
                    initialDateTime.minute,
                    true,
                ).apply {
                    setTitle(
                        "Heure de la prochaine proposition"
                    )
                }.show()
            },
            initialDateTime.year,
            initialDateTime.monthValue - 1,
            initialDateTime.dayOfMonth,
        )

    dateDialog.setTitle(
        "Modifier la prochaine proposition"
    )

    dateDialog.datePicker.minDate =
        LocalDate.now()
            .atStartOfDay(
                ZoneId.systemDefault()
            )
            .toInstant()
            .toEpochMilli()

    dateDialog.show()
}

private fun formatNextProposalDateTime(
    dateTime: LocalDateTime,
): String {

    val day =
        "%02d/%02d/%04d".format(
            dateTime.dayOfMonth,
            dateTime.monthValue,
            dateTime.year,
        )

    val time =
        "%02d:%02d".format(
            dateTime.hour,
            dateTime.minute,
        )

    return "$day · $time"
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


private const val SNOOZE_CUSTOM_STEP_MINUTES = 5
