package com.gnaanaa.mtimer.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gnaanaa.mtimer.domain.model.Preset

@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val prepareSeconds: Int,
    val startSoundId: String,
    val durationSeconds: Int,
    val endSoundId: String,
    val colorAccent: Int?,
    val createdAt: Long,
    val syncedAt: Long?
)

fun PresetEntity.toDomain() = Preset(
    id = id,
    name = name,
    prepareSeconds = prepareSeconds,
    startSoundId = startSoundId,
    durationSeconds = durationSeconds,
    endSoundId = endSoundId,
    colorAccent = colorAccent,
    createdAt = createdAt,
    syncedAt = syncedAt
)

fun Preset.toEntity() = PresetEntity(
    id = id,
    name = name,
    prepareSeconds = prepareSeconds,
    startSoundId = startSoundId,
    durationSeconds = durationSeconds,
    endSoundId = endSoundId,
    colorAccent = colorAccent,
    createdAt = createdAt,
    syncedAt = syncedAt
)
