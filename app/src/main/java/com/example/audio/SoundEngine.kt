package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.model.WeaponId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.sin
import kotlin.random.Random

class SoundEngine(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    var isSoundEnabled: Boolean = true
    var isVibrationEnabled: Boolean = true

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private val sampleRate = 22050
    private val bufferCache = ConcurrentHashMap<String, ShortArray>()

    init {
        // Pre-warm synthesizer caches
        scope.launch {
            generateBuffers()
        }
    }

    private fun generateBuffers() {
        bufferCache["shoot_laser"] = generateTone(880f, 440f, 0.09f, 0.6f)
        bufferCache["shoot_shotgun"] = generateNoise(0.18f, 0.8f, true)
        bufferCache["shoot_railgun"] = generateTone(1320f, 220f, 0.22f, 0.7f)
        bufferCache["shoot_rocket"] = generateTone(220f, 110f, 0.25f, 0.8f)
        bufferCache["shoot_tesla"] = generateTone(1200f, 800f, 0.08f, 0.5f)
        bufferCache["shoot_flame"] = generateNoise(0.06f, 0.4f, false)
        bufferCache["hit"] = generateTone(400f, 200f, 0.05f, 0.4f)
        bufferCache["explosion"] = generateNoise(0.35f, 1.0f, true)
        bufferCache["powerup"] = generateChime(listOf(523.25f, 659.25f, 783.99f, 1046.50f), 0.06f)
        bufferCache["shield_break"] = generateNoise(0.2f, 0.7f, false)
        bufferCache["dash"] = generateTone(300f, 600f, 0.12f, 0.5f)
        bufferCache["victory"] = generateChime(listOf(440f, 554.37f, 659.25f, 880f), 0.15f)
    }

    private fun generateTone(startFreq: Float, endFreq: Float, durationSec: Float, maxVolume: Float): ShortArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / totalSamples
            val currentFreq = startFreq + (endFreq - startFreq) * t
            val envelope = (1.0 - t) * maxVolume
            phase += 2.0 * Math.PI * currentFreq / sampleRate
            val sample = (sin(phase) * envelope * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateNoise(durationSec: Float, maxVolume: Float, isHeavy: Boolean): ShortArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)
        var lastVal = 0f
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val env = (1f - t) * maxVolume
            val white = (Random.nextFloat() * 2f - 1f)
            // Low-pass filter if heavy
            val filtered = if (isHeavy) {
                lastVal = lastVal * 0.75f + white * 0.25f
                lastVal
            } else {
                white
            }
            val sample = (filtered * env * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateChime(frequencies: List<Float>, noteDuration: Float): ShortArray {
        val noteSamples = (sampleRate * noteDuration).toInt()
        val totalSamples = noteSamples * frequencies.size
        val buffer = ShortArray(totalSamples)
        for ((noteIdx, freq) in frequencies.withIndex()) {
            var phase = 0.0
            for (i in 0 until noteSamples) {
                val t = i.toDouble() / noteSamples
                val env = (1.0 - t) * 0.7
                phase += 2.0 * Math.PI * freq / sampleRate
                val sample = (sin(phase) * env * Short.MAX_VALUE).toInt()
                val globalIdx = noteIdx * noteSamples + i
                if (globalIdx < totalSamples) {
                    buffer[globalIdx] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
        }
        return buffer
    }

    private fun playBuffer(key: String) {
        if (!isSoundEnabled) return
        scope.launch {
            val samples = bufferCache[key] ?: return@launch
            try {
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, samples.size)
                audioTrack.play()
                // Auto release after sound finishes
                scope.launch {
                    kotlinx.coroutines.delay((samples.size * 1000L / sampleRate) + 100)
                    try {
                        audioTrack.stop()
                        audioTrack.release()
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {}
        }
    }

    fun playShoot(weaponId: WeaponId) {
        when (weaponId) {
            WeaponId.ASSAULT_RIFLE -> playBuffer("shoot_laser")
            WeaponId.PLASMA_SHOTGUN -> playBuffer("shoot_shotgun")
            WeaponId.SNIPER_RAILGUN -> playBuffer("shoot_railgun")
            WeaponId.HOMING_ROCKET -> playBuffer("shoot_rocket")
            WeaponId.TESLA_ARC -> playBuffer("shoot_tesla")
            WeaponId.FLAME_BLASTER -> playBuffer("shoot_flame")
        }
        vibrate(15)
    }

    fun playHit() {
        playBuffer("hit")
        vibrate(20)
    }

    fun playExplosion() {
        playBuffer("explosion")
        vibrate(60)
    }

    fun playPowerUp() {
        playBuffer("powerup")
        vibrate(30)
    }

    fun playShieldBreak() {
        playBuffer("shield_break")
        vibrate(45)
    }

    fun playDash() {
        playBuffer("dash")
        vibrate(25)
    }

    fun playVictory() {
        playBuffer("victory")
        vibrate(80)
    }

    private fun vibrate(durationMs: Long) {
        if (!isVibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }
}
