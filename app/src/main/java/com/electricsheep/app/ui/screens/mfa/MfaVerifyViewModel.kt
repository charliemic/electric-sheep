package com.electricsheep.app.ui.screens.mfa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.electricsheep.app.auth.AuthError
import com.electricsheep.app.auth.SupabaseAuthProvider
import com.electricsheep.app.auth.User
import com.electricsheep.app.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for MFA verification screen (during login).
 * Manages MFA code input and verification during sign-in.
 */
class MfaVerifyViewModel(
    private val authProvider: SupabaseAuthProvider,
    private val challengeId: String,
    private val userId: String
) : ViewModel() {
    
    // Verification code input
    private val _verificationCode = MutableStateFlow("")
    val verificationCode: StateFlow<String> = _verificationCode.asStateFlow()
    
    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _verificationResult = MutableStateFlow<Result<User>?>(null)
    val verificationResult: StateFlow<Result<User>?> = _verificationResult.asStateFlow()
    
    /**
     * Update verification code input.
     */
    fun updateVerificationCode(code: String) {
        // Only allow 6 digits
        val filtered = code.filter { it.isDigit() }.take(6)
        _verificationCode.value = filtered
        _errorMessage.value = null
    }
    
    /**
     * Verify MFA code and complete sign-in.
     */
    fun verifyCode() {
        val code = _verificationCode.value
        
        if (code.length != 6) {
            _errorMessage.value = "Please enter a 6-digit code from your authenticator app."
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            authProvider.verifyMfaSignIn(challengeId, code)
                .onSuccess { user ->
                    Logger.info("MfaVerifyViewModel", "MFA verification successful, user signed in: ${user.id}")
                    _verificationResult.value = Result.success(user)
                }
                .onFailure { error ->
                    Logger.error("MfaVerifyViewModel", "MFA verification failed", error)
                    _errorMessage.value = when (error) {
                        is AuthError.Generic -> error.message
                        else -> "Invalid code. Please check your authenticator app and try again."
                    }
                    // Clear code on error
                    _verificationCode.value = ""
                }
            
            _isLoading.value = false
        }
    }
}

