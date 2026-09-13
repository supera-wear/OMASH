package com.cie.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
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
internal fun BrandMark() {
    Surface(shape = RoundedCornerShape(16.dp), color = Ink, modifier = Modifier.size(52.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Text("cie", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
        }
    }
}

@Composable
internal fun PageHeader(title: String, subtitle: String) {
    Column {
        Text(title, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
        Text(subtitle, color = Muted, fontSize = 11.sp)
    }
}

@Composable
internal fun SurfaceCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(Modifier.padding(17.dp), content = content)
    }
}

@Composable
internal fun Metric(value: String, label: String, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(20.dp), color = Color.White, border = BorderStroke(1.dp, Border)) {
        Column(Modifier.padding(16.dp)) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Text(label, fontSize = 10.sp, color = Muted)
        }
    }
}

@Composable
internal fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp).padding(bottom = 24.dp),
        content = content
    )
}

@Composable
internal fun CircleAction(label: String, onClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = Color.White,
        border = BorderStroke(1.dp, Border),
        modifier = Modifier.size(44.dp).clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun CieBottomBar(tab: CieTab, onTab: (CieTab) -> Unit) {
    Surface(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        color = Color.White,
        shape = RoundedCornerShape(26.dp),
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, Border)
    ) {
        Row(Modifier.fillMaxWidth().padding(7.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            CieTab.entries.forEach { item ->
                val selected = item == tab
                Column(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(18.dp))
                        .background(if (selected) Blue else Color.Transparent)
                        .clickable { onTab(item) }
                        .padding(vertical = 9.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(item.glyph, fontSize = 17.sp, color = if (selected) Ink else Muted)
                    Text(item.label, fontSize = 9.sp, color = if (selected) Ink else Muted, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}
