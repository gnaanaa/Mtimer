package com.gnaanaa.mtimer.ui.preset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gnaanaa.mtimer.data.repository.PresetRepository
import com.gnaanaa.mtimer.domain.model.Preset
import com.gnaanaa.mtimer.domain.usecase.DeletePresetUseCase
import com.gnaanaa.mtimer.domain.usecase.SavePresetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PresetViewModel @Inject constructor(
    private val repository: PresetRepository,
    private val savePresetUseCase: SavePresetUseCase,
    private val deletePresetUseCase: DeletePresetUseCase
) : ViewModel() {

    val presets: StateFlow<List<Preset>> = repository.getAllPresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getPreset(id: String): Preset? {
        return repository.getPresetById(id)
    }

    fun savePreset(preset: Preset) {
        viewModelScope.launch {
            savePresetUseCase(preset)
        }
    }

    fun deletePreset(preset: Preset) {
        viewModelScope.launch {
            deletePresetUseCase(preset)
        }
    }
}
