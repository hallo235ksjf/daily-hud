package com.halel.dailyhud

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(app)

    private val _tasks = MutableStateFlow(repo.loadTasks())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _alarms = MutableStateFlow(repo.loadAlarms())
    val alarms: StateFlow<List<Alarm>> = _alarms.asStateFlow()

    private val _notes = MutableStateFlow(repo.loadNotes())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _settings = MutableStateFlow(repo.loadSettings())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    init {
        // arm whatever alarms already exist on cold start
        AlarmScheduler.rescheduleAll(app, _alarms.value.filter { it.enabled })
    }

    // ---------- Tasks ----------
    fun upsertTask(id: String?, text: String, time: String) {
        val list = _tasks.value.toMutableList()
        if (id != null) {
            list.find { it.id == id }?.let { it.text = text; it.time = time }
        } else {
            if (list.size >= 10) return
            list.add(Task(text = text, time = time))
        }
        _tasks.value = list
        repo.saveTasks(list)
    }

    fun toggleTaskDone(id: String) {
        val list = _tasks.value.toMutableList()
        list.find { it.id == id }?.let { it.done = !it.done }
        _tasks.value = list
        repo.saveTasks(list)
    }

    fun deleteTask(id: String) {
        val list = _tasks.value.filterNot { it.id == id }
        _tasks.value = list
        repo.saveTasks(list)
    }

    // ---------- Alarms ----------
    fun upsertAlarm(id: String?, label: String, time: String) {
        val list = _alarms.value.toMutableList()
        val alarm: Alarm
        if (id != null) {
            val existing = list.find { it.id == id }!!
            existing.label = label; existing.time = time
            alarm = existing
        } else {
            alarm = Alarm(label = label, time = time)
            list.add(alarm)
        }
        _alarms.value = list
        repo.saveAlarms(list)
        AlarmScheduler.schedule(getApplication(), alarm)
    }

    fun toggleAlarmEnabled(id: String) {
        val list = _alarms.value.toMutableList()
        val a = list.find { it.id == id } ?: return
        a.enabled = !a.enabled
        _alarms.value = list
        repo.saveAlarms(list)
        if (a.enabled) AlarmScheduler.schedule(getApplication(), a) else AlarmScheduler.cancel(getApplication(), a)
    }

    fun deleteAlarm(id: String) {
        val a = _alarms.value.find { it.id == id } ?: return
        AlarmScheduler.cancel(getApplication(), a)
        val list = _alarms.value.filterNot { it.id == id }
        _alarms.value = list
        repo.saveAlarms(list)
    }

    // ---------- Notes ----------
    fun upsertNote(id: String?, title: String, text: String) {
        val list = _notes.value.toMutableList()
        if (id != null) {
            list.find { it.id == id }?.let { it.title = title; it.text = text }
        } else {
            list.add(Note(title = title, text = text))
        }
        _notes.value = list
        repo.saveNotes(list)
    }

    fun deleteNote(id: String) {
        val list = _notes.value.filterNot { it.id == id }
        _notes.value = list
        repo.saveNotes(list)
    }

    // ---------- Settings ----------
    fun setTheme(theme: AppTheme) {
        _settings.value = _settings.value.copy(theme = theme)
        repo.saveSettings(_settings.value)
    }

    fun setRingtone(tone: Ringtone) {
        _settings.value = _settings.value.copy(ringtone = tone)
        repo.saveSettings(_settings.value)
    }

    fun setVolume(v: Float) {
        _settings.value = _settings.value.copy(volume = v)
        repo.saveSettings(_settings.value)
    }

    fun setMusic(uri: String?, name: String?) {
        _settings.value = _settings.value.copy(musicUri = uri, musicName = name)
        repo.saveSettings(_settings.value)
    }
}
