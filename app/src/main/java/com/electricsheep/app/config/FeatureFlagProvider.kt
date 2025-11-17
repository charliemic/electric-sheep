package com.electricsheep.app.config

/**
 * Provider interface for feature flags.
 * Allows different implementations (config-based, remote, etc.)
 */
interface FeatureFlagProvider {
    /**
     * Get the value of a boolean feature flag.
     * 
     * @param key Feature flag key
     * @param defaultValue Default value if flag is not found
     * @return Feature flag value
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    
    /**
     * Get the value of a string feature flag.
     * 
     * @param key Feature flag key
     * @param defaultValue Default value if flag is not found
     * @return Feature flag value
     */
    fun getString(key: String, defaultValue: String = ""): String
    
    /**
     * Get the value of an integer feature flag.
     * 
     * @param key Feature flag key
     * @param defaultValue Default value if flag is not found
     * @return Feature flag value
     */
    fun getInt(key: String, defaultValue: Int = 0): Int
    
    /**
     * Check if a feature flag is enabled (convenience method for boolean flags).
     * 
     * @param key Feature flag key
     * @param defaultValue Default value if flag is not found
     * @return True if flag is enabled
     */
    fun isEnabled(key: String, defaultValue: Boolean = false): Boolean {
        return getBoolean(key, defaultValue)
    }
}

