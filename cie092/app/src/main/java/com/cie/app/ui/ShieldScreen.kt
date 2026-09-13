package com.cie.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ShieldScreen(
    roleHeld: Boolean,
    blockedCount: Int,
    status: String?,
    onSettings: () -> Unit,
    onOpenBlocked: () -> Unit,
    onEnableCalls: () -> Unit
) {
    ScreenColumn {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            BrandMark()
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("CIE", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                Text("Protection", fontSize = 11.sp, color = Muted)
            }
            CircleAction("⚙", onSettings)
        }

        Spacer(Modifier.height(28.dp))
        Text(
            if (roleHeld) "Protection active" else "Set up protection",
            fontSize = 34.sp,
            lineHeight = 37.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Ink
        )
        Spacer(Modifier.height(6.dp))
        Text("Block the company. Not the number.", color = Muted, fontSize = 15.sp)

        if (status != null) {
            Spacer(Modifier.height(14.dp))
            Surface(color = Color(0xFFE8F3EE), shape = RoundedCornerShape(16.dp)) {
                Text(status, modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp), color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(20.dp))
        SurfaceCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).clip(CircleShape).background(if (roleHeld) Green else Color(0xFFD39A38)))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("CIE Shield", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Text(if (roleHeld) "Calls protected" else "Android permission required", color = if (roleHeld) Green else Muted, fontSize = 11.sp)
                }
                Switch(checked = roleHeld, onCheckedChange = { checked -> if (checked && !roleHeld) onEnableCalls() })
            }
            Spacer(Modifier.height(12.dp))
            Text("Verified unwanted company calls are matched locally. Unknown callers are allowed by default.", color = Muted, fontSize = 11.sp, lineHeight = 16.sp)
        }

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Metric(blockedCount.toString(), "Blocked", Modifier.weight(1f))
            Metric(if (roleHeld) "On" else "Off", "Calls", Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))
        SurfaceCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Blocked companies", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                    Text("One switch applies protection to the company identity.", color = Muted, fontSize = 10.sp)
                }
                FilledTonalButton(onClick = onOpenBlocked, shape = RoundedCornerShape(14.dp)) {
                    Text("Manage", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        SurfaceCard {
            Text("Message Shield", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
            Spacer(Modifier.height(5.dp))
            Text("SMS/MMS protection stays behind the beta gate until the complete Android default-SMS flow passes end-to-end testing.", color = Muted, fontSize = 10.sp, lineHeight = 15.sp)
            Spacer(Modifier.height(10.dp))
            Surface(color = Color(0xFFFFF0D8), shape = RoundedCornerShape(100.dp)) {
                Text("BETA GATE", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
