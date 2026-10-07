# Talo Music 🎵

> **Your music. Your space.**  
> A premium, local-first Android music player and musician study workspace.

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-brightgreen.svg)](https://developer.android.com)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)
[![Audio](https://img.shields.io/badge/Audio-AndroidX%20Media3%20%2F%20ExoPlayer-orange.svg)](https://developer.android.com/guide/topics/media/media3)
[![Database](https://img.shields.io/badge/Database-Room-yellow.svg)](https://developer.android.com/training/data-storage/room)
[![Brand](https://img.shields.io/badge/Brand%20Accent-%23FF8232-orange.svg)](#color-system)

---

## 1. Product Principle

> **"Music is the content. The interface should get out of its way."**

Talo Music is **NOT** a streaming service and **NOT** a Spotify clone. It is a local-first, distraction-free music player designed around the user's own library with zero internet dependency, zero ads, zero telemetry, and reliable background playback.

---

## 2. Key Features

### 🎧 Core Playback
* **AndroidX Media3 (ExoPlayer)**: Studio-grade audio rendering supporting high-res FLAC, WAV, ALAC, MP3, AAC, M4A, OGG, and OPUS.
* **Background Playback**: Continuous foreground playback using `MediaSessionService` with lock-screen widgets and notification controls.
* **Hardware Audio Effects**: Hardware-accelerated 5-band Equalizer, dedicated Bass Booster, and 3D Virtualizer.
* **Interruption Handling**: Instant audio focus management and `BECOMING_NOISY` receiver (auto-pausing on headphone/Bluetooth disconnect).

### 📚 Local Library Management
* **MediaStore Scanning**: Rapid scanning of local audio files with cover art URIs, durations, track numbers, and genres without duplicating files.
* **Room Database**: Persistent storage for Favorites, custom playlists, recent play history, and user settings.
* **Instant Offline Search**: Zero-latency local search across tracks, artists, albums, and playlists.

### 🔬 Music Lab (Musician Study Workspace)
* **Core Principle**: *"Computer detects → Musician verifies → Talo remembers."*
* **Key & BPM Detection**: Automatic key estimation (Major/Minor) and tempo calculation, with 1-tap musician override.
* **Chord Progression Timeline**: Structured chord events (`rootPitchClass`, `quality`, `bassPitchClass`, `confidence`, `source`).
* **Manual Chord Editing**: Add, edit, or delete chords with interactive 12-pitch selector and qualities (`Maj`, `m`, `7`, `maj7`, `m7`, `sus4`, `dim`, `aug`). Musician corrections are persisted as authoritative.
* **Chord Transposition**: Real-time transposition (+/- semitones) while preserving chord qualities (e.g. `Cmaj7` + 2 st = `Dmaj7`).
* **Harmonic Notation Switching**: Instant conversion to **Roman Numerals** (`I`, `vi`, `IV`, `V`) and **Nashville Numbers** (`1`, `6m`, `4`, `5`).
* **A-B Loop Practice**: Set start `A` and end `B` boundaries to continuously loop difficult phrases.
* **Pitch-Preserving Speed**: Variable playback speed (`0.50x`, `0.75x`, `1.00x`, `1.25x`, `1.50x`).

---

## 3. Design System

### Typography
* **Primary Typeface**: **Manrope**
* **Scale**:
  * Display: 32 / 40 / 700
  * H1: 28 / 36 / 700
  * H2: 22 / 28 / 700
  * H3: 18 / 24 / 600
  * Body: 16 / 24 / 400
  * Body Small: 14 / 20 / 400
  * Label: 13 / 18 / 500
  * Caption: 12 / 16 / 500

### Color System
* **Brand Accent**: `#FF8232` (Primary Orange)
* **Rule**: ~90% neutral surfaces/text + 10% brand accent.
* **Dark Theme (Hero Visual Experience)**:
  * Background: `#0A0A0A`
  * Surface: `#141414`
  * Surface Elevated: `#1C1C1C`
  * Surface Strong: `#242424`
  * Primary Text: `#F7F5F2`
  * Secondary Text: `#A8A29D`
  * Divider: `#292929`
* **Light Theme (Warm Editorial)**:
  * Background: `#FAF9F7`
  * Surface: `#FFFFFF`
  * Surface Elevated: `#F3F1EE`
  * Primary Text: `#171513`
  * Secondary Text: `#6F6A65`
  * Divider: `#E7E2DC`

### Spacing & Corners
* **4px Base Spacing System**: 4, 8, 12, 16, 20, 24, 32, 40, 48, 64 dp.
* **Corner Tokens**: Small 8dp, Medium 12dp, Large 16dp, Hero 20dp, Circular 50%.

---

## 4. Architecture & Directory Structure

```text
TaloMusic/
├── app/
│   ├── build.gradle.kts                       # Jetpack Compose, Media3, Room dependencies
│   ├── proguard-rules.pro
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml            # Media playback service & storage permissions
│           ├── java/com/talodabi/talomusic/
│           │   ├── TaloApplication.kt         # Application lifecycle and DI singletons
│           │   ├── MainActivity.kt            # Activity host, permissions, theme observer
│           │   ├── data/
│           │   │   ├── analysis/              # AudioAnalysisEngine (Key/BPM/Chords heuristics)
│           │   │   ├── local/                 # Room Database, DAOs, and Entities
│           │   │   ├── model/                 # Domain models (Song, Album, Artist, Playlist)
│           │   │   ├── music/                 # Music Theory Engine (PitchClass, Chords, Roman Numerals)
│           │   │   └── repository/            # MusicRepository, AnalysisRepository, SettingsRepository
│           │   ├── playback/                  # Media3 MusicService, PlaybackConnection, AudioEffects
│           │   └── ui/
│           │       ├── components/            # MiniPlayer, SongListItem, AlbumCard, TrackSlider, TaloLogo
│           │       ├── navigation/            # Screen routes and TaloAppScaffold NavGraph
│           │       ├── screens/
│           │       │   ├── home/              # HomeScreen (Greeting, Recently Played, Playlists)
│           │       │   ├── library/           # LibraryScreen (Songs, Albums, Artists, Genres)
│           │       │   ├── musiclab/          # MusicLabScreen, ChordEditorDialog, KeyBpmEditorDialog
│           │       │   ├── nowplaying/        # NowPlayingScreen (Hero artwork, swipe gestures, queue)
│           │       │   ├── playlist/          # PlaylistDetailScreen
│           │       │   └── settings/          # SettingsScreen, EqualizerDialog
│           │       ├── theme/                 # Color, Type, Spacing, Shape, Theme tokens
│           │       └── viewmodel/             # MusicViewModel
│           └── res/                           # Vector assets, launcher icons, values
├── preview/                                   # Live interactive browser preview & design testbed
├── build.gradle.kts                           # Root Gradle build script
├── settings.gradle.kts                        # Module definitions
└── gradle/                                    # Gradle wrapper and version catalog
```

---

## 5. Building & Running

### Option A: Via Android Studio (Recommended)
1. Open [Android Studio](https://developer.android.com/studio).
2. Select **Open** and select the `TaloMusic` root folder.
3. Plug your Android phone in via USB with **USB Debugging** enabled in Developer Options.
4. Click the green **Run `▶`** button (or press `Shift + F10`).

### Option B: Via Command Line
```powershell
# Compile debug APK
.\gradlew assembleDebug

# Output APK path:
# app\build\outputs\apk\debug\app-debug.apk

# Deploy to connected device via ADB:
adb install app\build\outputs\apk\debug\app-debug.apk
```

---

## 6. Live Interactive Prototype

To test the design system, gestures, audio synth, and Music Lab immediately in a browser:
```powershell
cd preview
npm install
npm run dev
```
Open `http://localhost:5173/` (or from your mobile browser at `http://<your-pc-ip>:5173/`).

---

## 7. License
Distributed under the MIT License. Crafted with precision for personal audio enjoyment.
