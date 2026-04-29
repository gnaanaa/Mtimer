# MTimer: Power-Minimal Meditation Timer

MTimer is a bold, minimalist meditation timer designed for Android. It focuses on power efficiency and a high-contrast "Dot Matrix" aesthetic, making it ideal for AMOLED displays.

## 🌟 Features

- **Dot Matrix Aesthetic:** A unique, high-contrast UI inspired by retro LED displays and dot-matrix printers.
- **AMOLED Friendly:** True black background (in dark mode) to minimize battery consumption on OLED screens.
- **Rotary Dial Selection:** An interactive, physics-based dial for quick preset switching.
- **Flexible Presets:** Create, edit, and delete meditation configurations with custom preparation times and sound options.
- **Health Connect Integration:** Sync mindfulness minutes and heart rate data directly to Health Connect (and Google Fit).
- **Google Drive Backup:** Automatically back up your presets and session history to your Google Account.
- **Interactive Widget:** Track your weekly mindfulness progress and start sessions directly from your home screen.
- **Immersive Timer:** A distraction-free, full-screen timer with audible start/end bells.

## 🛠 Tech Stack

- **UI:** Jetpack Compose (100%)
- **Architecture:** Clean Architecture + MVVM
- **Dependency Injection:** Dagger Hilt
- **Local Database:** Room
- **Background Tasks:** WorkManager & Foreground Services
- **Data Persistence:** DataStore Preferences
- **Health Data:** Health Connect API
- **Cloud Sync:** Google Drive REST API
- **Testing:** JUnit, MockK, Coroutines Test

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug (or newer)
- Android SDK 28+
- A Google Cloud Project (for Drive API integration)

### Configuration

1. **Google Drive API:**
   - Enable the Google Drive API in the [Google Cloud Console](https://console.cloud.google.com/).
   - Add your debug and release SHA-1 fingerprints to the project credentials.
2. **Health Connect:**
   - Ensure the Health Connect app is installed on the target device (integrated in Android 14+).
3. **Assets:**
   - Place meditation bell sounds (.mp3) in `app/src/main/assets/sounds/`.

## 📂 Project Structure

- `domain`: Pure Kotlin business logic (Models, UseCases, Repository Interfaces).
- `data`: Implementation of repositories, local DB (Room), DataStore, and Sync logic (Drive/Health Connect).
- `ui`: Jetpack Compose screens, ViewModels, and Navigation.
- `service`: Foreground services for the active timer and audio playback.
- `widget`: Glance-based app widgets for home screen tracking.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
