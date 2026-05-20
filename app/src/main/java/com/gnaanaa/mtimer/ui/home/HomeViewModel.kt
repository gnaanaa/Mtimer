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

import com.gnaanaa.mtimer.data.sync.fetchHeartRateRange
import androidx.health.connect.client.records.HeartRateRecord
import com.gnaanaa.mtimer.data.datastore.UserPreferencesDataStore
import java.time.Instant
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val isLoading: Boolean = true,
    val presets: List<Preset> = emptyList(),
    val recentSessions: List<Session> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionRepository: SessionRepository,
    private val presetRepository: PresetRepository,
    private val startTimerUseCase: StartTimerUseCase,
    private val userPreferencesDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _heartRateSamples = MutableStateFlow<List<HeartRateRecord.Sample>>(emptyList())
    val heartRateSamples = _heartRateSamples.asStateFlow()

    val showHomeHint: StateFlow<Boolean> = userPreferencesDataStore.homeHintShown
        .map { !it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

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

    fun fetchHeartRate(session: Session) {
        viewModelScope.launch {
            val start = Instant.ofEpochMilli(session.startTime)
            val end = if (session.endTime > session.startTime) {
                Instant.ofEpochMilli(session.endTime)
            } else {
                start.plusSeconds(session.durationSeconds.toLong())
            }
            _heartRateSamples.value = fetchHeartRateRange(context, start, end)
        }
    }

    fun clearHeartRate() {
        _heartRateSamples.value = emptyList()
    }

    fun dismissHomeHint() {
        viewModelScope.launch {
            userPreferencesDataStore.setHomeHintShown()
        }
    }
}
