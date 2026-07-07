package com.halel.dailyhud

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.exp

/**
 * Recreates the three synthetic ringtones from the web version's
 * Web Audio oscillator patterns (beep / chime / alert), fully offline,
 * no sound files required.
 */
object ToneGen {
    private const val SAMPLE_RATE = 44100

    // (frequencyHz, durationSeconds, peakGain) — 0 Hz = silence gap
    private val patterns: Map<Ringtone, List<Triple<Int, Double, Double>>> = mapOf(
        Ringtone.BEEP to listOf(Triple(880, 0.15, 0.6)),
        Ringtone.CHIME to listOf(
            Triple(660, 0.15, 0.6), Triple(880, 0.15, 0.7), Triple(1320, 0.20, 0.8)
        ),
        Ringtone.ALERT to listOf(
            Triple(440, 0.12, 0.6), Triple(0, 0.06, 0.0),
            Triple(440, 0.12, 0.6), Triple(0, 0.06, 0.0),
            Triple(440, 0.12, 0.6)
        )
    )

    /** Returns total duration of the pattern in milliseconds. */
    fun durationMs(tone: Ringtone): Long {
        val seq = patterns[tone] ?: patterns[Ringtone.BEEP]!!
        return (seq.sumOf { it.second } * 1000).toLong()
    }

    /** Plays the tone once, blocking until finished. Call from a background thread. */
    fun playBlocking(tone: Ringtone) {
        val seq = patterns[tone] ?: patterns[Ringtone.BEEP]!!
        val samples = buildSamples(seq)
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(samples.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(samples, 0, samples.size)
        track.play()
        Thread.sleep(durationMs(tone) + 50)
        track.stop()
        track.release()
    }

    private fun buildSamples(seq: List<Triple<Int, Double, Double>>): ShortArray {
        val all = ArrayList<Short>()
        seq.forEach { (freq, dur, gain) ->
            val n = (SAMPLE_RATE * dur).toInt()
            for (i in 0 until n) {
                val t = i.toDouble() / SAMPLE_RATE
                val envelope = exp(-4.0 * t / dur) // quick decay, like the JS exponential ramp
                val value = if (freq > 0) sin(2 * PI * freq * t) * gain * envelope else 0.0
                all.add((value * Short.MAX_VALUE).toInt().toShort())
            }
        }
        return all.toShortArray()
    }
}
