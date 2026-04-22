package com.gnaanaa.mtimer.data.repository

import com.gnaanaa.mtimer.data.db.SessionDao
import com.gnaanaa.mtimer.data.db.toDomain
import com.gnaanaa.mtimer.data.db.toEntity
import com.gnaanaa.mtimer.domain.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao
) : SessionRepository {
    override fun getAllSessions(): Flow<List<Session>> {
        return sessionDao.getAllSessions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveSession(session: Session): Long {
        return sessionDao.insertSession(session.toEntity())
    }

    override suspend fun getUnsyncedSessions(): List<Session> {
        return sessionDao.getUnsyncedSessions().map { it.toDomain() }
    }

    override suspend fun markSynced(sessionId: Long, recordId: String) {
        sessionDao.markSynced(sessionId, recordId)
    }
}
