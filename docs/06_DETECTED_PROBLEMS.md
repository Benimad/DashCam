# Detected Problems & Issues

## Critical Issues (Must Fix Before Production)

### 🔴 CRITICAL #1: Plain Text Password Storage

**Severity**: CRITICAL 🔴  
**Category**: Security  
**Location**: `User` entity, `AuthViewModel`

**Problem**:
Passwords are stored in plain text in the Room database without any encryption or hashing.

**Code**:
```kotlin
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val password: String  // ❌ Plain text!
)
```

**Impact**:
- Anyone with database access can read all passwords
- Violates security best practices
- Vulnerable to data breaches
- Non-compliant with security standards (GDPR, OWASP)

**How to Fix**:
```kotlin
// Option 1: Use BCrypt
dependencies {
    implementation("at.favre.lib:bcrypt:0.10.2")
}

// In AuthViewModel
fun register(name: String, email: String, password: String) {
    val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())
    val user = User(name = name, email = email, password = hashedPassword)
    userRepository.insert(user)
}

fun login(email: String, password: String) {
    val user = userRepository.getUserByEmail(email)
    if (user != null) {
        val result = BCrypt.verifyer().verify(password.toCharArray(), user.password)
        if (result.verified) {
            // Login success
        }
    }
}

// Option 2: Use Android Security Crypto
dependencies {
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
```

**Priority**: IMMEDIATE

---

### 🔴 CRITICAL #2: Broken Session Management

**Severity**: CRITICAL 🔴  
**Category**: Authentication  
**Location**: `AuthViewModel.checkLoginStatus()`

**Problem**:
The app uses `getAnyUser()` to check login status, which returns the first user in the database regardless of who actually logged in. This breaks multi-user support and proper logout functionality.

**Code**:
```kotlin
private fun checkLoginStatus() {
    viewModelScope.launch {
        val user = userRepository.getAnyUser()  // ❌ Gets ANY user!
        _authState.value = _authState.value.copy(
            isLoggedIn = user != null,
            currentUser = user
        )
    }
}
```

**Impact**:
- Multi-user support completely broken
- Logout doesn't work (only clears ViewModel state)
- Wrong user may be logged in after app restart
- User can't switch accounts

**How to Fix**:
```kotlin
// 1. Add SharedPreferences to store logged-in user ID
class SessionManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    
    fun saveUserId(userId: Long) {
        prefs.edit().putLong("user_id", userId).apply()
    }
    
    fun getUserId(): Long? {
        val id = prefs.getLong("user_id", -1)
        return if (id == -1L) null else id
    }
    
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}

// 2. Update AuthViewModel
private fun checkLoginStatus() {
    viewModelScope.launch {
        val userId = sessionManager.getUserId()
        if (userId != null) {
            val user = userRepository.getUserById(userId)
            _authState.value = _authState.value.copy(
                isLoggedIn = user != null,
                currentUser = user
            )
        }
    }
}

fun login(email: String, password: String) {
    viewModelScope.launch {
        val user = userRepository.login(email, password)
        if (user != null) {
            sessionManager.saveUserId(user.id)  // Save session
            _authState.value = _authState.value.copy(
                isLoggedIn = true,
                currentUser = user
            )
        }
    }
}

fun logout() {
    sessionManager.clearSession()  // Clear session
    _authState.value = AuthState(isLoggedIn = false, currentUser = null)
}
```

**Priority**: IMMEDIATE

---

### 🔴 CRITICAL #3: Missing Storage Permissions (Android 13+)

**Severity**: CRITICAL 🔴  
**Category**: Permissions  
**Location**: `CameraScreen`, `AndroidManifest.xml`

**Problem**:
The app doesn't request storage permissions at runtime for Android 13+ (API 33+). While `READ_MEDIA_VIDEO` is declared in the manifest, it's never requested at runtime.

**Code**:
```kotlin
// AndroidManifest.xml
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />

// CameraScreen.kt - Only requests CAMERA and RECORD_AUDIO
val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
) { permissions ->
    hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
    hasAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] == true
    // ❌ Missing READ_MEDIA_VIDEO check
}
```

