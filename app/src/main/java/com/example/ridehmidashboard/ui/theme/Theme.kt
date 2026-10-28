package com.example.ridehmidashboard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// The dashboard is always dark, like a vehicle display, so there is no light scheme.
private val DashboardColorScheme = darkColorScheme(
    primary = AccentOrange,
    background = DashboardBackground,
    surface = CardSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = StatusRed
)

@Composable
fun RideHmiDashboardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DashboardColorScheme,
        typography = Typography,
        content = content
    )
}
