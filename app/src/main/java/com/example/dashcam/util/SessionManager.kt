package com.example.dashcam.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun saveSession(userId: Long) {
        sharedPreferences.edit().apply {
            putLong(KEY_USER_ID, userId)
            putBoolean(KEY_IS_LOGGED_IN, true)
            putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
            apply()
        }
    }
    
    fun getLoggedInUserId(): Long? {
        val userId = sharedPreferences.getLong(KEY_USER_ID, -1L)
        val isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        return if (userId != -1L && isLoggedIn) userId else null
    }
    
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false) && 
               sharedPreferences.getLong(KEY_USER_ID, -1L) != -1L
    }
    
    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
    
    fun getLoginTime(): Long {
        return sharedPreferences.getLong(KEY_LOGIN_TIME, 0L)
    }
    
    fun isSessionExpired(timeoutMillis: Long = SESSION_TIMEOUT): Boolean {
        val loginTime = getLoginTime()
        if (loginTime == 0L) return true
        return (System.currentTimeMillis() - loginTime) > timeoutMillis
    }
    
    fun recordFailedLoginAttempt(email: String) {
        val attempts = getFailedLoginAttempts(email)
        sharedPreferences.edit().apply {
            putInt("failed_attempts_$email", attempts + 1)
            putLong("lockout_time_$email", System.currentTimeMillis())
            apply()
        }
    }
    
    fun getFailedLoginAttempts(email: String): Int {
        return sharedPreferences.getInt("failed_attempts_$email", 0)
    }
    
    fun clearFailedLoginAttempts(email: String) {
        sharedPreferences.edit().apply {
            remove("failed_attempts_$email")
            remove("lockout_time_$email")
            apply()
        }
    }
    
    fun isAccountLocked(email: String): Boolean {
        val attempts = getFailedLoginAttempts(email)
        if (attempts < MAX_LOGIN_ATTEMPTS) return false
        
        val lockoutTime = sharedPreferences.getLong("lockout_time_$email", 0L)
        val timeSinceLockout = System.currentTimeMillis() - lockoutTime
        
        if (timeSinceLockout > LOCKOUT_DURATION) {
            clearFailedLoginAttempts(email)
            return false
        }
        
        return true
    }
    
    fun getRemainingLockoutTime(email: String): Long {
        val lockoutTime = sharedPreferences.getLong("lockout_time_$email", 0L)
        val elapsed = System.currentTimeMillis() - lockoutTime
        val remaining = LOCKOUT_DURATION - elapsed
        return if (remaining > 0) remaining else 0
    }
    
    companion object {
        private const val PREFS_NAME = "dashcam_secure_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LOGIN_TIME = "login_time"
        private const val SESSION_TIMEOUT = 7L * 24L * 60L * 60L * 1000L
        private const val MAX_LOGIN_ATTEMPTS = 5
        private const val LOCKOUT_DURATION = 15L * 60L * 1000L
        
        @Volatile
        private var instance: SessionManager? = null
        
        fun getInstance(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
