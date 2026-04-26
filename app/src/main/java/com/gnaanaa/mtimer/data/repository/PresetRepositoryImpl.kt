package com.gnaanaa.mtimer.data.repository

import com.gnaanaa.mtimer.data.db.PresetDao
import com.gnaanaa.mtimer.data.db.toDomain
import com.gnaanaa.mtimer.data.db.toEntity
import com.gnaanaa.mtimer.domain.model.Preset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresetRepositoryImpl @Inject constructor(
    private val presetDao: PresetDao
) : PresetRepository {
    override fun getAllPresets(): Flow<List<Preset>> {
        return presetDao.getAllPresets().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAllPresetsList(): List<Preset> {
        return presetDao.getAllPresetsList().map { it.toDomain() }
    }

    override suspend fun getPresetById(id: String): Preset? {
        return presetDao.getPresetById(id)?.toDomain()
    }

    override suspend fun savePreset(preset: Preset) {
        presetDao.insertPreset(preset.toEntity())
    }

    override suspend fun deletePreset(preset: Preset) {
        presetDao.deletePreset(preset.toEntity())
    }
}
