package com.gnaanaa.mtimer.ui.preset

import android.media.MediaPlayer
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import com.gnaanaa.mtimer.domain.model.Preset
import com.gnaanaa.mtimer.ui.home.DotMatrix

private val SOUND_OPTIONS = listOf(
    "bell_tibetan" to "Tibetan Bowl",
    "bell_singing" to "Singing Bowl",
    "chime_soft"   to "Soft Chime",
    "bell_simple"  to "Simple Bell",
    "silence"      to "Silence"
)

// Fill this in once you add files to res/raw/, e.g.:
//   "bell_tibetan" to R.raw.bell_tibetan
// Until then, selecting any sound just won't play audio.
private val SOUND_RAW_IDS: Map<String, Int> = emptyMap()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetEditScreen(
    backStackEntry: NavBackStackEntry,
    onBack: () -> Unit,
    viewModel: PresetViewModel = hiltViewModel()
) {
    val presetId = backStackEntry.arguments?.getString("presetId") ?: "new"
    val isNew    = presetId == "new"
    val context  = LocalContext.current

    val customSounds by viewModel.customSounds.collectAsState()

    val soundPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { u ->
            val name = context.contentResolver
                .query(u, null, null, null, null)
                ?.use { cursor ->
                    val col = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    cursor.getString(col)
                } ?: "imported_sound_${System.currentTimeMillis()}.mp3"
            viewModel.importSound(u, name)
        }
    }

    var name           by rememberSaveable { mutableStateOf("") }
    var durationHours  by rememberSaveable { mutableIntStateOf(0) }
    var durationMins   by rememberSaveable { mutableIntStateOf(10) }
    var prepareSeconds by rememberSaveable { mutableIntStateOf(0) }
    var startSoundId   by rememberSaveable { mutableStateOf("bell_tibetan") }
    var endSoundId     by rememberSaveable { mutableStateOf("bell_tibetan") }
    var loaded         by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(presetId) {
        if (!isNew && !loaded) {
            viewModel.getPreset(presetId)?.let { existing ->
                name           = existing.name
                durationHours = (existing.durationSeconds / 3600).coerceIn(0, 23)
                durationMins = ((existing.durationSeconds % 3600) / 60).coerceIn(0, 59)
                prepareSeconds = existing.prepareSeconds
                startSoundId = existing.startSoundId
                endSoundId = existing.endSoundId
            }
            loaded = true
        }
    }

    // ── Sound playback ────────────────────────────────────────────────────
    // Keep a single player reference; create a new one on each preview call.
    // MediaPlayer.create() is safe to call on the main thread for raw resources.
    var currentPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    DisposableEffect(Unit) {
        onDispose {
            currentPlayer?.stop()
            currentPlayer?.release()
        }
    }

    val playPreview: (String) -> Unit = { soundId ->
        // Stop & release the previous player first
        currentPlayer?.stop()
        currentPlayer?.release()
        currentPlayer = null

        val rawId: Int? = SOUND_RAW_IDS[soundId]
        if (rawId != null) {
            // create() returns null if the resource can't be decoded
            val mp = MediaPlayer.create(context, rawId)
            if (mp != null) {
                // Release automatically when playback finishes
                mp.setOnCompletionListener { player ->
                    player.release()
                }
                mp.start()
                currentPlayer = mp
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (isNew) "NEW PRESET" else "EDIT PRESET",
                            fontFamily    = DotMatrix,
                            letterSpacing = 4.sp
                        )
                        Text(
                            if (isNew) "CONFIGURE YOUR SESSION" else name.uppercase(),
                            fontFamily    = DotMatrix,
                            fontSize      = 11.sp,
                            letterSpacing = 2.sp,
                            color         = MaterialTheme.colorScheme.onBackground.copy(0.5f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val resolvedName = name.trim().ifBlank { "Meditation" }
                        val totalSeconds = (durationHours * 3600) + (durationMins * 60)
                        val preset = Preset(
                            id              = if (isNew) java.util.UUID.randomUUID().toString() else presetId,
                            name            = resolvedName,
                            durationSeconds = totalSeconds,
                            prepareSeconds  = prepareSeconds,
                            startSoundId    = startSoundId,
                            endSoundId      = endSoundId
                        )
                        viewModel.savePreset(preset)
                        onBack()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->

        // No scroll — everything fits in one screen
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Name ──────────────────────────────────────────────────────
            SectionLabel("SESSION NAME")
            OutlinedTextField(
                value         = name,
                onValueChange = { name = it },
                placeholder   = {
                    Text(
                        "e.g. MORNING CALM",
                        fontFamily = DotMatrix,
                        fontSize   = 13.sp,
                        color      = MaterialTheme.colorScheme.onBackground.copy(0.3f)
                    )
                },
                textStyle  = LocalTextStyle.current.copy(
                    fontFamily    = DotMatrix,
                    letterSpacing = 2.sp
                ),
                modifier   = Modifier.fillMaxWidth(),
                singleLine = true,
                shape      = RoundedCornerShape(16.dp)
            )

            // ── Duration ──────────────────────────────────────────────────
            SectionLabel("DURATION")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SpinnerPicker(
                    modifier      = Modifier.weight(1f),
                    label         = "HRS",
                    values        = (0..23).toList(),
                    selectedValue = durationHours, // Pass Value, not Index
                    display       = { v -> "%02d".format(v) },
                    onValueSelected = { durationHours = it }
                )
                SpinnerPicker(
                    modifier      = Modifier.weight(1f),
                    label         = "MIN",
                    values        = (0..59).toList(),
                    selectedValue = durationMins,
                    display       = { v -> "%02d".format(v) },
                    onValueSelected = { durationMins = it }
                )
            }

            // ── Prepare time ──────────────────────────────────────────────
            SectionLabel("PREPARE TIME")
            val prepareValues: List<Int> = (0..60 step 5).toList()
            SpinnerPicker(
                modifier      = Modifier.fillMaxWidth(),
                label         = "SEC",
                values        = prepareValues,
                selectedValue = prepareSeconds,
                display       = { v -> "${v}s" },
                onValueSelected = { prepareSeconds = it }
            )

            // ── Sounds ────────────────────────────────────────────────────
            SectionLabel("START SOUND")
            SoundPicker(
                selectedId   = startSoundId,
                customSounds = customSounds,
                onSelected   = { id -> startSoundId = id; playPreview(id) },
                onImport     = { soundPickerLauncher.launch("audio/*") }
            )

            SectionLabel("END SOUND")
            SoundPicker(
                selectedId   = endSoundId,
                customSounds = customSounds,
                onSelected   = { id -> endSoundId = id; playPreview(id) },
                onImport     = { soundPickerLauncher.launch("audio/*") }
            )
        }
    }
}