**Impact**:
- App may crash when accessing videos on Android 13+
- Videos may not be visible in the list
- Violates Android 13+ storage permissions model

**How to Fix**:
```kotlin
// CameraScreen.kt
var hasStoragePermission by remember {
    mutableStateOf(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, 
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not needed for older versions
        }
    )
}

val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
) { permissions ->
    hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
    hasAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] == true
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        hasStoragePermission = permissions[Manifest.permission.READ_MEDIA_VIDEO] == true
    }
}

LaunchedEffect(Unit) {
    val permissionsToRequest = mutableListOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
    }
    permissionLauncher.launch(permissionsToRequest.toTypedArray())
}
```

**Priority**: IMMEDIATE

---

### 🔴 CRITICAL #4: No Database Encryption

**Severity**: CRITICAL 🔴  
**Category**: Security  
**Location**: `AppDatabase`

**Problem**:
The Room database is not encrypted, making all data (including passwords) accessible on rooted devices or via ADB backup.

**Impact**:
- User data vulnerable to extraction
- Passwords readable (especially critical with plain text storage)
- Privacy violation
- Non-compliant with data protection regulations

**How to Fix**:
```kotlin
// Add SQLCipher dependency
dependencies {
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")
}

// Update AppDatabase
fun getDatabase(context: Context): AppDatabase {
    return INSTANCE ?: synchronized(this) {
        val passphrase = SQLiteDatabase.getBytes("your-secure-passphrase".toCharArray())
        val factory = SupportFactory(passphrase)
        
        val instance = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "dashcam_database"
        )
        .openHelperFactory(factory)  // Add encryption
        .build()
        
        INSTANCE = instance
        instance
    }
}
```

**Priority**: HIGH

---

## High Priority Issues

### 🟠 HIGH #5: No Database Migration Strategy

**Severity**: HIGH 🟠  
**Category**: Data Persistence  
**Location**: `AppDatabase`

**Problem**:
```kotlin
@Database(
    entities = [User::class, Video::class],
    version = 1,
    exportSchema = false  // ❌ Should be true
)
```

**Impact**:
- App will crash if database schema changes
- Users will lose all data on updates
- No version control for database

**How to Fix**:
```kotlin
@Database(
    entities = [User::class, Video::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add migration logic
                database.execSQL("ALTER TABLE videos ADD COLUMN user_id INTEGER")
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "dashcam_database"
            )
            .addMigrations(MIGRATION_1_2)
            .build()
        }
    }
}
```

**Priority**: HIGH

---

### 🟠 HIGH #6: No Dependency Injection

**Severity**: HIGH 🟠  
**Category**: Architecture  
**Location**: `MainActivity`, `NavGraph`

**Problem**:
Dependencies are created manually, leading to tight coupling and difficult testing.

**Code**:
```kotlin
// MainActivity.kt
val database = (application as DashCamApplication).database
val userRepository = UserRepository(database.userDao())
val videoRepository = VideoRepository(database.videoDao())

// NavGraph.kt
val viewModelFactory = ViewModelFactory(userRepository, videoRepository)
val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
```

**Impact**:
- Tight coupling
- Difficult to test
- Boilerplate code
- No scope management

**How to Fix**:
```kotlin
// Add Hilt
dependencies {
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
}

// Application
@HiltAndroidApp
class DashCamApplication : Application()

// Module
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
}

// ViewModel
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel()

// Composable
@Composable
fun LoginScreen(viewModel: AuthViewModel = hiltViewModel())
```

**Priority**: HIGH

---

### 🟠 HIGH #7: No Error Logging/Crash Reporting

**Severity**: HIGH 🟠  
**Category**: Monitoring  
**Location**: Project-wide

**Problem**:
No crash reporting or error logging implemented. Impossible to track production issues.

**Impact**:
- Can't diagnose production crashes
- No visibility into user issues
- Difficult to improve app quality

