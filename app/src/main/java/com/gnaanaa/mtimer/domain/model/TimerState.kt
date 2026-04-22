package com.gnaanaa.mtimer.domain.model

sealed class TimerState {
    object Idle : TimerState()
    data class Preparing(val remainingSeconds: Int, val totalSeconds: Int) : TimerState()
    data class Running(val remainingSeconds: Int, val totalSeconds: Int, val presetName: String?) : TimerState()
    data class Ending(val presetName: String?) : TimerState()   // end sound playing
    data class Completed(val sessionId: Long) : TimerState()
}
