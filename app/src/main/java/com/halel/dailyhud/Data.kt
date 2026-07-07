package com.halel.dailyhud

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    var text: String,
    var time: String, // "HH:mm"
    var done: Boolean = false
)

data class Alarm(
    val id: String = UUID.randomUUID().toString(),
    var label: String,
    var time: String, // "HH:mm"
    var enabled: Boolean = true
)

data class Note(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var text: String
)

enum class Ringtone { BEEP, CHIME, ALERT }
enum class AppTheme { DARK, LIGHT }

data class Settings(
    var theme: AppTheme = AppTheme.DARK,
    var ringtone: Ringtone = Ringtone.BEEP,
    var volume: Float = 0.5f,
    var musicUri: String? = null,
    var musicName: String? = null
)

/**
 * Simple JSON-over-SharedPreferences store. No extra JSON library needed,
 * mirrors the localStorage approach from the original web app.
 */
class Repository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("hud_store", Context.MODE_PRIVATE)

    // ---------- Tasks ----------
    fun loadTasks(): MutableList<Task> {
        val arr = JSONArray(prefs.getString("hud_tasks", "[]"))
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Task(o.getString("id"), o.getString("text"), o.getString("time"), o.getBoolean("done"))
        }.toMutableList()
    }

    fun saveTasks(tasks: List<Task>) {
        val arr = JSONArray()
        tasks.forEach {
            arr.put(JSONObject().apply {
                put("id", it.id); put("text", it.text); put("time", it.time); put("done", it.done)
            })
        }
        prefs.edit().putString("hud_tasks", arr.toString()).apply()
    }

    // ---------- Alarms ----------
    fun loadAlarms(): MutableList<Alarm> {
        val arr = JSONArray(prefs.getString("hud_alarms", "[]"))
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Alarm(o.getString("id"), o.getString("label"), o.getString("time"), o.getBoolean("enabled"))
        }.toMutableList()
    }

    fun saveAlarms(alarms: List<Alarm>) {
        val arr = JSONArray()
        alarms.forEach {
            arr.put(JSONObject().apply {
                put("id", it.id); put("label", it.label); put("time", it.time); put("enabled", it.enabled)
            })
        }
        prefs.edit().putString("hud_alarms", arr.toString()).apply()
    }

    // ---------- Notes ----------
    fun loadNotes(): MutableList<Note> {
        val arr = JSONArray(prefs.getString("hud_notes", "[]"))
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Note(o.getString("id"), o.getString("title"), o.getString("text"))
        }.toMutableList()
    }

    fun saveNotes(notes: List<Note>) {
        val arr = JSONArray()
        notes.forEach {
            arr.put(JSONObject().apply {
                put("id", it.id); put("title", it.title); put("text", it.text)
            })
        }
        prefs.edit().putString("hud_notes", arr.toString()).apply()
    }

    // ---------- Settings ----------
    fun loadSettings(): Settings {
        val o = JSONObject(prefs.getString("hud_settings", "{}") ?: "{}")
        return Settings(
            theme = runCatching { AppTheme.valueOf(o.optString("theme", "DARK")) }.getOrDefault(AppTheme.DARK),
            ringtone = runCatching { Ringtone.valueOf(o.optString("ringtone", "BEEP")) }.getOrDefault(Ringtone.BEEP),
            volume = o.optDouble("volume", 0.5).toFloat(),
            musicUri = o.optString("musicUri", null.toString()).takeIf { it.isNotBlank() && it != "null" },
            musicName = o.optString("musicName", null.toString()).takeIf { it.isNotBlank() && it != "null" }
        )
    }

    fun saveSettings(s: Settings) {
        val o = JSONObject().apply {
            put("theme", s.theme.name)
            put("ringtone", s.ringtone.name)
            put("volume", s.volume.toDouble())
            put("musicUri", s.musicUri ?: "")
            put("musicName", s.musicName ?: "")
        }
        prefs.edit().putString("hud_settings", o.toString()).apply()
    }
}