**How to Fix**:
```kotlin
// Add Firebase Crashlytics
dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
}

// Add Timber for logging
dependencies {
    implementation("com.jakewharton.timber:timber:5.0.1")
}

// Application
class DashCamApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
    }
}

// Custom Crashlytics Tree
class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.ERROR || priority == Log.WARN) {
            FirebaseCrashlytics.getInstance().log(message)
            t?.let { FirebaseCrashlytics.getInstance().recordException(it) }
        }
    }
}
```

**Priority**: HIGH

---

## Medium Priority Issues

### 🟡 MEDIUM #8: No Unit Tests

**Severity**: MEDIUM 🟡  
**Category**: Testing  
**Location**: Project-wide

**Problem**:
No unit tests implemented. Only example tests exist.

**Impact**:
- No confidence in code changes
- Difficult to refactor
- Bugs may go unnoticed

**How to Fix**:
```kotlin
// Add testing dependencies
dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("io.mockk:mockk:1.13.8")
}

// Example ViewModel test
class AuthViewModelTest {
    @Test
    fun `login with valid credentials updates state`() = runTest {
        val repository = mockk<UserRepository>()
        val viewModel = AuthViewModel(repository)
        
        coEvery { repository.login(any(), any()) } returns User(1, "Test", "test@test.com", "hash")
        
        viewModel.login("test@test.com", "password")
        
        viewModel.authState.test {
            val state = awaitItem()
            assertTrue(state.isLoggedIn)
            assertEquals("Test", state.currentUser?.name)
        }
    }
}
```

**Priority**: MEDIUM

---

### 🟡 MEDIUM #9: No Storage Quota Management

**Severity**: MEDIUM 🟡  
**Category**: Storage  
**Location**: `CameraViewModel`

**Problem**:
No check for available storage space before recording. Videos accumulate indefinitely.

**Impact**:
- May fill device storage
- App may crash when storage full
- Poor user experience

**How to Fix**:
```kotlin
fun checkStorageSpace(context: Context): Long {
    val statFs = StatFs(context.getExternalFilesDir(null)?.path)
    return statFs.availableBytes
}

fun startRecording() {
    val availableSpace = checkStorageSpace(context)
    val minimumSpace = 100 * 1024 * 1024 // 100 MB
    
    if (availableSpace < minimumSpace) {
        _cameraState.value = _cameraState.value.copy(
            errorMessage = "Espace de stockage insuffisant"
        )
        return
    }
    
    // Proceed with recording
}

// Auto-delete old videos
suspend fun cleanupOldVideos(maxVideos: Int = 50) {
    val videos = videoRepository.getAllVideos().first()
    if (videos.size > maxVideos) {
        val videosToDelete = videos.drop(maxVideos)
        videosToDelete.forEach { video ->
            videoRepository.delete(video)
            File(video.filePath).delete()
        }
    }
}
```

**Priority**: MEDIUM

---

### 🟡 MEDIUM #10: Missing Database Indices

**Severity**: MEDIUM 🟡  
**Category**: Performance  
**Location**: `User`, `Video` entities

**Problem**:
No indices on frequently queried columns (email, timestamp).

**Impact**:
- Slow queries as data grows
- Poor performance with many users/videos

**How to Fix**:
```kotlin
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class User(...)

@Entity(
    tableName = "videos",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["user_id"])  // If added
    ]
)
data class Video(...)
```

**Priority**: MEDIUM

---

### 🟡 MEDIUM #11: No ProGuard/R8 Configuration

**Severity**: MEDIUM 🟡  
**Category**: Build  
**Location**: `app/build.gradle.kts`

**Problem**:
```kotlin
release {
    isMinifyEnabled = false  // ❌ Should be true
}
```

**Impact**:
- Larger APK size
- Code not obfuscated
- Easier to reverse engineer

**How to Fix**:
```kotlin
release {
    isMinifyEnabled = true
    isShrinkResources = true
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}

// proguard-rules.pro
-keep class com.example.dashcam.data.entity.** { *; }
-keep class com.example.dashcam.data.dao.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static ** getDatabase(...);
}
```

**Priority**: MEDIUM

---

### 🟡 MEDIUM #12: No Video Pagination

**Severity**: MEDIUM 🟡  
**Category**: Performance  
**Location**: `VideosViewModel`

