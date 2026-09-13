package com.cie.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun BlockedScreen(companies: List<Company>, onToggle: (Int, Boolean) -> Unit) {
    var query by remember { mutableStateOf("") }
    ScreenColumn {
        PageHeader("Blocked", "Companies you control")
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Search company") },
            shape = RoundedCornerShape(18.dp)
        )
        Spacer(Modifier.height(12.dp))
        companies.forEachIndexed { index, company ->
            if (query.isBlank() || company.name.contains(query, true)) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Border)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFEEEDE9), modifier = Modifier.size(44.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(company.name.take(2).uppercase(), fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(company.name, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                Spacer(Modifier.width(5.dp))
                                Text("✓", color = Green, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                            Text(company.category, color = Muted, fontSize = 10.sp)
                        }
                        Switch(checked = company.blocked, onCheckedChange = { onToggle(index, it) })
                    }
                }
            }
        }
    }
}
