package org.sedentaryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import org.sedentaryapp.data.CatalogActivity
import org.sedentaryapp.data.CatalogRepository
import org.sedentaryapp.engine.model.DecisionPolicy
import org.sedentaryapp.engine.model.RepetitionCoefficients
import org.sedentaryapp.engine.model.ScoringPolicy
import org.sedentaryapp.ui.activity.ActivityScreen
import org.sedentaryapp.ui.activity.ActivityViewModel
import org.sedentaryapp.ui.home.HomeScreen
import org.sedentaryapp.ui.theme.SedentaryAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)

        val activities =
            CatalogRepository(this)
                .loadActivities()

        val policy =
            DecisionPolicy(
                scoring =
                    ScoringPolicy(
                        minWeight = 0.05,
                        maxWeight = 20.0,
                        historyWindowProposals = 4,
                        repetitionCoefficients =
                            RepetitionCoefficients(
                                sameAsPreviousActivity =
                                    0.15,
                                sameActivityInRecentWindow =
                                    0.55,
                                noRepetition =
                                    1.0,
                            ),
                    )
            )

        val factory =
            ActivityViewModel.factory(
                activities = activities,
                policy = policy,
            )

        val viewModel =
            ViewModelProvider(
                this,
                factory,
            )[ActivityViewModel::class.java]

        setContent {
            SedentaryAppTheme {
                val uiState by
                viewModel.uiState
                    .collectAsState()

                var showActivity by
                rememberSaveable {
                    mutableStateOf(false)
                }

                val activity =
                    uiState.activity

                if (
                    showActivity &&
                    activity != null
                ) {
                    BackHandler {
                        showActivity = false
                    }

                    ActivityScreen(
                        activity = activity,
                        postureLabel =
                            postureLabel(activity),
                        isActiveProposal =
                            uiState.isActiveProposal,
                        hasAlternative =
                            uiState.hasAlternative,
                        onOtherActivity =
                            viewModel::onOtherActivity,
                        onNotNow = {
                            viewModel.onNotNow()
                            showActivity = false
                        },
                    )
                } else {
                    HomeScreen(
                        lastActivity = activity,
                        onOpenLastActivity = {
                            showActivity = true
                        },
                        onSimulateProposal = {
                            viewModel
                                .startDemoProposal()

                            showActivity = true
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
        activity.definition
            .standingRequired ->
            "Debout nécessaire"

        activity.preferredPosture ==
                "standing" ->
            "Debout conseillé"

        else ->
            null
    }
}