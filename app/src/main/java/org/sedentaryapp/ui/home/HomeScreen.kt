package org.sedentaryapp.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
    lastActivity: CatalogActivity?,
    scheduleStatusText: String,
    onOpenLastActivity: () -> Unit,
    onOpenSettings: () -> Unit,
    onSimulateProposal: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
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

            Text(
                text = scheduleStatusText,
                style =
                    MaterialTheme.typography
                        .bodyLarge,
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp),
            )

            TextButton(
                onClick = onOpenSettings,
            ) {
                Text(
                    "Modifier mes réglages"
                )
            }

            if (lastActivity != null) {
                Spacer(
                    modifier =
                        Modifier.height(28.dp),
                )

                Text(
                    text =
                        "Dernière activité proposée",
                    style =
                        MaterialTheme.typography
                            .titleMedium,
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp),
                )

                Card(
                    modifier = Modifier
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
                                MaterialTheme
                                    .typography
                                    .titleLarge,
                        )

                        Spacer(
                            modifier =
                                Modifier.height(8.dp),
                        )

                        Text(
                            text =
                                "Voir l'activité",
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.weight(1f),
            )

            Text(
                text = "Test développeur",
                style =
                    MaterialTheme.typography
                        .labelSmall,
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp),
            )

            OutlinedButton(
                modifier =
                    Modifier.fillMaxWidth(),
                onClick =
                    onSimulateProposal,
            ) {
                Text(
                    "Simuler une proposition"
                )
            }
        }
    }
}