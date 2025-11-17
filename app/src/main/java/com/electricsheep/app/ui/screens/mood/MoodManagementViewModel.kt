package com.electricsheep.app.ui.screens.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.electricsheep.app.auth.User
import com.electricsheep.app.auth.UserManager
import com.electricsheep.app.config.MoodConfig
import com.electricsheep.app.data.model.Mood
import com.electricsheep.app.data.repository.MoodRepository
import com.electricsheep.app.util.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for Mood Management screen.
 * Manages authentication state, mood input, and mood history.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MoodManagementViewModel(
    private val userManager: UserManager,
    private val moodRepository: MoodRepository?
) : ViewModel() {
    
    // Authentication state
    val currentUser: StateFlow<User?> = userManager.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    
    // Login UI state
    private val _emailText = MutableStateFlow("")
    val emailText: StateFlow<String> = _emailText.asStateFlow()
    
    private val _passwordText = MutableStateFlow("")
    val passwordText: StateFlow<String> = _passwordText.asStateFlow()
    
    private val _isSignUpMode = MutableStateFlow(false)
    val isSignUpMode: StateFlow<Boolean> = _isSignUpMode.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _googleOAuthUrl = MutableStateFlow<String?>(null)
    val googleOAuthUrl: StateFlow<String?> = _googleOAuthUrl.asStateFlow()
    
    // Mood input state
    private val _moodScoreText = MutableStateFlow("")
    val moodScoreText: StateFlow<String> = _moodScoreText.asStateFlow()
    
    private val _isSavingMood = MutableStateFlow(false)
    val isSavingMood: StateFlow<Boolean> = _isSavingMood.asStateFlow()
    
    private val _moodErrorMessage = MutableStateFlow<String?>(null)
    val moodErrorMessage: StateFlow<String?> = _moodErrorMessage.asStateFlow()
    
    // Mood history - observe from repository when authenticated
    val moods: StateFlow<List<Mood>> = currentUser
        .flatMapLatest { user ->
            if (user != null && moodRepository != null) {
                moodRepository.getAllMoods()
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    /**
     * Update email input text
     */
    fun updateEmailText(text: String) {
        _emailText.value = text
        _errorMessage.value = null
    }
    
    /**
     * Update password input text
     */
    fun updatePasswordText(text: String) {
        _passwordText.value = text
        _errorMessage.value = null
    }
    
    /**
     * Toggle between sign in and sign up mode
     */
    fun toggleSignUpMode() {
        _isSignUpMode.value = !_isSignUpMode.value
        _errorMessage.value = null
    }
    
    /**
     * Sign in with email and password
     */
    fun signIn() {
        val email = _emailText.value.trim()
        val password = _passwordText.value
        
        if (email.isBlank()) {
            _errorMessage.value = "Please enter an email address"
            return
        }
        
        if (password.isBlank()) {
            _errorMessage.value = "Please enter a password"
            return
        }
        
        _isLoading.value = true
        _errorMessage.value = null
        Logger.info("MoodManagementViewModel", "Sign in attempt for: $email")
        
        viewModelScope.launch {
            userManager.signIn(email, password)
                .onSuccess {
                    Logger.info("MoodManagementViewModel", "Sign in successful: ${it.id}")
                    _isLoading.value = false
                    _emailText.value = ""
                    _passwordText.value = ""
                }
                .onFailure { error ->
                    Logger.error("MoodManagementViewModel", "Sign in failed", error)
                    _errorMessage.value = "Sign in failed: ${error.message}"
                    _isLoading.value = false
                }
        }
    }
    
    /**
     * Sign up with email and password
     */
    fun signUp() {
        val email = _emailText.value.trim()
        val password = _passwordText.value
        
        if (email.isBlank()) {
            _errorMessage.value = "Please enter an email address"
            return
        }
        
        if (password.isBlank()) {
            _errorMessage.value = "Please enter a password"
            return
        }
        
        if (password.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters"
            return
        }
        
        _isLoading.value = true
        _errorMessage.value = null
        Logger.info("MoodManagementViewModel", "Sign up attempt for: $email")
        
        viewModelScope.launch {
            userManager.signUp(email, password)
                .onSuccess {
                    Logger.info("MoodManagementViewModel", "Sign up successful: ${it.id}")
                    _isLoading.value = false
                    _emailText.value = ""
                    _passwordText.value = ""
                    _isSignUpMode.value = false
                }
                .onFailure { error ->
                    Logger.error("MoodManagementViewModel", "Sign up failed", error)
                    _errorMessage.value = "Sign up failed: ${error.message}"
                    _isLoading.value = false
                }
        }
    }
    
    /**
     * Initiate Google OAuth sign-in
     * Returns the OAuth URL to open in browser
     */
    fun signInWithGoogle() {
        _isLoading.value = true
        _errorMessage.value = null
        Logger.info("MoodManagementViewModel", "Initiating Google OAuth sign-in")
        
        viewModelScope.launch {
            userManager.getGoogleOAuthUrl()
                .onSuccess { url ->
                    Logger.info("MoodManagementViewModel", "Google OAuth URL obtained")
                    _googleOAuthUrl.value = url
                    _isLoading.value = false
                }
                .onFailure { error ->
                    Logger.error("MoodManagementViewModel", "Failed to get Google OAuth URL", error)
                    _errorMessage.value = "Failed to start Google sign-in: ${error.message}"
                    _isLoading.value = false
                }
        }
    }
    
    /**
     * Sign out current user
     */
    fun signOut() {
        Logger.info("MoodManagementViewModel", "User tapped logout")
        viewModelScope.launch {
            userManager.signOut()
            _errorMessage.value = null
        }
    }
    
    /**
     * Update mood score input text
     */
    fun updateMoodScoreText(text: String) {
        // Only allow numeric input
        if (text.isEmpty() || text.all { char -> char.isDigit() }) {
            _moodScoreText.value = text
            _moodErrorMessage.value = null
        }
    }
    
    /**
     * Save mood entry
     */
    fun saveMood() {
        val scoreText = _moodScoreText.value.trim()
        val score = scoreText.toIntOrNull()
        
        if (score == null) {
            _moodErrorMessage.value = "Please enter a valid number"
            return
        }
        
        if (!MoodConfig.isValidScore(score)) {
            _moodErrorMessage.value = "Score must be between ${MoodConfig.MIN_SCORE} and ${MoodConfig.MAX_SCORE}"
            return
        }
        
        _isSavingMood.value = true
        _moodErrorMessage.value = null
        Logger.info("MoodManagementViewModel", "Saving mood: score=$score")
        
        viewModelScope.launch {
            try {
                // Defensive check: verify user is still authenticated
                val userId = userManager.getUserIdOrNull()
                if (userId == null) {
                    Logger.error("MoodManagementViewModel", "User not authenticated when saving mood")
                    _moodErrorMessage.value = "Please sign in to save mood entries"
                    _isSavingMood.value = false
                    return@launch
                }
                
                if (moodRepository == null) {
                    Logger.error("MoodManagementViewModel", "Mood repository not available")
                    _moodErrorMessage.value = "Mood repository not available"
                    _isSavingMood.value = false
                    return@launch
                }
                
                val mood = Mood(
                    id = "", // Will be generated by repository
                    userId = userId,
                    score = score,
                    timestamp = System.currentTimeMillis()
                )
                
                moodRepository.saveMood(mood)
                    .onSuccess {
                        Logger.info("MoodManagementViewModel", "Mood saved successfully: ${it.id}")
                        _moodScoreText.value = ""
                        _isSavingMood.value = false
                    }
                    .onFailure { error ->
                        Logger.error("MoodManagementViewModel", "Failed to save mood", error)
                        _moodErrorMessage.value = "Failed to save mood: ${error.message ?: "Unknown error"}"
                        _isSavingMood.value = false
                    }
            } catch (e: IllegalStateException) {
                Logger.error("MoodManagementViewModel", "User authentication error when saving mood", e)
                _moodErrorMessage.value = "Please sign in to save mood entries"
                _isSavingMood.value = false
            } catch (e: Exception) {
                Logger.error("MoodManagementViewModel", "Unexpected error saving mood", e)
                _moodErrorMessage.value = "Error: ${e.message ?: "Unknown error"}"
                _isSavingMood.value = false
            }
        }
    }
    
    /**
     * Clear mood error message
     */
    fun clearMoodError() {
        _moodErrorMessage.value = null
    }
    
    /**
     * Clear login error message
     */
    fun clearLoginError() {
        _errorMessage.value = null
    }
}

