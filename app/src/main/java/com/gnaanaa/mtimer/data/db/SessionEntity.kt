package com.gnaanaa.mtimer.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gnaanaa.mtimer.domain.model.Session

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val presetId: String?,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Int,
    val completed: Boolean,
    val healthConnectSynced: Boolean = false,
    val healthConnectRecordId: String? = null
)

fun SessionEntity.toDomain() = Session(
    id = id,
    presetId = presetId,
    startTime = startTime,
    endTime = endTime,
    durationSeconds = durationSeconds,
    completed = completed,
    healthConnectSynced = healthConnectSynced,
    healthConnectRecordId = healthConnectRecordId
)

fun Session.toEntity() = SessionEntity(
    id = id,
    presetId = presetId,
    startTime = startTime,
    endTime = endTime,
    durationSeconds = durationSeconds,
    completed = completed,
    healthConnectSynced = healthConnectSynced,
    healthConnectRecordId = healthConnectRecordId
)
