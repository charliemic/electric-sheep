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
import com.electricsheep.app.ui.components.AccessibleButton
import com.electricsheep.app.ui.components.AccessibleScreen
import com.electricsheep.app.ui.components.AccessibleTextField
import com.electricsheep.app.ui.components.LoadingIndicator
import com.electricsheep.app.ui.theme.Spacing
import com.electricsheep.app.auth.MfaManager
import com.electricsheep.app.ElectricSheepApplication

/**
 * MFA setup screen for enrolling in multi-factor authentication.
 * 
 * Flow:
 * 1. User starts enrollment → QR code displayed
 * 2. User scans QR code with authenticator app
 * 3. User enters code from app → Verification
 * 4. Enrollment complete
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MfaSetupScreen(
    mfaManager: MfaManager,
    onEnrollmentComplete: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel: MfaSetupViewModel = viewModel(
        factory = MfaSetupViewModelFactory(mfaManager)
    )
    
    val enrollmentResponse by viewModel.enrollmentResponse.collectAsState()
    val verificationCode by viewModel.verificationCode.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isEnrollmentComplete by viewModel.isEnrollmentComplete.collectAsState()
    
    // Start enrollment when screen loads
    LaunchedEffect(Unit) {
        if (enrollmentResponse == null && !isEnrollmentComplete) {
            viewModel.startEnrollment()
        }
    }
    
    // Navigate back when enrollment complete
    LaunchedEffect(isEnrollmentComplete) {
        if (isEnrollmentComplete) {
            onEnrollmentComplete()
        }
    }
    
    AccessibleScreen(
        title = "Set Up Two-Factor Authentication",
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
                text = "Protect your account with two-factor authentication",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Screen title: Protect your account with two-factor authentication"
                    }
            )
            
            Text(
                text = "Scan the QR code with your authenticator app (Google Authenticator, Authy, etc.)",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            
            // QR Code Display
            if (enrollmentResponse != null && enrollmentResponse?.qrCode != null) {
                Card(
                    modifier = Modifier
                        .size(256.dp)
                        .semantics {
                            contentDescription = "QR code for two-factor authentication setup. Scan with your authenticator app."
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // TODO: Display QR code image
                        // For now, show placeholder
                        // Need to add QR code library (e.g., zxing-android-embedded)
                        Text(
                            text = "QR Code\n(Image to be displayed)",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Manual entry option
                Text(
                    text = "Or enter this code manually:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Secret key (for manual entry)
                enrollmentResponse?.secret?.let { secret ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = secret,
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier
                                .padding(Spacing.md)
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = "Secret key for manual entry: $secret"
                                }
                        )
                    }
                }
            }
            
            // Verification Code Input
            if (enrollmentResponse != null) {
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
                    supportingText = "Enter the 6-digit code from your authenticator app"
                )
                
                AccessibleButton(
                    text = "Verify and Enable",
                    onClick = { viewModel.verifyEnrollment() },
                    modifier = Modifier.fillMaxWidth(),
                    isLoading = isLoading,
                    loadingDescription = "Verifying code"
                )
            }
            
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
            if (isLoading && enrollmentResponse == null) {
                LoadingIndicator(message = "Setting up two-factor authentication...")
            }
        }
    }
}

/**
 * Factory for creating MfaSetupViewModel with dependency injection.
 */
class MfaSetupViewModelFactory(
    private val mfaManager: MfaManager
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MfaSetupViewModel::class.java)) {
            return MfaSetupViewModel(mfaManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

