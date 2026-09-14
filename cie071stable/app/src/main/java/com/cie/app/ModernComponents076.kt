package com.cie.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DateFormat
import java.util.Date

enum class ModernScreen076 { SHIELD, BLOCKED, ACTIVITY, SETTINGS, REPORT }

@Composable
internal fun Cie076BackBar(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(Icons.Rounded.ArrowBack, contentDescription = cieText("back"), tint = Cie076Colors.Text)
        }
        Spacer(Modifier.width(4.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}

@Composable
internal fun Cie076Card(container: Color = Cie076Colors.Card, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = container),
        border = BorderStroke(1.dp, Cie076Colors.Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(Modifier.padding(18.dp), content = content)
    }
}

@Composable
internal fun Cie076Toggle(checked: Boolean, onChange: (Boolean) -> Unit) {
    Box(
        Modifier
            .width(52.dp)
            .height(31.dp)
            .background(if (checked) Cie076Colors.Blue else Cie076Colors.Track, RoundedCornerShape(99.dp))
            .clickable { onChange(!checked) }
            .padding(3.dp)
    ) {
        Box(
            Modifier
                .size(25.dp)
                .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
                .background(Color.White, RoundedCornerShape(99.dp))
        )
    }
}

@Composable
internal fun Cie076MetricTile(value: String, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .background(Color.White.copy(alpha = 0.88f), RoundedCornerShape(18.dp))
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(label, color = Cie076Colors.Muted, fontSize = 12.sp)
        }
    }
}

@Composable
internal fun Cie076CompanyCard(company: CompanyRow, onToggle: (CompanyRow, Boolean) -> Unit) {
    Cie076Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(company.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (company.verified) {
                        Spacer(Modifier.width(8.dp))
                        Text(cieText("verified"), color = Cie076Colors.Blue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(company.category.replaceFirstChar { it.uppercase() }, color = Cie076Colors.Muted, fontSize = 12.sp)
            }
            Cie076Toggle(company.blocked) { onToggle(company, it) }
        }
    }
}

@Composable
internal fun Cie076ActivityRow(event: CallEvent) {
    Cie076Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).background(if (event.blocked) Cie076Colors.Red else Cie076Colors.Green, RoundedCornerShape(99.dp)))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(event.companyName ?: cieText("unknown_caller"), fontWeight = FontWeight.Bold)
                Text(cie076ShortDate(event.timestamp), color = Cie076Colors.Muted, fontSize = 11.sp)
            }
            Text(if (event.blocked) cieText("blocked_state") else cieText("allowed"), color = if (event.blocked) Cie076Colors.Red else Cie076Colors.Green, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
internal fun Cie076EmptyState(text: String, withPhoneIcon: Boolean = false) {
    Cie076Card {
        if (withPhoneIcon) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).background(Color(0xFFF2F3F5), RoundedCornerShape(99.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Phone, contentDescription = null, tint = Cie076Colors.Muted, modifier = Modifier.size(23.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(text, fontWeight = FontWeight.SemiBold, color = Cie076Colors.Muted)
                    Text(cieText("activity_empty_hint"), color = Cie076Colors.Muted, fontSize = 12.sp)
                }
            }
        } else {
            Text(text, color = Cie076Colors.Muted, modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
internal fun Cie076PrimaryButton(text: String, enabled: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

private fun cie076ShortDate(timestamp: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(timestamp))
