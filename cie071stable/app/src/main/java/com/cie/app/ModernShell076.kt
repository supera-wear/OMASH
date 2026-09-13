package com.cie.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal object Cie076Colors {
    val Background = Color(0xFFF7F7F5)
    val Card = Color.White
    val Text = Color(0xFF111318)
    val Muted = Color(0xFF747983)
    val Border = Color(0xFFE6E8EC)
    val Blue = Color(0xFF1469D2)
    val BlueDeep = Color(0xFF123B9E)
    val BlueLight = Color(0xFF5DB2FF)
    val BlueSoft = Color(0xFFEAF3FF)
    val Green = Color(0xFF20B96A)
    val Red = Color(0xFFC64848)
    val Track = Color(0xFFE8EAED)
}

@Composable
internal fun Cie076Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Cie076Colors.Blue,
            onPrimary = Color.White,
            background = Cie076Colors.Background,
            surface = Cie076Colors.Card,
            onBackground = Cie076Colors.Text,
            onSurface = Cie076Colors.Text,
            outline = Cie076Colors.Border
        ),
        content = content
    )
}

@Composable
internal fun CieWhiteWordmark() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("cie", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-2.3).sp)
        Spacer(Modifier.width(12.dp))
        Box(Modifier.width(1.dp).height(38.dp).background(Color.White.copy(alpha = 0.35f)))
        Spacer(Modifier.width(12.dp))
        Text("Company\nIdentity\nEngine", color = Color.White.copy(alpha = 0.86f), fontSize = 9.sp, lineHeight = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
internal fun CieDarkWordmark() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("cie", color = Cie076Colors.Text, fontSize = 32.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-2).sp)
        Spacer(Modifier.width(10.dp))
        Box(Modifier.width(1.dp).height(32.dp).background(Cie076Colors.Border))
        Spacer(Modifier.width(10.dp))
        Text("Company\nIdentity\nEngine", color = Cie076Colors.Muted, fontSize = 8.sp, lineHeight = 9.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
internal fun Cie076Hero(active: Boolean, loading: Boolean, onSettings: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(Cie076Colors.BlueDeep, Cie076Colors.Blue, Cie076Colors.BlueLight)),
                RoundedCornerShape(28.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CieWhiteWordmark()
                Spacer(Modifier.weight(1f))
                Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(27.dp))
                    }
                    Box(
                        Modifier
                            .padding(top = 3.dp, end = 2.dp)
                            .size(9.dp)
                            .background(if (active) Cie076Colors.Green else Color(0xFFFFB74D), RoundedCornerShape(99.dp))
                    )
                }
            }
            Spacer(Modifier.height(30.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(52.dp).background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(99.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        when {
                            loading -> "CIE is updating"
                            active -> "CIE is ready"
                            else -> "CIE needs setup"
                        },
                        color = Color.White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (active) "Your phone is protected" else "Enable Call Shield to finish setup",
                        color = Color.White.copy(alpha = 0.82f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
internal fun Cie076TopBar(onSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        CieDarkWordmark()
        Spacer(Modifier.weight(1f))
        Box(contentAlignment = Alignment.TopEnd) {
            IconButton(onClick = onSettings) {
                Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = Cie076Colors.Text, modifier = Modifier.size(25.dp))
            }
            Box(Modifier.padding(top = 4.dp, end = 3.dp).size(8.dp).background(Cie076Colors.Green, RoundedCornerShape(99.dp)))
        }
    }
}

@Composable
internal fun Cie076BottomBar(current: ModernScreen076, onSelect: (ModernScreen076) -> Unit) {
    Surface(color = Color.White, tonalElevation = 0.dp, shadowElevation = 0.dp) {
        Column {
            HorizontalDivider(color = Cie076Colors.Border)
            Row(
                Modifier.fillMaxWidth().navigationBarsPadding().height(72.dp).padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Cie076BottomItem("Shield", Icons.Rounded.Shield, current == ModernScreen076.SHIELD, Modifier.weight(1f)) { onSelect(ModernScreen076.SHIELD) }
                Cie076BottomItem("Blocked", Icons.Rounded.Block, current == ModernScreen076.BLOCKED, Modifier.weight(1f)) { onSelect(ModernScreen076.BLOCKED) }
                Cie076BottomItem("Activity", Icons.Rounded.History, current == ModernScreen076.ACTIVITY, Modifier.weight(1f)) { onSelect(ModernScreen076.ACTIVITY) }
            }
        }
    }
}

@Composable
private fun Cie076BottomItem(label: String, icon: ImageVector, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.fillMaxHeight().clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(34.dp).background(if (selected) Cie076Colors.BlueSoft else Color.Transparent, RoundedCornerShape(99.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = if (selected) Cie076Colors.Blue else Cie076Colors.Muted, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.height(2.dp))
        Text(label, color = if (selected) Cie076Colors.Blue else Cie076Colors.Muted, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
    }
}
