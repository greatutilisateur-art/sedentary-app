package org.sedentaryapp.ui.activity

import android.content.Context
import android.content.res.Configuration
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sedentaryapp.R
import org.sedentaryapp.data.ActivityGuidanceMode
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
    completionVibrationEnabled: Boolean,
    onBack: () -> Unit,
    onPreviousActivity: () -> Unit,
    onNextActivity: () -> Unit,
    onOtherActivity: () -> Unit,
    onNotNow: () -> Unit,
    onStartGuidedSession: () -> Unit,
    onCompleteSimpleActivity: () -> Unit,
    onPauseGuidedSession: () -> Unit,
    onResumeGuidedSession: () -> Unit,
    onEndGuidedSession: () -> Unit,
    onCloseGuidedSession: () -> Unit,
    onCompletionHapticPlayed: () -> Unit,
) {
    val context =
        LocalContext.current

    val configuration =
        LocalConfiguration.current

    val fontScale =
        LocalDensity.current.fontScale

    val compactLandscape =
        configuration.orientation ==
            Configuration.ORIENTATION_LANDSCAPE &&
            fontScale < 1.5f

    LaunchedEffect(
        guidedSessionState,
        shouldPlayCompletionHaptic,
        completionVibrationEnabled,
    ) {
        if (
            guidedSessionState ==
                GuidedSessionState.TIME_ELAPSED &&
            shouldPlayCompletionHaptic
        ) {
            if (completionVibrationEnabled) {
                vibrateCompletion(
                    context
                )
            }

            // Marqué comme traité même si l'utilisateur a désactivé la vibration,
            // pour ne jamais la rejouer plus tard dans la même session.
            onCompletionHapticPlayed()
        }
    }

    Surface(
        modifier =
            Modifier.fillMaxSize(),
        color =
            MaterialTheme.colorScheme.background,
    ) {
        if (compactLandscape) {
            LandscapeActivityContent(
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
                guidedSessionState =
                    guidedSessionState,
                remainingSeconds =
                    remainingSeconds,
                onBack =
                    onBack,
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
                onCompleteSimpleActivity =
                    onCompleteSimpleActivity,
                onPauseGuidedSession =
                    onPauseGuidedSession,
                onResumeGuidedSession =
                    onResumeGuidedSession,
                onEndGuidedSession =
                    onEndGuidedSession,
                onCloseGuidedSession =
                    onCloseGuidedSession,
            )
        } else {
            PortraitActivityContent(
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
                guidedSessionState =
                    guidedSessionState,
                remainingSeconds =
                    remainingSeconds,
                onBack =
                    onBack,
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
                onCompleteSimpleActivity =
                    onCompleteSimpleActivity,
                onPauseGuidedSession =
                    onPauseGuidedSession,
                onResumeGuidedSession =
                    onResumeGuidedSession,
                onEndGuidedSession =
                    onEndGuidedSession,
                onCloseGuidedSession =
                    onCloseGuidedSession,
            )
        }
    }
}

@Composable
private fun PortraitActivityContent(
    activity: CatalogActivity,
    postureLabel: String?,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    canRequestAnother: Boolean,
    showNotNow: Boolean,
    guidedSessionState: GuidedSessionState,
    remainingSeconds: Int,
    onBack: () -> Unit,
    onPreviousActivity: () -> Unit,
    onNextActivity: () -> Unit,
    onOtherActivity: () -> Unit,
    onNotNow: () -> Unit,
    onStartGuidedSession: () -> Unit,
    onCompleteSimpleActivity: () -> Unit,
    onPauseGuidedSession: () -> Unit,
    onResumeGuidedSession: () -> Unit,
    onEndGuidedSession: () -> Unit,
    onCloseGuidedSession: () -> Unit,
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
                    vertical = 16.dp,
                ),
    ) {
        BackButton(
            onBack =
                onBack
        )

        Spacer(
            modifier =
                Modifier.height(8.dp),
        )

        when (guidedSessionState) {
            GuidedSessionState.READY ->
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
                    onCompleteSimpleActivity =
                        onCompleteSimpleActivity,
                )

            GuidedSessionState.RUNNING ->
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

            GuidedSessionState.PAUSED ->
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

            GuidedSessionState.TIME_ELAPSED ->
                FinishedContent(
                    diameter = 184.dp,
                    onCloseGuidedSession =
                        onCloseGuidedSession,
                )
        }
    }
}

