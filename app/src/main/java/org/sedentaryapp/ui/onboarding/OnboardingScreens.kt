package org.sedentaryapp.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
                    .padding(
                        horizontal = 24.dp,
                        vertical = 48.dp,
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
                    Modifier.height(20.dp),
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
                    Modifier.height(12.dp),
            )

            Text(
                text =
                    "Tu gardes la main : tu peux ignorer une proposition ou mettre l’application en pause à tout moment.",
                style =
                    MaterialTheme.typography
                        .bodyLarge,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant,
            )

            Spacer(
                modifier =
                    Modifier.weight(1f),
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
                        vertical = 48.dp,
                    ),
        ) {
            Text(
                text = "Recevoir les propositions",
                style =
                    MaterialTheme.typography
                        .headlineMedium,
            )

            Spacer(
                modifier =
                    Modifier.height(20.dp),
            )

            Text(
                text =
                    "Pour recevoir une activité aux horaires choisis, l’application a besoin de l’autorisation d’afficher des notifications.",
                style =
                    MaterialTheme.typography
                        .bodyLarge,
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp),
            )

            Text(
                text =
                    "Tu pourras modifier ce choix plus tard dans les réglages Android.",
                style =
                    MaterialTheme.typography
                        .bodyMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant,
            )

            Spacer(
                modifier =
                    Modifier.weight(1f),
            )

            Button(
                modifier =
                    Modifier.fillMaxWidth(),
                onClick =
                    onAllowNotifications,
            ) {
                Text(
                    if (notificationsAlreadyAllowed) {
                        "Continuer"
                    } else {
                        "Autoriser les notifications"
                    }
                )
            }

            if (!notificationsAlreadyAllowed) {
                Spacer(
                    modifier =
                        Modifier.height(8.dp),
                )

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