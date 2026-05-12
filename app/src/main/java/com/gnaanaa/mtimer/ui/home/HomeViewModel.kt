package com.gnaanaa.mtimer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gnaanaa.mtimer.data.repository.SessionRepository
import com.gnaanaa.mtimer.domain.model.Session
import com.gnaanaa.mtimer.domain.usecase.StartTimerUseCase
import com.gnaanaa.mtimer.data.repository.PresetRepository
import com.gnaanaa.mtimer.domain.model.Preset
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val presets: List<Preset> = emptyList(),
    val recentSessions: List<Session> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val presetRepository: PresetRepository,
    private val startTimerUseCase: StartTimerUseCase
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        presetRepository.getAllPresets(),
        sessionRepository.getAllSessions().map { it.take(4) }
    ) { presets, sessions ->
        HomeUiState(
            isLoading = false,
            presets = presets,
            recentSessions = sessions
        )
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun startTimer(preset: Preset) {
        startTimerUseCase(preset)
    }
}
