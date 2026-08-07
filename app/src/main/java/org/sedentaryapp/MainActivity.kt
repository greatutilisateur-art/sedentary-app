package org.sedentaryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sedentaryapp.data.CatalogActivity
import org.sedentaryapp.data.CatalogRepository
import org.sedentaryapp.engine.decision.DecisionEngine
import org.sedentaryapp.engine.model.CandidateFilterRequest
import org.sedentaryapp.engine.model.DecisionPolicy
import org.sedentaryapp.engine.model.RepetitionCoefficients
import org.sedentaryapp.engine.model.ScoringPolicy
import org.sedentaryapp.ui.theme.SedentaryAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val catalogRepository = CatalogRepository(this)
        val activities = catalogRepository.loadActivities()

        setContent {
            SedentaryAppTheme {
                RealCatalogScreen(
                    activities = activities,
                )
            }
        }
    }
}

@Composable
fun RealCatalogScreen(
    activities: List<CatalogActivity>,
) {
    val policy = remember {
        DecisionPolicy(
            scoring = ScoringPolicy(
                minWeight = 0.05,
                maxWeight = 20.0,
                historyWindowProposals = 4,
                repetitionCoefficients = RepetitionCoefficients(
                    sameAsPreviousActivity = 0.15,
                    sameActivityInRecentWindow = 0.55,
                    noRepetition = 1.0,
                ),
            )
        )
    }

    val definitions = remember(activities) {
        activities.map { it.definition }
    }

    fun chooseActivity(
        excludedIds: Set<String>,
        attempt: Int,
    ): CatalogActivity? {
        val selectedDefinition = DecisionEngine.chooseActivity(
            activities = definitions,
            history = emptyList(),
            policy = policy,
            seed = "real-catalog-demo-$attempt",
            filterRequest = CandidateFilterRequest(
                opportunityType = "standard",
                excludedActivityIds = excludedIds,
            ),
        )

        return activities.firstOrNull {
            it.definition.id == selectedDefinition?.id
        }
    }

    var shownIds by remember {
        mutableStateOf(emptySet<String>())
    }

    var attempt by remember {
        mutableIntStateOf(0)
    }

    var currentActivity by remember {
        mutableStateOf(
            chooseActivity(
                excludedIds = emptySet(),
                attempt = 0,
            )
        )
    }

    val availableStandardActivities = remember(activities) {
        activities.filter {
            "standard" in it.definition.eligibleOpportunityTypes
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            text = "Mes pauses",
            style = MaterialTheme.typography.headlineMedium,
        )

        currentActivity?.let { activity ->

            Text(
                modifier = Modifier.padding(top = 32.dp),
                text = activity.title,
                style = MaterialTheme.typography.titleLarge,
            )

            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = activity.instruction,
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = formatDuration(
                    minSeconds = activity.definition.durationSecondsMin,
                    maxSeconds = activity.definition.durationSecondsMax,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )

            postureLabel(activity)?.let { label ->
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            val excludedForNext =
                shownIds + activity.definition.id

            val alternativesRemain =
                availableStandardActivities.any {
                    it.definition.id !in excludedForNext
                }

            if (alternativesRemain) {
                Button(
                    modifier = Modifier.padding(top = 32.dp),
                    onClick = {
                        val newShownIds =
                            shownIds + activity.definition.id

                        val newAttempt = attempt + 1

                        currentActivity = chooseActivity(
                            excludedIds = newShownIds,
                            attempt = newAttempt,
                        )

                        shownIds = newShownIds
                        attempt = newAttempt
                    }
                ) {
                    Text("Autre activité")
                }
            }
        } ?: Text(
            text = "Aucune activité ne correspond actuellement à ces réglages.",
        )
    }
}

private fun formatDuration(
    minSeconds: Int,
    maxSeconds: Int,
): String {
    fun value(seconds: Int): String {
        return when {
            seconds < 60 -> "$seconds s"
            seconds % 60 == 0 -> "${seconds / 60} min"
            else -> "${seconds / 60} min ${seconds % 60} s"
        }
    }

    return if (minSeconds == maxSeconds) {
        value(minSeconds)
    } else {
        "${value(minSeconds)} – ${value(maxSeconds)}"
    }
}

private fun postureLabel(
    activity: CatalogActivity,
): String? {
    return when {
        activity.definition.standingRequired ->
            "Debout nécessaire"

        activity.preferredPosture == "standing" ->
            "Debout conseillé"

        else ->
            null
    }
}