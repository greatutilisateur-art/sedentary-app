package org.sedentaryapp.ui.catalog

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.sedentaryapp.R
import org.sedentaryapp.data.ActivityGuidanceMode
import org.sedentaryapp.data.CatalogActivity

@Composable
fun CatalogScreen(
    activities: List<CatalogActivity>,
    onActivitySelected: (String) -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(
        onBack =
            onBack,
    )

    val grouped =
        activities
            .filter {
                it.definition.status ==
                    "published"
            }
            .groupBy {
                it.definition.userCategory
            }

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
                        vertical = 24.dp,
                    ),
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick =
                        onBack,
                ) {
                    Icon(
                        painter =
                            painterResource(
                                R.drawable.ic_arrow_back
                            ),
                        contentDescription =
                            "Retour",
                    )
                }

                Spacer(
                    modifier =
                        Modifier.size(4.dp),
                )

                Text(
                    text =
                        "Catalogue d’activités",
                    style =
                        MaterialTheme.typography
                            .headlineSmall,
                )
            }

            Spacer(
                modifier =
                    Modifier.height(8.dp),
            )

            Text(
                text =
                    "Choisis directement une activité.",
                style =
                    MaterialTheme.typography
                        .bodyMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant,
            )

            Spacer(
                modifier =
                    Modifier.height(22.dp),
            )

            CATEGORY_ORDER
                .forEach { category ->
                    val categoryActivities =
                        grouped[category]
                            .orEmpty()

                    if (
                        categoryActivities.isEmpty()
                    ) {
                        return@forEach
                    }

                    Text(
                        text =
                            categoryLabel(
                                category
                            ),
                        style =
                            MaterialTheme.typography
                                .titleMedium,
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp),
                    )

                    categoryActivities
                        .forEachIndexed {
                                index,
                                activity,
                            ->

                            CatalogActivityCard(
                                activity =
                                    activity,
                                onClick = {
                                    onActivitySelected(
                                        activity
                                            .definition
                                            .id
                                    )
                                },
                            )

                            if (
                                index !=
                                categoryActivities
                                    .lastIndex
                            ) {
                                Spacer(
                                    modifier =
                                        Modifier.height(8.dp),
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
}

@Composable
private fun CatalogActivityCard(
    activity: CatalogActivity,
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
                Modifier.padding(16.dp),
        ) {
            Text(
                text =
                    activity.title,
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
                    buildString {
                        append(
                            durationLabel(
                                activity
                            )
                        )

                        append(" · ")

                        append(
                            if (
                                activity
                                    .guidanceMode ==
                                ActivityGuidanceMode
                                    .TIMER
                            ) {
                                "Guidée"
                            } else {
                                "Ponctuelle"
                            }
                        )
                    },
                style =
                    MaterialTheme.typography
                        .bodySmall,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant,
            )
        }
    }
}

private fun durationLabel(
    activity: CatalogActivity,
): String {
    val minSeconds =
        activity.definition
            .durationSecondsMin

    val maxSeconds =
        activity.definition
            .durationSecondsMax

    return if (
        minSeconds ==
        maxSeconds
    ) {
        formatDuration(
            minSeconds
        )
    } else {
        "${formatDuration(minSeconds)} – ${formatDuration(maxSeconds)}"
    }
}

private fun formatDuration(
    seconds: Int,
): String {
    if (seconds < 60) {
        return "$seconds s"
    }

    if (
        seconds % 60 == 0
    ) {
        val minutes =
            seconds / 60

        return if (
            minutes == 1
        ) {
            "1 min"
        } else {
            "$minutes min"
        }
    }

    val minutes =
        seconds / 60

    val remainingSeconds =
        seconds % 60

    return "${minutes} min ${remainingSeconds} s"
}

private fun categoryLabel(
    category: String,
): String =
    when (category) {
        "mouvement" ->
            "Marcher et bouger"

        "mobilite" ->
            "Mobilité"

        "etirement" ->
            "Étirements"

        "dynamique" ->
            "Plus dynamique"

        "bien_etre" ->
            "Respiration et relâchement"

        "visuel" ->
            "Pause visuelle"

        else ->
            category
                .replaceFirstChar {
                    it.uppercase()
                }
    }

private val CATEGORY_ORDER =
    listOf(
        "mouvement",
        "mobilite",
        "etirement",
        "dynamique",
        "bien_etre",
        "visuel",
    )
