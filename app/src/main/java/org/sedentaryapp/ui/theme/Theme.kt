package org.sedentaryapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme =
    lightColorScheme(
        primary = IntervalPrimary,
        onPrimary = IntervalOnPrimary,
        primaryContainer = IntervalPrimaryContainer,
        onPrimaryContainer = IntervalOnPrimaryContainer,
        background = IntervalBackground,
        onBackground = IntervalOnSurface,
        surface = IntervalSurface,
        onSurface = IntervalOnSurface,
        surfaceVariant = IntervalSurfaceVariant,
        onSurfaceVariant = IntervalOnSurfaceVariant,
        outline = IntervalOutline,
        outlineVariant = IntervalOutlineVariant,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = IntervalDarkPrimary,
        onPrimary = IntervalDarkOnPrimary,
        primaryContainer = IntervalDarkPrimaryContainer,
        onPrimaryContainer = IntervalDarkOnPrimaryContainer,
        background = IntervalDarkBackground,
        onBackground = IntervalDarkOnSurface,
        surface = IntervalDarkSurface,
        onSurface = IntervalDarkOnSurface,
        surfaceVariant = IntervalDarkSurfaceVariant,
        onSurfaceVariant = IntervalDarkOnSurfaceVariant,
        outline = IntervalDarkOutline,
        outlineVariant = IntervalDarkOutlineVariant,
    )

@Composable
fun SedentaryAppTheme(
    darkTheme: Boolean =
        isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme =
            if (darkTheme) {
                DarkColorScheme
            } else {
                LightColorScheme
            },
        typography =
            SedentaryTypography,
        content =
            content,
    )
}
