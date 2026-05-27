package com.example.srokikd.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AppDarkScheme = darkColorScheme(
    primary = AppPrimary,
    onPrimary = AppTextPrimary,
    secondary = AppPrimaryMuted,
    background = AppBackground,
    onBackground = AppTextPrimary,
    surface = AppSurface,
    onSurface = AppTextPrimary,
    outline = AppOutline,
    surfaceVariant = AppSurfaceElevated,
    onSurfaceVariant = AppTextSecondary,
)

@Composable
fun SrokiKDTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AppBackgroundSecondary.toArgb()
            window.navigationBarColor = AppBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = AppDarkScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
