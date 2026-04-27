package com.gnaanaa.mtimer.widget.worker

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
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
        val minutes = try {
            getWeeklyMindfulnessMinutes(applicationContext)
        } catch (e: Exception) {
            0
        }

        val presets = db.presetDao().getAllPresetsList()
        val presetsJson = Json.encodeToString(presets)

        val manager = GlanceAppWidgetManager(applicationContext)
        val glanceIds = manager.getGlanceIds(MTimerWidget::class.java)

        glanceIds.forEach { glanceId ->
            updateAppWidgetState(applicationContext, MTimerWidgetStateDefinition, glanceId) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[intPreferencesKey("weekly_minutes")] = minutes
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
    }
}
