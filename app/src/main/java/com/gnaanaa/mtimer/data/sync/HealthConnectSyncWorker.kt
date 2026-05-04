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
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.gnaanaa.mtimer.data.db.PresetDao
import com.gnaanaa.mtimer.data.repository.SessionRepository
import java.time.Instant
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
        
        if (!isGoogleFitEnabled && !isHealthConnectEnabled) {
             android.util.Log.d("HealthConnect", "Both integrations disabled, skipping work.")
             return Result.success()
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

                // Fetch Heart Rate samples for use in both/either sync provider
                val hrSamples = try {
                    fetchHeartRateRange(
                        applicationContext,
                        Instant.ofEpochMilli(session.startTime),
                        Instant.ofEpochMilli(if (session.endTime > session.startTime) session.endTime else session.startTime + (session.durationSeconds * 1000L))
                    )
                } catch (e: Exception) {
                    emptyList()
                }

                // Prioritize Google Fit for AIA Vitality
                var fitSuccess = false
                if (isGoogleFitEnabled) {
                    try {
                        val samplesForFit = hrSamples.map { it.time.toEpochMilli() to it.beatsPerMinute.toDouble() }
                        fitSuccess = syncSessionToGoogleFit(applicationContext, session, samplesForFit)
                    } catch (e: Exception) {
                        android.util.Log.e("GoogleFit", "Fit sync failed for ${session.id}", e)
                    }
                }

                // Sync to Health Connect
                // We ONLY sync the session to Health Connect if Google Fit is disabled or failed.
                // This prevents the "double entry" since Google Fit will sync its session back to HC anyway.
                try {
                    if (isHealthConnectEnabled && hasAnyWritePermission(applicationContext)) {
                        // If fitSuccess is true, we still might want to sync the Heart Rate record 
                        // from MTimer if we trust MTimer's HR record more, but to satisfy the 
                        // "no double entry" request, we skip the HC session record.
                        if (!fitSuccess) {
                            syncSessionToHealthConnect(
                                context = applicationContext,
                                session = session,
                                presetName = presetName
                            )
                        } else {
                            android.util.Log.d("HealthConnect", "Skipping HC session record as Google Fit sync was successful (AIA Priority)")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("HealthConnect", "HC sync failed for ${session.id}", e)
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

    companion object {
        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<HealthConnectSyncWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
