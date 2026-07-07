package com.halel.dailyhud.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.halel.dailyhud.AppViewModel
import com.halel.dailyhud.LocalHudColors
import com.halel.dailyhud.Task
import com.halel.dailyhud.Alarm
import com.halel.dailyhud.Note
import kotlinx.coroutines.launch

private val pageTitles = listOf("AUFGABEN", "FOKUS", "WECKER", "NOTIZEN")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(vm: AppViewModel) {
    val c = LocalHudColors.current
    val tasks by vm.tasks.collectAsState()
    val alarms by vm.alarms.collectAsState()
    val notes by vm.notes.collectAsState()
    val settings by vm.settings.collectAsState()

    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    var showSettings by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var showTaskSheet by remember { mutableStateOf(false) }
    var editingAlarm by remember { mutableStateOf<Alarm?>(null) }
    var showAlarmSheet by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var showNoteSheet by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(c.bg)) {
        Column(Modifier.fillMaxSize()) {
            TopBar(
                title = pageTitles[pagerState.currentPage],
                pageCount = 4,
                currentPage = pagerState.currentPage,
                onDotClick = { i -> scope.launch { pagerState.animateScrollToPage(i) } },
                onSettingsClick = { showSettings = true }
            )
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                when (page) {
                    0 -> TasksPage(
                        tasks = tasks,
                        onToggle = vm::toggleTaskDone,
                        onEdit = { editingTask = it; showTaskSheet = true },
                        onDelete = vm::deleteTask
                    )
                    1 -> FocusPage(tasks = tasks, onMarkDone = vm::toggleTaskDone)
                    2 -> AlarmsPage(
                        alarms = alarms,
                        onToggle = vm::toggleAlarmEnabled,
                        onEdit = { editingAlarm = it; showAlarmSheet = true },
                        onDelete = vm::deleteAlarm
                    )
                    3 -> NotesPage(
                        notes = notes,
                        onEdit = { editingNote = it; showNoteSheet = true },
                        onDelete = vm::deleteNote
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                when (pagerState.currentPage) {
                    0 -> { editingTask = null; showTaskSheet = true }
                    1 -> scope.launch { pagerState.animateScrollToPage(0) }
                    2 -> { editingAlarm = null; showAlarmSheet = true }
                    3 -> { editingNote = null; showNoteSheet = true }
                }
            },
            containerColor = c.cyan,
            contentColor = c.bg,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 22.dp, bottom = 30.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Hinzufügen")
        }
    }

    if (showTaskSheet) {
        TaskEditSheet(
            initialText = editingTask?.text ?: "",
            initialTime = editingTask?.time ?: "",
            title = if (editingTask != null) "Aufgabe bearbeiten" else "Neue Aufgabe",
            onDismiss = { showTaskSheet = false },
            onSave = { text, time ->
                vm.upsertTask(editingTask?.id, text, time)
                showTaskSheet = false
            }
        )
    }

    if (showAlarmSheet) {
        AlarmEditSheet(
            initialLabel = editingAlarm?.label ?: "",
            initialTime = editingAlarm?.time ?: "",
            title = if (editingAlarm != null) "Wecker bearbeiten" else "Neuer Wecker",
            onDismiss = { showAlarmSheet = false },
            onSave = { label, time ->
                vm.upsertAlarm(editingAlarm?.id, label, time)
                showAlarmSheet = false
            }
        )
    }

    if (showNoteSheet) {
        NoteEditSheet(
            initialTitle = editingNote?.title ?: "",
            initialText = editingNote?.text ?: "",
            title = if (editingNote != null) "Notiz bearbeiten" else "Neue Notiz",
            onDismiss = { showNoteSheet = false },
            onSave = { title, text ->
                vm.upsertNote(editingNote?.id, title, text)
                showNoteSheet = false
            }
        )
    }

    if (showSettings) {
        SettingsSheet(
            settings = settings,
            onDismiss = { showSettings = false },
            onThemeChange = vm::setTheme,
            onRingtoneChange = vm::setRingtone,
            onVolumeChange = vm::setVolume,
            onMusicChange = vm::setMusic
        )
    }
}
