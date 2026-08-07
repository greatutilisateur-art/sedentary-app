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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sedentaryapp.engine.decision.DecisionEngine
import org.sedentaryapp.engine.model.ActivityDefinition
import org.sedentaryapp.engine.model.CandidateFilterRequest
import org.sedentaryapp.engine.model.DecisionPolicy
import org.sedentaryapp.engine.model.RepetitionCoefficients
import org.sedentaryapp.engine.model.ScoringPolicy
import org.sedentaryapp.ui.theme.SedentaryAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SedentaryAppTheme {
                EngineDemoScreen()
            }
        }
    }
}

@Composable
fun EngineDemoScreen() {

    val activities = remember {
        listOf(
            ActivityDefinition(
                id = "A",
                status = "published",
                userCategory = "mouvement",
                internalFamily = "walking",
                intensity = "gentle",
                durationSecondsMin = 120,
                durationSecondsMax = 300,
                standingRequired = true,
                walkingRequired = true,
                eligibleOpportunityTypes = setOf("standard"),
                baseWeight = 1.0,
            ),
            ActivityDefinition(
                id = "B",
                status = "published",
                userCategory = "mobilite",
                internalFamily = "shoulders",
                intensity = "gentle",
                durationSecondsMin = 30,
                durationSecondsMax = 60,
                standingRequired = false,
                walkingRequired = false,
                eligibleOpportunityTypes = setOf("standard"),
                baseWeight = 1.0,
            ),
            ActivityDefinition(
                id = "C",
                status = "published",
                userCategory = "dynamique",
                internalFamily = "squats",
                intensity = "active",
                durationSecondsMin = 30,
                durationSecondsMax = 60,
                standingRequired = true,
                walkingRequired = false,
                eligibleOpportunityTypes = setOf("standard"),
                baseWeight = 1.0,
            ),
        )
    }

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

    fun chooseActivity(
        excludedIds: Set<String>,
        attempt: Int,
    ): ActivityDefinition? {
        return DecisionEngine.chooseActivity(
            activities = activities,
            history = emptyList(),
            policy = policy,
            seed = "demo-$attempt",
            filterRequest = CandidateFilterRequest(
                excludedActivityIds = excludedIds,
            ),
        )
    }

    var shownIds by remember {
        mutableStateOf(emptySet<String>())
    }

    var attempt by remember {
        mutableStateOf(0)
    }

    var currentActivity by remember {
        mutableStateOf(chooseActivity(emptySet(), 0))
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

        Text(
            modifier = Modifier.padding(top = 32.dp),
            text = when (currentActivity?.id) {
                "A" -> "Une petite marche ?"
                "B" -> "Quelques cercles d’épaules ?"
                "C" -> "Quelques squats tranquilles ?"
                else -> "Aucune autre activité"
            },
            style = MaterialTheme.typography.titleLarge,
        )

        currentActivity?.let { activity ->

            Text(
                modifier = Modifier.padding(top = 12.dp),
                text = "${activity.durationSecondsMin}–${activity.durationSecondsMax} secondes",
            )

            val excludedForNext = shownIds + activity.id

            if (excludedForNext.size < activities.size) {

                Button(
                    modifier = Modifier.padding(top = 32.dp),
                    onClick = {
                        val newShownIds = shownIds + activity.id
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
        }
    }
}