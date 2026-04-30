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
import kotlinx.coroutines.flow.first

import androidx.health.connect.client.feature.ExperimentalMindfulnessSessionApi
import com.gnaanaa.mtimer.data.datastore.UserPreferencesDataStore

@OptIn(ExperimentalMindfulnessSessionApi::class)
@HiltWorker
class HealthConnectSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val sessionRepository: SessionRepository,
    private val presetDao: PresetDao,
    private val userPreferencesDataStore: UserPreferencesDataStore
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        android.util.Log.d("HealthConnect", "SyncWorker starting...")
        
        val isGoogleFitEnabled = userPreferencesDataStore.isGoogleFitEnabled.first()
        val isHealthConnectEnabled = userPreferencesDataStore.isHealthConnectEnabled.first()

        val unsyncedSessions = sessionRepository.getUnsyncedSessions()
        android.util.Log.d("HealthConnect", "Found ${unsyncedSessions.size} unsynced sessions in database")
        
        var successCount = 0
        for (session in unsyncedSessions) {
            try {
                android.util.Log.d("HealthConnect", "Syncing session ID: ${session.id}, Duration: ${session.durationSeconds}s")
                
                val presetName = session.presetId?.let { 
                    presetDao.getPresetById(it)?.name 
                }
                
                // Sync to Health Connect
                try {
                    if (isHealthConnectEnabled && hasAnyWritePermission(applicationContext)) {
                        syncSessionToHealthConnect(
                            context = applicationContext,
                            session = session,
                            presetName = presetName
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("HealthConnect", "HC sync failed for ${session.id}", e)
                }

                // Sync to Google Fit
                if (isGoogleFitEnabled) {
                    try {
                        syncSessionToGoogleFit(applicationContext, session)
                    } catch (e: Exception) {
                        android.util.Log.e("GoogleFit", "Fit sync failed for ${session.id}", e)
                    }
                }
                
                sessionRepository.markSynced(session.id, "synced_${System.currentTimeMillis()}")
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
                com.gnaanaa.mtimer.widget.worker.WidgetUpdateWorker.updateNow(applicationContext)
                android.util.Log.d("HealthConnect", "Widget update enqueued after $successCount syncs")
            } catch (e: Exception) {
                android.util.Log.e("HealthConnect", "Failed to enqueue widget update", e)
            }
        }
        
        android.util.Log.d("HealthConnect", "SyncWorker finished. Synced $successCount/${unsyncedSessions.size} records.")
        return Result.success()
    }
}
