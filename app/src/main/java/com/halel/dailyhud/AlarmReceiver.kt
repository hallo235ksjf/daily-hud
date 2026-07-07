package com.halel.dailyhud

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra("id") ?: return
        val label = intent.getStringExtra("label") ?: "Wecker"
        val time = intent.getStringExtra("time") ?: ""

        // Re-arm for tomorrow (alarms in this app repeat daily, like the web version's minute-tick loop).
        val repo = Repository(context)
        repo.loadAlarms().find { it.id == id }?.let { AlarmScheduler.schedule(context, it) }

        val serviceIntent = Intent(context, AlarmRingService::class.java).apply {
            putExtra("id", id); putExtra("label", label); putExtra("time", time)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val repo = Repository(context)
            AlarmScheduler.rescheduleAll(context, repo.loadAlarms().filter { it.enabled })
        }
    }
}
