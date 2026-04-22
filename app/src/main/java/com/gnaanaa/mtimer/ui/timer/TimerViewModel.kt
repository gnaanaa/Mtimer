package com.gnaanaa.mtimer.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gnaanaa.mtimer.domain.model.TimerState
import com.gnaanaa.mtimer.domain.usecase.StopTimerUseCase
import com.gnaanaa.mtimer.service.MeditationForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val stopTimerUseCase: StopTimerUseCase
) : ViewModel() {

    val timerState: StateFlow<TimerState> = MeditationForegroundService.timerState

    fun stopTimer() {
        viewModelScope.launch {
            stopTimerUseCase()
        }
    }
}
