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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
                    .safeDrawingPadding()
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
fun PermissionsOnboardingScreen(
    notificationsAllowed: Boolean,
    exactAlarmsAllowed: Boolean,
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    onSoundChanged: (Boolean) -> Unit,
    onVibrationChanged: (Boolean) -> Unit,
    onConfigurePermissions: () -> Unit,
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
                    .safeDrawingPadding()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        horizontal = 24.dp,
                        vertical = 32.dp,
                    ),
        ) {
            Text(
                text =
                    "Recevoir les propositions",
                style =
                    MaterialTheme.typography
                        .headlineLarge,
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp),
            )

            Text(
                text =
                    "Intervalles utilise deux autorisations Android pour afficher les propositions au moment choisi.",
                style =
                    MaterialTheme.typography
                        .bodyLarge,
            )

            Spacer(
                modifier =
                    Modifier.height(20.dp),
            )

            PermissionStatusRow(
                label =
                    "Notifications",
                allowed =
                    notificationsAllowed,
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp),
            )

            PermissionStatusRow(
                label =
                    "Horaires précis",
                allowed =
                    exactAlarmsAllowed,
            )

            Spacer(
                modifier =
                    Modifier.height(28.dp),
            )

            Text(
                text = "Alerte",
                style =
                    MaterialTheme.typography
                        .titleMedium,
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp),
            )

            PreferenceSwitchRow(
                title = "Son",
                checked =
                    soundEnabled,
                onCheckedChange =
                    onSoundChanged,
            )

            PreferenceSwitchRow(
                title = "Vibration",
                checked =
                    vibrationEnabled,
                onCheckedChange =
                    onVibrationChanged,
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp),
            )

            Text(
                text =
                    "Tu pourras modifier ces choix plus tard dans les paramètres.",
                style =
                    MaterialTheme.typography
                        .bodySmall,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant,
            )

            Spacer(
                modifier =
                    Modifier.height(28.dp),
            )

            Button(
                modifier =
                    Modifier.fillMaxWidth(),
                onClick =
                    onConfigurePermissions,
                shape =
                    RoundedCornerShape(14.dp),
            ) {
                Text(
                    if (
                        notificationsAllowed &&
                        exactAlarmsAllowed
                    ) {
                        "Continuer"
                    } else {
                        "Configurer les autorisations"
                    }
                )
            }

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

@Composable
private fun PermissionStatusRow(
    label: String,
    allowed: Boolean,
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
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                text =
                    if (allowed) {
                        "Autorisée"
                    } else {
                        "À autoriser"
                    },
                style =
                    MaterialTheme.typography
                        .bodyMedium,
                color =
                    if (allowed) {
                        MaterialTheme
                            .colorScheme
                            .primary
                    } else {
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                    },
            )
        }
    }
}

@Composable
private fun PreferenceSwitchRow(
    title: String,
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
        Text(
            modifier =
                Modifier.weight(1f),
            text = title,
            style =
                MaterialTheme.typography
                    .bodyLarge,
        )

        Switch(
            checked =
                checked,
            onCheckedChange =
                onCheckedChange,
        )
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
