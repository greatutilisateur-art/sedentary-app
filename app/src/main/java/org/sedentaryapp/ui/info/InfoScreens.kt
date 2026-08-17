package org.sedentaryapp.ui.info

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WhyAppScreen(
    onBack: () -> Unit,
) {
    InfoScreen(
        title =
            "Pourquoi cette application ?",
        paragraphs =
            listOf(
                "Au travail, il est facile de rester assis longtemps sans y penser.",
                "L’application propose, aux horaires que tu choisis, de courtes occasions de bouger de 30 secondes à 5 minutes.",
                "L’objectif n’est pas de faire du sport ni de compter des performances. Chaque proposition reste une invitation : tu peux la faire, la reporter ou l’ignorer.",
                "Le produit privilégie des interruptions simples, peu contraignantes et sans culpabilisation.",
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
                "Application Android gratuite et open source, conçue pour fonctionner entièrement hors ligne.",
                "Aucun compte n’est nécessaire. Les réglages et le petit historique utilisé pour varier les propositions restent sur l’appareil.",
                "Version V0 en développement.",
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
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(24.dp),
        ) {
            Text(
                text = title,
                style =
                    MaterialTheme.typography
                        .headlineMedium,
            )

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
                    Modifier.height(32.dp),
            )

            TextButton(
                modifier =
                    Modifier.fillMaxWidth(),
                onClick =
                    onBack,
            ) {
                Text("Fermer")
            }
        }
    }
}