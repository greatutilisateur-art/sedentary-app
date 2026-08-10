package org.sedentaryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import org.sedentaryapp.data.CatalogActivity
import org.sedentaryapp.data.CatalogRepository
import org.sedentaryapp.engine.model.DecisionPolicy
import org.sedentaryapp.engine.model.RepetitionCoefficients
import org.sedentaryapp.engine.model.ScoringPolicy
import org.sedentaryapp.ui.activity.ActivityScreen
import org.sedentaryapp.ui.activity.ActivityViewModel
import org.sedentaryapp.ui.activity.NoActivityScreen
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

                val activity =
                    uiState.activity

                if (activity == null) {

                    NoActivityScreen()

                } else {

                    ActivityScreen(
                        activity = activity,
                        postureLabel =
                            postureLabel(
                                activity
                            ),
                        hasAlternative =
                            uiState.hasAlternative,
                        onOtherActivity =
                            viewModel::onOtherActivity,
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