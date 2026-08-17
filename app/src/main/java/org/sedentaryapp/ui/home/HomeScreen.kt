package org.sedentaryapp.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sedentaryapp.data.CatalogActivity

@Composable
fun HomeScreen(
    proposalsPaused: Boolean,
    notificationPermissionRequired: Boolean,
    exactAlarmPermissionRequired: Boolean,
    nextActivity: CatalogActivity?,
    noEligibleActivity: Boolean,
    nextActivityWhenText: String?,
    lastActivity: CatalogActivity?,
    lastActivityWhenText: String?,
    onPauseProposals: () -> Unit,
    onResumeProposals: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onRequestExactAlarmPermission: () -> Unit,
    onOpenNextActivity: () -> Unit,
    onOpenLastActivity: () -> Unit,
    onOpenSettings: () -> Unit,
) {
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
                    .padding(
                        horizontal = 20.dp,
                        vertical = 32.dp,
                    ),
        ) {
            Text(
                text = "Mes pauses",
                style =
                    MaterialTheme.typography
                        .headlineMedium,
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp),
            )

            Text(
                text =
                    "Des occasions simples de bouger pendant la journée.",
                style =
                    MaterialTheme.typography
                        .bodyMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant,
            )

            Spacer(
                modifier =
                    Modifier.height(28.dp),
            )

            when {
                proposalsPaused -> {
                    StatusCard(
                        title =
                            "Propositions en pause",
                        body =
                            "Tes réglages sont conservés. Reprends quand tu veux.",
                        primaryLabel =
                            "Reprendre",
                        onPrimary =
                            onResumeProposals,
                        onOpenSettings =
                            onOpenSettings,
                    )
                }

                notificationPermissionRequired -> {
                    StatusCard(
                        title =
                            "Notifications désactivées",
                        body =
                            "Autorise-les pour recevoir tes propositions aux horaires choisis.",
                        primaryLabel =
                            "Autoriser les notifications",
                        onPrimary =
                            onRequestNotificationPermission,
                        onOpenSettings =
                            onOpenSettings,
                    )
                }

                exactAlarmPermissionRequired -> {
                    StatusCard(
                        title =
                            "Horaires précis désactivés",
                        body =
                            "Autorise l’accès « Alarmes et rappels » pour recevoir les propositions au plus près des horaires choisis.",
                        primaryLabel =
                            "Autoriser les horaires précis",
                        onPrimary =
                            onRequestExactAlarmPermission,
                        onOpenSettings =
                            onOpenSettings,
                    )
                }

                else -> {
                    ActiveContent(
                        nextActivity =
                            nextActivity,
                        noEligibleActivity =
                            noEligibleActivity,
                        nextActivityWhenText =
                            nextActivityWhenText,
                        lastActivity =
                            lastActivity,
                        lastActivityWhenText =
                            lastActivityWhenText,
                        onPauseProposals =
                            onPauseProposals,
                        onOpenNextActivity =
                            onOpenNextActivity,
                        onOpenLastActivity =
                            onOpenLastActivity,
                        onOpenSettings =
                            onOpenSettings,
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(24.dp),
            )
        }
    }
}

@Composable
private fun StatusCard(
    title: String,
    body: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(22.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme
                        .secondaryContainer,
            ),
    ) {
        Column(
            modifier =
                Modifier.padding(20.dp),
        ) {
            Text(
                text = title,
                style =
                    MaterialTheme.typography
                        .titleLarge,
                color =
                    MaterialTheme.colorScheme
                        .onSecondaryContainer,
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp),
            )

            Text(
                text = body,
                style =
                    MaterialTheme.typography
                        .bodyLarge,
                color =
                    MaterialTheme.colorScheme
                        .onSecondaryContainer,
            )

            Spacer(
                modifier =
                    Modifier.height(20.dp),
            )

            Button(
                modifier =
                    Modifier.fillMaxWidth(),
                onClick =
                    onPrimary,
            ) {
                Text(primaryLabel)
            }

            TextButton(
                modifier =
                    Modifier.fillMaxWidth(),
                onClick =
                    onOpenSettings,
            ) {
                Text("Modifier mes réglages")
            }
        }
    }
}

