package com.gnaanaa.mtimer.domain.usecase

import com.gnaanaa.mtimer.data.repository.PresetRepository
import com.gnaanaa.mtimer.domain.model.Preset
import javax.inject.Inject

class DeletePresetUseCase @Inject constructor(
    private val repository: PresetRepository
) {
    suspend operator fun invoke(preset: Preset) {
        repository.deletePreset(preset)
    }
}
