package com.halel.dailyhud.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halel.dailyhud.LocalHudColors
import java.util.Calendar

@Composable
private fun TimeField(label: String, value: String, onChange: (String) -> Unit) {
    val c = LocalHudColors.current
    val context = LocalContext.current
    Column {
        Text(label, color = c.textDim, fontSize = 13.sp, modifier = Modifier.padding(bottom = 6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(c.panel2)
                .border(1.dp, c.border, RoundedCornerShape(12.dp))
                .clickable {
                    val cal = Calendar.getInstance()
                    val parts = value.split(":")
                    val h = parts.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.HOUR_OF_DAY)
                    val m = parts.getOrNull(1)?.toIntOrNull() ?: cal.get(Calendar.MINUTE)
                    TimePickerDialog(context, { _, hh, mm ->
                        onChange("%02d:%02d".format(hh, mm))
                    }, h, m, true).show()
                }
                .padding(14.dp)
        ) {
            Text(value.ifBlank { "--:--" }, color = c.text, fontSize = 16.sp)
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TextFieldRow(label: String, value: String, onChange: (String) -> Unit, singleLine: Boolean = true, maxLines: Int = 1) {
    val c = LocalHudColors.current
    Column {
        Text(label, color = c.textDim, fontSize = 13.sp, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            singleLine = singleLine,
            maxLines = maxLines,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = c.cyan,
                unfocusedBorderColor = c.border,
                focusedContainerColor = c.panel2,
                unfocusedContainerColor = c.panel2,
                focusedTextColor = c.text,
                unfocusedTextColor = c.text
            )
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun EditSheet(title: String, onDismiss: () -> Unit, onSave: () -> Unit, saveEnabled: Boolean, content: @Composable ColumnScope.() -> Unit) {
    val c = LocalHudColors.current
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = c.panel) {
        Column(Modifier.padding(horizontal = 22.dp).padding(bottom = 30.dp)) {
            Text(title, color = c.cyan, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 18.dp))
            content()
            Spacer(Modifier.height(22.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Abbrechen") }
                Button(
                    onClick = onSave,
                    enabled = saveEnabled,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = c.cyan, contentColor = c.bg)
                ) { Text("Speichern", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
fun TaskEditSheet(initialText: String, initialTime: String, onDismiss: () -> Unit, onSave: (String, String) -> Unit, title: String) {
    var text by remember { mutableStateOf(initialText) }
    var time by remember { mutableStateOf(initialTime) }
    EditSheet(title, onDismiss, { onSave(text.trim(), time) }, saveEnabled = text.isNotBlank() && time.isNotBlank()) {
        TextFieldRow("Was steht an?", text, { if (it.length <= 80) text = it })
        Spacer(Modifier.height(12.dp))
        TimeField("Uhrzeit", time) { time = it }
    }
}

@Composable
fun AlarmEditSheet(initialLabel: String, initialTime: String, onDismiss: () -> Unit, onSave: (String, String) -> Unit, title: String) {
    var label by remember { mutableStateOf(initialLabel) }
    var time by remember { mutableStateOf(initialTime) }
    EditSheet(title, onDismiss, { onSave(label.trim(), time) }, saveEnabled = label.isNotBlank() && time.isNotBlank()) {
        TextFieldRow("Aufgabe", label, { if (it.length <= 80) label = it })
        Spacer(Modifier.height(12.dp))
        TimeField("Uhrzeit", time) { time = it }
    }
}

@Composable
fun NoteEditSheet(initialTitle: String, initialText: String, onDismiss: () -> Unit, onSave: (String, String) -> Unit, title: String) {
    var noteTitle by remember { mutableStateOf(initialTitle) }
    var noteText by remember { mutableStateOf(initialText) }
    EditSheet(title, onDismiss, { onSave(noteTitle.trim(), noteText.trim()) }, saveEnabled = noteTitle.isNotBlank()) {
        TextFieldRow("Titel", noteTitle, { if (it.length <= 40) noteTitle = it })
        Spacer(Modifier.height(12.dp))
        TextFieldRow("Inhalt", noteText, { noteText = it }, singleLine = false, maxLines = 6)
    }
}
