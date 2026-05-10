package com.gnaanaa.mtimer.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class WeeklyStats(
    val weekStart: String,
    val totalSeconds: Long
)

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Query("SELECT * FROM sessions WHERE healthConnectSynced = 0")
    suspend fun getUnsyncedSessions(): List<SessionEntity>

    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    suspend fun getAllSessionsList(): List<SessionEntity>

    @Query("UPDATE sessions SET healthConnectSynced = 1, healthConnectRecordId = :recordId WHERE id = :sessionId")
    suspend fun markSynced(sessionId: Long, recordId: String)

    @Query("SELECT COUNT(*) FROM sessions")
    fun getSessionCount(): Flow<Int>

    @Query("SELECT SUM(durationSeconds) FROM sessions")
    fun getTotalDuration(): Flow<Long?>

    @Query("""
        SELECT date(startTime / 1000, 'unixepoch', 'weekday 1', '-7 days') as weekStart, 
               SUM(durationSeconds) as totalSeconds 
        FROM sessions 
        WHERE completed = 1 
        GROUP BY weekStart 
        ORDER BY weekStart DESC
    """)
    fun getWeeklyStats(): Flow<List<WeeklyStats>>
}
