package com.electricsheep.app.ui.screens.mood

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.error
import com.electricsheep.app.config.MoodConfig
import com.electricsheep.app.data.model.Mood
import com.electricsheep.app.util.DateFormatter
import com.electricsheep.app.util.Logger

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MoodManagementScreen(
    viewModel: MoodManagementViewModel,
    onNavigateBack: () -> Unit
) {
    Logger.info("MoodManagementScreen", "Mood Management screen displayed")
    
    // Observe ViewModel state
    val currentUser by viewModel.currentUser.collectAsState()
    val emailText by viewModel.emailText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val moodScoreText by viewModel.moodScoreText.collectAsState()
    val isSavingMood by viewModel.isSavingMood.collectAsState()
    val moodErrorMessage by viewModel.moodErrorMessage.collectAsState()
    val moods by viewModel.moods.collectAsState()
    
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mood Management") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            Logger.debug("MoodManagementScreen", "User tapped back button")
                            onNavigateBack()
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "Navigate back to main screen"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null // Handled by parent IconButton
                        )
                    }
                },
                actions = {
                    // Show logout button if user is authenticated
                    if (currentUser != null) {
                        TextButton(
                            onClick = {
                                Logger.info("MoodManagementScreen", "User tapped logout")
                                viewModel.signOut()
                            },
                            modifier = Modifier.semantics {
                                contentDescription = "Sign out from current account"
                            }
                        ) {
                            Text("Logout")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        // Use LazyColumn instead of Column with verticalScroll to avoid nested scrolling issues
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding() // Add padding for IME (keyboard) and text selection toolbar
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Show login UI if not authenticated
            if (currentUser == null) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Sign In",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Enter an email address to sign in (development only)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Email input
                        OutlinedTextField(
                            value = emailText,
                            onValueChange = { viewModel.updateEmailText(it) },
                            label = { Text("Email") },
                            placeholder = { Text("user@example.com") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = "Email address input field for signing in"
                                },
                            enabled = !isLoading,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )
                        
                        // Error message
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        // Sign in button
                        Button(
                            onClick = { viewModel.signIn() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = if (isLoading) {
                                        "Signing in, please wait"
                                    } else {
                                        "Sign in with email address"
                                    }
                                    if (isLoading) {
                                        stateDescription = "Loading"
                                    }
                                },
                            enabled = !isLoading && emailText.isNotBlank()
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .semantics { contentDescription = "Signing in progress" },
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Sign In")
                            }
                        }
                    }
                }
            } else {
                // Show app content when authenticated
                item {
                    Text(
                        text = "Mood Management",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Mood input section - moved to top for better UX and to avoid toolbar overlap
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "How are you feeling?",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = "Rate your mood from ${MoodConfig.MIN_SCORE} to ${MoodConfig.MAX_SCORE}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            OutlinedTextField(
                                value = moodScoreText,
                                onValueChange = { viewModel.updateMoodScoreText(it) },
                                label = { Text("Mood Score") },
                                placeholder = { Text("${MoodConfig.MIN_SCORE}-${MoodConfig.MAX_SCORE}") },
                                modifier = Modifier
                                    .fillMaxWidth(0.8f) // Make it narrower and centered
                                    .semantics {
                                        contentDescription = "Mood score input field. Enter a number between ${MoodConfig.MIN_SCORE} and ${MoodConfig.MAX_SCORE}"
                                        if (moodErrorMessage != null) {
                                            error(moodErrorMessage!!)
                                        }
                                    },
                                enabled = !isSavingMood,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                supportingText = {
                                    if (moodErrorMessage == null) {
                                        Text("Enter a number between ${MoodConfig.MIN_SCORE} and ${MoodConfig.MAX_SCORE}")
                                    } else {
                                        Text(moodErrorMessage!!)
                                    }
                                },
                                isError = moodErrorMessage != null
                            )
                            
                            Button(
                                onClick = {
                                    viewModel.saveMood()
                                    // Hide keyboard after save is triggered
                                    keyboardController?.hide()
                                },
                                modifier = Modifier
                                    .fillMaxWidth(0.8f) // Match text field width
                                    .semantics {
                                        contentDescription = if (isSavingMood) {
                                            "Saving mood entry, please wait"
                                        } else {
                                            "Save mood entry"
                                        }
                                        if (isSavingMood) {
                                            stateDescription = "Saving"
                                        }
                                    },
                                enabled = !isSavingMood && moodScoreText.isNotBlank()
                            ) {
                                if (isSavingMood) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .semantics { contentDescription = "Saving progress" },
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text("Save Mood")
                                }
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
                
                // User info section - moved below input for better flow
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Signed in as:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = currentUser!!.email,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            currentUser!!.displayName?.let { displayName ->
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Mood history section
                item {
                    Text(
                        text = "Mood History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (moods.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No mood entries yet. Add your first mood above!",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Use items() for the mood list directly in LazyColumn
                    items(moods) { mood ->
                        MoodEntryRow(mood = mood)
                    }
                }
                
                // Add extra spacing at bottom to account for keyboard and toolbar
                item {
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }
    }
}
