# MTimer — Claude Code Prompt File
# ============================================================
# HOW TO USE THIS FILE
# ============================================================
#
# 1. Create a new Android project in Android Studio:
#       File → New → New Project → Empty Activity
#       Name: MTimer
#       Package: com.gnaanaa.mtimer   ← change "yourname"
#       Language: Kotlin
#       Minimum SDK: API 28
#
# 2. Open a terminal in the project root directory
#
# 3. Run: claude
#
# 4. At the Claude Code prompt, type:
#       /read MTimer_Prompt.md
#    OR paste the full contents of this file and say:
#       "This is the full spec. Begin with Phase 1."
#
# 5. After each phase completes:
#    - Open Android Studio and click "Sync Project with Gradle Files"
#    - Fix any build errors (ask Claude Code to fix them)
#    - Run the app on your real device to test
#    - Only then say "Phase X is working, proceed to Phase X+1"
#
# ============================================================

---

# SYSTEM INSTRUCTIONS FOR CLAUDE CODE

You are building a complete production-quality Android application from scratch.
The full specification follows in this file. Read it entirely before writing any code.

## Non-negotiable rules

1. **Kotlin only.** No Java files under any circumstances.
2. **Jetpack Compose only** for all UI. No XML layouts, no View system.
3. **Health Connect only** for health data sync. Do NOT use Google Fit, Google Fit REST API, or any `com.google.android.gms:play-services-fitness` dependency. It is deprecated and must not appear anywhere in the project.
4. **Use exactly the dependency versions listed** in the Gradle setup section. Do not upgrade or substitute without being asked.
5. **Power discipline is mandatory.** Every file you touch must comply with the Power Optimization Checklist in this spec. If you are unsure whether something violates it, it probably does — ask before proceeding.
6. **The Health Connect MindfulnessSessionRecord API snippet in Section 2.5 is pinned.** Use it verbatim. Do not paraphrase, simplify, or restructure it. It is new enough that you may have incomplete training data on it — trust the snippet over your instincts.
7. **Build phase by phase.** Complete each phase fully, confirm the file list of what was created, then wait for the user to confirm before starting the next phase.
8. After generating files in each phase, output a checklist of every file created or modified so the user can verify.

---

# PROJECT SPECIFICATION

## Overview

App name: **MTimer**
A power-minimal, OLED-first Android meditation timer with Health Connect sync,
home screen widget support, and cloud-backed presets.

- Language: Kotlin
- UI: Jetpack Compose + Material3
- Architecture: MVVM + Clean Architecture (ViewModel → UseCase → Repository)
- DI: Hilt
- Min SDK: API 28 (Android 9.0 Pie)
- Target SDK: API 35

---

## Target File Structure

Create files in this exact structure. Do not deviate.

