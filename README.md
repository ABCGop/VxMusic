# VxMusic

A FOSS YouTube Music client for Android and Desktop with many features from Spotify, SponsorBlock, ReturnYouTubeDislike using Compose Multiplatform.

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-orange.svg)](https://developer.android.com/about/versions/oreo)

## About

VxMusic is a feature-rich music streaming application for Android and Desktop that provides seamless access to YouTube Music content. Built with Compose Multiplatform, it offers a beautiful Material 3 interface and powerful features for music lovers.

**Based on [SimpMusic](https://github.com/maxrave-dev/SimpMusic)** by [maxrave-dev](https://github.com/maxrave-dev).

## Features

- Play music from YouTube Music or YouTube for free, without ads and in the background
- High quality up-to 256kbps stream for YouTube Music Premium users
- Browsing Home, Charts, Podcast, Moods & Genre with YouTube Music data at high speed
- Search everything on YouTube
- Analyze your playing data, create custom playlists, and sync with YouTube Music
- Spotify Canvas supported
- Play 1080p video option with subtitle
- AI song suggestions
- Customize your playlist, synced with YouTube Music
- Notifications from followed artists
- Caching and offline playback support
- Crossfade with DJ-style like Apple Music
- Synced lyrics from multiple providers, LRCLIB, Spotify and YouTube Transcript - AI lyrics translation (BETA)
- Personalize data and multi-YouTube-account support
- Local "scrobble" like Last.fm
- Supports SponsorBlock and Return YouTube Dislike
- Sleep Timer
- Android Auto with online content
- Discord Rich Presence support
- Desktop support (Windows, macOS, Linux)
- And many more!

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Compose Multiplatform (Jetpack Compose)
- **Architecture**: Clean Architecture + MVVM
- **Dependency Injection**: Koin
- **Networking**: Ktor Client
- **Media Playback**: Media3 (ExoPlayer) for Android, VLCJ for Desktop
- **Database**: Room
- **Async**: Coroutines & Flow
- **Build System**: Gradle (Kotlin DSL)

## Module Structure

- **androidApp/**: Android-specific entry point
- **composeApp/**: Shared Compose Multiplatform module (Android, Desktop, iOS)
- **core/**: Core modules (common, data, domain, media, service)
- **crashlytics/**: Crash reporting (Full version with Sentry)
- **crashlytics-empty/**: FOSS version without tracking

## Build Variants

### Android
- **Full**: With Sentry crash reporting
- **FOSS**: No tracking, no proprietary dependencies

### Desktop
- **Windows**: `.msi` installer
- **macOS**: `.dmg` (ARM and x86-64)
- **Linux**: `.AppImage`

## Building from Source

### Prerequisites

- Android Studio or IntelliJ IDEA
- JDK 21
- Android SDK 37

### Build

```bash
# Clone the repository
git clone https://github.com/ABCGop/VxMusic.git
cd VxMusic

# Debug build (Android)
./gradlew assembleFullDebug

# Release build (Android)
./gradlew assembleFullRelease

# FOSS build (Android)
./gradlew assembleFossRelease

# Desktop
./gradlew :composeApp:run
```

## Requirements

- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 15 (API 36)
- **Compile SDK**: Android 16 (API 37)

## Acknowledgments

VxMusic is built upon the excellent [SimpMusic](https://github.com/maxrave-dev/SimpMusic) project by [maxrave-dev](https://github.com/maxrave-dev). We are grateful for their outstanding open-source contribution.

### Other Credits

- **Media3 (ExoPlayer)** - Media playback by Google
- **Jetpack Compose** - Modern UI toolkit by Google
- **VLCJ** - Desktop audio playback
- **Ktor** - HTTP client
- **Koin** - Dependency injection
- **SponsorBlock** - Skip sponsors
- **ReturnYouTubeDislike** - Vote information
- **LRCLIB** - Lyrics provider

## License

This project is licensed under the GPL-3.0 License - see the [LICENSE](LICENSE) file for details.
