package org.sedentaryapp.ui.activity

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sedentaryapp.R
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
    onBack: () -> Unit,
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
                    Modifier.height(8.dp),
            )

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
                        paused = false,
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
                        paused = true,
                        onPrimary =
                            onResumeGuidedSession,
                        onEndGuidedSession =
                            onEndGuidedSession,
                    )
                }

                GuidedSessionState.TIME_ELAPSED -> {
                    FinishedContent(
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
    Text(
        text =
            activity.title,
        style =
            MaterialTheme.typography
                .titleLarge,
    )

    Spacer(
        modifier =
            Modifier.height(6.dp),
    )

    Text(
        text =
            buildList {
                add(
                    formatDuration(
                        minSeconds =
                            activity.definition
                                .durationSecondsMin,
                        maxSeconds =
                            activity.definition
                                .durationSecondsMax,
                    )
                )

                postureLabel?.let {
                    add(it)
                }
            }.joinToString(
                separator = " · "
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
            Modifier.height(28.dp),
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
            Modifier.height(32.dp),
    )

    FilledTonalButton(
        modifier =
            Modifier.fillMaxWidth(),
        onClick =
            onStartGuidedSession,
        shape =
            RoundedCornerShape(14.dp),
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

        AlternativeButtons(
            canGoPrevious =
                canGoPrevious,
            canGoNext =
                canGoNext,
            canRequestAnother =
                canRequestAnother,
            onPreviousActivity =
                onPreviousActivity,
            onNextActivity =
                onNextActivity,
            onOtherActivity =
                onOtherActivity,
        )
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
private fun AlternativeButtons(
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    canRequestAnother: Boolean,
    onPreviousActivity: () -> Unit,
    onNextActivity: () -> Unit,
    onOtherActivity: () -> Unit,
) {
    val fontScale =
        LocalDensity.current
            .fontScale

    if (fontScale >= 1.5f) {
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
                    shape =
                        RoundedCornerShape(14.dp),
                ) {
                    Text("Précédent")
                }

                Spacer(
                    modifier =
                        Modifier.height(8.dp),
                )
            }

            if (canGoNext) {
                OutlinedButton(
                    modifier =
                        Modifier.fillMaxWidth(),
                    onClick =
                        onNextActivity,
                    shape =
                        RoundedCornerShape(14.dp),
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
                    shape =
                        RoundedCornerShape(14.dp),
                ) {
                    Text("Autre activité")
                }
            }
        }
    } else {
        Row(
            modifier =
                Modifier.fillMaxWidth(),
        ) {
            if (canGoPrevious) {
                OutlinedButton(
                    modifier =
                        Modifier.weight(1f),
                    onClick =
                        onPreviousActivity,
                    shape =
                        RoundedCornerShape(14.dp),
                ) {
                    Text("Précédent")
                }

                Spacer(
                    modifier =
                        Modifier.size(8.dp),
                )
            }

            if (canGoNext) {
                OutlinedButton(
                    modifier =
                        Modifier.weight(1f),
                    onClick =
                        onNextActivity,
                    shape =
                        RoundedCornerShape(14.dp),
                ) {
                    Text("Suivant")
                }
            } else if (
                canRequestAnother
            ) {
                OutlinedButton(
                    modifier =
                        Modifier.weight(1f),
                    onClick =
                        onOtherActivity,
                    shape =
                        RoundedCornerShape(14.dp),
                ) {
                    Text("Autre activité")
                }
            }
        }
    }
}

@Composable
private fun GuidedContent(
    activity: CatalogActivity,
    remainingSeconds: Int,
    paused: Boolean,
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

    CircularCountdown(
        remainingSeconds =
            remainingSeconds,
        totalSeconds =
            activity.definition
                .durationSecondsMax,
    )

    if (paused) {
        Spacer(
            modifier =
                Modifier.height(10.dp),
        )

        Text(
            modifier =
                Modifier.fillMaxWidth(),
            text = "En pause",
            style =
                MaterialTheme.typography
                    .bodyMedium,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant,
        )
    }

    Spacer(
        modifier =
            Modifier.height(30.dp),
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
            Modifier.height(32.dp),
    )

    FilledTonalButton(
        modifier =
            Modifier.fillMaxWidth(),
        onClick =
            onPrimary,
        shape =
            RoundedCornerShape(14.dp),
    ) {
        Text(
            if (paused) {
                "Reprendre"
            } else {
                "Pause"
            }
        )
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
private fun CircularCountdown(
    remainingSeconds: Int,
    totalSeconds: Int,
) {
    val trackColor =
        MaterialTheme.colorScheme
            .outlineVariant

    val progressColor =
        MaterialTheme.colorScheme
            .primary

    val safeTotal =
        totalSeconds
            .coerceAtLeast(1)

    val fraction =
        (
            remainingSeconds
                .coerceIn(
                    0,
                    safeTotal,
                )
                .toFloat() /
                safeTotal.toFloat()
        )

    Box(
        modifier =
            Modifier
                .fillMaxWidth(),
        contentAlignment =
            Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(184.dp)
                    .semantics {
                        contentDescription =
                            countdownTalkBack(
                                remainingSeconds
                            )
                    },
            contentAlignment =
                Alignment.Center,
        ) {
            Canvas(
                modifier =
                    Modifier.fillMaxSize(),
            ) {
                val stroke =
                    5.dp.toPx()

                val inset =
                    stroke / 2f

                val arcSize =
                    Size(
                        width =
                            size.width -
                                stroke,
                        height =
                            size.height -
                                stroke,
                    )

                drawArc(
                    color =
                        trackColor,
                    startAngle =
                        -90f,
                    sweepAngle =
                        360f,
                    useCenter =
                        false,
                    topLeft =
                        Offset(
                            inset,
                            inset,
                        ),
                    size =
                        arcSize,
                    style =
                        Stroke(
                            width =
                                stroke,
                            cap =
                                StrokeCap.Round,
                        ),
                )

                drawArc(
                    color =
                        progressColor,
                    startAngle =
                        -90f,
                    sweepAngle =
                        360f *
                            fraction,
                    useCenter =
                        false,
                    topLeft =
                        Offset(
                            inset,
                            inset,
                        ),
                    size =
                        arcSize,
                    style =
                        Stroke(
                            width =
                                stroke,
                            cap =
                                StrokeCap.Round,
                        ),
                )
            }

            Text(
                text =
                    "%d:%02d".format(
                        remainingSeconds / 60,
                        remainingSeconds % 60,
                    ),
                fontSize = 56.sp,
                lineHeight = 64.sp,
                fontWeight =
                    FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun FinishedContent(
    onCloseGuidedSession: () -> Unit,
) {
    Spacer(
        modifier =
            Modifier.height(48.dp),
    )

    Text(
        modifier =
            Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription =
                        "Chronomètre terminé, zéro minute zéro seconde"
                },
        text = "0:00",
        fontSize = 56.sp,
        lineHeight = 64.sp,
        fontWeight =
            FontWeight.Medium,
    )

    Spacer(
        modifier =
            Modifier.height(36.dp),
    )

    Button(
        modifier =
            Modifier.fillMaxWidth(),
        onClick =
            onCloseGuidedSession,
        shape =
            RoundedCornerShape(14.dp),
    ) {
        Text("C’est fini !")
    }
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
