package org.sedentaryapp.ui.info

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

@Composable
fun WhyAppScreen(
    onBack: () -> Unit,
) {
    InfoScreen(
        title =
            "Pourquoi ces pauses ?",
        paragraphs =
            listOf(
                "Au travail, il est facile de rester assis longtemps sans y penser.",
                "Intervalles propose de courtes occasions de bouger pendant la journée de travail, aux horaires que tu choisis.",
                "L’objectif n’est pas de faire du sport ni de mesurer une performance. Chaque proposition reste une invitation : tu peux la faire, la reporter ou l’ignorer.",
                "Les textes scientifiques détaillés et leurs sources seront intégrés uniquement après validation du référentiel scientifique.",
            ),
        onBack =
            onBack,
    )
}

@Composable
fun AboutScreen(
    onBack: () -> Unit,
) {
    InfoScreen(
        title =
            "À propos",
        paragraphs =
            listOf(
                "Intervalles — V0",
                "Une application Android gratuite et open source conçue pour aider à interrompre la sédentarité au travail grâce à de courtes activités.",
                "Tes réglages restent sur ton appareil. Aucun compte n’est nécessaire et l’application fonctionne hors ligne.",
                "Intervalles est un outil de prévention et de soutien au quotidien. Il ne remplace pas un professionnel de santé.",
            ),
        onBack =
            onBack,
    )
}

@Composable
private fun InfoScreen(
    title: String,
    paragraphs: List<String>,
    onBack: () -> Unit,
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
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        horizontal = 20.dp,
                        vertical = 20.dp,
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

                Text(
                    text = title,
                    style =
                        MaterialTheme.typography
                            .headlineMedium,
                )
            }

            Spacer(
                modifier =
                    Modifier.height(24.dp),
            )

            paragraphs.forEachIndexed {
                    index,
                    paragraph,
                ->

                Text(
                    text = paragraph,
                    style =
                        MaterialTheme.typography
                            .bodyLarge,
                )

                if (
                    index <
                    paragraphs.lastIndex
                ) {
                    Spacer(
                        modifier =
                            Modifier.height(16.dp),
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
