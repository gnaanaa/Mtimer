package com.gnaanaa.mtimer.ui.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import com.gnaanaa.mtimer.ui.home.DotMatrix
import com.gnaanaa.mtimer.domain.model.TimerState

@Composable
fun TimerScreen(
    onClose: () -> Unit,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val timerState by viewModel.timerState.collectAsState()
    val view = LocalView.current
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val window = (view.context as android.app.Activity).window
        val insetsController = WindowCompat.getInsetsController(window, view)
        
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        onDispose {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    // Handle completion
    androidx.compose.runtime.LaunchedEffect(timerState) {
        if (timerState is TimerState.Completed) {
            kotlinx.coroutines.delay(2000) // Show 00:00 for 2 seconds
            onClose()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        // Pause/Resume would go here
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val (timeText, labelText) = when (val state = timerState) {
                is TimerState.Preparing -> {
                    formatTime(state.remainingSeconds) to "Preparing"
                }
                is TimerState.Running -> {
                    formatTime(state.remainingSeconds) to (state.presetName ?: "Meditation")
                }
                is TimerState.Ending -> {
                    "00:00" to (state.presetName ?: "Meditation")
                }
                else -> "00:00" to ""
            }

            Text(
                text = buildAnnotatedString {
                    val parts = timeText.split(":")
                    if (parts.size == 2) {
                        append(parts[0])
                        withStyle(SpanStyle(baselineShift = BaselineShift(0.2f))) {
                            append(":")
                        }
                        append(parts[1])
                    } else {
                        append(timeText)
                    }
                },
                color = Color.White,
                fontSize = 84.sp,
                fontFamily = DotMatrix,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = labelText.uppercase(),
                color = Color.White.copy(alpha = 0.9f),
                fontFamily = DotMatrix,
                fontSize = 16.sp,
                letterSpacing = 2.sp
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .size(48.dp)
                .clickable {
                    viewModel.stopTimer()
                    onClose()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
