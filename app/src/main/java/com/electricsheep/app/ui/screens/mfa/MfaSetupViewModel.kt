package com.electricsheep.app.ui.screens.mfa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.electricsheep.app.auth.MfaError
import com.electricsheep.app.auth.MfaManager
import com.electricsheep.app.util.Logger
import io.github.jan.supabase.gotrue.mfa.MfaResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for MFA setup screen.
 * Manages MFA enrollment flow: QR code display, code verification.
 */
class MfaSetupViewModel(
    private val mfaManager: MfaManager
) : ViewModel() {
    
    // Enrollment state
    private val _enrollmentResponse = MutableStateFlow<MfaResponse?>(null)
    val enrollmentResponse: StateFlow<MfaResponse?> = _enrollmentResponse.asStateFlow()
    
    // Verification code input
    private val _verificationCode = MutableStateFlow("")
    val verificationCode: StateFlow<String> = _verificationCode.asStateFlow()
    
    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _isEnrollmentComplete = MutableStateFlow(false)
    val isEnrollmentComplete: StateFlow<Boolean> = _isEnrollmentComplete.asStateFlow()
    
    /**
     * Start MFA enrollment process.
     * Displays QR code for user to scan with authenticator app.
     */
    fun startEnrollment() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            mfaManager.startEnrollment()
                .onSuccess { response: io.github.jan.supabase.gotrue.mfa.MfaResponse ->
                    Logger.info("MfaSetupViewModel", "MFA enrollment started successfully")
                    _enrollmentResponse.value = response
                }
                .onFailure { error: Throwable ->
                    Logger.error("MfaSetupViewModel", "MFA enrollment failed", error)
                    _errorMessage.value = when (error) {
                        is MfaError.EnrollmentFailed -> "Failed to start MFA setup. Please try again."
                        else -> "An error occurred. Please try again."
                    }
                }
            
            _isLoading.value = false
        }
    }
    
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
     * Verify enrollment with code from authenticator app.
     */
    fun verifyEnrollment() {
        val code = _verificationCode.value
        val enrollmentResponse = _enrollmentResponse.value
        // Note: MfaResponse structure from Supabase SDK may differ
        // Update based on actual SDK response structure
        val challengeId = enrollmentResponse?.challengeId
        
        if (code.length != 6) {
            _errorMessage.value = "Please enter a 6-digit code from your authenticator app."
            return
        }
        
        if (challengeId == null) {
            _errorMessage.value = "Enrollment not started. Please try again."
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            mfaManager.verifyEnrollment(challengeId, code)
                .onSuccess {
                    Logger.info("MfaSetupViewModel", "MFA enrollment verified successfully")
                    _isEnrollmentComplete.value = true
                }
                .onFailure { error ->
                    Logger.error("MfaSetupViewModel", "MFA verification failed", error)
                    _errorMessage.value = when (error) {
                        is MfaError.InvalidCode -> error.message
                        is MfaError.VerificationFailed -> "Invalid code. Please check your authenticator app and try again."
                        else -> "Verification failed. Please try again."
                    }
                    // Clear code on error
                    _verificationCode.value = ""
                }
            
            _isLoading.value = false
        }
    }
}

