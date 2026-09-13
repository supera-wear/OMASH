package com.cie.app.ui

import androidx.compose.ui.graphics.Color

internal val Ink = Color(0xFF111318)
internal val Muted = Color(0xFF767D86)
internal val Green = Color(0xFF178A55)
internal val Blue = Color(0xFFB9E5FF)
internal val Background = Color(0xFFF4F2ED)
internal val Border = Color(0xFFE3E1DC)

internal enum class CieTab(val label: String, val glyph: String) {
    Shield("Shield", "◈"),
    Blocked("Blocked", "⊘"),
    Activity("Activity", "◴")
}

internal data class Company(
    val name: String,
    val category: String,
    val blocked: Boolean
)
