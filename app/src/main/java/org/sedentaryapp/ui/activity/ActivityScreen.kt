package org.sedentaryapp.ui.activity

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sedentaryapp.data.CatalogActivity

@Composable
fun ActivityScreen(
    activity: CatalogActivity,
    postureLabel: String?,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    canRequestAnother: Boolean,
    showNotNow: Boolean,
    guidedSessionState: GuidedSessionState,
    remainingSeconds: Int,
    shouldPlayCompletionHaptic: Boolean,
    onPreviousActivity: () -> Unit,
    onNextActivity: () -> Unit,
    onOtherActivity: () -> Unit,
    onNotNow: () -> Unit,
    onStartGuidedSession: () -> Unit,
    onPauseGuidedSession: () -> Unit,
    onResumeGuidedSession: () -> Unit,
    onEndGuidedSession: () -> Unit,
    onCloseGuidedSession: () -> Unit,
    onCompletionHapticPlayed: () -> Unit,
) {
    val view =
        LocalView.current

    LaunchedEffect(
        guidedSessionState,
        shouldPlayCompletionHaptic,
    ) {
        if (
            guidedSessionState ==
            GuidedSessionState.TIME_ELAPSED &&
            shouldPlayCompletionHaptic
        ) {
            val feedbackType =
                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.R
                ) {
                    HapticFeedbackConstants.CONFIRM
                } else {
                    HapticFeedbackConstants.CLOCK_TICK
                }

            view.performHapticFeedback(
                feedbackType
            )

            onCompletionHapticPlayed()
        }
    }

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
                    .padding(
                        horizontal = 20.dp,
                        vertical = 28.dp,
                    ),
        ) {
            when (
                guidedSessionState
            ) {
                GuidedSessionState.READY -> {
                    ReadyContent(
                        activity =
                            activity,
                        postureLabel =
                            postureLabel,
                        canGoPrevious =
                            canGoPrevious,
                        canGoNext =
                            canGoNext,
                        canRequestAnother =
                            canRequestAnother,
                        showNotNow =
                            showNotNow,
                        onPreviousActivity =
                            onPreviousActivity,
                        onNextActivity =
                            onNextActivity,
                        onOtherActivity =
                            onOtherActivity,
                        onNotNow =
                            onNotNow,
                        onStartGuidedSession =
                            onStartGuidedSession,
                    )
                }

                GuidedSessionState.RUNNING -> {
                    GuidedContent(
                        activity =
                            activity,
                        remainingSeconds =
                            remainingSeconds,
                        statusLabel =
                            null,
                        primaryLabel =
                            "Pause",
                        onPrimary =
                            onPauseGuidedSession,
                        onEndGuidedSession =
                            onEndGuidedSession,
                    )
                }

                GuidedSessionState.PAUSED -> {
                    GuidedContent(
                        activity =
                            activity,
                        remainingSeconds =
                            remainingSeconds,
                        statusLabel =
                            "En pause",
                        primaryLabel =
                            "Reprendre",
                        onPrimary =
                            onResumeGuidedSession,
                        onEndGuidedSession =
                            onEndGuidedSession,
                    )
                }

                GuidedSessionState.TIME_ELAPSED -> {
                    FinishedContent(
                        activity =
                            activity,
                        onCloseGuidedSession =
                            onCloseGuidedSession,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadyContent(
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
    onStartGuidedSession: () -> Unit,
) {
    postureLabel?.let {
        Surface(
            shape =
                RoundedCornerShape(999.dp),
            color =
                MaterialTheme.colorScheme
                    .secondaryContainer,
        ) {
            Text(
                modifier =
                    Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 7.dp,
                    ),
                text = it,
                style =
                    MaterialTheme.typography
                        .labelLarge,
                color =
                    MaterialTheme.colorScheme
                        .onSecondaryContainer,
            )
        }

        Spacer(
            modifier =
                Modifier.height(18.dp),
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
            Modifier.height(10.dp),
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

    Spacer(
        modifier =
            Modifier.height(24.dp),
    )

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(22.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme
                        .surfaceVariant,
            ),
    ) {
        Text(
            modifier =
                Modifier.padding(20.dp),
            text =
                activity.instruction,
            style =
                MaterialTheme.typography
                    .bodyLarge,
        )
    }

    Spacer(
        modifier =
            Modifier.height(26.dp),
    )

    Button(
        modifier =
            Modifier.fillMaxWidth(),
        onClick =
            onStartGuidedSession,
    ) {
        Text("C’est parti")
    }

    if (
        canGoPrevious ||
        canGoNext ||
        canRequestAnother
    ) {
        Spacer(
            modifier =
                Modifier.height(12.dp),
        )

        Column(
            modifier =
                Modifier.fillMaxWidth(),
        ) {
            if (canGoPrevious) {
                OutlinedButton(
                    modifier =
                        Modifier.fillMaxWidth(),
                    onClick =
                        onPreviousActivity,
                ) {
                    Text("Précédent")
                }

                if (
                    canGoNext ||
                    canRequestAnother
                ) {
                    Spacer(
                        modifier =
                            Modifier.height(8.dp),
                    )
                }
            }

            if (canGoNext) {
                OutlinedButton(
                    modifier =
                        Modifier.fillMaxWidth(),
                    onClick =
                        onNextActivity,
                ) {
                    Text("Suivant")
                }
            } else if (
                canRequestAnother
            ) {
                OutlinedButton(
                    modifier =
                        Modifier.fillMaxWidth(),
                    onClick =
                        onOtherActivity,
                ) {
                    Text("Autre activité")
                }
            }
        }
    }

    if (showNotNow) {
        Spacer(
            modifier =
                Modifier.height(8.dp),
        )

        TextButton(
            modifier =
                Modifier.fillMaxWidth(),
            onClick =
                onNotNow,
        ) {
            Text("Pas maintenant")
        }
    }
}

@Composable
private fun GuidedContent(
    activity: CatalogActivity,
    remainingSeconds: Int,
    statusLabel: String?,
    primaryLabel: String,
    onPrimary: () -> Unit,
    onEndGuidedSession: () -> Unit,
) {
    Text(
        text =
            activity.title,
        style =
            MaterialTheme.typography
                .titleLarge,
    )

    Spacer(
        modifier =
            Modifier.height(30.dp),
    )

    Countdown(
        seconds =
            remainingSeconds,
    )

    if (statusLabel != null) {
        Spacer(
            modifier =
                Modifier.height(8.dp),
        )

        Text(
            modifier =
                Modifier.fillMaxWidth(),
            text =
                statusLabel,
            style =
                MaterialTheme.typography
                    .titleMedium,
            color =
                MaterialTheme.colorScheme
                    .primary,
        )
    }

    Spacer(
        modifier =
            Modifier.height(28.dp),
    )

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(22.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme
                        .surfaceVariant,
            ),
    ) {
        Text(
            modifier =
                Modifier.padding(20.dp),
            text =
                activity.instruction,
            style =
                MaterialTheme.typography
                    .bodyLarge,
        )
    }

    Spacer(
        modifier =
            Modifier.height(28.dp),
    )

    Button(
        modifier =
            Modifier.fillMaxWidth(),
        onClick =
            onPrimary,
    ) {
        Text(primaryLabel)
    }

    TextButton(
        modifier =
            Modifier.fillMaxWidth(),
        onClick =
            onEndGuidedSession,
    ) {
        Text("Terminer")
    }
}

@Composable
private fun FinishedContent(
    activity: CatalogActivity,
    onCloseGuidedSession: () -> Unit,
) {
    Text(
        text =
            activity.title,
        style =
            MaterialTheme.typography
                .titleLarge,
    )

    Spacer(
        modifier =
            Modifier.height(30.dp),
    )

    Countdown(
        seconds = 0,
    )

    Spacer(
        modifier =
            Modifier.height(18.dp),
    )

    Text(
        text = "C’est fini !",
        style =
            MaterialTheme.typography
                .headlineMedium,
        color =
            MaterialTheme.colorScheme
                .primary,
    )

    Spacer(
        modifier =
            Modifier.height(28.dp),
    )

    Button(
        modifier =
            Modifier.fillMaxWidth(),
        onClick =
            onCloseGuidedSession,
    ) {
        Text("Fermer")
    }
}

@Composable
private fun Countdown(
    seconds: Int,
) {
    Text(
        modifier =
            Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription =
                        countdownTalkBack(
                            seconds
                        )
                },
        text =
            "%d:%02d".format(
                seconds / 60,
                seconds % 60,
            ),
        fontSize = 64.sp,
        lineHeight = 72.sp,
        fontWeight =
            FontWeight.SemiBold,
        color =
            MaterialTheme.colorScheme
                .primary,
    )
}

private fun countdownTalkBack(
    seconds: Int,
): String {

    val minutes =
        seconds / 60

    val remainingSeconds =
        seconds % 60

    return when {
        minutes == 0 ->
            "$remainingSeconds secondes restantes"

        remainingSeconds == 0 ->
            if (minutes == 1) {
                "1 minute restante"
            } else {
                "$minutes minutes restantes"
            }

        else ->
            "$minutes minutes et $remainingSeconds secondes restantes"
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