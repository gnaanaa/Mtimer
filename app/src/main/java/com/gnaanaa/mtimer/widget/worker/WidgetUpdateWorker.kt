package com.gnaanaa.mtimer.widget.worker

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.gnaanaa.mtimer.data.db.MTimerDatabase
import com.gnaanaa.mtimer.data.sync.getWeeklyMindfulnessMinutes
import com.gnaanaa.mtimer.widget.MTimerWidget
import com.gnaanaa.mtimer.widget.MTimerWidgetStateDefinition
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import androidx.hilt.work.HiltWorker
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val db: MTimerDatabase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()
        val weekAgo = now - TimeUnit.DAYS.toMillis(7)
        
        // Sum local sessions
        val localSessions = db.sessionDao().getAllSessionsList()
        val localMinutes = localSessions
            .filter { it.startTime >= weekAgo && it.completed }
            .sumOf { it.durationSeconds } / 60

        // Sum Health Connect (sessions from other apps)
        val hcMinutes = try {
            getWeeklyMindfulnessMinutes(applicationContext)
        } catch (e: Exception) {
            0
        }

        // Use the larger of the two or combine? 
        // If we sync our sessions to HC, combining might double-count.
        // Let's take the max to be safe, or just HC if available as it should include ours.
        val totalMinutes = maxOf(localMinutes.toInt(), hcMinutes)

        // Get last 3 used presets
        val lastUsedPresetIds = localSessions
            .mapNotNull { it.presetId }
            .distinct()
            .take(3)
        
        val allPresets = db.presetDao().getAllPresetsList()
        val recentPresets = if (lastUsedPresetIds.isEmpty()) {
            allPresets.take(3)
        } else {
            lastUsedPresetIds.mapNotNull { id -> allPresets.find { it.id == id } }
        }

        val presetsJson = Json.encodeToString(recentPresets)

        val manager = GlanceAppWidgetManager(applicationContext)
        val glanceIds = manager.getGlanceIds(MTimerWidget::class.java)

        glanceIds.forEach { glanceId ->
            updateAppWidgetState(applicationContext, MTimerWidgetStateDefinition, glanceId) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[intPreferencesKey("weekly_minutes")] = totalMinutes
                    this[stringPreferencesKey("presets_json")] = presetsJson
                }
            }
            MTimerWidget().update(applicationContext, glanceId)
        }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "MTimerWidgetUpdateWork"

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(30, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun updateNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
