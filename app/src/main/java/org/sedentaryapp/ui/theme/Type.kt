package org.sedentaryapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val SedentaryTypography =
    Typography(
        headlineMedium =
            TextStyle(
                fontFamily =
                    FontFamily.Default,
                fontWeight =
                    FontWeight.SemiBold,
                fontSize = 30.sp,
                lineHeight = 36.sp,
                letterSpacing = (-0.3).sp,
            ),
        titleLarge =
            TextStyle(
                fontFamily =
                    FontFamily.Default,
                fontWeight =
                    FontWeight.SemiBold,
                fontSize = 21.sp,
                lineHeight = 27.sp,
            ),
        titleMedium =
            TextStyle(
                fontFamily =
                    FontFamily.Default,
                fontWeight =
                    FontWeight.SemiBold,
                fontSize = 17.sp,
                lineHeight = 23.sp,
            ),
        bodyLarge =
            TextStyle(
                fontFamily =
                    FontFamily.Default,
                fontWeight =
                    FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily =
                    FontFamily.Default,
                fontWeight =
                    FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
        bodySmall =
            TextStyle(
                fontFamily =
                    FontFamily.Default,
                fontWeight =
                    FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 17.sp,
            ),
        labelLarge =
            TextStyle(
                fontFamily =
                    FontFamily.Default,
                fontWeight =
                    FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
    )
