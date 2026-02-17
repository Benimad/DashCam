# CarDashCam - Project Overview

## Executive Summary

**CarDashCam** is a modern Android dashcam application built with Kotlin and Jetpack Compose. The application allows users to record video footage while driving, manage recorded videos, and maintain user authentication. It follows modern Android development best practices with MVVM architecture, Room database, and Material Design 3.

---

## Project Information

| Property | Value |
|----------|-------|
| **Project Name** | CarDashCam (DashCam) |
| **Package Name** | com.example.dashcam |
| **Version** | 1.0 (versionCode: 1) |
| **Min SDK** | 24 (Android 7.0 Nougat) |
| **Target SDK** | 36 (Android 14+) |
| **Compile SDK** | 36 |
| **Language** | Kotlin 2.0.21 |
| **Build System** | Gradle (Kotlin DSL) |
| **UI Framework** | Jetpack Compose |

---

## Core Features

### 1. User Authentication System
- **User Registration**: Create new accounts with name, email, and password
- **User Login**: Secure authentication with email and password
- **Session Management**: Persistent login state across app restarts
- **Input Validation**: Email format validation and password strength requirements (min 6 characters)

### 2. Video Recording
- **Real-time Camera Preview**: Live camera feed using CameraX
- **HD Video Recording**: Records video in HD quality (1280x720)
- **Audio Recording**: Captures audio alongside video
- **Recording Controls**: Start/stop recording with visual feedback
- **Duration Tracking**: Real-time display of recording duration
- **Automatic Storage**: Videos saved to device storage with automatic database entry

### 3. Video Management
- **Video Gallery**: Browse all recorded videos in chronological order
- **Video Playback**: Full-featured video player with ExoPlayer
- **Video Information**: Display file name, size, date, and duration
- **Video Deletion**: Remove unwanted videos from storage and database
- **Thumbnail Preview**: Visual preview of video content

### 4. Video Player Features
- **Play/Pause Controls**: Standard playback controls
- **Seek Functionality**: Jump forward/backward 10 seconds
- **Progress Slider**: Visual timeline with seek capability
- **Fullscreen Mode**: Landscape orientation support
- **Auto-hide Controls**: Controls fade after 4 seconds of inactivity

### 5. User Interface
- **Splash Screen**: Animated loading screen with app branding
- **Dark Theme**: Modern dark color scheme optimized for night driving
- **Material Design 3**: Latest Material Design guidelines
- **Responsive Layout**: Adapts to different screen sizes
- **Smooth Animations**: Polished transitions and visual feedback

---

## Technology Stack

### Core Technologies
- **Kotlin**: Primary programming language (v2.0.21)
- **Jetpack Compose**: Modern declarative UI framework (v1.7.6)
- **Material 3**: Latest Material Design components (v1.3.1)
- **Coroutines**: Asynchronous programming (v1.9.0)

### Architecture Components
- **Room Database**: Local data persistence (v2.6.1)
- **ViewModel**: UI state management
- **LiveData/StateFlow**: Reactive data streams
- **Navigation Compose**: Screen navigation (v2.8.5)
- **Lifecycle**: Lifecycle-aware components (v2.8.7)

### Media & Camera
- **CameraX**: Camera API (v1.4.1)
  - camera-core
  - camera-camera2
  - camera-lifecycle
  - camera-video
  - camera-view
- **Media3 ExoPlayer**: Video playback (v1.5.0)
- **Coil**: Image loading with video frame extraction (v2.7.0)

### Build Tools
- **Android Gradle Plugin**: 8.13.2
- **KSP**: Kotlin Symbol Processing (v2.0.21-1.0.28)
- **Kotlin Compose Compiler Plugin**: Integrated with Kotlin 2.0.21

---

## Project Structure

```
DashCam/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/dashcam/
│   │   │   │   ├── data/
│   │   │   │   │   ├── dao/           # Database access objects
│   │   │   │   │   ├── database/      # Room database configuration
│   │   │   │   │   ├── entity/        # Data models
│   │   │   │   │   └── repository/    # Data repositories
│   │   │   │   ├── ui/
│   │   │   │   │   ├── navigation/    # Navigation graph
│   │   │   │   │   ├── screen/        # Compose screens
│   │   │   │   │   ├── theme/         # Design system
│   │   │   │   │   └── viewmodel/     # ViewModels
│   │   │   │   ├── DashCamApplication.kt
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/                   # Resources
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                      # Unit tests
│   │   └── androidTest/               # Instrumentation tests
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml             # Version catalog
├── build.gradle.kts
└── settings.gradle.kts
```

---

## Application Flow

```
App Launch
    ↓
Splash Screen (2.5s)
    ↓
Check Login Status
    ↓
    ├─→ Not Logged In → Login Screen
    │                      ↓
    │                   Register Screen (optional)
    │                      ↓
    └─→ Logged In ────→ Camera Screen (Main)
                           ↓
                        Videos List Screen
                           ↓
                        Video Player Screen
```

---

## Key Screens

1. **SplashScreen**: Animated logo with loading indicator
2. **LoginScreen**: Email/password authentication
3. **RegisterScreen**: New user registration with validation
4. **CameraScreen**: Main recording interface
5. **VideosListScreen**: Gallery of recorded videos
6. **VideoPlayerScreen**: Full-featured video playback

---

## Data Storage

### Database Tables

#### Users Table
- id (Primary Key, Auto-generated)
- name (String)
- email (String)
- password (String) - **Security Issue: Plain text storage**

#### Videos Table
- id (Primary Key, Auto-generated)
- fileName (String)
- filePath (String)
- fileSize (Long)
- timestamp (Long)
- duration (Long)

### File Storage
- Videos stored in: `{ExternalFilesDir}/Movies/CarDashCam/`
- Naming format: `video_yyyyMMdd_HHmmss.mp4`

---

## Permissions Required

- `CAMERA`: Access device camera
- `RECORD_AUDIO`: Record audio with video
- `READ_EXTERNAL_STORAGE`: Read media files (SDK ≤ 32)
- `WRITE_EXTERNAL_STORAGE`: Write media files (SDK ≤ 32)
- `READ_MEDIA_VIDEO`: Read video files (SDK ≥ 33)

---

## Target Audience

- **Primary**: Individual drivers seeking dashcam functionality
- **Use Cases**: 
  - Accident documentation
  - Road trip recording
  - Security monitoring
  - Evidence collection

---

## Development Status

✅ **Completed Features**:
- User authentication system
- Video recording with CameraX
- Video playback with ExoPlayer
- Video management (list, view, delete)
- Database integration
- Modern UI with Material Design 3

⚠️ **Known Issues** (detailed in separate reports):
- Password stored in plain text
- No user logout from database
- Missing storage permission handling for Android 13+
- No video quality settings
- No backup/export functionality

---

## Future Enhancement Opportunities

1. **Security**: Password encryption, biometric authentication
2. **Features**: GPS tracking, speed overlay, G-sensor detection
3. **Cloud**: Cloud backup, multi-device sync
4. **Settings**: Video quality options, storage management
5. **Advanced**: Loop recording, emergency video protection
6. **Social**: Video sharing, community features

---

## Conclusion

CarDashCam is a well-structured, modern Android application that successfully implements core dashcam functionality. The codebase demonstrates good architectural practices with MVVM pattern, proper separation of concerns, and modern Android development tools. However, there are critical security issues and missing features that should be addressed before production deployment.
