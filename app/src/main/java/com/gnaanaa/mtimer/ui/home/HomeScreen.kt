package com.gnaanaa.mtimer.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.gnaanaa.mtimer.ui.history.HeartRateChart
import androidx.health.connect.client.records.HeartRateRecord
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

val InterFont = FontFamily.SansSerif

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
    val uiState by viewModel.uiState.collectAsState()
    val heartRateSamples by viewModel.heartRateSamples.collectAsState()
    val recentSessions = uiState.recentSessions
    val presets = uiState.presets

    var selectedSession by remember { mutableStateOf<Session?>(null) }
    var selectedPreset by remember { mutableStateOf<Preset?>(null) }

    LaunchedEffect(presets) {
        if (selectedPreset == null && presets.isNotEmpty()) {
            selectedPreset = presets.first()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
                            fontFamily = InterFont,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
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

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {

                StartSessionButton(
                    enabled = selectedPreset != null,
                    selectedPreset = selectedPreset,
                    labelOverride = if (presets.isEmpty()) "CREATE PRESET >>" else null,
                    alwaysEnabled = presets.isEmpty(),
                    onClick = {
                        if (presets.isEmpty()) {
                            onNavigateToPresets()
                        } else {
                            selectedPreset?.let {
                                viewModel.startTimer(it)
                                onStartTimer()
                            }
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
                        "RECENT SESSIONS",
                        fontFamily = DotMatrix,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp
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
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(recentSessions, key = { it.id }) { session ->
                            val preset = presets.find { it.id == session.presetId }
                            HistoryRow(
                                session = session,
                                presetDurationSeconds = preset?.durationSeconds,
                                onClick = { 
                                    selectedSession = session
                                    viewModel.fetchHeartRate(session)
                                },
                                onStartAgain = {
                                    preset?.let {
                                        viewModel.startTimer(it)
                                        onStartTimer()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    selectedSession?.let {
        SessionDetailDialog(
            session = it, 
            heartRateSamples = heartRateSamples,
            onDismiss = { 
                selectedSession = null
                viewModel.clearHeartRate()
            }
        )
    }
}

// ── Start Button ───────────────────────────────────────────────────────────
@Composable
fun StartSessionButton(
    enabled: Boolean,
    selectedPreset: Preset?,
    labelOverride: String? = null,
    alwaysEnabled: Boolean = false,
    onClick: () -> Unit
) {
    val isEffectiveEnabled = enabled || alwaysEnabled
    val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 0.5 }
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val meditationGreen = Color(0xFF4CAF50)
    val accentColor = if (isDark) meditationGreen else primaryColor

    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(84.dp)
            .border(
                width = 2.dp,
                color = if (isEffectiveEnabled) accentColor.copy(alpha = 0.4f) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                if (isEffectiveEnabled) accentColor.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = isEffectiveEnabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Left side: Icon and Duration block
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp), // Match the horizontal padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Spa,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isEffectiveEnabled)
                    accentColor
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f)
            )

            if (labelOverride == null && selectedPreset != null) {
                val mins = selectedPreset.durationSeconds / 60
                Text(
                    text = "${mins}M".styleDottedDigits(),
                    fontFamily = InterFont,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    color = if (isEffectiveEnabled)
                        accentColor
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f)
                )
            }
        }

        // Center: Text block
        Column(
            modifier = Modifier.fillMaxWidth(0.65f), // Occupy the middle 65% of the button width
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                labelOverride ?: "START SESSION",
                fontFamily = InterFont,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isEffectiveEnabled) (if (isDark) Color.White else accentColor) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (labelOverride == null) {
                selectedPreset?.let { preset ->
                    Text(
                        preset.name.uppercase(),
                        fontFamily = InterFont,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isEffectiveEnabled)
                            MaterialTheme.colorScheme.onBackground.copy(0.8f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)
                    )
                }
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

    val haptic = LocalHapticFeedback.current
    val step = 360f / presets.size
    val rotation = remember { Animatable(0f) }

    fun indexFromRotation(rot: Float): Int =
        ((-rot / step).roundToInt()).mod(presets.size)

    val currentIndex by remember(presets) {
        derivedStateOf { indexFromRotation(rotation.value) }
    }

    // Haptic feedback when the index changes during rotation
    LaunchedEffect(currentIndex) {
        if (rotation.isRunning || rotation.value != 0f) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
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
                            // Trigger haptic on initial touch
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            
                            var prevAngle = atan2(
                                down.position.y - centerY,
                                down.position.x - centerX
                            ).toDegrees()

                            launch { rotation.stop() }

                            do {
                                val event = awaitPointerEvent()
                                val pointer = event.changes.firstOrNull() ?: break
                                
                                if (event.type == PointerEventType.Move) {
                                    val curAngle = atan2(
                                        pointer.position.y - centerY,
                                        pointer.position.x - centerX
                                    ).toDegrees()

                                    var delta = curAngle - prevAngle
                                    if (delta > 180f) delta -= 360f
                                    if (delta < -180f) delta += 360f

                                    launch { rotation.snapTo(rotation.value + delta) }
                                    prevAngle = curAngle
                                }
                                
                                pointer.consume()
                                if (!pointer.pressed) break
                            } while (true)

                            val snappedIndex = indexFromRotation(rotation.value)
                            // Trigger stronger haptic on release/snap
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)

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

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .offset(xDp, yDp)
                        .width(120.dp)
                        .scale(1.25f)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = preset.name.uppercase(),
                        fontFamily = InterFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Text(
                    text = preset.name.uppercase(),
                    fontFamily = InterFont,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier
                        .offset(xDp, yDp)
                        .scale(0.85f)
                        .widthIn(max = 100.dp)
                        .clickable { onSelected(preset) },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.8f)
                )
            }
        }

        Text(
            "→",
            fontFamily = InterFont,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// ── History Row ────────────────────────────────────────────────────────────
@Composable
fun HistoryRow(
    session: Session,
    presetDurationSeconds: Int?,
    onClick: () -> Unit,
    onStartAgain: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val dateFormat = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(69.dp)
            .border(
                width = 1.dp,
                color = primaryColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                primaryColor.copy(alpha = 0.03f),
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Main Clickable Area (Left & Center)
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(onClick = onClick)
                .padding(start = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = (session.presetName ?: "MEDITATION").uppercase(),
                    fontFamily = InterFont,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.7f)
                )
                Text(
                    text = "${dateFormat.format(Date(session.startTime))} • ${timeFormat.format(Date(session.startTime))}".uppercase(),
                    fontFamily = InterFont,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)
                )
            }

            // Completed time and status marker
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = formatDurationAligned(session.durationSeconds),
                    fontFamily = InterFont, // Base font, digits overridden by styleDottedDigits
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.9f)
                )

                Icon(
                    imageVector = if (session.completed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (session.completed) Color(0xFF4CAF50).copy(0.8f) else MaterialTheme.colorScheme.error.copy(0.8f)
                )
            }
        }

        // Start Again Button (Fixed Width, Full Height)
        val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 0.5 }
        val meditationGreen = Color(0xFF4CAF50)
        val accentColor = if (isDark) meditationGreen else primaryColor

        Box(
            modifier = Modifier
                .width(69.dp)
                .fillMaxHeight()
                .background(accentColor.copy(alpha = 0.15f))
                .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                .clickable(onClick = onStartAgain),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = accentColor
                )
                val mins = (presetDurationSeconds ?: session.durationSeconds) / 60
                Text(
                    text = "${mins}M".styleDottedDigits(),
                    fontFamily = InterFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )
            }
        }
    }
}

