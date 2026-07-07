package com.halel.dailyhud

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class AlarmRingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        val label = intent.getStringExtra("label") ?: "Wecker"
        val time = intent.getStringExtra("time") ?: ""

        setContent {
            HudTheme(theme = AppTheme.DARK) {
                AlarmRingScreen(label = label, time = time, onStop = ::stopAlarm)
            }
        }
    }

    private fun stopAlarm() {
        sendBroadcast(Intent("com.halel.dailyhud.ACTION_STOP_ALARM"))
        val svc = Intent(this, AlarmRingService::class.java)
        stopService(svc)
        finish()
    }
}

@Composable
fun AlarmRingScreen(label: String, time: String, onStop: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HudColors.bg),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⏰", fontSize = 64.sp)
            Spacer(Modifier.height(20.dp))
            Text(label, color = HudColors.text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(time, color = HudColors.cyan, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = onStop,
                colors = ButtonDefaults.buttonColors(containerColor = HudColors.cyan),
                modifier = Modifier.height(56.dp).width(200.dp)
            ) {
                Text("Stopp", color = HudColors.bg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
