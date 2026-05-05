package com.gnaanaa.mtimer.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ln

@Singleton
class SoundPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mediaPlayer: MediaPlayer? = null

    /**
     * Standard play for short sounds (like intervals)
     */
    suspend fun playSound(soundId: String, waitAfterMs: Long = 0) {
        if (soundId == "silence") return
        
        stopCurrentSound()
        
        val player = createPlayer(soundId) ?: return
        mediaPlayer = player
        
        try {
            if (requestAudioFocus()) {
                player.start()
                if (waitAfterMs > 0) delay(waitAfterMs)
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            android.util.Log.e("SoundPlayer", "Error playing sound: $soundId", e)
            playFallbackTone()
        }
    }

    /**
     * Plays a sound that persists for 10 seconds and then fades out over the last 3 seconds.
     * Note: Does NOT loop.
     */
    suspend fun playSoundWithFade(soundId: String) {
        if (soundId == "silence") return

        stopCurrentSound()

        val player = createPlayer(soundId) ?: return
        mediaPlayer = player
        
        try {
            if (requestAudioFocus()) {
                player.isLooping = false // Explicitly no looping
                player.start()

                // Persist for 7 seconds at full volume
                // This assumes the sound file itself is long enough or we just want to keep the "vibration" alive
                delay(7000)

                // Fade out over the last 3 seconds
                val fadeDuration = 3000L
                val steps = 30
                val stepMs = fadeDuration / steps
                
                for (i in steps downTo 0) {
                    if (mediaPlayer != player) break // STOPPED or replaced
                    val logVolume = 1 - (ln((steps - i + 1).toDouble()) / ln((steps + 1).toDouble())).toFloat()
                    try {
                        player.setVolume(logVolume, logVolume)
                    } catch (_: IllegalStateException) {
                        break
                    }
                    delay(stepMs)
                }
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            android.util.Log.e("SoundPlayer", "Error playing sound with fade: $soundId", e)
            playFallbackTone()
        } finally {
            if (mediaPlayer == player) {
                stopCurrentSound()
            }
        }
    }

    fun stopCurrentSound() {
        val playerToRelease = mediaPlayer
        mediaPlayer = null
        try {
            playerToRelease?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        } catch (e: Exception) {
            android.util.Log.e("SoundPlayer", "Error stopping player", e)
        }
    }

    private fun createPlayer(soundId: String): MediaPlayer? {
        val assetPath = when (soundId) {
            "bell_tibetan" -> "sounds/bell_tibetan.mp3"
            "bell_singing" -> "sounds/bell_singing.mp3"
            "chime_soft" -> "sounds/chime_soft.mp3"
            "bell_simple" -> "sounds/bell_simple.mp3"
            else -> null
        }

        return try {
            val player = MediaPlayer()
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )

            if (assetPath != null) {
                val descriptor = context.assets.openFd(assetPath)
                player.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                descriptor.close()
            } else {
                val soundFile = java.io.File(context.filesDir, "sounds/$soundId")
                if (soundFile.exists()) {
                    player.setDataSource(soundFile.absolutePath)
                } else {
                    return null
                }
            }
            player.prepare()
            player
        } catch (e: Exception) {
            android.util.Log.e("SoundPlayer", "Failed to create player for $soundId", e)
            null
        }
    }

    private fun requestAudioFocus(): Boolean {
        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()

        val result = audioManager.requestAudioFocus(focusRequest)
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun playFallbackTone() {
        try {
            val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 500)
        } catch (e: Exception) {
            android.util.Log.e("SoundPlayer", "Failed to play fallback tone", e)
        }
    }

    fun release() {
        stopCurrentSound()
    }
}
