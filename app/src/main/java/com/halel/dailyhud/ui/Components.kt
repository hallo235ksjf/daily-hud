package com.halel.dailyhud.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halel.dailyhud.LocalHudColors
import kotlinx.coroutines.delay

@Composable
fun TopBar(title: String, pageCount: Int, currentPage: Int, onDotClick: (Int) -> Unit, onSettingsClick: () -> Unit) {
    val c = LocalHudColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .background(c.bg)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("◈", color = c.cyan, fontSize = 18.sp)
            Spacer(Modifier.width(8.dp))
            Text(title, color = c.text, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 2.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(pageCount) { i ->
                val active = i == currentPage
                Box(
                    Modifier
                        .padding(horizontal = 3.dp)
                        .height(7.dp)
                        .width(if (active) 20.dp else 7.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (active) c.cyan else c.border)
                        .clickable { onDotClick(i) }
                )
            }
        }
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(c.panel)
                .border(1.dp, c.border, RoundedCornerShape(12.dp))
        ) {
            Icon(Icons.Filled.Settings, contentDescription = "Einstellungen", tint = c.text)
        }
    }
}

/** Double-tap-to-delete chip, mirroring the web app's confirmDelete() behaviour. */
@Composable
fun DeleteChip(onConfirmed: () -> Unit) {
    val c = LocalHudColors.current
    var armed by remember { mutableStateOf(false) }

    LaunchedEffect(armed) {
        if (armed) {
            delay(2500)
            armed = false
        }
    }

    Box(
        Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (armed) c.danger else c.panel2)
            .border(1.dp, if (armed) c.danger else c.border, RoundedCornerShape(10.dp))
            .clickable {
                if (armed) { armed = false; onConfirmed() } else { armed = true }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            if (armed) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = "Löschen",
            tint = if (armed) Color.White else c.danger,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun ChipButton(icon: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String, onClick: () -> Unit) {
    val c = LocalHudColors.current
    Box(
        Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(c.panel2)
            .border(1.dp, c.border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = c.textDim, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun CardShell(done: Boolean = false, borderColorOverride: Color? = null, content: @Composable RowScope.() -> Unit) {
    val c = LocalHudColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.panel)
            .border(1.dp, borderColorOverride ?: c.border, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .alpha(if (done) 0.5f else 1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
fun EmptyState(glyph: String, message: String) {
    val c = LocalHudColors.current
    Column(
        Modifier.fillMaxWidth().padding(top = 90.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(glyph, color = c.cyan, fontSize = 38.sp)
        Spacer(Modifier.height(10.dp))
        Text(message, color = c.textDim, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
