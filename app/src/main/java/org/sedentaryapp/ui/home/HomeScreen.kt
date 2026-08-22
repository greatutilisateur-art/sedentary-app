package org.sedentaryapp.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.sedentaryapp.R
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
    onMoveNow: () -> Unit,
    onOpenLastActivity: () -> Unit,
    onOpenSettings: () -> Unit,
) {
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
                        vertical = 28.dp,
                    ),
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically,
            ) {
                Icon(
                    modifier =
                        Modifier.size(30.dp),
                    painter =
                        painterResource(
                            R.drawable.ic_intervalles_mark
                        ),
                    contentDescription = null,
                    tint =
                        MaterialTheme.colorScheme
                            .primary,
                )

                Spacer(
                    modifier =
                        Modifier.size(10.dp),
                )

                Text(
                    modifier =
                        Modifier.weight(1f),
                    text = "Intervalles",
                    style =
                        MaterialTheme.typography
                            .headlineMedium,
                    color =
                        MaterialTheme.colorScheme
                            .primary,
                )

                IconButton(
                    onClick =
                        onOpenSettings,
                ) {
                    Icon(
                        painter =
                            painterResource(
                                R.drawable.ic_settings
                            ),
                        contentDescription =
                            "Paramètres",
                        tint =
                            MaterialTheme.colorScheme
                                .primary,
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(28.dp),
            )

            if (proposalsPaused) {
                Text(
                    text =
                        "Propositions en pause",
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
                    onClick =
                        onResumeProposals,
                ) {
                    Text("Réactiver")
                }

                Spacer(
                    modifier =
                        Modifier.height(12.dp),
                )

                MoveNowButton(
                    onClick =
                        onMoveNow,
                )

                return@Column
            }

            if (
                notificationPermissionRequired
            ) {
                PermissionStatusCard(
                    title =
                        "Notifications désactivées",
                    body =
                        "Autorise-les pour recevoir les propositions aux horaires choisis.",
                    actionLabel =
                        "Autoriser les notifications",
                    onAction =
                        onRequestNotificationPermission,
                )

                Spacer(
                    modifier =
                        Modifier.height(24.dp),
                )
            } else if (
                exactAlarmPermissionRequired
            ) {
                PermissionStatusCard(
                    title =
                        "Horaires précis désactivés",
                    body =
                        "Autorise l’accès « Alarmes et rappels » pour recevoir les propositions au plus près des horaires choisis.",
                    actionLabel =
                        "Autoriser les horaires précis",
                    onAction =
                        onRequestExactAlarmPermission,
                )

                Spacer(
                    modifier =
                        Modifier.height(24.dp),
                )
            }

            Text(
                text =
                    "Prochaine activité",
                style =
                    MaterialTheme.typography
                        .titleMedium,
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp),
            )

            if (
                nextActivity != null &&
                nextActivityWhenText != null
            ) {
                PrimaryActivityCard(
                    activity =
                        nextActivity,
                    whenText =
                        nextActivityWhenText,
                    onClick =
                        onOpenNextActivity,
                )
            } else {
                EmptyActivityCard(
                    text =
                        if (noEligibleActivity) {
                            "Aucune activité ne correspond actuellement à tes réglages."
                        } else {
                            "Aucune proposition prévue."
                        },
                )
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp),
            )

            MoveNowButton(
                onClick =
                    onMoveNow,
            )

            if (
                lastActivity != null &&
                lastActivityWhenText != null
            ) {
                Spacer(
                    modifier =
                        Modifier.height(30.dp),
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
                        Modifier.height(10.dp),
                )

                SecondaryActivityCard(
                    activity =
                        lastActivity,
                    whenText =
                        lastActivityWhenText,
                    onClick =
                        onOpenLastActivity,
                )
            }

            Spacer(
                modifier =
                    Modifier.height(30.dp),
            )

            OutlinedButton(
                modifier =
                    Modifier.fillMaxWidth(),
                onClick =
                    onPauseProposals,
                shape =
                    RoundedCornerShape(14.dp),
            ) {
                Text("Mettre en pause")
            }

            Spacer(
                modifier =
                    Modifier.height(16.dp),
            )
        }
    }
}

@Composable
private fun MoveNowButton(
    onClick: () -> Unit,
) {
    FilledTonalButton(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(
                    min = 56.dp
                ),
        onClick =
            onClick,
        shape =
            RoundedCornerShape(18.dp),
        colors =
            ButtonDefaults
                .filledTonalButtonColors(
                    containerColor =
                        MaterialTheme.colorScheme
                            .secondaryContainer,
                    contentColor =
                        MaterialTheme.colorScheme
                            .onSecondaryContainer,
                ),
    ) {
        Icon(
            modifier =
                Modifier.size(20.dp),
            painter =
                painterResource(
                    R.drawable.ic_intervalles_mark
                ),
            contentDescription = null,
        )

        Spacer(
            modifier =
                Modifier.size(10.dp),
        )

        Text(
            text =
                "Bouger maintenant",
            style =
                MaterialTheme.typography
                    .titleLarge,
        )
    }
}

@Composable
private fun PermissionStatusCard(
    title: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(18.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme
                        .surfaceVariant,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 0.dp
            ),
    ) {
        Column(
            modifier =
                Modifier.padding(18.dp),
        ) {
            Text(
                text = title,
                style =
                    MaterialTheme.typography
                        .titleMedium,
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp),
            )

            Text(
                text = body,
                style =
                    MaterialTheme.typography
                        .bodyMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant,
            )

            Spacer(
                modifier =
                    Modifier.height(14.dp),
            )

            Button(
                modifier =
                    Modifier.fillMaxWidth(),
                onClick =
                    onAction,
                shape =
                    RoundedCornerShape(14.dp),
            ) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun PrimaryActivityCard(
    activity: CatalogActivity,
    whenText: String,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick =
                        onClick
                ),
        shape =
            RoundedCornerShape(18.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme
                        .primaryContainer,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 0.dp
            ),
    ) {
        Column(
            modifier =
                Modifier.padding(18.dp),
        ) {
            Text(
                text =
                    activity.title,
                style =
                    MaterialTheme.typography
                        .titleLarge,
                color =
                    MaterialTheme.colorScheme
                        .onPrimaryContainer,
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp),
            )

            Text(
                text =
                    formatDuration(
                        minSeconds =
                            activity.definition
                                .durationSecondsMin,
                        maxSeconds =
                            activity.definition
                                .durationSecondsMax,
                    ),
                style =
                    MaterialTheme.typography
                        .bodyLarge,
                color =
                    MaterialTheme.colorScheme
                        .onPrimaryContainer,
            )

            Spacer(
                modifier =
                    Modifier.height(4.dp),
            )

            Text(
                text =
                    whenText,
                style =
                    MaterialTheme.typography
                        .bodyMedium,
                color =
                    MaterialTheme.colorScheme
                        .onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun SecondaryActivityCard(
    activity: CatalogActivity,
    whenText: String,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick =
                        onClick
                ),
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
        Column(
            modifier =
                Modifier.padding(18.dp),
        ) {
            Text(
                text =
                    activity.title,
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
                    whenText,
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

@Composable
private fun EmptyActivityCard(
    text: String,
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(18.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme
                        .surfaceVariant,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 0.dp
            ),
    ) {
        Text(
            modifier =
                Modifier.padding(18.dp),
            text = text,
            style =
                MaterialTheme.typography
                    .bodyLarge,
        )
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
            maxSeconds
        )
    } else {
        "≈ ${format(maxSeconds)}"
    }
}
