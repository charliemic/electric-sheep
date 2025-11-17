package com.electricsheep.app.ui.screens.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.electricsheep.app.auth.UserManager
import com.electricsheep.app.data.repository.MoodRepository

/**
 * Factory for creating MoodManagementViewModel with dependency injection.
 */
class MoodManagementViewModelFactory(
    private val userManager: UserManager,
    private val moodRepository: MoodRepository?
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MoodManagementViewModel::class.java)) {
            return MoodManagementViewModel(userManager, moodRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

