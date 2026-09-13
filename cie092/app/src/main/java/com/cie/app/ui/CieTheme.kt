package com.cie.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CieColors = lightColorScheme(
    primary = Color(0xFF111318),
    onPrimary = Color.White,
    background = Color(0xFFF4F2ED),
    onBackground = Color(0xFF111318),
    surface = Color.White,
    onSurface = Color(0xFF111318),
    surfaceVariant = Color(0xFFEDEBE6),
    outline = Color(0xFFE0DDD6)
)

@Composable
fun CieTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CieColors, content = content)
}
