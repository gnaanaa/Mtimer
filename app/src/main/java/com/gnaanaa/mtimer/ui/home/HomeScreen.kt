package com.gnaanaa.mtimer.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gnaanaa.mtimer.R
import com.gnaanaa.mtimer.domain.model.Preset
import com.gnaanaa.mtimer.domain.model.Session
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

@OptIn(ExperimentalTextApi::class)
val DotMatrix = FontFamily(
    Font(
        resId = R.font.doto_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.Setting("ROND", 100f), // Full round dots
            FontVariation.Setting("wght", 700f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val DotMatrixSquare = FontFamily(
    Font(
        resId = R.font.doto_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.Setting("ROND", 0f),  // square pixels
            FontVariation.Setting("wght", 700f)
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartTimer: () -> Unit,
    onNavigateToPresets: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val recentSessions by viewModel.recentSessions.collectAsState()
    val presets by viewModel.presets.collectAsState()

    var selectedSession by remember { mutableStateOf<Session?>(null) }
    var selectedPreset by remember { mutableStateOf<Preset?>(null) }

    val dateFormat = remember {
        SimpleDateFormat("MMM dd • HH:mm", Locale.getDefault())
    }

    LaunchedEffect(presets) {
        if (selectedPreset == null && presets.isNotEmpty()) {
            selectedPreset = presets.first()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "MTIMER",
                            fontFamily = DotMatrix,
                            letterSpacing = 4.sp
                        )
                        Text(
                            "RETURN TO YOURSELF, DAILY.",
                            fontFamily = DotMatrix,
                            fontSize = 12.sp,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(0.9f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            StartSessionButton(
                enabled = selectedPreset != null,
                selectedPreset = selectedPreset,
                onClick = {
                    selectedPreset?.let {
                        viewModel.startTimer(it)
                        onStartTimer()
                    }
                }
            )

            Spacer(Modifier.height(12.dp))

            // ── Rotary Dial with spring-snap ───────────────────────────────
            PresetDial(
                presets = presets,
                selectedPreset = selectedPreset,
                onSelected = { selectedPreset = it }
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "HISTORY",
                    fontFamily = DotMatrix,
                    fontSize = 12.sp,
                    letterSpacing = 3.sp
                )
            }

            if (recentSessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "NO SESSIONS YET",
                        fontFamily = DotMatrix,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(0.8f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(recentSessions.take(5), key = { it.id }) { session ->
                        HistoryRow(
                            session = session,
                            formattedDate = dateFormat.format(Date(session.startTime)).uppercase().alignColons(),
                            onClick = { selectedSession = session }
                        )
                    }
                }
            }
        }
    }

    selectedSession?.let {
        SessionDetailDialog(it) { selectedSession = null }
    }
}

// ── Start Button ───────────────────────────────────────────────────────────
@Composable
fun StartSessionButton(
    enabled: Boolean,
    selectedPreset: Preset?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(76.dp)
            .background(
                if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.large
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Lotus icon — left side
        Icon(
            imageVector = Icons.Default.Spa,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = if (enabled)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f)
        )

        Spacer(Modifier.width(16.dp))

        // Text block
        Column {
            Text(
                "START SESSION",
                fontFamily = DotMatrix,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                fontSize = 16.sp
            )
            selectedPreset?.let { preset ->
                val label = buildString {
                    append(preset.name.uppercase())
                    if (preset.durationSeconds > 0) {
                        val mins = preset.durationSeconds / 60
                        append("  (${mins}m)")
                    }
                }
                Text(
                    label,
                    fontFamily = DotMatrix,
                    fontSize = 13.sp,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.9f)
                )
            }
        }
    }
}

// ── Rotary Dial ────────────────────────────────────────────────────────────
@Composable
fun PresetDial(
    presets: List<Preset>,
    selectedPreset: Preset?,
    onSelected: (Preset) -> Unit
) {
    if (presets.isEmpty()) return

    val step = 360f / presets.size
    val rotation = remember { Animatable(0f) }

    fun indexFromRotation(rot: Float): Int =
        ((-rot / step).roundToInt()).mod(presets.size)

    val currentIndex by remember(presets) {
        derivedStateOf { indexFromRotation(rotation.value) }
    }

    LaunchedEffect(currentIndex, presets) {
        if (presets.isNotEmpty() && currentIndex < presets.size) {
            onSelected(presets[currentIndex])
        }
    }

    LaunchedEffect(selectedPreset, presets) {
        val targetIndex = presets.indexOf(selectedPreset)
        if (targetIndex != -1 && targetIndex != indexFromRotation(rotation.value)) {
            val rawTarget = -targetIndex * step
            val currentRot = rotation.value
            val turns = ((currentRot - rawTarget) / 360f).roundToInt()
            val targetRotation = rawTarget + turns * 360f
            rotation.animateTo(
                targetValue = targetRotation,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    val density = LocalDensity.current
    val radiusDp = 100.dp
    val radiusPx = with(density) { radiusDp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .pointerInput(presets) {
                val centerX = size.width / 2f
                val centerY = size.height / 2f

                forEachGesture {
                    coroutineScope {
                        awaitPointerEventScope {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var prevAngle = atan2(
                                down.position.y - centerY,
                                down.position.x - centerX
                            ).toDegrees()

                            launch { rotation.stop() }

                            do {
                                val event = awaitPointerEvent()
                                val pointer = event.changes.firstOrNull() ?: break
                                if (!pointer.pressed) break

                                val curAngle = atan2(
                                    pointer.position.y - centerY,
                                    pointer.position.x - centerX
                                ).toDegrees()

                                var delta = curAngle - prevAngle
                                if (delta > 180f) delta -= 360f
                                if (delta < -180f) delta += 360f

                                launch { rotation.snapTo(rotation.value + delta) }
                                prevAngle = curAngle
                                pointer.consume()
                            } while (true)

                            val snappedIndex = indexFromRotation(rotation.value)
                            val rawTarget = -snappedIndex * step
                            val currentRot = rotation.value
                            val turns = ((currentRot - rawTarget) / 360f).roundToInt()
                            val targetRotation = rawTarget + turns * 360f

                            launch {
                                rotation.animateTo(
                                    targetValue = targetRotation,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        presets.forEachIndexed { index, preset ->
            val worldAngleDeg = rotation.value + index * step
            val rad = Math.toRadians(worldAngleDeg.toDouble())

            val xDp = with(density) { (radiusPx * cos(rad)).toFloat().toDp() }
            val yDp = with(density) { (radiusPx * sin(rad)).toFloat().toDp() }

            val isSelected = index == currentIndex

            Text(
                text = preset.name.uppercase(),
                fontFamily = DotMatrix,
                fontSize = if (isSelected) 14.sp else 12.sp,
                letterSpacing = if (isSelected) 2.sp else 1.sp,
                modifier = Modifier
                    .offset(xDp, yDp)
                    .scale(if (isSelected) 1.25f else 0.85f)
                    .clickable { onSelected(preset) },
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onBackground.copy(0.8f)
            )
        }

        Text(
            "→",
            fontFamily = DotMatrix,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// ── History Row ────────────────────────────────────────────────────────────
@Composable
fun HistoryRow(session: Session, formattedDate: Any, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (formattedDate is AnnotatedString) {
            Text(
                text = formattedDate,
                fontFamily = DotMatrix,
                fontSize = 13.sp,
                letterSpacing = 1.sp
            )
        } else {
            Text(
                text = formattedDate.toString(),
                fontFamily = DotMatrix,
                fontSize = 13.sp,
                letterSpacing = 1.sp
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formatDurationAligned(session.durationSeconds),
                fontFamily = DotMatrix,
                fontSize = 14.sp
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = if (session.completed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (session.completed) androidx.compose.ui.graphics.Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            )
        }
    }
}

// ── Session Detail Dialog ──────────────────────────────────────────────────
@Composable
fun SessionDetailDialog(session: Session, onDismiss: () -> Unit) {
    val dateFormat = remember {
        SimpleDateFormat("EEEE, MMM dd yyyy", Locale.getDefault())
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", fontFamily = DotMatrix, letterSpacing = 2.sp)
            }
        },
        title = {
            Text(
                "SESSION DETAILS",
                fontFamily = DotMatrix,
                letterSpacing = 3.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow("DATE", dateFormat.format(Date(session.startTime)).uppercase().alignColons())
                DetailRow("DURATION", formatDurationAligned(session.durationSeconds))
                DetailRow("STATUS", if (session.completed) "COMPLETED" else "STOPPED")
                if (session.healthConnectSynced) {
                    DetailRow("SYNC", "HEALTH CONNECT ✓")
                }
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: Any) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontFamily = DotMatrix,
            fontSize = 12.sp,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
        )
        if (value is AnnotatedString) {
            Text(text = value, fontFamily = DotMatrix, fontSize = 13.sp)
        } else {
            Text(text = value.toString(), fontFamily = DotMatrix, fontSize = 13.sp)
        }
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────
fun formatDurationAligned(seconds: Int): AnnotatedString {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s).alignColons()
}

fun String.alignColons(): AnnotatedString {
    return buildAnnotatedString {
        val parts = this@alignColons.split(":")
        for (i in parts.indices) {
            append(parts[i])
            if (i < parts.lastIndex) {
                withStyle(SpanStyle(baselineShift = BaselineShift(0.15f))) {
                    append(":")
                }
            }
        }
    }
}

fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}

private fun Float.toDegrees() = Math.toDegrees(this.toDouble()).toFloat()