@Composable
private fun ActiveContent(
    nextActivity: CatalogActivity?,
    noEligibleActivity: Boolean,
    nextActivityWhenText: String?,
    lastActivity: CatalogActivity?,
    lastActivityWhenText: String?,
    onPauseProposals: () -> Unit,
    onOpenNextActivity: () -> Unit,
    onOpenLastActivity: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    SectionLabel(
        text =
            "Prochaine activité"
    )

    Spacer(
        modifier =
            Modifier.height(10.dp),
    )

    if (
        nextActivity != null &&
        nextActivityWhenText != null
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick =
                            onOpenNextActivity
                    ),
            shape =
                RoundedCornerShape(22.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme
                            .primaryContainer,
                ),
        ) {
            Column(
                modifier =
                    Modifier.padding(20.dp),
            ) {
                Text(
                    text =
                        nextActivityWhenText,
                    style =
                        MaterialTheme.typography
                            .labelLarge,
                    color =
                        MaterialTheme.colorScheme
                            .onPrimaryContainer,
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp),
                )

                Text(
                    text =
                        nextActivity.title,
                    style =
                        MaterialTheme.typography
                            .titleLarge,
                    color =
                        MaterialTheme.colorScheme
                            .onPrimaryContainer,
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp),
                )

                Text(
                    text =
                        formatDuration(
                            minSeconds =
                                nextActivity
                                    .definition
                                    .durationSecondsMin,
                            maxSeconds =
                                nextActivity
                                    .definition
                                    .durationSecondsMax,
                        ),
                    style =
                        MaterialTheme.typography
                            .bodyMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onPrimaryContainer,
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp),
                )

                Text(
                    text = "Voir l’activité",
                    style =
                        MaterialTheme.typography
                            .labelLarge,
                    color =
                        MaterialTheme.colorScheme
                            .primary,
                )
            }
        }
    } else {
        Card(
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                RoundedCornerShape(20.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme
                            .surfaceVariant,
                ),
        ) {
            Text(
                modifier =
                    Modifier.padding(18.dp),
                text =
                    if (
                        noEligibleActivity
                    ) {
                        "Aucune activité ne correspond actuellement à tes réglages."
                    } else {
                        "Aucune proposition prévue."
                    },
                style =
                    MaterialTheme.typography
                        .bodyLarge,
            )
        }
    }

    Spacer(
        modifier =
            Modifier.height(16.dp),
    )

    TextButton(
        modifier =
            Modifier.fillMaxWidth(),
        onClick =
            onOpenSettings,
    ) {
        Text("Modifier mes réglages")
    }

    OutlinedButton(
        modifier =
            Modifier.fillMaxWidth(),
        onClick =
            onPauseProposals,
    ) {
        Text("Mettre en pause")
    }

    if (
        lastActivity != null &&
        lastActivityWhenText != null
    ) {
        Spacer(
            modifier =
                Modifier.height(30.dp),
        )

        SectionLabel(
            text =
                "Dernière activité"
        )

        Spacer(
            modifier =
                Modifier.height(10.dp),
        )

        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick =
                            onOpenLastActivity
                    ),
            shape =
                RoundedCornerShape(20.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme
                            .surfaceVariant,
                ),
        ) {
            Column(
                modifier =
                    Modifier.padding(18.dp),
            ) {
                Text(
                    text =
                        lastActivity.title,
                    style =
                        MaterialTheme.typography
                            .titleMedium,
                )

                Spacer(
                    modifier =
                        Modifier.height(6.dp),
                )

                Text(
                    text =
                        lastActivityWhenText,
                    style =
                        MaterialTheme.typography
                            .bodyMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(
    text: String,
) {
    Text(
        text = text,
        style =
            MaterialTheme.typography
                .titleMedium,
    )
}

private fun formatDuration(
    minSeconds: Int,
    maxSeconds: Int,
): String {

    fun format(
        seconds: Int,
    ): String {
        return when {
            seconds < 60 ->
                "$seconds s"

            seconds % 60 == 0 ->
                "${seconds / 60} min"

            else ->
                "${seconds / 60} min ${seconds % 60} s"
        }
    }

    return if (
        minSeconds ==
        maxSeconds
    ) {
        format(
            minSeconds
        )
    } else {
        "${format(minSeconds)} – ${format(maxSeconds)}"
    }
}
