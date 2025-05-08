package com.example.wear_momentum.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

private val DarkGreenColorScheme = Colors(
    primary = Color(0xFFC6FF00),
    onPrimary = Color.Black,
    secondary = Color(0xFF76FF03),
    onSecondary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF1A1A1A),
    onSurface = Color.White,
    error = Color.Red,
    onError = Color.White,
)

@Composable
fun MomentumTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = DarkGreenColorScheme,
        content = content
    )
}
