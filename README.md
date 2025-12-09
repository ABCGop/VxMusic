# VxMusic 🎵

A modern Android music streaming app built with Jetpack Compose and Material 3 design.

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-orange.svg)](https://developer.android.com/about/versions/oreo)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-brightgreen.svg)](https://developer.android.com/about/versions)

## 📱 About

VxMusic is a feature-rich music streaming application for Android that provides seamless access to YouTube Music content. Built with modern Android development practices, it offers a beautiful Material 3 interface and powerful features for music lovers.

## ✨ Features

### Core Features
- 🎨 **Modern UI** - Beautiful Material 3 design with Jetpack Compose
- 🎵 **YouTube Music** - Stream millions of songs
- 💻 **Chromebook Compatible** - Full support with keyboard shortcuts and resizable windows
- 📱 **16 KB Page Size Support** - Optimized for Android 15+ devices
- 🔔 **Push Notifications** - Stay updated with Firebase Cloud Messaging
- 🔄 **In-App Updates** - Seamless updates via Google Play
- 📥 **Download Manager** - Save music for offline playback
- 🎧 **Advanced Playback** - Lyrics, queue management, and more
- 🌓 **Dark Mode** - Eye-friendly dark theme support
- 🔍 **Smart Search** - Find songs, artists, albums, and playlists
- 📊 **Charts & Trending** - Discover what's hot

### 🔥 NEW: Admin Control System
- 🛡️ **Remote User Management** - Block/unblock users without app updates
- 📢 **Push Notifications** - Send in-app notifications to all users or specific users
- 💬 **In-App Popups** - Show info, warnings, updates, or promos instantly
- 🎨 **Dynamic Theme Control** - Change app colors remotely (no update needed!)
- ⚙️ **Feature Toggles** - Enable/disable features on-the-fly
- 🎮 **Remote Music Control** - Control playback from any device via web interface
- 📊 **Activity Logs** - Track all admin actions with timestamps
- 🌐 **Web Admin Panel** - Beautiful web interface to control everything

## 🛠️ Tech Stack

- **Language**: Kotlin 2.2.20
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Koin
- **Networking**: Retrofit, OkHttp
- **Media Playback**: ExoPlayer (Media3)
- **Database**: Room
- **Async**: Coroutines & Flow
- **Firebase**: Cloud Messaging, Analytics

## 📦 Build Variants

VxMusic comes in two flavors:

### Full Flavor
- Google Play Services integration
- Firebase Cloud Messaging for push notifications
- In-App Updates via Google Play
- Firebase Analytics

### FOSS Flavor
- Completely open source
- No proprietary dependencies
- No Google Play Services required
- Perfect for F-Droid and privacy-focused users

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug or later
- JDK 21
- Android SDK 36
- Gradle 8.14.3

### Building from Source

1. **Clone the repository**
   ```bash
   git clone https://github.com/ABCGop/VxMusic.git
   cd VxMusic
   ```

2. **Configure signing (for release builds)**
   ```bash
   cp keystore.properties.template keystore.properties
   # Edit keystore.properties with your keystore details
   ```

3. **Add Firebase configuration (for full flavor)**
   - Download `google-services.json` from Firebase Console
   - Place it in the `app/` directory

4. **Build the app**
   ```bash
   # Debug build
   ./gradlew assembleFullDebug
   
   # Release build
   ./gradlew assembleFullRelease
   
   # FOSS build
   ./gradlew assembleFossRelease
   ```

## 🔐 Security

This repository does **NOT** include:
- Keystore files (`.jks`)
- Keystore passwords (`keystore.properties`)
- Firebase configuration (`google-services.json`)
- Google Cloud credentials

## 📋 Requirements

- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 15 (API 36)
- **Compile SDK**: Android 15 (API 36)

## 💻 Platform Support

- **Android Phones**: ✅ Fully supported
- **Android Tablets**: ✅ Adaptive UI with tablet layout
- **Chromebooks**: ✅ Full support with keyboard shortcuts ([Learn more](CHROMEBOOK_SUPPORT.md))
- **Android Auto**: ✅ Media controls while driving
- **Android TV**: 🚧 Coming soon

## 🙏 Acknowledgments

### Based on SimpMusic

VxMusic is built upon the excellent work of the [SimpMusic](https://github.com/maxrave-dev/SimpMusic) project by [maxrave-dev](https://github.com/maxrave-dev). We are grateful for their outstanding open-source contribution that made this project possible.

**Original Project**: [SimpMusic](https://github.com/maxrave-dev/SimpMusic)

Key components inherited from SimpMusic:
- YouTube Music scraper and API integration
- Core media playback architecture
- Database schema and repository patterns
- Base UI components and navigation structure

### Other Credits

- **ExoPlayer** - Media playback by Google
- **Jetpack Compose** - Modern UI toolkit by Google
- **Coil** - Image loading library
- **Material 3** - Design system by Google
- **Koin** - Dependency injection framework

## 📜 License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

This means:
- ✅ You can use this code freely
- ✅ You can modify and distribute it
- ✅ You must disclose source code
- ✅ You must license derivative works under GPL v3
- ✅ You must state changes made to the code

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/ABCGop/VxMusic/issues)
- **Email**: [opabcg@gmail.com](mailto:opabcg@gmail.com)

## 🌟 Star History

If you like this project, please consider giving it a ⭐ on GitHub!

## ⚠️ Disclaimer

This app is for educational purposes only. VxMusic does not host or store any music files. It streams content from publicly available sources. Please respect copyright laws and support artists by purchasing their music.

---

**Made with ❤️ by Vishesh Gangwar**

**Built on the foundation of [SimpMusic](https://github.com/maxrave-dev/SimpMusic)**