```
app/
├── build.gradle.kts
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml
│   │   ├── assets/sounds/
│   │   │   ├── bell_tibetan.mp3
│   │   │   ├── bell_singing.mp3
│   │   │   ├── chime_soft.mp3
│   │   │   └── bell_simple.mp3
│   │   └── java/com/gnaanaa/mtimer/
│   │       ├── MainActivity.kt
│   │       ├── MTimerApp.kt                  ← Hilt @HiltAndroidApp
│   │       ├── di/
│   │       │   ├── DatabaseModule.kt
│   │       │   ├── DataStoreModule.kt
│   │       │   └── HealthConnectModule.kt
│   │       ├── data/
│   │       │   ├── db/
│   │       │   │   ├── MTimerDatabase.kt
│   │       │   │   ├── SessionDao.kt
│   │       │   │   ├── PresetDao.kt
│   │       │   │   ├── SessionEntity.kt
│   │       │   │   └── PresetEntity.kt
│   │       │   ├── datastore/
│   │       │   │   └── UserPreferencesDataStore.kt
│   │       │   ├── repository/
│   │       │   │   ├── SessionRepository.kt
│   │       │   │   ├── SessionRepositoryImpl.kt
│   │       │   │   ├── PresetRepository.kt
│   │       │   │   └── PresetRepositoryImpl.kt
│   │       │   └── sync/
│   │       │       ├── HealthConnectSync.kt
│   │       │       ├── HealthConnectSyncWorker.kt
│   │       │       ├── DriveSync.kt
│   │       │       └── DriveSyncWorker.kt
│   │       ├── domain/
│   │       │   ├── model/
│   │       │   │   ├── Session.kt
│   │       │   │   ├── Preset.kt
│   │       │   │   ├── TimerState.kt
│   │       │   │   └── WeeklyStats.kt
│   │       │   └── usecase/
│   │       │       ├── StartTimerUseCase.kt
│   │       │       ├── StopTimerUseCase.kt
│   │       │       ├── SavePresetUseCase.kt
│   │       │       ├── DeletePresetUseCase.kt
│   │       │       ├── SyncSessionUseCase.kt
│   │       │       └── GetWeeklyStatsUseCase.kt
│   │       ├── service/
│   │       │   ├── MeditationForegroundService.kt
│   │       │   ├── SoundPlayer.kt
│   │       │   └── WakeLockManager.kt
│   │       ├── ui/
│   │       │   ├── navigation/
│   │       │   │   └── MTimerNavGraph.kt
│   │       │   ├── theme/
│   │       │   │   ├── Theme.kt
│   │       │   │   ├── Color.kt
│   │       │   │   └── Type.kt
│   │       │   ├── timer/
│   │       │   │   ├── TimerScreen.kt
│   │       │   │   └── TimerViewModel.kt
│   │       │   ├── home/
│   │       │   │   ├── HomeScreen.kt
│   │       │   │   └── HomeViewModel.kt
│   │       │   ├── preset/
│   │       │   │   ├── PresetListScreen.kt
│   │       │   │   ├── PresetEditScreen.kt
│   │       │   │   └── PresetViewModel.kt
│   │       │   ├── settings/
│   │       │   │   ├── SettingsScreen.kt
│   │       │   │   └── SettingsViewModel.kt
│   │       │   ├── history/
│   │       │   │   ├── SessionHistoryScreen.kt
│   │       │   │   └── SessionHistoryViewModel.kt
│   │       │   └── onboarding/
│   │       │       ├── OnboardingScreen.kt
│   │       │       └── HealthPermissionsRationaleActivity.kt
│   │       ├── widget/
│   │       │   ├── MTimerWidget.kt
│   │       │   ├── MTimerWidgetReceiver.kt
│   │       │   └── MTimerWidgetStateDefinition.kt
│   │       └── receiver/
│   │           └── BootReceiver.kt
│   └── test/ and androidTest/
│       └── (unit + instrumented tests per phase)
```

---

## Gradle Setup

### libs.versions.toml (version catalog)

```toml
[versions]
agp = "8.5.1"
kotlin = "2.0.0"
ksp = "2.0.0-1.0.22"
hilt = "2.51.1"
compose-bom = "2024.09.00"
room = "2.6.1"
datastore = "1.1.1"
health-connect = "1.1.0-beta02"
glance = "1.1.0"
work = "2.9.1"
navigation = "2.7.7"
lifecycle = "2.8.4"
coroutines = "1.8.1"
play-services-auth = "21.2.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version = "1.13.1" }
androidx-lifecycle-runtime = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version = "1.9.1" }
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-icons = { group = "androidx.compose.material", name = "material-icons-extended" }
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }
hilt-work = { group = "androidx.hilt", name = "hilt-work", version = "1.2.0" }
hilt-work-compiler = { group = "androidx.hilt", name = "hilt-compiler", version = "1.2.0" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
datastore = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
health-connect = { group = "androidx.health.connect", name = "connect-client", version.ref = "health-connect" }
glance-appwidget = { group = "androidx.glance", name = "glance-appwidget", version.ref = "glance" }
glance-material3 = { group = "androidx.glance", name = "glance-material3", version.ref = "glance" }
work-runtime = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "work" }
play-services-auth = { group = "com.google.android.gms", name = "play-services-auth", version.ref = "play-services-auth" }
kotlinx-coroutines = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-serialization = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version = "1.7.1" }
accompanist-systemuicontroller = { group = "com.google.accompanist", name = "accompanist-systemuicontroller", version = "0.34.0" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

### app/build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.gnaanaa.mtimer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gnaanaa.mtimer"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }

    buildFeatures { compose = true }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.activity.compose)

    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.material3)
    implementation(libs.compose.icons)

    implementation(libs.navigation.compose)
    implementation(libs.accompanist.systemuicontroller)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.datastore)
    implementation(libs.health.connect)
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)
    implementation(libs.work.runtime)
    implementation(libs.play.services.auth)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization)
}
```

