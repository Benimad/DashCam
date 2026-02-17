package com.example.dashcam.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DashCamDarkColorScheme = darkColorScheme(
    primary = DeepBlueLight,
    onPrimary = White,
    primaryContainer = DeepBlueDark,
    onPrimaryContainer = VeryLightGrey,
    
    secondary = LightGrey,
    onSecondary = DarkCharcoal,
    secondaryContainer = MediumGrey,
    onSecondaryContainer = VeryLightGrey,
    
    tertiary = RecordingRedLight,
    onTertiary = White,
    tertiaryContainer = RecordingRedDark,
    onTertiaryContainer = VeryLightGrey,
    
    error = RecordingRed,
    onError = White,
    errorContainer = RecordingRedDark,
    onErrorContainer = VeryLightGrey,
    
    background = DarkCharcoal,
    onBackground = White,
    surface = DarkGrey,
    onSurface = White,
    surfaceVariant = MediumGrey,
    onSurfaceVariant = LightGrey,
    
    outline = LightGrey,
    outlineVariant = MediumGrey,
    
    inverseSurface = White,
    inverseOnSurface = DarkCharcoal,
    inversePrimary = DeepBlue,
    
    surfaceTint = DeepBlueLight,
    scrim = Color.Black.copy(alpha = 0.5f)
)

private val DashCamLightColorScheme = lightColorScheme(
    primary = DeepBlue,
    onPrimary = White,
    primaryContainer = DeepBlueLight,
    onPrimaryContainer = White,
    
    secondary = MediumGrey,
    onSecondary = White,
    secondaryContainer = LightGrey,
    onSecondaryContainer = DarkCharcoal,
    
    tertiary = RecordingRed,
    onTertiary = White,
    tertiaryContainer = RecordingRedLight,
    onTertiaryContainer = White,
    
    error = RecordingRedDark,
    onError = White,
    errorContainer = RecordingRedLight,
    onErrorContainer = White,
    
    background = VeryLightGrey,
    onBackground = DarkCharcoal,
    surface = White,
    onSurface = DarkCharcoal,
    surfaceVariant = LightGrey,
    onSurfaceVariant = MediumGrey,
    
    outline = MediumGrey,
    outlineVariant = LightGrey,
    
    inverseSurface = DarkCharcoal,
    inverseOnSurface = White,
    inversePrimary = DeepBlueLight,
    
    surfaceTint = DeepBlue,
    scrim = Color.Black.copy(alpha = 0.3f)
)

@Composable
fun DashCamTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DashCamDarkColorScheme else DashCamLightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkCharcoal.toArgb()
            window.navigationBarColor = DarkCharcoal.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DashCamTypography,
        shapes = DashCamShapes,
        content = content
    )
}
