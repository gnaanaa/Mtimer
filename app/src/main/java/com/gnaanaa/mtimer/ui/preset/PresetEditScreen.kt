package com.gnaanaa.mtimer.ui.preset

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import com.gnaanaa.mtimer.domain.model.Preset

// Built-in sound options
private val SOUND_OPTIONS = listOf(
    "bell_tibetan" to "Tibetan Bowl",
    "bell_singing" to "Singing Bowl",
    "chime_soft" to "Soft Chime",
    "bell_simple" to "Simple Bell",
    "silence" to "Silence"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetEditScreen(
    backStackEntry: NavBackStackEntry,
    onBack: () -> Unit,
    viewModel: PresetViewModel = hiltViewModel()
) {
    // Read the presetId argument that was passed via navigation
    val presetId = backStackEntry.arguments?.getString("presetId") ?: "new"
    val isNew = presetId == "new"

    // Local editable state — rememberSaveable survives recomposition
    var name by rememberSaveable { mutableStateOf("") }
    var durationMinutes by rememberSaveable { mutableIntStateOf(10) }
    var prepareSeconds by rememberSaveable { mutableIntStateOf(0) }
    var startSoundId by rememberSaveable { mutableStateOf("bell_tibetan") }
    var endSoundId by rememberSaveable { mutableStateOf("bell_tibetan") }
    var loaded by rememberSaveable { mutableStateOf(false) }

    // If editing an existing preset, load its values once
    LaunchedEffect(presetId) {
        if (!isNew && !loaded) {
            val existing = viewModel.getPreset(presetId)
            if (existing != null) {
                name = existing.name
                durationMinutes = existing.durationSeconds / 60
                prepareSeconds = existing.prepareSeconds
                startSoundId = existing.startSoundId
                endSoundId = existing.endSoundId
            }
            loaded = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "New Preset" else "Edit Preset") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val resolvedName = name.trim().ifBlank { "Meditation" }
                        if (isNew) {
                            // Always insert a fresh Preset with a new UUID
                            viewModel.savePreset(
                                Preset(
                                    name = resolvedName,
                                    durationSeconds = durationMinutes * 60,
                                    prepareSeconds = prepareSeconds,
                                    startSoundId = startSoundId,
                                    endSoundId = endSoundId
                                )
                            )
                        } else {
                            // Preserve the original ID so it updates the existing row
                            viewModel.savePreset(
                                Preset(
                                    id = presetId,
                                    name = resolvedName,
                                    durationSeconds = durationMinutes * 60,
                                    prepareSeconds = prepareSeconds,
                                    startSoundId = startSoundId,
                                    endSoundId = endSoundId
                                )
                            )
                        }
                        onBack()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                placeholder = { Text("e.g. Morning Calm") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Duration: $durationMinutes min",
                style = MaterialTheme.typography.bodyLarge
            )
            Slider(
                value = durationMinutes.toFloat(),
                onValueChange = { durationMinutes = it.toInt() },
                valueRange = 1f..120f,
                steps = 118  // 120 - 1 - 1 = 118 steps for values 1..120
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Prepare time: $prepareSeconds sec",
                style = MaterialTheme.typography.bodyLarge
            )
            Slider(
                value = prepareSeconds.toFloat(),
                onValueChange = { prepareSeconds = it.toInt() },
                valueRange = 0f..60f,
                steps = 59  // 60 - 0 - 1 = 59 steps for values 0..60
            )

            Spacer(modifier = Modifier.height(24.dp))

            SoundPicker(
                label = "Start sound",
                selectedId = startSoundId,
                onSelected = { startSoundId = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SoundPicker(
                label = "End sound",
                selectedId = endSoundId,
                onSelected = { endSoundId = it }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SoundPicker(
    label: String,
    selectedId: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = SOUND_OPTIONS.firstOrNull { it.first == selectedId }?.second ?: selectedId

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SOUND_OPTIONS.forEach { (id, displayName) ->
                DropdownMenuItem(
                    text = { Text(displayName) },
                    onClick = {
                        onSelected(id)
                        expanded = false
                    }
                )
            }
        }
    }
}