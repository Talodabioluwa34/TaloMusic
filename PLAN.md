# TaloMusic — Custom Android Music Player Master Plan

**Project**: TaloMusic  
**Platform**: Android (Native Kotlin)  
**Target Hardware**: Personal Android Smartphone  
**Architecture**: Jetpack Compose + AndroidX Media3 (ExoPlayer) + Clean Architecture (MVI/MVVM)

---

## 1. Project Overview & Vision

**TaloMusic** is a high-performance, offline-first native Android music player crafted specifically for personal use. It focuses on audio fidelity, battery-efficient design, and an expressive, customizable aesthetic that outperforms generic stock music players.

### Core Objectives
1. **Zero Bloat & 100% Offline**: Instant startup with no ads, trackers, or unwanted cloud sync.
2. **Audio Powerhouse**: Studio-grade 5/10-band hardware equalizer, dedicated bass booster, audio virtualizer, and real-time waveform visualizers.
3. **Immersive Custom Aesthetics**: True AMOLED Pitch Black mode for OLED battery efficiency, plus Cyberpunk Neon and frosted glassmorphic styles.
4. **Intuitive Organization**: Hybrid library supporting tag-based views (Artists, Albums, Genres) and raw directory folder browsing for custom storage structures.

---

## 2. Feature Specification Matrix

| Category | Feature | Description |
|---|---|---|
| **Audio Core** | Lossless & Compressed Support | High-res playback of FLAC, WAV, ALAC, MP3, AAC, M4A, OGG, and OPUS. |
| | Background Playback | Uninterrupted audio via AndroidX `MediaSessionService` with lock-screen widget and notification controls. |
| | Hardware Audio Effects | Hardware-accelerated 5/10-band Graphic Equalizer, Bass Boost slider, and 3D Virtualizer. |
| | Real-time Visualizer | 60 FPS responsive frequency bars and waveform animations synchronized to playback. |
| | Seamless Transitions | Gapless playback and customizable crossfade between tracks. |
| **Library & Management** | Fast Local Scanner | MediaStore API indexer with instant delta scanning on app launch. |
| | Folder-Based Browsing | Direct filesystem navigation for SD card and internal storage folders. |
| | Smart & Custom Playlists | Favorites, Most Played, Recently Added, and custom user-managed playlists. |
| | Tag & Metadata Viewer | Read ID3v1/ID3v2 tags (Title, Artist, Album, Year, Bitrate, Sample Rate, Embedded Cover Art). |
| **Player UX** | Synced & Plain Lyrics | Support for embedded `.lrc` synchronized lyrics with auto-scroll and manual text view. |
| | Sleep Timer | Countdown timer (15, 30, 45, 60 min, or custom) with smooth audio fade-out. |
| | Variable Playback Speed | Pitch-preserving playback speed adjustment (0.5x to 2.0x). |
| | Headset & Bluetooth Actions | Auto-pause on disconnect; Bluetooth AVRCP metadata sync; headphone button remote controls. |
| **Theming & Visuals** | AMOLED Pitch Black | Pure `#000000` dark theme optimized for OLED power conservation. |
| | Cyberpunk Neon | Dark navy/violet background with glowing cyan and magenta accents. |
| | Dynamic Glassmorphism | Ambient background glow that dynamically tints to match the active album artwork. |

---

## 3. Technology Stack & Architecture

