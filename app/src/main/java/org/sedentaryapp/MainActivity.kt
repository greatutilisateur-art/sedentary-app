package org.sedentaryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.sedentaryapp.data.CatalogActivity
import org.sedentaryapp.data.CatalogRepository
import org.sedentaryapp.engine.decision.DecisionEngine
import org.sedentaryapp.engine.model.CandidateFilterRequest
import org.sedentaryapp.engine.model.DecisionPolicy
import org.sedentaryapp.engine.model.RepetitionCoefficients
import org.sedentaryapp.engine.model.ScoringPolicy
import org.sedentaryapp.ui.activity.ActivityScreen
import org.sedentaryapp.ui.theme.SedentaryAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val catalogRepository = CatalogRepository(this)
        val activities = catalogRepository.loadActivities()

        setContent {
            SedentaryAppTheme {

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

                fun selectActivity(
                    excludedIds: Set<String>,
                    attempt: Int,
                ): CatalogActivity? {

                    val selected =
                        DecisionEngine.chooseActivity(
                            activities = definitions,
                            history = emptyList(),
                            policy = policy,
                            seed = "activity-screen-$attempt",
                            filterRequest = CandidateFilterRequest(
                                opportunityType = "standard",
                                excludedActivityIds = excludedIds,
                            ),
                        )

                    return activities.firstOrNull {
                        it.definition.id == selected?.id
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
                        selectActivity(
                            excludedIds = emptySet(),
                            attempt = 0,
                        )
                    )
                }

                currentActivity?.let { activity ->

                    val excludedForNext =
                        shownIds + activity.definition.id

                    val canShowAlternative =
                        definitions.any {
                            "standard" in it.eligibleOpportunityTypes &&
                                    it.id !in excludedForNext
                        }

                    ActivityScreen(
                        activity = activity,
                        postureLabel = postureLabel(activity),
                        hasAlternative = canShowAlternative,

                        onOtherActivity = {

                            val newShownIds =
                                shownIds + activity.definition.id

                            val newAttempt =
                                attempt + 1

                            currentActivity =
                                selectActivity(
                                    excludedIds = newShownIds,
                                    attempt = newAttempt,
                                )

                            shownIds = newShownIds
                            attempt = newAttempt
                        },

                        onNotNow = {
                            finish()
                        },
                    )
                }
            }
        }
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