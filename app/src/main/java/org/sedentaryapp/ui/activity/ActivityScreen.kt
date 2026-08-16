package org.sedentaryapp.ui.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sedentaryapp.data.CatalogActivity

@Composable
fun ActivityScreen(
    activity: CatalogActivity,
    postureLabel: String?,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    canRequestAnother: Boolean,
    showNotNow: Boolean,
    onPreviousActivity: () -> Unit,
    onNextActivity: () -> Unit,
    onOtherActivity: () -> Unit,
    onNotNow: () -> Unit,
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
                        vertical = 32.dp,
                    ),
            verticalArrangement =
                Arrangement.Center,
        ) {

            postureLabel?.let {

                Text(
                    text = it,
                    style =
                        MaterialTheme.typography
                            .labelLarge,
                    color =
                        MaterialTheme.colorScheme
                            .primary,
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp),
                )
            }

            Text(
                text =
                    activity.title,
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
                    activity.instruction,
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
                        .bodyMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant,
            )

            if (
                canGoPrevious ||
                canGoNext ||
                canRequestAnother
            ) {

                Spacer(
                    modifier =
                        Modifier.height(32.dp),
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically,
                ) {

                    if (canGoPrevious) {
                        TextButton(
                            onClick =
                                onPreviousActivity,
                        ) {
                            Text(
                                "← Précédente"
                            )
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.weight(1f),
                    )

                    when {
                        canGoNext -> {
                            TextButton(
                                onClick =
                                    onNextActivity,
                            ) {
                                Text(
                                    "Suivante →"
                                )
                            }
                        }

                        canRequestAnother -> {
                            Button(
                                onClick =
                                    onOtherActivity,
                            ) {
                                Text(
                                    "Autre activité"
                                )
                            }
                        }
                    }
                }
            }

            if (showNotNow) {

                Spacer(
                    modifier =
                        Modifier.height(12.dp),
                )

                TextButton(
                    modifier =
                        Modifier.fillMaxWidth(),
                    onClick =
                        onNotNow,
                ) {
                    Text(
                        "Pas maintenant"
                    )
                }
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