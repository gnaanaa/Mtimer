package com.gnaanaa.mtimer.data.sync

import android.content.Context
import android.os.Build
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
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
            val client = HealthConnectClient.getOrCreate(healthContext)
            val permissions = setOf(
                HealthPermission.getReadPermission(MindfulnessSessionRecord::class),
                HealthPermission.getWritePermission(MindfulnessSessionRecord::class)
            )
            
            val grantedPermissions = client.permissionController.getGrantedPermissions()
            android.util.Log.d("HealthConnect", "Granted permissions in worker: $grantedPermissions")
            
            // On some devices/SDK versions, the permission name might vary slightly
            val hasMindfulness = grantedPermissions.any { 
                it.contains("MINDFULNESS", ignoreCase = true) 
            }
            
            if (!hasMindfulness) {
                android.util.Log.w("HealthConnect", "Mindfulness permissions not found in granted list. Aborting sync.")
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
                
                syncSessionToHealthConnect(
                    context = applicationContext,
                    session = session,
                    presetName = presetName
                )
                
                sessionRepository.markSynced(session.id, "hc_synced_${System.currentTimeMillis()}")
                successCount++
                android.util.Log.i("HealthConnect", "Successfully synced and marked session ${session.id}")
                
                // Update widget after successful sync
                try {
                    MTimerWidget().updateAll(applicationContext)
                } catch (e: Exception) {
                    android.util.Log.e("HealthConnect", "Failed to update widget", e)
                }
            } catch (e: Exception) {
                android.util.Log.e("HealthConnect", "Failed to sync session ${session.id}", e)
                // Continue with next session, don't abort everything
            }
        }
        
        android.util.Log.d("HealthConnect", "SyncWorker finished. Synced $successCount/${unsyncedSessions.size} records.")
        return Result.success()
    }
}