// ── Session Detail Dialog ──────────────────────────────────────────────────
@Composable
fun SessionDetailDialog(
    session: Session, 
    heartRateSamples: List<HeartRateRecord.Sample> = emptyList(),
    onDismiss: () -> Unit
) {
    val dateFormat = remember {
        SimpleDateFormat("EEEE, MMM dd yyyy", Locale.getDefault())
    }
    val timeFormat = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault())
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
                DetailRow("PRESET", (session.presetName ?: "MEDITATION").uppercase())
                DetailRow("DATE", dateFormat.format(Date(session.startTime)).uppercase().styleDottedDigits())
                
                val startStr = timeFormat.format(Date(session.startTime))
                val endStr = if (session.endTime > 0) timeFormat.format(Date(session.endTime)) else "--:--"
                
                DetailRow("START TIME", startStr.styleDottedDigits())
                DetailRow("FINISH TIME", endStr.styleDottedDigits())

                DetailRow("DURATION", formatDurationAligned(session.durationSeconds))
                DetailRow("STATUS", if (session.completed) "COMPLETED" else "STOPPED")
                if (session.healthConnectSynced) {
                    DetailRow("SYNC", "HEALTH CONNECT ✓")
                }

                if (heartRateSamples.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    HeartRateChart(samples = heartRateSamples)
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
            Text(text = value, fontFamily = InterFont, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        } else {
            Text(text = value.toString().styleDottedDigits(), fontFamily = InterFont, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────
fun formatDurationAligned(seconds: Int): AnnotatedString {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s).styleDottedDigits()
}

fun String.alignColons(): AnnotatedString = styleDottedDigits()

/**
 * Styles all digits and colons with DotMatrix font and applies a slight baseline shift to colons.
 */
fun String.styleDottedDigits(): AnnotatedString {
    return buildAnnotatedString {
        this@styleDottedDigits.forEach { char ->
            if (char.isDigit()) {
                withStyle(SpanStyle(fontFamily = DotMatrix, fontWeight = FontWeight.ExtraBold)) {
                    append(char)
                }
            } else if (char == ':') {
                withStyle(SpanStyle(fontFamily = DotMatrix, fontWeight = FontWeight.ExtraBold, baselineShift = BaselineShift(0.15f))) {
                    append(char)
                }
            } else {
                append(char)
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
