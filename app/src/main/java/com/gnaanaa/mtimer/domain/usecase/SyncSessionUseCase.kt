package com.gnaanaa.mtimer.domain.usecase

import android.content.Context
import com.gnaanaa.mtimer.data.db.PresetDao
import com.gnaanaa.mtimer.data.repository.SessionRepository
import com.gnaanaa.mtimer.data.sync.syncSessionToHealthConnect
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SyncSessionUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionRepository: SessionRepository,
    private val presetDao: PresetDao
) {
    suspend operator fun invoke() {
        val unsynced = sessionRepository.getUnsyncedSessions()
        for (session in unsynced) {
            try {
                val presetName = session.presetId?.let { 
                    presetDao.getPresetById(it)?.name 
                }
                syncSessionToHealthConnect(context, session, presetName)
                sessionRepository.markSynced(session.id, "hc_synced")
            } catch (e: Exception) {
                // Log or handle error
            }
        }
    }
}