---

## AndroidManifest.xml — Full Required Content

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Timer service -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK"/>
    <uses-permission android:name="android.permission.WAKE_LOCK"/>
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>

    <!-- Health Connect -->
    <uses-permission android:name="android.permission.health.WRITE_MINDFULNESS"/>
    <uses-permission android:name="android.permission.health.READ_MINDFULNESS"/>

    <application
        android:name=".MTimerApp"
        android:allowBackup="true"
        android:theme="@style/Theme.MTimer">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>

        <!-- Required by Health Connect — must be present or permissions will be rejected -->
        <activity
            android:name=".ui.onboarding.HealthPermissionsRationaleActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE"/>
            </intent-filter>
        </activity>

        <service
            android:name=".service.MeditationForegroundService"
            android:foregroundServiceType="mediaPlayback"
            android:exported="false"/>

        <receiver
            android:name=".receiver.BootReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED"/>
            </intent-filter>
        </receiver>

        <receiver android:name=".widget.MTimerWidgetReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE"/>
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/mtimer_widget_info"/>
        </receiver>

    </application>
</manifest>
```

---

## Domain Models

### TimerState.kt
```kotlin
sealed class TimerState {
    object Idle : TimerState()
    data class Preparing(val remainingSeconds: Int, val totalSeconds: Int) : TimerState()
    data class Running(val remainingSeconds: Int, val totalSeconds: Int, val presetName: String?) : TimerState()
    data class Ending(val presetName: String?) : TimerState()   // end sound playing
    data class Completed(val sessionId: Long) : TimerState()
}
```

### Session.kt
```kotlin
data class Session(
    val id: Long = 0,
    val presetId: String? = null,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Int,
    val completed: Boolean,
    val healthConnectSynced: Boolean = false,
    val healthConnectRecordId: String? = null
)
```

### Preset.kt
```kotlin
data class Preset(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val prepareSeconds: Int = 0,
    val startSoundId: String = "bell_tibetan",
    val durationSeconds: Int = 600,
    val endSoundId: String = "bell_tibetan",
    val colorAccent: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null
)
```

### WeeklyStats.kt
```kotlin
enum class StatsSource { HEALTH_CONNECT, LOCAL }