@Composable
private fun LandscapeActivityContent(
    activity: CatalogActivity,
    postureLabel: String?,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    canRequestAnother: Boolean,
    showNotNow: Boolean,
    guidedSessionState: GuidedSessionState,
    remainingSeconds: Int,
    onBack: () -> Unit,
    onPreviousActivity: () -> Unit,
    onNextActivity: () -> Unit,
    onOtherActivity: () -> Unit,
    onNotNow: () -> Unit,
    onStartGuidedSession: () -> Unit,
    onCompleteSimpleActivity: () -> Unit,
    onPauseGuidedSession: () -> Unit,
    onResumeGuidedSession: () -> Unit,
    onEndGuidedSession: () -> Unit,
    onCloseGuidedSession: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),
    ) {
        BackButton(
            onBack =
                onBack
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
        ) {
            when (guidedSessionState) {
                GuidedSessionState.READY -> {
                    Column(
                        modifier =
                            Modifier
                                .weight(1.15f)
                                .padding(
                                    end = 18.dp
                                ),
                    ) {
                        ActivityIdentity(
                            activity =
                                activity,
                            postureLabel =
                                postureLabel,
                        )

                        Spacer(
                            modifier =
                                Modifier.height(14.dp),
                        )

                        Text(
                            text =
                                activity.instruction,
                            style =
                                MaterialTheme.typography
                                    .bodyLarge,
                        )
                    }

                    Column(
                        modifier =
                            Modifier
                                .weight(0.85f)
                                .align(
                                    Alignment.CenterVertically
                                ),
                    ) {
                        FilledTonalButton(
                            modifier =
                                Modifier.fillMaxWidth(),
                            onClick =
                                if (
                                    activity.guidanceMode ==
                                    ActivityGuidanceMode.TIMER
                                ) {
                                    onStartGuidedSession
                                } else {
                                    onCompleteSimpleActivity
                                },
                            shape =
                                RoundedCornerShape(14.dp),
                        ) {
                            Text(
                                if (
                                    activity.guidanceMode ==
                                    ActivityGuidanceMode.TIMER
                                ) {
                                    "Commencer"
                                } else {
                                    "Commencer"
                                }
                            )
                        }

                        if (
                            canGoPrevious ||
                            canGoNext ||
                            canRequestAnother
                        ) {
                            Spacer(
                                modifier =
                                    Modifier.height(8.dp),
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
                                forceVertical = true,
                            )
                        }

                        if (showNotNow) {
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
                }

                GuidedSessionState.RUNNING,
                GuidedSessionState.PAUSED -> {
                    val paused =
                        guidedSessionState ==
                            GuidedSessionState.PAUSED

                    Column(
                        modifier =
                            Modifier
                                .weight(1f)
                                .padding(
                                    end = 18.dp
                                ),
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
                                Modifier.height(14.dp),
                        )

                        Text(
                            text =
                                activity.instruction,
                            style =
                                MaterialTheme.typography
                                    .bodyLarge,
                        )
                    }

                    Column(
                        modifier =
                            Modifier
                                .weight(1f)
                                .align(
                                    Alignment.CenterVertically
                                ),
                        horizontalAlignment =
                            Alignment.CenterHorizontally,
                    ) {
                        CircularCountdown(
                            remainingSeconds =
                                remainingSeconds,
                            totalSeconds =
                                activity.definition
                                    .durationSecondsMax,
                            diameter =
                                142.dp,
                            timeFontSize =
                                42,
                        )

                        if (paused) {
                            Text(
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
                                Modifier.height(8.dp),
                        )

                        FilledTonalButton(
                            modifier =
                                Modifier.fillMaxWidth(),
                            onClick =
                                if (paused) {
                                    onResumeGuidedSession
                                } else {
                                    onPauseGuidedSession
                                },
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
                }

                GuidedSessionState.TIME_ELAPSED -> {
                    Spacer(
                        modifier =
                            Modifier.weight(0.2f),
                    )

                    Column(
                        modifier =
                            Modifier
                                .weight(0.8f)
                                .align(
                                    Alignment.CenterVertically
                                ),
                        horizontalAlignment =
                            Alignment.CenterHorizontally,
                    ) {
                        FinishedCountdown(
                            diameter =
                                142.dp,
                            timeFontSize =
                                42,
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.weight(0.12f),
                    )

                    Button(
                        modifier =
                            Modifier
                                .weight(0.8f)
                                .align(
                                    Alignment.CenterVertically
                                ),
                        onClick =
                            onCloseGuidedSession,
                        shape =
                            RoundedCornerShape(14.dp),
                    ) {
                        Text("C’est fini !")
                    }

                    Spacer(
                        modifier =
                            Modifier.weight(0.2f),
                    )
                }
            }
        }
    }
}

@Composable
private fun BackButton(
    onBack: () -> Unit,
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
    onCompleteSimpleActivity: () -> Unit,
) {
    ActivityIdentity(
        activity =
            activity,
        postureLabel =
            postureLabel,
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
            if (
                activity.guidanceMode ==
                ActivityGuidanceMode.TIMER
            ) {
                onStartGuidedSession
            } else {
                onCompleteSimpleActivity
            },
        shape =
            RoundedCornerShape(14.dp),
    ) {
        Text(
            if (
                activity.guidanceMode ==
                ActivityGuidanceMode.TIMER
            ) {
                "Commencer"
            } else {
                "Commencer"
            }
        )
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
private fun ActivityIdentity(
    activity: CatalogActivity,
    postureLabel: String?,
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
                    formatGuidedDuration(
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
}

@Composable
private fun AlternativeButtons(
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    canRequestAnother: Boolean,
    onPreviousActivity: () -> Unit,
    onNextActivity: () -> Unit,
    onOtherActivity: () -> Unit,
    forceVertical: Boolean = false,
) {
    val fontScale =
        LocalDensity.current
            .fontScale

    if (
        forceVertical ||
        fontScale >= 1.5f
    ) {
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
        diameter =
            184.dp,
        timeFontSize =
            56,
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
    diameter: Dp,
    timeFontSize: Int,
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
            Modifier.fillMaxWidth(),
        contentAlignment =
            Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(diameter)
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

                // Sens horaire : la progression "tombe" vers la droite.
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
                fontSize =
                    timeFontSize.sp,
                lineHeight =
                    (timeFontSize + 8).sp,
                fontWeight =
                    FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun FinishedContent(
    diameter: Dp,
    onCloseGuidedSession: () -> Unit,
) {
    Spacer(
        modifier =
            Modifier.height(30.dp),
    )

    FinishedCountdown(
        diameter =
            diameter,
        timeFontSize =
            56,
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

@Composable
private fun FinishedCountdown(
    diameter: Dp,
    timeFontSize: Int,
) {
    val trackColor =
        MaterialTheme.colorScheme
            .outlineVariant

    Box(
        modifier =
            Modifier.fillMaxWidth(),
        contentAlignment =
            Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(diameter)
                    .semantics {
                        contentDescription =
                            "Chronomètre terminé, zéro minute zéro seconde"
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
                        Size(
                            width =
                                size.width -
                                    stroke,
                            height =
                                size.height -
                                    stroke,
                        ),
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
                text = "0:00",
                fontSize =
                    timeFontSize.sp,
                lineHeight =
                    (timeFontSize + 8).sp,
                fontWeight =
                    FontWeight.Medium,
            )
        }
    }
}

private fun vibrateCompletion(
    context: Context,
) {
    val vibrator =
        context.getSystemService(
            Vibrator::class.java
        ) ?: return

    if (!vibrator.hasVibrator()) {
        return
    }

    vibrator.vibrate(
        VibrationEffect.createOneShot(
            55L,
            VibrationEffect.DEFAULT_AMPLITUDE,
        )
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

private fun formatGuidedDuration(
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
            maxSeconds
        )
    } else {
        "≈ ${format(maxSeconds)}"
    }
}
