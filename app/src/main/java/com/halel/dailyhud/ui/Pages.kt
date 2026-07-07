package com.halel.dailyhud.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halel.dailyhud.LocalHudColors
import com.halel.dailyhud.Task
import com.halel.dailyhud.Alarm
import com.halel.dailyhud.Note

@Composable
fun TasksPage(
    tasks: List<Task>,
    onToggle: (String) -> Unit,
    onEdit: (Task) -> Unit,
    onDelete: (String) -> Unit
) {
    val c = LocalHudColors.current
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Text(
            "Bis zu 10 Aufgaben · ${tasks.size}/10",
            color = c.textDim, fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 14.dp)
        )
        if (tasks.isEmpty()) {
            EmptyState("＋", "Noch keine Aufgaben.\nTipp unten auf Plus, um loszulegen.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(tasks.sortedBy { it.time }, key = { it.id }) { task ->
                    CardShell(done = task.done) {
                        Box(
                            Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (task.done) c.cyan else c.panel)
                                .border(2.dp, c.cyanDim, RoundedCornerShape(8.dp))
                                .clickable { onToggle(task.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (task.done) Icon(Icons.Filled.Check, null, tint = c.bg, modifier = Modifier.size(14.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                task.text, color = c.text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                                textDecoration = if (task.done) TextDecoration.LineThrough else null
                            )
                            Text(task.time, color = c.cyan, fontSize = 13.sp)
                        }
                        ChipButton(Icons.Filled.Edit, "Bearbeiten") { onEdit(task) }
                        DeleteChip { onDelete(task.id) }
                    }
                }
            }
        }
    }
}

@Composable
fun FocusPage(tasks: List<Task>, onMarkDone: (String) -> Unit) {
    val c = LocalHudColors.current
    val open = tasks.filter { !it.done }.sortedBy { it.time }
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Text(
            "Offene Aufgaben, sortiert nach Uhrzeit",
            color = c.textDim, fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 14.dp)
        )
        if (open.isEmpty()) {
            EmptyState("✓", "Alles erledigt.\nNeue Aufgaben erscheinen hier automatisch.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(open, key = { it.id }) { task ->
                    CardShell(borderColorOverride = null) {
                        Column(Modifier.weight(1f)) {
                            Text(task.text, color = c.text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text(task.time, color = c.cyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        ChipButton(Icons.Filled.Check, "Erledigt") { onMarkDone(task.id) }
                    }
                }
            }
        }
    }
}

@Composable
fun AlarmsPage(
    alarms: List<Alarm>,
    onToggle: (String) -> Unit,
    onEdit: (Alarm) -> Unit,
    onDelete: (String) -> Unit
) {
    val c = LocalHudColors.current
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Text(
            "Alarme mit Aufgabe & Uhrzeit",
            color = c.textDim, fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 14.dp)
        )
        if (alarms.isEmpty()) {
            EmptyState("⏰", "Noch kein Wecker gestellt.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(alarms.sortedBy { it.time }, key = { it.id }) { alarm ->
                    CardShell {
                        Box(
                            Modifier
                                .width(44.dp).height(26.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (alarm.enabled) c.cyan.copy(alpha = 0.18f) else c.panel2)
                                .border(1.dp, if (alarm.enabled) c.cyan else c.border, RoundedCornerShape(20.dp))
                                .clickable { onToggle(alarm.id) },
                            contentAlignment = if (alarm.enabled) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Box(
                                Modifier
                                    .padding(2.dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (alarm.enabled) c.cyan else c.textDim)
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text(alarm.label, color = c.text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text(alarm.time, color = c.cyan, fontSize = 13.sp)
                        }
                        ChipButton(Icons.Filled.Edit, "Bearbeiten") { onEdit(alarm) }
                        DeleteChip { onDelete(alarm.id) }
                    }
                }
            }
        }
    }
}

@Composable
fun NotesPage(
    notes: List<Note>,
    onEdit: (Note) -> Unit,
    onDelete: (String) -> Unit
) {
    val c = LocalHudColors.current
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Text(
            "Schnelle Notizen",
            color = c.textDim, fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 14.dp)
        )
        if (notes.isEmpty()) {
            EmptyState("▤", "Noch keine Notizen.")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    Column(
                        Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(c.panel)
                            .border(1.dp, c.border, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                            .heightIn(min = 120.dp)
                    ) {
                        Text(note.title, color = c.cyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            note.text, color = c.textDim, fontSize = 13.5.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Row(
                            Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End)
                        ) {
                            ChipButton(Icons.Filled.Edit, "Bearbeiten") { onEdit(note) }
                            DeleteChip { onDelete(note.id) }
                        }
                    }
                }
            }
        }
    }
}
