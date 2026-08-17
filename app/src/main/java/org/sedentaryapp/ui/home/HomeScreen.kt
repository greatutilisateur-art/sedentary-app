package org.sedentaryapp.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
                    .padding(
                        horizontal = 24.dp,
                        vertical = 40.dp,
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
                    Modifier.height(16.dp),
            )

            when {
                proposalsPaused -> {
                    PausedContent(
                        onResumeProposals =
                            onResumeProposals,
                        onOpenSettings =
                            onOpenSettings,
                    )
                }

                notificationPermissionRequired -> {
                    PermissionContent(
                        title =
                            "Les notifications sont désactivées sur cet appareil.",
                        body =
                            "Autorise-les pour recevoir tes propositions aux horaires choisis.",
                        buttonLabel =
                            "Autoriser les notifications",
                        onRequestPermission =
                            onRequestNotificationPermission,
                        onOpenSettings =
                            onOpenSettings,
                    )
                }

                exactAlarmPermissionRequired -> {
                    PermissionContent(
                        title =
                            "Android n’autorise pas encore les horaires précis.",
                        body =
                            "Autorise l’accès « Alarmes et rappels » pour recevoir les propositions au plus près des horaires choisis.",
                        buttonLabel =
                            "Autoriser les horaires précis",
                        onRequestPermission =
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
        }
    }
}

@Composable
private fun PausedContent(
    onResumeProposals: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Text(
        text = "Propositions en pause",
        style =
            MaterialTheme.typography
                .titleMedium,
    )

    Spacer(
        modifier =
            Modifier.height(12.dp),
    )

    OutlinedButton(
        modifier =
            Modifier.fillMaxWidth(),
        onClick =
            onResumeProposals,
    ) {
        Text("Reprendre")
    }

    Spacer(
        modifier =
            Modifier.height(4.dp),
    )

    TextButton(
        onClick =
            onOpenSettings,
    ) {
        Text(
            "Modifier mes réglages"
        )
    }
}

@Composable
private fun PermissionContent(
    title: String,
    body: String,
    buttonLabel: String,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Text(
        text = title,
        style =
            MaterialTheme.typography
                .titleMedium,
    )

    Spacer(
        modifier =
            Modifier.height(12.dp),
    )

    Text(
        text = body,
        style =
            MaterialTheme.typography
                .bodyLarge,
    )

    Spacer(
        modifier =
            Modifier.height(16.dp),
    )

    Button(
        modifier =
            Modifier.fillMaxWidth(),
        onClick =
            onRequestPermission,
    ) {
        Text(
            buttonLabel
        )
    }

    Spacer(
        modifier =
            Modifier.height(4.dp),
    )

    TextButton(
        onClick =
            onOpenSettings,
    ) {
        Text(
            "Modifier mes réglages"
        )
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
    if (
        nextActivity != null &&
        nextActivityWhenText != null
    ) {
        Text(
            text = "Prochaine activité",
            style =
                MaterialTheme.typography
                    .titleMedium,
        )

        Spacer(
            modifier =
                Modifier.height(12.dp),
        )

        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick =
                            onOpenNextActivity
                    ),
        ) {
            Column(
                modifier =
                    Modifier.padding(20.dp),
            ) {
                Text(
                    text =
                        nextActivity.title,
                    style =
                        MaterialTheme.typography
                            .titleLarge,
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
                )

                Spacer(
                    modifier =
                        Modifier.height(6.dp),
                )

                Text(
                    text =
                        nextActivityWhenText,
                    style =
                        MaterialTheme.typography
                            .bodyMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,
                )
            }
        }
    } else {
        Text(
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

    Spacer(
        modifier =
            Modifier.height(12.dp),
    )

    TextButton(
        onClick =
            onOpenSettings,
    ) {
        Text(
            "Modifier mes réglages"
        )
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
                Modifier.height(28.dp),
        )

        Text(
            text =
                "Dernière activité",
            style =
                MaterialTheme.typography
                    .titleMedium,
        )

        Spacer(
            modifier =
                Modifier.height(12.dp),
        )

        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick =
                            onOpenLastActivity
                    ),
        ) {
            Column(
                modifier =
                    Modifier.padding(20.dp),
            ) {
                Text(
                    text =
                        lastActivity.title,
                    style =
                        MaterialTheme.typography
                            .titleLarge,
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