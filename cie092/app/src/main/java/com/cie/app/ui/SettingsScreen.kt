package com.cie.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun SettingsScreen(onBack: () -> Unit, onStatus: (String) -> Unit) {
    var sync by remember { mutableStateOf(true) }
    ScreenColumn {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircleAction("‹", onBack)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Settings", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                Text("Protection & privacy", color = Muted, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(18.dp))
        SurfaceCard {
            Text("On-device analysis", fontWeight = FontWeight.Bold)
            Text("Core protection decisions stay local.", color = Muted, fontSize = 10.sp)
            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = Border)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Company directory sync", fontWeight = FontWeight.Bold)
                    Text("Refresh verified business identity rules.", color = Muted, fontSize = 10.sp)
                }
                Switch(checked = sync, onCheckedChange = { sync = it })
            }
        }
        Spacer(Modifier.height(12.dp))
        SurfaceCard {
            Text("Privacy", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
            Text("Private message content is not uploaded by this build.", color = Muted, fontSize = 10.sp)
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = { onStatus("Local settings reset") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Text("Reset local settings")
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("CIE 0.9.2", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text("Community intelligence: off", color = Muted, fontSize = 9.sp)
    }
}
