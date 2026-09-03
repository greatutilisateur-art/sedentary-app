package org.sedentaryapp.ui.activity

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import android.content.Context
import android.content.res.Configuration
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
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
    showAlreadyDone: Boolean,
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
    onMarkActivityDone: () -> Unit,
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
                showAlreadyDone =
                    showAlreadyDone,
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
                onMarkActivityDone =
                    onMarkActivityDone,
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
                showAlreadyDone =
                    showAlreadyDone,
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
                onMarkActivityDone =
                    onMarkActivityDone,
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
    showAlreadyDone: Boolean,
    guidedSessionState: GuidedSessionState,
    remainingSeconds: Int,
    onBack: () -> Unit,
    onPreviousActivity: () -> Unit,
    onNextActivity: () -> Unit,
    onOtherActivity: () -> Unit,
    onNotNow: () -> Unit,
    onStartGuidedSession: () -> Unit,
    onMarkActivityDone: () -> Unit,
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
                    showAlreadyDone =
                        showAlreadyDone,
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
                    onMarkActivityDone =
                        onMarkActivityDone,
                )

            GuidedSessionState.RUNNING ->
                GuidedContent(
                    activity =
                        activity,
                    postureLabel =
                        postureLabel,
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
                    postureLabel =
                        postureLabel,
                    remainingSeconds =
                        remainingSeconds,
                    paused = true,
                    onPrimary =
                        onResumeGuidedSession,
                    onEndGuidedSession =
                        onEndGuidedSession,
                )

            GuidedSessionState.TIME_ELAPSED -> {
                ActivityGuidanceContent(
                    activity =
                        activity,
                    postureLabel =
                        postureLabel,
                    imageHeight =
                        176.dp,
                    illustrationTopSpacing =
                        28.dp,
                    illustrationBottomSpacing =
                        16.dp,
                )

                FinishedContent(
                    onCloseGuidedSession =
                        onCloseGuidedSession,
                )
            }
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
    showAlreadyDone: Boolean,
    guidedSessionState: GuidedSessionState,
    remainingSeconds: Int,
    onBack: () -> Unit,
    onPreviousActivity: () -> Unit,
    onNextActivity: () -> Unit,
    onOtherActivity: () -> Unit,
    onNotNow: () -> Unit,
    onStartGuidedSession: () -> Unit,
    onMarkActivityDone: () -> Unit,
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
                                .verticalScroll(
                                    rememberScrollState()
                                )
                                .padding(
                                    end = 18.dp,
                                    bottom = 8.dp,
                                ),
                    ) {
                        ActivityGuidanceContent(
                            activity =
                                activity,
                            postureLabel =
                                postureLabel,
                            imageHeight =
                                148.dp,
                            illustrationTopSpacing =
                                14.dp,
                            illustrationBottomSpacing =
                                10.dp,
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
                                    onMarkActivityDone
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
                                    "Fait"
                                }
                            )
                        }

                        if (
                            activity.guidanceMode ==
                                ActivityGuidanceMode.TIMER &&
                            showAlreadyDone
                        ) {
                            TextButton(
                                modifier =
                                    Modifier.fillMaxWidth(),
                                onClick =
                                    onMarkActivityDone,
                            ) {
                                Text("Déjà fait")
                            }
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
                                .weight(1.15f)
                                .verticalScroll(
                                    rememberScrollState()
                                )
                                .padding(
                                    end = 18.dp,
                                    bottom = 8.dp,
                                ),
                    ) {
                        ActivityGuidanceContent(
                            activity =
                                activity,
                            postureLabel =
                                postureLabel,
                            imageHeight =
                                148.dp,
                            illustrationTopSpacing =
                                14.dp,
                            illustrationBottomSpacing =
                                10.dp,
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
                        GuidedControls(
                            activity =
                                activity,
                            remainingSeconds =
                                remainingSeconds,
                            paused =
                                paused,
                            timeFontSize =
                                40,
                            onPrimary =
                                if (paused) {
                                    onResumeGuidedSession
                                } else {
                                    onPauseGuidedSession
                                },
                            onEndGuidedSession =
                                onEndGuidedSession,
                        )
                    }
                }

                GuidedSessionState.TIME_ELAPSED -> {
                    Column(
                        modifier =
                            Modifier
                                .weight(1.15f)
                                .verticalScroll(
                                    rememberScrollState()
                                )
                                .padding(
                                    end = 18.dp,
                                    bottom = 8.dp,
                                ),
                    ) {
                        ActivityGuidanceContent(
                            activity =
                                activity,
                            postureLabel =
                                postureLabel,
                            imageHeight =
                                148.dp,
                            illustrationTopSpacing =
                                14.dp,
                            illustrationBottomSpacing =
                                10.dp,
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
                        FinishedContent(
                            onCloseGuidedSession =
                                onCloseGuidedSession,
                            timeFontSize =
                                40,
                        )
                    }
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
private fun ActivityIllustration(
    resourceName: String?,
    imageHeight: Dp = 176.dp,
    topSpacing: Dp = 0.dp,
    bottomSpacing: Dp = 16.dp,
) {
    if (resourceName.isNullOrBlank()) {
        return
    }

    val resourceId =
        when (resourceName) {
            "ill_mouv01" ->
                R.drawable.ill_mouv01

            "ill_mouv02" ->
                R.drawable.ill_mouv02

            "ill_mouv03" ->
                R.drawable.ill_mouv03

            "ill_mouv04" ->
                R.drawable.ill_mouv04

            "ill_mob01" ->
                R.drawable.ill_mob01

            "ill_mob02" ->
                R.drawable.ill_mob02

            "ill_mob03" ->
                R.drawable.ill_mob03

            "ill_mob04" ->
                R.drawable.ill_mob04

            "ill_mob05" ->
                R.drawable.ill_mob05

            "ill_mob06" ->
                R.drawable.ill_mob06

            "ill_etir01" ->
                R.drawable.ill_etir01

            "ill_etir02" ->
                R.drawable.ill_etir02

            "ill_etir03" ->
                R.drawable.ill_etir03

            "ill_dyn01" ->
                R.drawable.ill_dyn01

            "ill_dyn02" ->
                R.drawable.ill_dyn02

            "ill_dyn03" ->
                R.drawable.ill_dyn03

            "ill_dyn04" ->
                R.drawable.ill_dyn04

            "ill_bien01" ->
                R.drawable.ill_bien01

            "ill_bien02" ->
                R.drawable.ill_bien02

            "ill_vis01" ->
                R.drawable.ill_vis01

            "ill_vis02" ->
                R.drawable.ill_vis02

            "ill_start01" ->
                R.drawable.ill_start01

            "ill_start02" ->
                R.drawable.ill_start02

            else ->
                return
        }

    if (topSpacing > 0.dp) {
        Spacer(
            modifier =
                Modifier.height(
                    topSpacing
                ),
        )
    }

    Image(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(imageHeight),
        painter =
            painterResource(
                resourceId
            ),
        // La consigne textuelle décrit entièrement l'activité.
        // Éviter une annonce TalkBack redondante du type "Illustration de...".
        contentDescription = null,
        contentScale =
            ContentScale.Fit,
    )

    if (bottomSpacing > 0.dp) {
        Spacer(
            modifier =
                Modifier.height(
                    bottomSpacing
                ),
        )
    }
}

@Composable
private fun ActivityGuidanceContent(
    activity: CatalogActivity,
    postureLabel: String?,
    imageHeight: Dp,
    illustrationTopSpacing: Dp,
    illustrationBottomSpacing: Dp,
) {
    ActivityIdentity(
        activity =
            activity,
        postureLabel =
            postureLabel,
    )

    ActivityIllustration(
        resourceName =
            activity.illustrationResourceName,
        imageHeight =
            imageHeight,
        topSpacing =
            illustrationTopSpacing,
        bottomSpacing =
            illustrationBottomSpacing,
    )

    Text(
        text =
            activity.instruction,
        style =
            MaterialTheme.typography
                .bodyLarge,
    )
}

@Composable
private fun ReadyContent(
    activity: CatalogActivity,
    postureLabel: String?,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    canRequestAnother: Boolean,
    showNotNow: Boolean,
    showAlreadyDone: Boolean,
    onPreviousActivity: () -> Unit,
    onNextActivity: () -> Unit,
    onOtherActivity: () -> Unit,
    onNotNow: () -> Unit,
    onStartGuidedSession: () -> Unit,
    onMarkActivityDone: () -> Unit,
) {
    ActivityGuidanceContent(
        activity =
            activity,
        postureLabel =
            postureLabel,
        imageHeight =
            176.dp,
        illustrationTopSpacing =
            28.dp,
        illustrationBottomSpacing =
            16.dp,
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
                onMarkActivityDone
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
                "Fait"
            }
        )
    }

    if (
        activity.guidanceMode ==
            ActivityGuidanceMode.TIMER &&
        showAlreadyDone
    ) {
        Spacer(
            modifier =
                Modifier.height(6.dp),
        )

        TextButton(
            modifier =
                Modifier.fillMaxWidth(),
            onClick =
                onMarkActivityDone,
        ) {
            Text("Déjà fait")
        }
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
    postureLabel: String?,
    remainingSeconds: Int,
    paused: Boolean,
    onPrimary: () -> Unit,
    onEndGuidedSession: () -> Unit,
) {
    ActivityGuidanceContent(
        activity =
            activity,
        postureLabel =
            postureLabel,
        imageHeight =
            176.dp,
        illustrationTopSpacing =
            28.dp,
        illustrationBottomSpacing =
            16.dp,
    )

    Spacer(
        modifier =
            Modifier.height(24.dp),
    )

    GuidedControls(
        activity =
            activity,
        remainingSeconds =
            remainingSeconds,
        paused =
            paused,
        timeFontSize =
            42,
        onPrimary =
            onPrimary,
        onEndGuidedSession =
            onEndGuidedSession,
    )
}

@Composable
private fun GuidedControls(
    activity: CatalogActivity,
    remainingSeconds: Int,
    paused: Boolean,
    timeFontSize: Int,
    onPrimary: () -> Unit,
    onEndGuidedSession: () -> Unit,
) {
    CountdownCard(
        remainingSeconds =
            remainingSeconds,
        totalSeconds =
            activity.definition
                .durationSecondsMax,
        paused =
            paused,
        timeFontSize =
            timeFontSize,
    )

    Spacer(
        modifier =
            Modifier.height(14.dp),
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
private fun CountdownCard(
    remainingSeconds: Int,
    totalSeconds: Int,
    paused: Boolean,
    timeFontSize: Int,
    accessibilityLabel: String =
        countdownTalkBack(
            remainingSeconds
        ),
) {
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

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription =
                        accessibilityLabel
                },
        shape =
            RoundedCornerShape(18.dp),
        color =
            MaterialTheme.colorScheme.surface,
        border =
            BorderStroke(
                width = 1.dp,
                color =
                    MaterialTheme.colorScheme
                        .outlineVariant,
            ),
    ) {
        Column(
            modifier =
                Modifier.padding(
                    horizontal = 18.dp,
                    vertical = 16.dp,
                ),
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically,
            ) {
                Text(
                    text =
                        "%d:%02d".format(
                            remainingSeconds / 60,
                            remainingSeconds % 60,
                        ),
                    fontSize =
                        timeFontSize.sp,
                    lineHeight =
                        (timeFontSize + 6).sp,
                    fontWeight =
                        FontWeight.Medium,
                )

                Spacer(
                    modifier =
                        Modifier.weight(1f),
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
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp),
            )

            CountdownProgressBar(
                fraction =
                    fraction,
            )
        }
    }
}

