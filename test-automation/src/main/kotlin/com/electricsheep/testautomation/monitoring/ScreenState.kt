package com.electricsheep.testautomation.monitoring

import java.io.File

/**
 * Represents the current state of the screen as observed by the Screen Monitor.
 */
data class ScreenState(
    val screenName: String? = null, // e.g., "Landing Screen", "Mood Management"
    val isLoading: Boolean = false,
    val hasErrors: Boolean = false,
    val errorMessages: List<String> = emptyList(),
    val visibleElements: List<String> = emptyList(), // Key visible elements
    val screenshot: File? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val testStartTime: Long? = null, // Test start time for relative timestamps
    val currentAction: String? = null, // Current action being executed (if known)
    val currentIntent: String? = null, // Current intent/goal (if known)
    val hasKeyboard: Boolean = false, // VISUALLY detected: keyboard is visible on screen
    val blockingElements: List<String> = emptyList() // VISUALLY detected: elements blocking interaction (keyboard, dialogs, etc.)
) {
    /**
     * Check if this state represents the same screen as another state.
     */
    fun isSameScreen(other: ScreenState?): Boolean {
        if (other == null) return false
        return screenName == other.screenName
    }
    
    /**
     * Check if state has changed significantly from another state.
     */
    fun hasChangedFrom(other: ScreenState?): Boolean {
        if (other == null) return true
        return screenName != other.screenName ||
               isLoading != other.isLoading ||
               hasErrors != other.hasErrors ||
               errorMessages != other.errorMessages
    }
    
    /**
     * Get relative timestamp from test start (in milliseconds).
     */
    fun getRelativeTimestampMs(): Long? {
        return testStartTime?.let { timestamp - it }
    }
    
    /**
     * Get formatted timestamp string.
     */
    fun getFormattedTimestamp(): String {
        val relative = getRelativeTimestampMs()
        return if (relative != null) {
            "${relative}ms"
        } else {
            java.time.Instant.ofEpochMilli(timestamp).toString()
        }
    }
}

