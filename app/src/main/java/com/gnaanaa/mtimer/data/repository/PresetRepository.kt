package com.gnaanaa.mtimer.data.repository

import com.gnaanaa.mtimer.domain.model.Preset
import kotlinx.coroutines.flow.Flow

interface PresetRepository {
    fun getAllPresets(): Flow<List<Preset>>
    suspend fun getAllPresetsList(): List<Preset>
    suspend fun getPresetById(id: String): Preset?
    suspend fun savePreset(preset: Preset)
    suspend fun deletePreset(preset: Preset)
}