```
+-------------------------------------------------------------------------+
|                           Jetpack Compose UI                            |
|  +------------------------+  +-------------------+  +----------------+  |
|  |  Now Playing Screen    |  |  Library & Tabs   |  | Equalizer & FX |  |
|  |  (Controls, Visualizer)|  |  (Tracks, Folders)|  | (5-Band, Bass) |  |
|  +------------------------+  +-------------------+  +----------------+  |
|  +------------------------+  +-------------------+  +----------------+  |
|  |  Lyrics Display View   |  |  Playlists Engine |  | Theme Selector |  |
|  +------------------------+  +-------------------+  +----------------+  |
+-------------------------------------------------------------------------+
                                     │
                             State Flow & MVI
                                     │
+-------------------------------------------------------------------------+
|                             Domain & Data                               |
|  +-------------------------------+     +-----------------------------+  |
|  | Room Database (SQLite)        |     | MediaStore Content Provider |  |
|  | Playlists, Favorites, History |     | Audio file & metadata index |  |
|  +-------------------------------+     +-----------------------------+  |
|  +-------------------------------------------------------------------+  |
|  | Repositories (MusicRepository, PlaylistRepository, Preferences)   |  |
|  +-------------------------------------------------------------------+  |
+-------------------------------------------------------------------------+
                                     │
                               MediaSession
                                     │
+-------------------------------------------------------------------------+
|                     AndroidX Media3 Service Layer                       |
|  +-------------------------------------------------------------------+  |
|  | MusicService (MediaSessionService)                                |  |
|  | ├── ExoPlayer Engine (Audio rendering, queue management)          |  |
|  | ├── AudioEffectsController (Equalizer, BassBoost, Virtualizer)    |  |
|  | ├── VisualizerEngine (FFT waveform capture for Compose UI)       |  |
|  | └── NotificationProvider (MediaStyle notification + Lockscreen)   |  |
|  +-------------------------------------------------------------------+  |
+-------------------------------------------------------------------------+
```

### Key Libraries
- **Language**: Kotlin 2.0+
- **UI**: Jetpack Compose, Material 3, Compose Navigation
- **Audio Engine**: `androidx.media3:media3-exoplayer`, `androidx.media3:media3-session`, `androidx.media3:media3-ui`
- **Database & Storage**: AndroidX Room, DataStore Preferences
- **Image Loading**: Coil Compose (for album art rendering & memory caching)
- **Dependency Injection**: Kotlin Flow / StateFlow / ViewModel

---

## 4. Planned Project Directory Structure

```
TaloMusic/
├── PLAN.md                                    # This planning document
├── build.gradle.kts                           # Root Gradle build script
├── settings.gradle.kts                        # Gradle module declarations
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
└── app/
    ├── build.gradle.kts                       # App module dependencies & plugins
    ├── proguard-rules.pro
    └── src/
        └── main/
            ├── AndroidManifest.xml            # Permissions, Service & Activity declarations
            ├── java/
            │   └── com/
            │       └── talodabi/
            │           └── talomusic/
            │               ├── MainActivity.kt
            │               ├── TaloMusicApp.kt
            │               ├── data/
            │               │   ├── local/
            │               │   │   ├── AppDatabase.kt
            │               │   │   ├── dao/
            │               │   │   │   ├── PlaylistDao.kt
            │               │   │   │   └── SongDao.kt
            │               │   │   └── entity/
            │               │   │       ├── PlaylistEntity.kt
            │               │   │       └── SongEntity.kt
            │               │   ├── model/
            │               │   │   ├── Song.kt
            │               │   │   ├── Album.kt
            │               │   │   ├── Artist.kt
            │               │   │   ├── Folder.kt
            │               │   │   ├── Lyrics.kt
            │               │   │   └── EqualizerPreset.kt
            │               │   └── repository/
            │               │       ├── MusicRepository.kt
            │               │       └── MediaStoreScanner.kt
            │               ├── playback/
            │               │   ├── MusicService.kt
            │               │   ├── AudioEffectsController.kt
            │               │   ├── VisualizerManager.kt
            │               │   ├── SleepTimerManager.kt
            │               │   └── PlaybackConnection.kt
            │               └── ui/
            │                   ├── components/
            │                   │   ├── MiniPlayer.kt
            │                   │   ├── WaveVisualizer.kt
            │                   │   ├── SongListItem.kt
            │                   │   ├── TrackSlider.kt
            │                   │   └── EqualizerSlider.kt
            │                   ├── screens/
            │                   │   ├── HomeScreen.kt
            │                   │   ├── NowPlayingScreen.kt
            │                   │   ├── LibraryScreen.kt
            │                   │   ├── FolderBrowserScreen.kt
            │                   │   ├── EqualizerScreen.kt
            │                   │   ├── LyricsScreen.kt
            │                   │   └── SettingsScreen.kt
            │                   ├── theme/
            │                   │   ├── Color.kt
            │                   │   ├── Theme.kt
            │                   │   ├── Type.kt
            │                   │   └── ThemeMode.kt
            │                   └── viewmodel/
            │                       ├── PlayerViewModel.kt
            │                       ├── LibraryViewModel.kt
            │                       └── EqualizerViewModel.kt
            └── res/
                ├── drawable/
                ├── mipmap-anydpi-v26/
                └── values/
                    ├── strings.xml
                    └── themes.xml
```

