package com.jhamburg.plantgurucompose.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat

private val LightColorScheme = lightColorScheme(
    primary = Green80,
    secondary = Sage80,
    tertiary = Color(0xFFD4966A),
    background = Color(0xFFFFFDF7),
    surface = Color(0xFFF5F3E8),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1A1C18),
    onSurface = Color(0xFF1A1C18),
    primaryContainer = Green80,
    secondaryContainer = Sage80,
    tertiaryContainer = Color(0xFFD4966A),
    surfaceVariant = Color(0xFFFFFDF7),
    onSurfaceVariant = Color(0xFF1A1C18),
    onPrimaryContainer = Color.White,
    onSecondaryContainer = Color.White,
    onTertiaryContainer = Color.White,
)

private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = LogoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 76.sp,
        lineHeight = 56.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = RalewayFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
).copy(
    displayLarge = Typography.displayLarge.copy(fontFamily = LogoFont),
    displayMedium = Typography.displayMedium.copy(fontFamily = RalewayFont),
    displaySmall = Typography.displaySmall.copy(fontFamily = RalewayFont),
    headlineLarge = Typography.headlineLarge.copy(fontFamily = RalewayFont),
    headlineMedium = Typography.headlineMedium.copy(fontFamily = RalewayFont),
    headlineSmall = Typography.headlineSmall.copy(fontFamily = RalewayFont),
    titleLarge = Typography.titleLarge.copy(fontFamily = RalewayFont),
    titleMedium = Typography.titleMedium.copy(fontFamily = RalewayFont),
    titleSmall = Typography.titleSmall.copy(fontFamily = RalewayFont),
    bodyLarge = Typography.bodyLarge.copy(fontFamily = RalewayFont),
    bodyMedium = Typography.bodyMedium.copy(fontFamily = RalewayFont),
    bodySmall = Typography.bodySmall.copy(fontFamily = RalewayFont),
    labelLarge = Typography.labelLarge.copy(fontFamily = RalewayFont),
    labelMedium = Typography.labelMedium.copy(fontFamily = RalewayFont),
    labelSmall = Typography.labelSmall.copy(fontFamily = RalewayFont)
)

@Composable
fun PlantGuruComposeTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor = colorScheme.surface.toArgb()
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}