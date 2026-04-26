package com.gnaanaa.mtimer.ui.preset

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gnaanaa.mtimer.data.repository.PresetRepository
import com.gnaanaa.mtimer.domain.model.Preset
import com.gnaanaa.mtimer.domain.usecase.DeletePresetUseCase
import com.gnaanaa.mtimer.domain.usecase.SavePresetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class PresetViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PresetRepository,
    private val savePresetUseCase: SavePresetUseCase,
    private val deletePresetUseCase: DeletePresetUseCase
) : ViewModel() {

    val presets: StateFlow<List<Preset>> = repository.getAllPresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _customSounds = MutableStateFlow<List<String>>(emptyList())
    val customSounds = _customSounds.asStateFlow()

    init {
        loadCustomSounds()
    }

    private fun loadCustomSounds() {
        viewModelScope.launch {
            val soundDir = File(context.filesDir, "sounds")
            if (soundDir.exists()) {
                _customSounds.value = soundDir.listFiles()?.map { it.name } ?: emptyList()
            }
        }
    }

    fun importSound(uri: Uri, name: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val soundDir = File(context.filesDir, "sounds")
                    if (!soundDir.exists()) soundDir.mkdirs()

                    val destFile = File(soundDir, name)
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(destFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    loadCustomSounds()
                } catch (e: Exception) {
                    android.util.Log.e("PresetViewModel", "Failed to import sound", e)
                }
            }
        }
    }

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
