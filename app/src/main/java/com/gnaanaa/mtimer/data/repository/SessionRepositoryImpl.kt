package com.gnaanaa.mtimer.data.repository

import android.content.Context
import com.gnaanaa.mtimer.data.db.SessionDao
import com.gnaanaa.mtimer.data.db.toDomain
import com.gnaanaa.mtimer.data.db.toEntity
import com.gnaanaa.mtimer.data.sync.DriveSyncWorker
import com.gnaanaa.mtimer.domain.model.Session
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionDao: SessionDao
) : SessionRepository {
    override fun getAllSessions(): Flow<List<Session>> {
        return sessionDao.getAllSessions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveSession(session: Session): Long {
        val id = sessionDao.insertSession(session.toEntity())
        DriveSyncWorker.enqueue(context)
        return id
    }

    override suspend fun getAllSessionsList(): List<Session> {
        return sessionDao.getAllSessionsList().map { it.toDomain() }
    }

    override suspend fun getUnsyncedSessions(): List<Session> {
        return sessionDao.getUnsyncedSessions().map { it.toDomain() }
    }

    override suspend fun markSynced(sessionId: Long, recordId: String) {
        sessionDao.markSynced(sessionId, recordId)
    }
}
