package com.cie.app.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ActivityScreen() {
    ScreenColumn {
        PageHeader("Activity", "What CIE did")
        Spacer(Modifier.height(16.dp))
        SurfaceCard {
            Event("Sample Retail blocked", "Company identity matched locally.", "16:43")
            HorizontalDivider(color = Border)
            Event("Directory ready", "Verified identities available on device.", "15:58")
            HorizontalDivider(color = Border)
            Event("DemoTel allowed", "Verified call allowed by your policy.", "14:21")
        }
    }
}

@Composable
private fun Event(title: String, body: String, time: String) {
    androidx.compose.foundation.layout.Column(Modifier.padding(vertical = 8.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(body, color = Muted, fontSize = 10.sp)
        Text(time, color = Color(0xFFA0A5AC), fontSize = 9.sp)
    }
}
