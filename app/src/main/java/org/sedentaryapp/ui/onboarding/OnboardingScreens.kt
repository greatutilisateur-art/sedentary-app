package org.sedentaryapp.ui.onboarding

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeScreen(
    onConfigure: () -> Unit,
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
                        horizontal = 24.dp,
                        vertical = 40.dp,
                    ),
        ) {
            Text(
                text = "Bienvenue",
                style =
                    MaterialTheme.typography
                        .headlineMedium,
            )

            Spacer(
                modifier =
                    Modifier.height(14.dp),
            )

            Text(
                text =
                    "De courtes activités pour interrompre les longues périodes assises au travail.",
                style =
                    MaterialTheme.typography
                        .bodyLarge,
            )

            Spacer(
                modifier =
                    Modifier.height(24.dp),
            )

            Card(
                modifier =
                    Modifier.fillMaxWidth(),
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
                        text = "30 s à 5 min",
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
                            "Pas de performance à atteindre. Tu gardes la main sur chaque proposition.",
                        style =
                            MaterialTheme.typography
                                .bodyLarge,
                        color =
                            MaterialTheme.colorScheme
                                .onPrimaryContainer,
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(30.dp),
            )

            Button(
                modifier =
                    Modifier.fillMaxWidth(),
                onClick =
                    onConfigure,
            ) {
                Text("Configurer")
            }
        }
    }
}

@Composable
fun NotificationOnboardingScreen(
    notificationsAlreadyAllowed: Boolean,
    onAllowNotifications: () -> Unit,
    onLater: () -> Unit,
) {
    PermissionStep(
        title =
            "Recevoir les propositions",
        body =
            "Pour recevoir une activité aux horaires choisis, l’application a besoin de l’autorisation d’afficher des notifications.",
        detail =
            "Tu pourras modifier ce choix plus tard dans les réglages Android.",
        alreadyAllowed =
            notificationsAlreadyAllowed,
        allowLabel =
            "Autoriser les notifications",
        onAllow =
            onAllowNotifications,
        onLater =
            onLater,
    )
}

@Composable
private fun PermissionStep(
    title: String,
    body: String,
    detail: String,
    alreadyAllowed: Boolean,
    allowLabel: String,
    onAllow: () -> Unit,
    onLater: () -> Unit,
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
                        horizontal = 24.dp,
                        vertical = 40.dp,
                    ),
        ) {
            Text(
                text = title,
                style =
                    MaterialTheme.typography
                        .headlineMedium,
            )

            Spacer(
                modifier =
                    Modifier.height(18.dp),
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
            ) {
                Text(
                    modifier =
                        Modifier.padding(16.dp),
                    text = detail,
                    style =
                        MaterialTheme.typography
                            .bodyMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,
                )
            }

            Spacer(
                modifier =
                    Modifier.height(30.dp),
            )

            Button(
                modifier =
                    Modifier.fillMaxWidth(),
                onClick =
                    onAllow,
            ) {
                Text(
                    if (alreadyAllowed) {
                        "Continuer"
                    } else {
                        allowLabel
                    }
                )
            }

            if (!alreadyAllowed) {
                TextButton(
                    modifier =
                        Modifier.fillMaxWidth(),
                    onClick =
                        onLater,
                ) {
                    Text("Plus tard")
                }
            }
        }
    }
}
