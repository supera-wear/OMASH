package com.cie.app

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
    Image(
        painter = painterResource(R.drawable.cie_wordmark_official),
        contentDescription = "CIE Company Identity Engine",
        modifier = Modifier.width(170.dp).height(61.dp),
        contentScale = ContentScale.Fit
    )
}

@Composable
internal fun CieDarkWordmark() {
    Image(
        painter = painterResource(R.drawable.cie_wordmark_official),
        contentDescription = "CIE Company Identity Engine",
        modifier = Modifier.width(154.dp).height(55.dp),
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(Cie076Colors.Text)
    )
}

@Composable
internal fun Cie076Hero(active: Boolean, loading: Boolean, onSettings: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    listOf(Cie076Colors.BlueDeep, Cie076Colors.Blue, Cie076Colors.BlueLight)
                ),
                shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)
            )
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CieWhiteWordmark()
                Spacer(Modifier.weight(1f))
                Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(onClick = onSettings) {
                        Icon(
                            Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(27.dp)
                        )
                    }
                    Box(
                        Modifier
                            .padding(top = 3.dp, end = 2.dp)
                            .size(9.dp)
                            .background(
                                if (active) Cie076Colors.Green else Color(0xFFFFB74D),
                                RoundedCornerShape(99.dp)
                            )
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(50.dp)
                        .background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(99.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(29.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        when {
                            loading -> "CIE Shield is updating"
                            active -> "CIE Shield is ready"
                            else -> "CIE Shield needs setup"
                        },
                        color = Color.White,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.45).sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (active) "Your phone is protected" else "Enable Call Shield to finish setup",
                        color = Color.White.copy(alpha = 0.84f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
internal fun Cie076TopBar(onSettings: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CieDarkWordmark()
        Spacer(Modifier.weight(1f))
        Box(contentAlignment = Alignment.TopEnd) {
            IconButton(onClick = onSettings) {
                Icon(
                    Icons.Rounded.Settings,
                    contentDescription = "Settings",
                    tint = Cie076Colors.Text,
                    modifier = Modifier.size(25.dp)
                )
            }
            Box(
                Modifier
                    .padding(top = 4.dp, end = 3.dp)
                    .size(8.dp)
                    .background(Cie076Colors.Green, RoundedCornerShape(99.dp))
            )
        }
    }
}

@Composable
internal fun Cie076BottomBar(current: ModernScreen076, onSelect: (ModernScreen076) -> Unit) {
    Surface(
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(Modifier.navigationBarsPadding()) {
            HorizontalDivider(color = Cie076Colors.Border)
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Cie076BottomItem(
                    "Shield",
                    Icons.Rounded.Shield,
                    current == ModernScreen076.SHIELD,
                    Modifier.weight(1f)
                ) { onSelect(ModernScreen076.SHIELD) }
                Cie076BottomItem(
                    "Blocked",
                    Icons.Rounded.Block,
                    current == ModernScreen076.BLOCKED,
                    Modifier.weight(1f)
                ) { onSelect(ModernScreen076.BLOCKED) }
                Cie076BottomItem(
                    "Activity",
                    Icons.Rounded.History,
                    current == ModernScreen076.ACTIVITY,
                    Modifier.weight(1f)
                ) { onSelect(ModernScreen076.ACTIVITY) }
            }
        }
    }
}

@Composable
private fun Cie076BottomItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier.fillMaxHeight().clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .size(34.dp)
                .background(
                    if (selected) Cie076Colors.BlueSoft else Color.Transparent,
                    RoundedCornerShape(99.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (selected) Cie076Colors.Blue else Cie076Colors.Muted,
                modifier = Modifier.size(21.dp)
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            color = if (selected) Cie076Colors.Blue else Cie076Colors.Muted,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
