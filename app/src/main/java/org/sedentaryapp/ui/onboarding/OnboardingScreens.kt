package org.sedentaryapp.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeScreen(
    onConfigure: () -> Unit,
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
                    .padding(
                        horizontal = 24.dp,
                        vertical = 40.dp,
                    ),
            verticalArrangement =
                Arrangement.Center,
            horizontalAlignment =
                Alignment.CenterHorizontally,
        ) {
            IntervallesLogoMark()

            Spacer(
                modifier =
                    Modifier.height(24.dp),
            )

            Text(
                text = "Intervalles",
                style =
                    MaterialTheme.typography
                        .headlineLarge,
                color =
                    MaterialTheme.colorScheme
                        .primary,
            )

            Spacer(
                modifier =
                    Modifier.height(24.dp),
            )

            Text(
                text =
                    "De petites pauses pour remettre du mouvement dans ta journée de travail.",
                style =
                    MaterialTheme.typography
                        .bodyLarge,
            )

            Spacer(
                modifier =
                    Modifier.height(38.dp),
            )

            Button(
                modifier =
                    Modifier.fillMaxWidth(),
                onClick =
                    onConfigure,
                shape =
                    RoundedCornerShape(14.dp),
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
        color =
            MaterialTheme.colorScheme.background,
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
                text =
                    "Rester informé",
                style =
                    MaterialTheme.typography
                        .headlineLarge,
            )

            Spacer(
                modifier =
                    Modifier.height(20.dp),
            )

            Text(
                text =
                    "Les notifications permettent de recevoir les propositions au moment prévu.",
                style =
                    MaterialTheme.typography
                        .bodyLarge,
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp),
            )

            Text(
                text =
                    "L’application fonctionne aussi sans elles.",
                style =
                    MaterialTheme.typography
                        .bodyLarge,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant,
            )

            Spacer(
                modifier =
                    Modifier.height(34.dp),
            )

            Button(
                modifier =
                    Modifier.fillMaxWidth(),
                onClick =
                    onAllowNotifications,
                shape =
                    RoundedCornerShape(14.dp),
            ) {
                Text(
                    if (
                        notificationsAlreadyAllowed
                    ) {
                        "Continuer"
                    } else {
                        "Autoriser les notifications"
                    }
                )
            }

            if (
                !notificationsAlreadyAllowed
            ) {
                TextButton(
                    modifier =
                        Modifier.fillMaxWidth(),
                    onClick =
                        onLater,
                ) {
                    Text("Pas maintenant")
                }
            }
        }
    }
}

@Composable
private fun IntervallesLogoMark() {
    Row(
        modifier =
            Modifier.height(64.dp),
        horizontalArrangement =
            Arrangement.spacedBy(8.dp),
        verticalAlignment =
            Alignment.CenterVertically,
    ) {
        LogoBar(
            height = 34.dp
        )

        LogoBar(
            height = 58.dp
        )

        LogoBar(
            height = 26.dp
        )
    }
}

@Composable
private fun LogoBar(
    height: androidx.compose.ui.unit.Dp,
) {
    Box(
        modifier =
            Modifier
                .size(
                    width = 10.dp,
                    height = height,
                )
                .background(
                    color =
                        MaterialTheme.colorScheme
                            .primary,
                    shape =
                        RoundedCornerShape(
                            999.dp
                        ),
                )
    )
}
