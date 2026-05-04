package com.gnaanaa.mtimer.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gnaanaa.mtimer.domain.model.Session

import com.google.gson.annotations.SerializedName

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) @SerializedName("id") val id: Long = 0,
    @SerializedName("presetId") val presetId: String?,
    @SerializedName("startTime") val startTime: Long,
    @SerializedName("endTime") val endTime: Long,
    @SerializedName("durationSeconds") val durationSeconds: Int,
    @SerializedName("completed") val completed: Boolean,
    @SerializedName("healthConnectSynced") val healthConnectSynced: Boolean = false,
    @SerializedName("healthConnectRecordId") val healthConnectRecordId: String? = null
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
