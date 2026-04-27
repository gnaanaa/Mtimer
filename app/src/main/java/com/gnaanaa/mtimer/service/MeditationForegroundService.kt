package com.gnaanaa.mtimer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.gnaanaa.mtimer.MainActivity
import com.gnaanaa.mtimer.R
import com.gnaanaa.mtimer.data.repository.SessionRepository
import com.gnaanaa.mtimer.data.sync.HealthConnectSyncWorker
import com.gnaanaa.mtimer.domain.model.Session
import com.gnaanaa.mtimer.domain.model.TimerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MeditationForegroundService : Service() {

    @Inject
    lateinit var sessionRepository: SessionRepository

    @Inject
    lateinit var soundPlayer: SoundPlayer

    @Inject
    lateinit var wakeLockManager: WakeLockManager

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"

        const val EXTRA_PRESET_ID = "EXTRA_PRESET_ID"
        const val EXTRA_PRESET_NAME = "EXTRA_PRESET_NAME"
        const val EXTRA_DURATION_SECONDS = "EXTRA_DURATION_SECONDS"
        const val EXTRA_PREPARE_SECONDS = "EXTRA_PREPARE_SECONDS"
        const val EXTRA_START_SOUND_ID = "EXTRA_START_SOUND_ID"
        const val EXTRA_END_SOUND_ID = "EXTRA_END_SOUND_ID"

        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "meditation_timer_channel"

        private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
        val timerState = _timerState.asStateFlow()

        fun startTimer(context: Context, preset: com.gnaanaa.mtimer.domain.model.Preset) {
            val intent = Intent(context, MeditationForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_PRESET_ID, preset.id)
                putExtra(EXTRA_PRESET_NAME, preset.name)
                putExtra(EXTRA_DURATION_SECONDS, preset.durationSeconds)
                putExtra(EXTRA_PREPARE_SECONDS, preset.prepareSeconds)
                putExtra(EXTRA_START_SOUND_ID, preset.startSoundId)
                putExtra(EXTRA_END_SOUND_ID, preset.endSoundId)
            }
            context.startForegroundService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val presetId = intent.getStringExtra(EXTRA_PRESET_ID)
                val presetName = intent.getStringExtra(EXTRA_PRESET_NAME)
                val duration = intent.getIntExtra(EXTRA_DURATION_SECONDS, 600)
                val prepare = intent.getIntExtra(EXTRA_PREPARE_SECONDS, 0)
                val startSound = intent.getStringExtra(EXTRA_START_SOUND_ID) ?: "bell_tibetan"
                val endSound = intent.getStringExtra(EXTRA_END_SOUND_ID) ?: "bell_tibetan"
                startTimer(presetId, presetName, duration, prepare, startSound, endSound)
            }
            ACTION_STOP -> stopTimer()
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
        }
        return START_STICKY
    }

    private var startTimeMillis: Long = 0
    private var targetDurationSeconds: Int = 0
    private var currentPresetId: String? = null
    private var currentPresetName: String? = null

    private fun startTimer(
        presetId: String?,
        presetName: String?,
        duration: Int,
        prepare: Int,
        startSound: String,
        endSound: String
    ) {
        timerJob?.cancel()
        startTimeMillis = System.currentTimeMillis()
        targetDurationSeconds = duration
        currentPresetId = presetId
        currentPresetName = presetName
        
        timerJob = serviceScope.launch {
            if (prepare > 0) {
                _timerState.value = TimerState.Preparing(prepare, prepare)
                startForeground(NOTIFICATION_ID, createNotification("Preparing..."))
                
                var remainingPrepare = prepare
                while (remainingPrepare > 0) {
                    delay(1000)
                    remainingPrepare--
                    _timerState.value = TimerState.Preparing(remainingPrepare, prepare)
                    updateNotification("Preparing: $remainingPrepare s")
                }
            }

            // Start sound
            soundPlayer.playSound(startSound)
            
            _timerState.value = TimerState.Running(duration, duration, presetName)
            startForeground(NOTIFICATION_ID, createNotification("Meditating..."))
            wakeLockManager.acquire()

            var remaining = duration
            while (remaining > 0) {
                if (_timerState.value is TimerState.Running) {
                    delay(1000)
                    remaining--
                    _timerState.value = TimerState.Running(remaining, duration, presetName)
                    updateNotification(formatTime(remaining))
                    
                    if (remaining % 10 == 0) {
                        android.util.Log.v("MeditationForegroundService", "Timer loop: ${remaining}s remaining...")
                    }
                } else {
                    // Paused state handled here implicitly by not decrementing
                    delay(500)
                }
            }

            // End sound
            _timerState.value = TimerState.Ending(presetName)
            android.util.Log.d("MeditationForegroundService", "Timer finished naturally, playing end sound")
            soundPlayer.playSound(endSound)
            
            saveAndSyncSession(true)
            
            wakeLockManager.release()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private suspend fun saveAndSyncSession(completed: Boolean) {
        val endTimeMillis = System.currentTimeMillis()
        val elapsedSeconds = ((endTimeMillis - startTimeMillis) / 1000).toInt()
        
        // Only save if we actually meditated for at least a few seconds
        if (elapsedSeconds < 5) return

        val session = Session(
            presetId = currentPresetId,
            startTime = startTimeMillis,
            endTime = endTimeMillis,
            durationSeconds = if (completed) targetDurationSeconds else elapsedSeconds,
            completed = completed
        )
        
        try {
            val sessionId = sessionRepository.saveSession(session)
            android.util.Log.d("MeditationForegroundService", "Session saved (ID: $sessionId, completed: $completed), enqueuing sync")
            
            val syncRequest = OneTimeWorkRequestBuilder<HealthConnectSyncWorker>().build()
            WorkManager.getInstance(applicationContext).enqueue(syncRequest)
            
            if (completed) {
                _timerState.value = TimerState.Completed(sessionId)
            }
        } catch (e: Exception) {
            android.util.Log.e("MeditationForegroundService", "Error saving session", e)
        }
    }

    private fun pauseTimer() {
        // Implementation for pause
    }

    private fun resumeTimer() {
        // Implementation for resume
    }

    private fun stopTimer() {
        android.util.Log.d("MeditationForegroundService", "stopTimer() called")
        serviceScope.launch {
            if (_timerState.value is TimerState.Running) {
                android.util.Log.d("MeditationForegroundService", "Timer stopped early. Saving partial session...")
                saveAndSyncSession(false)
            }
            timerJob?.cancel()
            _timerState.value = TimerState.Idle
            wakeLockManager.release()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun createNotificationChannel() {
        val name = "Meditation Timer"
        val descriptionText = "Shows active meditation timer"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(content: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("MTimer")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use a better icon later
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(content: String) {
        val notification = createNotification(content)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%02d:%02d".format(m, s)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        timerJob?.cancel()
        wakeLockManager.release()
        soundPlayer.release()
        super.onDestroy()
    }
}