@Composable
private fun CountdownProgressBar(
    fraction: Float,
) {
    val trackColor =
        MaterialTheme.colorScheme
            .outlineVariant

    val progressColor =
        MaterialTheme.colorScheme
            .primary

    Canvas(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(6.dp),
    ) {
        val y =
            size.height / 2f

        val stroke =
            size.height

        drawLine(
            color =
                trackColor,
            start =
                Offset(
                    x = 0f,
                    y = y,
                ),
            end =
                Offset(
                    x = size.width,
                    y = y,
                ),
            strokeWidth =
                stroke,
            cap =
                StrokeCap.Round,
        )

        val safeFraction =
            fraction.coerceIn(
                0f,
                1f,
            )

        if (safeFraction > 0f) {
            drawLine(
                color =
                    progressColor,
                start =
                    Offset(
                        x = 0f,
                        y = y,
                    ),
                end =
                    Offset(
                        x =
                            size.width *
                                safeFraction,
                        y = y,
                    ),
                strokeWidth =
                    stroke,
                cap =
                    StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun FinishedContent(
    onCloseGuidedSession: () -> Unit,
    timeFontSize: Int = 42,
) {
    Spacer(
        modifier =
            Modifier.height(24.dp),
    )

    CountdownCard(
        remainingSeconds =
            0,
        totalSeconds =
            1,
        paused =
            false,
        timeFontSize =
            timeFontSize,
        accessibilityLabel =
            "Chronomètre terminé, zéro minute zéro seconde",
    )

    Spacer(
        modifier =
            Modifier.height(20.dp),
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
