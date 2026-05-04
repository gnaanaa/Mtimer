package com.gnaanaa.mtimer.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gnaanaa.mtimer.domain.model.Preset
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("prepareSeconds") val prepareSeconds: Int,
    @SerializedName("startSoundId") val startSoundId: String,
    @SerializedName("durationSeconds") val durationSeconds: Int,
    @SerializedName("endSoundId") val endSoundId: String,
    @SerializedName("colorAccent") val colorAccent: Int?,
    @SerializedName("createdAt") val createdAt: Long,
    @SerializedName("syncedAt") val syncedAt: Long?
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
