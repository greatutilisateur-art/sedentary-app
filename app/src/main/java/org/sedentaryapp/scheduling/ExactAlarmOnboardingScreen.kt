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
fun ExactAlarmOnboardingScreen(
    exactAlarmsAlreadyAllowed: Boolean,
    onAllowExactAlarms: () -> Unit,
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
                text = "Respecter tes horaires",
                style =
                    MaterialTheme.typography
                        .headlineMedium,
            )

            Spacer(
                modifier =
                    Modifier.height(18.dp),
            )

            Text(
                text =
                    "Pour déclencher les propositions au plus près des horaires choisis, Android demande l’accès « Alarmes et rappels ».",
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
                    text =
                        "L’application programme uniquement la prochaine proposition et ne reste pas active en arrière-plan.",
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
                    onAllowExactAlarms,
            ) {
                Text(
                    if (exactAlarmsAlreadyAllowed) {
                        "Continuer"
                    } else {
                        "Autoriser les horaires précis"
                    }
                )
            }

            if (!exactAlarmsAlreadyAllowed) {
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
