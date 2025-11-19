package com.electricsheep.app.config

import android.content.Context
import android.content.SharedPreferences
import com.electricsheep.app.BuildConfig
import com.electricsheep.app.util.Logger

/**
 * Manages runtime environment selection (staging vs production).
 * Only available in debug builds.
 *
 * Stores the selected environment in SharedPreferences so it persists across app restarts.
 */
enum class Environment {
    PRODUCTION,
    STAGING
}

class EnvironmentManager(
    private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "environment_prefs"
        private const val KEY_SELECTED_ENVIRONMENT = "selected_environment"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Get the currently selected environment.
     * In debug builds, returns the stored preference or defaults to staging if USE_STAGING_SUPABASE is true.
     * In release builds, always returns PRODUCTION.
     */
    fun getSelectedEnvironment(): Environment {
        if (!BuildConfig.DEBUG) {
            return Environment.PRODUCTION
        }

        // Check if staging is available (configured in BuildConfig)
        val stagingAvailable = BuildConfig.USE_STAGING_SUPABASE &&
                               BuildConfig.SUPABASE_STAGING_URL.isNotEmpty() &&
                               BuildConfig.SUPABASE_STAGING_URL != "\"\""

        if (!stagingAvailable) {
            return Environment.PRODUCTION
        }

        // Read from preferences, default to staging if USE_STAGING_SUPABASE is true
        val storedValue = prefs.getString(KEY_SELECTED_ENVIRONMENT, null)
        return when {
            storedValue == Environment.STAGING.name -> Environment.STAGING
            storedValue == Environment.PRODUCTION.name -> Environment.PRODUCTION
            BuildConfig.USE_STAGING_SUPABASE -> Environment.STAGING // Default to staging if flag is set
            else -> Environment.PRODUCTION
        }
    }

    /**
     * Set the selected environment.
     * Only works in debug builds.
     *
     * @return true if the environment was set, false if not in debug build or staging not available
     */
    fun setSelectedEnvironment(environment: Environment): Boolean {
        if (!BuildConfig.DEBUG) {
            Logger.warn("EnvironmentManager", "Cannot change environment in release builds")
            return false
        }

        // Check if staging is available
        val stagingAvailable = BuildConfig.USE_STAGING_SUPABASE &&
                               BuildConfig.SUPABASE_STAGING_URL.isNotEmpty() &&
                               BuildConfig.SUPABASE_STAGING_URL != "\"\""

        if (environment == Environment.STAGING && !stagingAvailable) {
            Logger.warn("EnvironmentManager", "Staging environment not available")
            return false
        }

        prefs.edit()
            .putString(KEY_SELECTED_ENVIRONMENT, environment.name)
            .apply()

        Logger.info("EnvironmentManager", "Environment set to: ${environment.name}")
        return true
    }

    /**
     * Check if environment switching is available (debug build with staging configured).
     */
    fun isEnvironmentSwitchingAvailable(): Boolean {
        if (!BuildConfig.DEBUG) return false

        return BuildConfig.USE_STAGING_SUPABASE &&
               BuildConfig.SUPABASE_STAGING_URL.isNotEmpty() &&
               BuildConfig.SUPABASE_STAGING_URL != "\"\""
    }

    /**
     * Get the current environment as a display string.
     */
    fun getEnvironmentDisplayName(): String {
        return when (getSelectedEnvironment()) {
            Environment.STAGING -> "STAGING"
            Environment.PRODUCTION -> "PRODUCTION"
        }
    }
}
