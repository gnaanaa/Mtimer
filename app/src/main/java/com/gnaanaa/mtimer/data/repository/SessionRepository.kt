package com.gnaanaa.mtimer.data.repository

import com.gnaanaa.mtimer.domain.model.Session
import kotlinx.coroutines.flow.Flow
import com.gnaanaa.mtimer.data.db.WeeklyStats

interface SessionRepository {
    fun getAllSessions(): Flow<List<Session>>
    suspend fun saveSession(session: Session): Long
    suspend fun getAllSessionsList(): List<Session>
    suspend fun getUnsyncedSessions(): List<Session>
    suspend fun markSynced(sessionId: Long, recordId: String)
    fun getSessionCount(): Flow<Int>
    fun getTotalDuration(): Flow<Long>
    fun getWeeklyStats(): Flow<List<WeeklyStats>>
}