---

## 5. Step-by-Step Implementation Roadmap

### Phase 1: Environment & Project Scaffolding
- [ ] Configure JDK 17/21 and Android build tools.
- [ ] Initialize root and app Gradle configurations (`build.gradle.kts`, `settings.gradle.kts`).
- [ ] Set up permissions in `AndroidManifest.xml` (`READ_MEDIA_AUDIO`, `FOREGROUND_SERVICE`, `RECORD_AUDIO` for visualizer).

### Phase 2: Audio Playback Core (Media3 + ExoPlayer)
- [ ] Implement `MusicService` extending `MediaSessionService`.
- [ ] Set up notification manager with playback controls (play, pause, next, prev, seek).
- [ ] Configure audio focus handling (calls, alarms, voice navigation) and `BECOMING_NOISY` receiver (unplugging headphones).
- [ ] Create `PlaybackConnection` to bridge Jetpack Compose UI with the background service.

### Phase 3: MediaStore Scanner & Room Storage
- [ ] Implement `MediaStoreScanner` to query local files with cover art URIs.
- [ ] Set up Room database for persistent favorites, playlists, and custom EQ presets.
- [ ] Build folder tree crawler for direct directory navigation.

### Phase 4: Jetpack Compose UI & Bespoke Theming
- [ ] Implement theme engine supporting **AMOLED Pitch Black**, **Cyberpunk Neon**, and **Glassmorphism**.
- [ ] Build `HomeScreen` with Library navigation tabs (All Songs, Albums, Artists, Folders, Playlists).
- [ ] Build `NowPlayingScreen` with album art rotation/scale, seek slider, playback buttons, and `MiniPlayer`.

### Phase 5: Equalizer, Visualizer, Lyrics & Utilities
- [ ] Build hardware `AudioEffectsController` (5-band graphic EQ, BassBoost, 3D Virtualizer).
- [ ] Implement `WaveVisualizer` using Android `Visualizer` FFT captures.
- [ ] Build `LyricsScreen` with synced timestamp parsing (`.lrc`).
- [ ] Add `SleepTimerManager` with volume attenuation and auto-stop.

### Phase 6: Build & Phone Installation
- [ ] Execute `./gradlew assembleDebug` to produce `app-debug.apk`.
- [ ] Test on Android device via USB debugging (`adb install`) or direct APK transfer.
- [ ] Verify background battery optimization whitelist for seamless all-day playback.

---

## 6. How to Build & Install on Your Phone

### Option A: Via Android Studio (Recommended)
1. Install [Android Studio](https://developer.android.com/studio).
2. Open the `TaloMusic` folder in Android Studio.
3. Plug your Android phone into your PC via USB and enable **USB Debugging** in Developer Options.
4. Click **Run (Play button)** or build the APK via **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

### Option B: Via Terminal / Command Line
1. Ensure JDK 17+ and Android SDK command-line tools are in your system `PATH`.
2. Run the Gradle build:
   ```powershell
   .\gradlew assembleDebug
   ```
3. Locate the output APK:
   `app\build\outputs\apk\debug\app-debug.apk`
4. Copy the `.apk` file to your phone storage and tap to install, or deploy via ADB:
   ```powershell
   adb install app\build\outputs\apk\debug\app-debug.apk
   ```