data class WeeklyStats(
    val weekStart: Long,
    val totalMinutes: Int,
    val sessionCount: Int,
    val source: StatsSource
)
```

---

## Feature Specifications

### Timer Engine (MeditationForegroundService)

- `START_STICKY` service, survives process death
- State exposed as `StateFlow<TimerState>` accessible via a singleton or bound service
- Timer uses `SystemClock.elapsedRealtime()` for drift-free countdown — NOT `System.currentTimeMillis()`
- State machine transitions: `IDLE → PREPARING (if prepareSeconds > 0) → RUNNING → ENDING (play end sound) → COMPLETED`
- `PARTIAL_WAKE_LOCK` acquired when entering `RUNNING`, released in `COMPLETED`, `Idle`, and any error path
- On `COMPLETED`: write `Session` to Room, then enqueue `HealthConnectSyncWorker`
- Persistent notification shown during PREPARING/RUNNING/ENDING with time remaining; auto-dismissed on COMPLETED
- Expose `startTimer(preset: Preset)`, `pauseTimer()`, `resumeTimer()`, `stopTimer()` as intent actions

### Sound Player

- Built-in sound IDs: `"bell_tibetan"`, `"bell_singing"`, `"chime_soft"`, `"bell_simple"`, `"silence"`
- Map IDs to `assets/sounds/*.mp3` files
- `"silence"` plays nothing — handle explicitly, do not attempt to load a file
- Use `SoundPool` for all built-in sounds (they are short, < 3 s)
- Request `AudioFocus` with `AUDIOFOCUS_GAIN_TRANSIENT` before playing, release immediately after
- If audio focus is denied, play anyway at reduced volume (do not silently skip)
- User-imported sounds stored in `app.filesDir/sounds/` with URI as the sound ID

### AMOLED Timer Screen (TimerScreen.kt)

**This screen must be built exactly as described — no shortcuts.**

```
Background: Color(0xFF000000)  ← true black, mandatory
```

- Full screen, no system bars (use WindowCompat + accompanist)
- Only three elements visible:
  1. Time remaining in `MM:SS` format — centered, large monospace font (fontSize = 72.sp), white
  2. Session label (preset name) — centered below time, 14.sp, white at 60% alpha
  3. Stop button — minimal, bottom-center, white outlined circle icon, 48.dp tap target
- Pause/resume: long-press on the time display (show a subtle "hold to pause" hint on first use)
- NO animations, NO gradients, NO progress rings, NO decorative elements
- Recomposition triggered ONLY by second-tick StateFlow changes — no other state should cause recompose
- `DisposableEffect` to hide/show system bars when screen enters/leaves composition
- When screen is shown, do NOT set `FLAG_KEEP_SCREEN_ON` — let the screen turn off naturally

### Power Optimization — Mandatory Rules

These apply to every file. Violating any of these is a bug, not a style preference:

- No `Handler.postDelayed` anywhere in the project
- No `while(true)` loops — use coroutine `delay` + `flow`
- No `AlarmManager` during active sessions
- No network calls on main thread
- `WakeLock` must be released in `finally` blocks and in `onDestroy`
- Widget updates via `WorkManager` only — no live `BroadcastReceiver`
- `SoundPool` released after each sound plays, reloaded fresh for next session
- All Health Connect and Drive operations on `Dispatchers.IO`

---

## PINNED API — Health Connect MindfulnessSessionRecord

**DO NOT MODIFY THIS CODE. Copy it exactly.**
This API is experimental and new. Use only this pattern — do not substitute.

```kotlin
@OptIn(ExperimentalFeatureAvailabilityApi::class)
suspend fun syncSessionToHealthConnect(
    context: Context,
    session: Session,
    presetName: String?
) {
    val client = HealthConnectClient.getOrCreate(context)

    val featureStatus = client.features.getFeatureStatus(
        HealthConnectFeatures.FEATURE_MINDFULNESS_SESSION
    )
    if (featureStatus != HealthConnectFeatures.FEATURE_STATUS_AVAILABLE) return

    val startInstant = Instant.ofEpochMilli(session.startTime)
    val endInstant = Instant.ofEpochMilli(session.endTime)

    client.insertRecords(
        listOf(
            MindfulnessSessionRecord(
                startTime = startInstant,
                startZoneOffset = ZoneOffset.systemDefault().rules.getOffset(startInstant),
                endTime = endInstant,
                endZoneOffset = ZoneOffset.systemDefault().rules.getOffset(endInstant),
                mindfulnessSessionType =
                    MindfulnessSessionRecord.MINDFULNESS_SESSION_TYPE_MEDITATION,
                title = presetName ?: "Meditation",
                metadata = Metadata.activelyRecorded(
                    clientRecordId = session.id.toString(),
                    device = Device(type = Device.TYPE_PHONE)
                )
            )
        )
    )
}
```

**PINNED API — Weekly stats read-back from Health Connect**

```kotlin
@OptIn(ExperimentalFeatureAvailabilityApi::class)
suspend fun getWeeklyMindfulnessMinutes(context: Context): Int {
    val client = HealthConnectClient.getOrCreate(context)
    val weekAgo = Instant.now().minus(7, ChronoUnit.DAYS)
    val response = client.readRecords(
        ReadRecordsRequest(
            recordType = MindfulnessSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.after(weekAgo)
        )
    )
    return response.records
        .sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }
        .toInt()
}
```

**Health Connect permission request — use this pattern, not ActivityCompat:**

```kotlin
val permissionsLauncher = rememberLauncherForActivityResult(
    PermissionController.createRequestPermissionResultContract()
) { granted ->
    // handle result
}

val permissions = setOf(
    HealthPermission.getWritePermission(MindfulnessSessionRecord::class),
    HealthPermission.getReadPermission(MindfulnessSessionRecord::class)
)

// To launch:
permissionsLauncher.launch(permissions)
```

---

## Theming (Theme.kt)

```kotlin
// MTimerDark — AMOLED-safe, default
val MTimerDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE8E8E8),
    onPrimary = Color(0xFF1A1A1A),
    background = Color(0xFF000000),   // true black
    surface = Color(0xFF0D0D0D),
    onBackground = Color(0xFFE8E8E8),
    onSurface = Color(0xFFCCCCCC),
    secondary = Color(0xFFAAAAAA),
    outline = Color(0xFF333333)
)

// MTimerLight — earthy, muted
val MTimerLightColorScheme = lightColorScheme(
    primary = Color(0xFF3D3D3D),
    onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFFF5F3EE),
    surface = Color(0xFFECEAE4),
    onBackground = Color(0xFF1A1A1A),
    onSurface = Color(0xFF2C2C2C),
    secondary = Color(0xFF6B6B6B),
    outline = Color(0xFFCCCAC4)
)
```

Theme selection stored in `UserPreferencesDataStore` as a boolean `useLightTheme` (default: false).
Timer screen always uses `MTimerDarkColorScheme` regardless of user setting.

---

## Home Screen Widget (Glance)

- Two widget sizes defined in `res/xml/mtimer_widget_info.xml`:
  - minWidth="110dp" minHeight="40dp" (2×1 compact)
  - targetCellWidth="4" targetCellHeight="2" (4×2 expanded, API 31+)
- Compact layout: `"X min this week"` text + "Start" button (last used preset)
- Expanded layout: weekly stats row + preset name chips (max 3 visible) + "Start" button
- Colors: dark surface `0xFF0D0D0D`, white text — matches AMOLED theme regardless of system theme
- Weekly stats sourced from: Health Connect if available + permission granted, else local Room DB
- WorkManager `PeriodicWorkRequest` with 30-minute interval refreshes widget data
- `MTimerWidgetReceiver` extends `GlanceAppWidgetReceiver`

---

## Screens Summary

| Screen | Key behaviour |
|---|---|
| `TimerScreen` | True black, MM:SS centered, stop button only, no system bars |
| `HomeScreen` | Recent sessions list, quick-start FAB, nav to presets/history/settings |
| `PresetListScreen` | LazyColumn, swipe-to-delete, tap to select+start, long-press to edit |
| `PresetEditScreen` | Name, prepare time slider (0–60s), duration picker, start/end sound pickers, color accent picker |
| `SettingsScreen` | Theme toggle, Health Connect status + permission button, Google account (Drive sync), about |
| `SessionHistoryScreen` | LazyColumn of past sessions with date, duration, completed/stopped indicator |
| `OnboardingScreen` | Shown on first launch only: Health Connect permission request card, optional Google sign-in card, skip option |
| `HealthPermissionsRationaleActivity` | Plain Activity (not Composable) explaining why mindfulness permissions are needed — required by Health Connect |

---

## Phased Build Instructions

### Phase 1 — Core timer + local presets
**Build these files and no others:**
- `build.gradle.kts`, `libs.versions.toml`
- `AndroidManifest.xml`
- `MTimerApp.kt`, `MainActivity.kt`
- All `di/` modules
- All `data/db/` files (entities, DAOs, database)
- `data/datastore/UserPreferencesDataStore.kt`
- All `domain/model/` files
- `domain/usecase/StartTimerUseCase.kt`, `StopTimerUseCase.kt`, `SavePresetUseCase.kt`, `DeletePresetUseCase.kt`
- `data/repository/SessionRepository*`, `PresetRepository*`
- `service/MeditationForegroundService.kt`, `SoundPlayer.kt`, `WakeLockManager.kt`
- `ui/theme/`, `ui/navigation/MTimerNavGraph.kt`
- `ui/timer/TimerScreen.kt`, `ui/timer/TimerViewModel.kt`
- `ui/home/HomeScreen.kt`, `ui/home/HomeViewModel.kt`
- `ui/preset/PresetListScreen.kt`, `ui/preset/PresetEditScreen.kt`, `ui/preset/PresetViewModel.kt`
- `receiver/BootReceiver.kt`

**Phase 1 success criteria:**
- Project builds without errors
- App launches on device
- Can create a preset with name, duration, prepare time, and sound selection
- Starting a preset shows the black timer screen with countdown
- Timer continues running when screen locks
- End sound plays when time runs out
- Session is saved to local DB

### Phase 2 — Health Connect integration
**Add these files:**
- `data/sync/HealthConnectSync.kt` — use PINNED API exactly
- `data/sync/HealthConnectSyncWorker.kt`
- `domain/usecase/SyncSessionUseCase.kt`, `GetWeeklyStatsUseCase.kt`
- `di/HealthConnectModule.kt`
- `ui/onboarding/OnboardingScreen.kt`
- `ui/onboarding/HealthPermissionsRationaleActivity.kt`

**Modify:**
- `MeditationForegroundService.kt` — enqueue `HealthConnectSyncWorker` on COMPLETED
- `SettingsScreen.kt` — add Health Connect permission status card
- `SessionEntity.kt` — add `healthConnectSynced`, `healthConnectRecordId` columns (migration required)

**Phase 2 success criteria:**
- Health Connect permission request appears on first launch
- After a session completes, record appears in Android Settings → Health Connect → Browse data → Mindfulness
- If Health Connect is not installed, user sees a prompt to install it

### Phase 3 — Google Drive preset sync
**Add these files:**
- `data/sync/DriveSync.kt`
- `data/sync/DriveSyncWorker.kt`

**Modify:**
- `SettingsScreen.kt` — add Google account sign-in card with avatar, email, sign-out
- `PresetRepositoryImpl.kt` — trigger sync on save/delete
- `PresetEntity.kt` — ensure `syncedAt` column exists

**Phase 3 success criteria:**
- User can sign in with Google
- Presets sync to/from Drive appDataFolder
- Signing out on a second device and signing back in restores presets

### Phase 4 — Home screen widget
**Add these files:**
- `widget/MTimerWidget.kt`
- `widget/MTimerWidgetReceiver.kt`
- `widget/MTimerWidgetStateDefinition.kt`
- `res/xml/mtimer_widget_info.xml`

**Phase 4 success criteria:**
- Widget appears in launcher widget picker
- Weekly minute count updates after a session
- Tapping Start from widget launches app directly into timer with last preset

### Phase 5 — Polish
- `ui/onboarding/OnboardingScreen.kt` — full onboarding flow
- `ui/history/SessionHistoryScreen.kt`
- Sound import via SAF (Storage Access Framework)
- Export/import presets as JSON
- Light mode fully wired through all screens
- Proguard rules for Health Connect, Hilt, Room, serialization

---

## Manual Steps (Cannot Be Done by Claude Code)

The following require you to act — Claude Code cannot complete them:

1. **Google Cloud Console:** Create a project, enable Drive API, configure OAuth consent screen,
   add your app's SHA-1 fingerprint, download `google-services.json` and place in `app/`

2. **Sound assets:** Obtain royalty-free MP3 files for the four built-in sounds and place them
   in `app/src/main/assets/sounds/`

3. **Device testing:** Connect your Android phone via USB, enable USB debugging,
   run the app via Android Studio's Run button

4. **Health Connect verification:** After Phase 2, manually check
   Android Settings → Health Connect → Browse data → Mindfulness

5. **Play Store submission:** Sign APK with a keystore, create Play Console listing,
   submit Health Connect permissions declaration form to Google

---

## Notes for Handling Build Errors

If you encounter Gradle sync or build errors:

- **Kapt/KSP conflict:** This project uses KSP only — if any kapt annotation processor appears, replace it with ksp()
- **Compose compiler version mismatch:** The compose-bom version is pinned — do not upgrade it independently
- **Health Connect `@OptIn` errors:** Add `@OptIn(ExperimentalFeatureAvailabilityApi::class)` to the calling function and the file-level `@file:OptIn` if needed
- **Hilt missing component:** Ensure `@HiltAndroidApp` is on `MTimerApp` and `@AndroidEntryPoint` is on `MainActivity`, `MeditationForegroundService`, and `BootReceiver`
- **Room schema export warning:** Add `room.schemaLocation` to KSP arguments in `build.gradle.kts`

---

## Reference URLs (for Claude Code to fetch if needed)

- Health Connect mindfulness API: https://developer.android.com/health-and-fitness/guides/health-connect/develop/mindfulness
- Health Connect permissions: https://developer.android.com/health-and-fitness/guides/health-connect/develop/get-started
- Glance widget: https://developer.android.com/jetpack/compose/glance
- Foreground services: https://developer.android.com/guide/components/foreground-services
