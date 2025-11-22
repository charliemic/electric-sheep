package com.electricsheep.app.ui.screens.mfa

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.electricsheep.app.auth.SupabaseAuthProvider
import com.electricsheep.app.auth.User
import com.electricsheep.app.ui.components.AccessibleButton
import com.electricsheep.app.ui.components.AccessibleScreen
import com.electricsheep.app.ui.components.AccessibleTextField
import com.electricsheep.app.ui.components.LoadingIndicator
import com.electricsheep.app.ui.theme.Spacing

/**
 * MFA verification screen for completing sign-in with MFA.
 * 
 * Displayed after user enters password and MFA is required.
 * User enters TOTP code from authenticator app to complete sign-in.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MfaVerifyScreen(
    authProvider: SupabaseAuthProvider,
    challengeId: String,
    userId: String,
    onVerificationComplete: (User) -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel: MfaVerifyViewModel = viewModel(
        factory = MfaVerifyViewModelFactory(authProvider, challengeId, userId)
    )
    
    val verificationCode by viewModel.verificationCode.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val verificationResult by viewModel.verificationResult.collectAsState()
    
    // Navigate when verification succeeds
    LaunchedEffect(verificationResult) {
        verificationResult?.onSuccess { user ->
            onVerificationComplete(user)
        }
    }
    
    AccessibleScreen(
        title = "Verify Your Identity",
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Spacing.md)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Instructions
            Text(
                text = "Enter verification code",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Screen title: Enter verification code"
                    }
            )
            
            Text(
                text = "Open your authenticator app and enter the 6-digit code",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Verification Code Input
            AccessibleTextField(
                value = verificationCode,
                onValueChange = { viewModel.updateVerificationCode(it) },
                label = "Verification Code",
                placeholder = "000000",
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                modifier = Modifier.fillMaxWidth(),
                contentDescriptionParam = "Enter 6-digit code from authenticator app",
                isError = errorMessage != null,
                errorMessage = errorMessage,
                supportingText = "Enter the 6-digit code from your authenticator app",
                imeAction = androidx.compose.ui.text.input.ImeAction.Done
            )
            
            AccessibleButton(
                text = "Verify and Sign In",
                onClick = { viewModel.verifyCode() },
                modifier = Modifier.fillMaxWidth(),
                enabled = verificationCode.length == 6,
                isLoading = isLoading,
                loadingDescription = "Verifying code"
            )
            
            // Error Message
            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            liveRegion = LiveRegionMode.Polite
                            contentDescription = "Error: $error"
                        }
                )
            }
            
            // Loading Indicator
            if (isLoading) {
                LoadingIndicator(message = "Verifying code...")
            }
        }
    }
}

/**
 * Factory for creating MfaVerifyViewModel with dependency injection.
 */
class MfaVerifyViewModelFactory(
    private val authProvider: SupabaseAuthProvider,
    private val challengeId: String,
    private val userId: String
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MfaVerifyViewModel::class.java)) {
            return MfaVerifyViewModel(authProvider, challengeId, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