**Problem**:
`getAllVideos()` loads all videos at once. Will be slow with 1000+ videos.

**Impact**:
- Slow loading with many videos
- High memory usage
- Poor scrolling performance

**How to Fix**:
```kotlin
// Add Paging 3
dependencies {
    implementation("androidx.paging:paging-runtime:3.2.1")
    implementation("androidx.paging:paging-compose:3.2.1")
}

// VideoDao
@Query("SELECT * FROM videos ORDER BY timestamp DESC")
fun getAllVideosPaged(): PagingSource<Int, Video>

// VideosViewModel
val videos: Flow<PagingData<Video>> = Pager(
    config = PagingConfig(pageSize = 20, enablePlaceholders = false),
    pagingSourceFactory = { videoRepository.getAllVideosPaged() }
).flow.cachedIn(viewModelScope)

// VideosListScreen
val lazyPagingItems = videos.collectAsLazyPagingItems()
LazyColumn {
    items(lazyPagingItems) { video ->
        VideoItem(video)
    }
}
```

**Priority**: MEDIUM

---

## Low Priority Issues

### 🟢 LOW #13: No Code Documentation

**Severity**: LOW 🟢  
**Category**: Maintainability  
**Location**: Project-wide

**Problem**:
No KDoc comments or documentation.

**How to Fix**:
```kotlin
/**
 * ViewModel for managing user authentication state.
 *
 * Handles user registration, login, and session management.
 * Exposes [authState] as a [StateFlow] for UI observation.
 *
 * @property userRepository Repository for user data operations
 */
class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {
    /**
     * Current authentication state.
     * Emits updates when user logs in, logs out, or encounters errors.
     */
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
}
```

**Priority**: LOW

---

### 🟢 LOW #14: Duplicate Utility Functions

**Severity**: LOW 🟢  
**Category**: Code Quality  
**Location**: Multiple screens

**Problem**:
Functions like `formatDuration()`, `formatFileSize()` are duplicated across files.

**How to Fix**:
```kotlin
// Create utils package
object DateTimeUtils {
    fun formatDuration(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60))
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}

object FileUtils {
    fun formatFileSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> String.format("%.2f MB", mb)
            kb >= 1.0 -> String.format("%.2f KB", kb)
            else -> "$bytes B"
        }
    }
}
```

**Priority**: LOW

---

### 🟢 LOW #15: No Haptic Feedback

**Severity**: LOW 🟢  
**Category**: UX  
**Location**: UI screens

**Problem**:
No haptic feedback for button presses or important actions.

**How to Fix**:
```kotlin
@Composable
fun RecordButton(onClick: () -> Unit) {
    val view = LocalView.current
    
    Button(
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            onClick()
        }
    ) {
        Text("Record")
    }
}
```

**Priority**: LOW

---

## Issues Summary

### By Severity:
- 🔴 **Critical**: 4 issues
- 🟠 **High**: 3 issues
- 🟡 **Medium**: 5 issues
- 🟢 **Low**: 3 issues

**Total**: 15 issues

### By Category:
- **Security**: 3 issues (Critical)
- **Architecture**: 2 issues (High)
- **Permissions**: 1 issue (Critical)
- **Data Persistence**: 2 issues (High/Medium)
- **Performance**: 2 issues (Medium)
- **Testing**: 1 issue (Medium)
- **Storage**: 1 issue (Medium)
- **Build**: 1 issue (Medium)
- **Code Quality**: 2 issues (Low)
- **UX**: 1 issue (Low)

### Priority Action Plan:

**Week 1 (Critical)**:
1. Implement password hashing
2. Fix session management
3. Add storage permissions for Android 13+
4. Add database encryption

**Week 2 (High)**:
5. Implement database migrations
6. Add Hilt for dependency injection
7. Add crash reporting (Firebase Crashlytics)

**Week 3 (Medium)**:
8. Add unit tests
9. Implement storage quota management
10. Add database indices
11. Configure ProGuard/R8
12. Implement video pagination

**Week 4 (Low)**:
13. Add code documentation
14. Extract utility classes
15. Add haptic feedback

**Estimated Total Time**: 4 weeks for full resolution
