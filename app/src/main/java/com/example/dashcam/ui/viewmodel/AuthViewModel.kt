package com.example.dashcam.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcam.data.entity.User
import com.example.dashcam.data.repository.UserRepository
import com.example.dashcam.util.PasswordHasher
import com.example.dashcam.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUser: User? = null,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            val userId = sessionManager.getLoggedInUserId()
            if (userId != null) {
                val user = userRepository.getUserByIdDirect(userId)
                _authState.value = _authState.value.copy(
                    isLoggedIn = user != null,
                    currentUser = user
                )
            } else {
                _authState.value = _authState.value.copy(
                    isLoggedIn = false,
                    currentUser = null
                )
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        if (!validateRegistrationInput(name, email, password)) {
            return
        }

        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
            try {
                val existingUser = userRepository.getUserByEmail(email)
                if (existingUser != null) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = "Email déjà utilisé"
                    )
                    return@launch
                }

                val hashedPassword = PasswordHasher.hashPassword(password)
                val user = User(name = name, email = email, password = hashedPassword)
                val userId = userRepository.insert(user)
                val newUser = user.copy(id = userId)
                
                sessionManager.saveSession(userId)
                
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    currentUser = newUser,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = "Erreur lors de l'inscription: ${e.message}"
                )
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = _authState.value.copy(
                errorMessage = "Veuillez remplir tous les champs"
            )
            return
        }

        if (sessionManager.isAccountLocked(email)) {
            val remainingMinutes = (sessionManager.getRemainingLockoutTime(email) / 60000).toInt() + 1
            _authState.value = _authState.value.copy(
                errorMessage = "Compte verrouillé. Réessayez dans $remainingMinutes minute(s)"
            )
            return
        }

        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
            try {
                val user = userRepository.getUserByEmail(email)
                if (user != null && PasswordHasher.verifyPassword(password, user.password)) {
                    sessionManager.clearFailedLoginAttempts(email)
                    sessionManager.saveSession(user.id)
                    
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        currentUser = user,
                        errorMessage = null
                    )
                } else {
                    sessionManager.recordFailedLoginAttempt(email)
                    val attempts = sessionManager.getFailedLoginAttempts(email)
                    val remainingAttempts = 5 - attempts
                    
                    val errorMsg = if (remainingAttempts > 0) {
                        "Email ou mot de passe incorrect ($remainingAttempts tentative(s) restante(s))"
                    } else {
                        "Compte verrouillé pendant 15 minutes"
                    }
                    
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = errorMsg
                    )
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = "Erreur lors de la connexion: ${e.message}"
                )
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
        _authState.value = AuthState(isLoggedIn = false, currentUser = null)
    }

    fun clearError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }

    private fun validateRegistrationInput(name: String, email: String, password: String): Boolean {
        when {
            name.isBlank() -> {
                _authState.value = _authState.value.copy(errorMessage = "Le nom est requis")
                return false
            }
            email.isBlank() -> {
                _authState.value = _authState.value.copy(errorMessage = "L'email est requis")
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _authState.value = _authState.value.copy(errorMessage = "Email invalide")
                return false
            }
            password.length < 8 -> {
                _authState.value = _authState.value.copy(
                    errorMessage = "Le mot de passe doit contenir au moins 8 caractères"
                )
                return false
            }
            !password.any { it.isUpperCase() } -> {
                _authState.value = _authState.value.copy(
                    errorMessage = "Le mot de passe doit contenir au moins une majuscule"
                )
                return false
            }
            !password.any { it.isLowerCase() } -> {
                _authState.value = _authState.value.copy(
                    errorMessage = "Le mot de passe doit contenir au moins une minuscule"
                )
                return false
            }
            !password.any { it.isDigit() } -> {
                _authState.value = _authState.value.copy(
                    errorMessage = "Le mot de passe doit contenir au moins un chiffre"
                )
                return false
            }
            !password.any { !it.isLetterOrDigit() } -> {
                _authState.value = _authState.value.copy(
                    errorMessage = "Le mot de passe doit contenir au moins un caractère spécial"
                )
                return false
            }
        }
        return true
    }
}
