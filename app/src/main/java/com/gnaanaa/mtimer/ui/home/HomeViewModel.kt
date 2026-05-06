package com.gnaanaa.mtimer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gnaanaa.mtimer.data.repository.SessionRepository
import com.gnaanaa.mtimer.domain.model.Session
import com.gnaanaa.mtimer.domain.usecase.StartTimerUseCase
import com.gnaanaa.mtimer.data.repository.PresetRepository
import com.gnaanaa.mtimer.domain.model.Preset
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val presetRepository: PresetRepository,
    private val startTimerUseCase: StartTimerUseCase
) : ViewModel() {

    val recentSessions: StateFlow<List<Session>> = sessionRepository.getAllSessions()
        .map { it.take(4) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val presets: StateFlow<List<Preset>> = presetRepository.getAllPresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun startTimer(preset: Preset) {
        startTimerUseCase(preset)
    }
}
