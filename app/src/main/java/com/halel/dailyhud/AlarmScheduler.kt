package com.halel.dailyhud

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object AlarmScheduler {

    private fun pendingIntent(context: Context, alarm: Alarm): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("id", alarm.id)
            putExtra("label", alarm.label)
            putExtra("time", alarm.time)
        }
        return PendingIntent.getBroadcast(
            context,
            alarm.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextTriggerMillis(time: String): Long {
        val (h, m) = time.split(":").map { it.toInt() }
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, h)
        cal.set(Calendar.MINUTE, m)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1) // next occurrence, daily repeat
        }
        return cal.timeInMillis
    }

    fun schedule(context: Context, alarm: Alarm) {
        if (!alarm.enabled) {
            cancel(context, alarm)
            return
        }
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val trigger = nextTriggerMillis(alarm.time)
        val pi = pendingIntent(context, alarm)
        try {
            am.setAlarmClock(AlarmManager.AlarmClockInfo(trigger, pi), pi)
        } catch (e: SecurityException) {
            // missing SCHEDULE_EXACT_ALARM on some OEMs; fall back to inexact
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi)
        }
    }

    fun cancel(context: Context, alarm: Alarm) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context, alarm))
    }

    /** Call after boot or after editing the alarm list to re-arm everything that's enabled. */
    fun rescheduleAll(context: Context, alarms: List<Alarm>) {
        alarms.forEach { schedule(context, it) }
    }
}
