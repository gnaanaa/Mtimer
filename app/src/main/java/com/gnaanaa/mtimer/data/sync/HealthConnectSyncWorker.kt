package com.gnaanaa.mtimer.data.sync

import android.content.Context
import android.os.Build
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.MindfulnessSessionRecord
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gnaanaa.mtimer.data.db.PresetDao
import com.gnaanaa.mtimer.data.repository.SessionRepository
import com.gnaanaa.mtimer.widget.MTimerWidget
import androidx.glance.appwidget.updateAll
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

import androidx.health.connect.client.feature.ExperimentalMindfulnessSessionApi

@OptIn(ExperimentalMindfulnessSessionApi::class)
@HiltWorker
class HealthConnectSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val sessionRepository: SessionRepository,
    private val presetDao: PresetDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        android.util.Log.d("HealthConnect", "SyncWorker starting...")
        
        try {
            val healthContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                applicationContext.createAttributionContext("health_connect_sync")
            } else {
                applicationContext
            }
            
            if (!hasAnyWritePermission(healthContext)) {
                android.util.Log.w("HealthConnect", "No write permissions found (Mindfulness or Exercise). Aborting sync.")
                return Result.failure()
            }
        } catch (e: Exception) {
            android.util.Log.e("HealthConnect", "Failed to check permissions", e)
            return Result.retry()
        }

        val unsyncedSessions = sessionRepository.getUnsyncedSessions()
        android.util.Log.d("HealthConnect", "Found ${unsyncedSessions.size} unsynced sessions in database")
        
        var successCount = 0
        for (session in unsyncedSessions) {
            try {
                android.util.Log.d("HealthConnect", "Syncing session ID: ${session.id}, Duration: ${session.durationSeconds}s")
                
                val presetName = session.presetId?.let { 
                    presetDao.getPresetById(it)?.name 
                }
                
                // Use attribution context to ensure MTimer is identified as the source app
                val healthContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    applicationContext.createAttributionContext("health_connect_sync")
                } else {
                    applicationContext
                }
                
                syncSessionToHealthConnect(
                    context = healthContext,
                    session = session,
                    presetName = presetName
                )
                
                sessionRepository.markSynced(session.id, "hc_synced_${System.currentTimeMillis()}")
                successCount++
                android.util.Log.i("HealthConnect", "Successfully synced and marked session ${session.id}")
            } catch (e: Exception) {
                android.util.Log.e("HealthConnect", "Failed to sync session ${session.id}", e)
                // Continue with next session, don't abort everything
            }
        }

        // Update widget once after all sessions are synced
        if (successCount > 0) {
            try {
                MTimerWidget().updateAll(applicationContext)
                android.util.Log.d("HealthConnect", "Widget updated after $successCount syncs")
            } catch (e: Exception) {
                android.util.Log.e("HealthConnect", "Failed to update widget", e)
            }
        }
        
        android.util.Log.d("HealthConnect", "SyncWorker finished. Synced $successCount/${unsyncedSessions.size} records.")
        return Result.success()
    }
}
