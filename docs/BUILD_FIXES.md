# Build Errors - Quick Fix Guide

## Missing Material Icons

The following Material Icons don't exist in the default `material-icons-core` library and need to be replaced or the extended library needs to be added.

### Option 1: Add Extended Icons Library (RECOMMENDED)

Add to `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation("androidx.compose.material:material-icons-extended:1.7.6")
}
```

This will provide all the missing icons:
- `Videocam`
- `VideoLibrary`
- `VideoFile`
- `CalendarToday`
- `Storage`
- `ErrorOutline`
- `Visibility` / `VisibilityOff`
- `PersonAdd`
- `Cancel`
- `Security`
- `RadioButtonUnchecked`
- `DirectionsCar`
- `FullscreenExit` / `Fullscreen`
- `Replay10` / `Forward10`
- `Pause`

### Option 2: Replace with Available Icons

If you don't want to add the extended library, replace icons in the code:

| Missing Icon | Replace With |
|--------------|--------------|
| `Videocam` | `PlayCircle` or `Camera` |
| `VideoLibrary` | `VideoCall` or `Collections` |
| `VideoFile` | `Movie` or `PlayCircle` |
| `CalendarToday` | `DateRange` or `Event` |
| `Storage` | `Folder` or `Save` |
| `ErrorOutline` | `Error` or `Warning` |
| `Visibility` | `RemoveRedEye` |
| `VisibilityOff` | `VisibilityOff` (use `RemoveRedEye` with alpha) |
| `PersonAdd` | `Person` or `Add` |
| `Cancel` | `Close` or `Clear` |
| `Security` | `Lock` or `Shield` |
| `RadioButtonUnchecked` | `RadioButtonChecked` (with alpha) or `Circle` |
| `DirectionsCar` | `DriveEta` or `LocalTaxi` |
| `FullscreenExit` | `Close` or `ArrowBack` |
| `Fullscreen` | `OpenInFull` or `AspectRatio` |
| `Replay10` | `Replay` or `SkipPrevious` |
| `Forward10` | `Forward` or `SkipNext` |
| `Pause` | `PauseCircle` or create custom |

## Quick Fix: Add Extended Icons

**Step 1**: Open `app/build.gradle.kts`

**Step 2**: Add this line in the `dependencies` block:

```kotlin
implementation("androidx.compose.material:material-icons-extended:1.7.6")
```

**Step 3**: Sync Gradle

**Step 4**: Rebuild project

This will resolve ALL icon-related errors immediately.

## Already Fixed Issues

✅ Duplicate `formatFileSize()` functions - Consolidated into `FormatUtils`
✅ Duplicate `formatDuration()` functions - Consolidated into `FormatUtils`  
✅ Duplicate `formatDate()` functions - Consolidated into `FormatUtils`
✅ `userId` parameter added to Video entity and CameraViewModel
✅ `const val` Pair issue in DesignTokens - Changed to `val`
✅ SessionManager integration completed
✅ Storage check before recording implemented
✅ Permission handling with "Don't ask again" scenario
✅ Exit confirmation dialog on back press

## Build Command

After adding the extended icons library:

```bash
gradlew.bat assembleDebug
```

Or in Android Studio: **Build > Rebuild Project**
