package com.gnaanaa.mtimer.data.repository

import com.gnaanaa.mtimer.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getAllSessions(): Flow<List<Session>>
    suspend fun saveSession(session: Session): Long
    suspend fun getAllSessionsList(): List<Session>
    suspend fun getUnsyncedSessions(): List<Session>
    suspend fun markSynced(sessionId: Long, recordId: String)
}