// ── Section label ──────────────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String) {
    Text(
        text          = text,
        fontFamily    = DotMatrix,
        fontSize      = 10.sp,
        letterSpacing = 3.sp,
        color         = MaterialTheme.colorScheme.onBackground.copy(0.45f)
    )
}

// ── Compact 2-row spinner ─────────────────────────────────────────────────
//
//  visibleItems = 3: prev (dimmed) | SELECTED (highlighted) | next (dimmed)
//  The selected item sits in the center row and gets a filled background pill.
//  Height = itemHeight * 3 — approximately 2 lines of content visible.
//
@Composable
private fun SpinnerPicker(
    modifier: Modifier = Modifier,
    label: String,
    values: List<Int>,
    selectedValue: Int, // Pass the actual value (e.g., 36)
    display: (Int) -> String,
    onValueSelected: (Int) -> Unit
) {
    val itemHeightDp = 36.dp
    val visibleItems = 3

    // Calculate the index of the value we want to show
    val initialIndex = remember(values, selectedValue) {
        values.indexOf(selectedValue).coerceAtLeast(0)
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val snapBehavior = rememberSnapFlingBehavior(listState)

    // CRITICAL: Sync the UI if the value changes externally (like loading from DB)
    LaunchedEffect(selectedValue) {
        val targetIndex = values.indexOf(selectedValue).coerceAtLeast(0)
        // Only scroll if the list isn't currently being touched by the user
        if (!listState.isScrollInProgress && listState.firstVisibleItemIndex != targetIndex) {
            listState.scrollToItem(targetIndex)
        }
    }

    val centeredIndex by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex.coerceIn(0, values.lastIndex)
        }
    }

    // Only notify the parent when the user actually changes the selection
    LaunchedEffect(centeredIndex) {
        val newValue = values[centeredIndex]
        if (newValue != selectedValue) {
            onValueSelected(newValue)
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, fontFamily = DotMatrix, fontSize = 9.sp, color = MaterialTheme.colorScheme.primary.copy(0.65f))
        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeightDp * visibleItems)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            // Center Highlight Pill
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
                    .height(itemHeightDp)
                    .background(MaterialTheme.colorScheme.primary.copy(0.18f), RoundedCornerShape(10.dp))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(0.35f), RoundedCornerShape(10.dp))
            )

            LazyColumn(
                state = listState,
                flingBehavior = snapBehavior,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = itemHeightDp)
            ) {
                items(values.size) { index ->
                    val isSelected = index == centeredIndex
                    Box(
                        modifier = Modifier.fillMaxWidth().height(itemHeightDp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = display(values[index]),
                            fontFamily = DotMatrix,
                            fontSize = if (isSelected) 17.sp else 13.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(0.3f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

// ── Sound picker dropdown ──────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SoundPicker(
    selectedId   : String,
    customSounds : List<String>,
    onSelected   : (String) -> Unit,
    onImport     : () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = SOUND_OPTIONS.firstOrNull { it.first == selectedId }?.second
        ?: customSounds.firstOrNull { it == selectedId }
        ?: selectedId

    ExposedDropdownMenuBox(
        expanded         = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value         = selectedLabel.uppercase(),
            onValueChange = {},
            readOnly      = true,
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            textStyle     = LocalTextStyle.current.copy(
                fontFamily    = DotMatrix,
                letterSpacing = 2.sp,
                fontSize      = 13.sp
            ),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        ExposedDropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SOUND_OPTIONS.forEach { (id, displayName) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            displayName.uppercase(),
                            fontFamily    = DotMatrix,
                            fontSize      = 12.sp,
                            letterSpacing = 1.sp
                        )
                    },
                    onClick = { onSelected(id); expanded = false }
                )
            }

            if (customSounds.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                customSounds.forEach { soundName ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                soundName.uppercase(),
                                fontFamily    = DotMatrix,
                                fontSize      = 12.sp,
                                letterSpacing = 1.sp
                            )
                        },
                        onClick = { onSelected(soundName); expanded = false }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier           = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "IMPORT SOUND...",
                            fontFamily    = DotMatrix,
                            fontSize      = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                },
                onClick = { onImport(); expanded = false }
            )
        }
    }
}