package com.halel.dailyhud.ui

import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halel.dailyhud.AppTheme
import com.halel.dailyhud.LocalHudColors
import com.halel.dailyhud.Ringtone
import com.halel.dailyhud.Settings
import com.halel.dailyhud.ToneGen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
private fun SegmentedRow(options: List<Pair<String, Boolean>>, onClick: (Int) -> Unit) {
    val c = LocalHudColors.current
    Row(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(c.panel2)
            .border(1.dp, c.border, RoundedCornerShape(12.dp))
            .padding(3.dp)
    ) {
        options.forEachIndexed { i, (label, active) ->
            Box(
                Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (active) c.cyan else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable { onClick(i) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    label,
                    color = if (active) c.bg else c.textDim,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp
                )
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    settings: Settings,
    onDismiss: () -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onRingtoneChange: (Ringtone) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onMusicChange: (String?, String?) -> Unit
) {
    val c = LocalHudColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val pickMusic = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val name = queryDisplayName(context, uri) ?: "Song geladen"
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
            onMusicChange(uri.toString(), name)
        }
    }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer?.release() }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = c.panel) {
        Column(
            Modifier
                .padding(horizontal = 22.dp)
                .padding(bottom = 30.dp)
        ) {
            Text("Einstellungen", color = c.cyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(16.dp))

            // ---------- Theme ----------
            SettingRow(label = "Darstellung") {
                SegmentedRow(
                    listOf("Dark" to (settings.theme == AppTheme.DARK), "Light" to (settings.theme == AppTheme.LIGHT))
                ) { i -> onThemeChange(if (i == 0) AppTheme.DARK else AppTheme.LIGHT) }
            }

            // ---------- Ringtone ----------
            SettingColumn(label = "Klingelton") {
                SegmentedRow(
                    listOf(
                        "Beep" to (settings.ringtone == Ringtone.BEEP),
                        "Chime" to (settings.ringtone == Ringtone.CHIME),
                        "Alert" to (settings.ringtone == Ringtone.ALERT)
                    )
                ) { i -> onRingtoneChange(listOf(Ringtone.BEEP, Ringtone.CHIME, Ringtone.ALERT)[i]) }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = {
                    scope.launch(Dispatchers.Default) { ToneGen.playBlocking(settings.ringtone) }
                }) { Text("▶ Vorhören") }
            }

            // ---------- Background music ----------
            SettingColumn(label = "Hintergrundmusik (Chill)") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { pickMusic.launch(arrayOf("audio/*")) }) {
                        Text("Datei wählen")
                    }
                    OutlinedButton(
                        onClick = {
                            val uriStr = settings.musicUri ?: return@OutlinedButton
                            if (mediaPlayer == null) {
                                mediaPlayer = MediaPlayer().apply {
                                    setDataSource(context, Uri.parse(uriStr))
                                    isLooping = true
                                    setVolume(settings.volume, settings.volume)
                                    prepare()
                                    start()
                                }
                                isPlaying = true
                            } else if (isPlaying) {
                                mediaPlayer?.pause()
                                isPlaying = false
                            } else {
                                mediaPlayer?.start()
                                isPlaying = true
                            }
                        },
                        enabled = settings.musicUri != null
                    ) {
                        Text(if (isPlaying) "⏸ Pause" else "▶ Play")
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(settings.musicName ?: "Kein Song geladen", color = c.textDim, fontSize = 12.5.sp)
                Slider(
                    value = settings.volume,
                    onValueChange = {
                        onVolumeChange(it)
                        mediaPlayer?.setVolume(it, it)
                    },
                    colors = SliderDefaults.colors(thumbColor = c.cyan, activeTrackColor = c.cyan)
                )
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "DAILY // HUD · lokal gespeichert, keine Cloud.",
                color = c.textDim, fontSize = 11.5.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun SettingRow(label: String, content: @Composable () -> Unit) {
    val c = LocalHudColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .border(0.dp, c.border)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = c.text, fontSize = 15.sp)
        content()
    }
    androidx.compose.material3.HorizontalDivider(color = c.border)
}

@Composable
private fun SettingColumn(label: String, content: @Composable ColumnScope.() -> Unit) {
    val c = LocalHudColors.current
    Column(Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
        Text(label, color = c.text, fontSize = 15.sp, modifier = Modifier.padding(bottom = 10.dp))
        content()
    }
    androidx.compose.material3.HorizontalDivider(color = c.border)
}

private fun queryDisplayName(context: android.content.Context, uri: Uri): String? {
    return runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && idx >= 0) cursor.getString(idx) else null
        }
    }.getOrNull()
}
