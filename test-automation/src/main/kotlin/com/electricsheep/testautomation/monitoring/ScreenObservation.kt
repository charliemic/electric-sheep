package com.electricsheep.testautomation.monitoring

import java.io.File

/**
 * Represents an observation about the screen state.
 * These are user-reportable issues or notable states.
 */
data class ScreenObservation(
    val type: ObservationType,
    val severity: Severity,
    val message: String,
    val element: String? = null, // Element description if applicable
    val screenshot: File? = null
) {
    enum class ObservationType {
        ERROR,              // Error message visible
        WARNING,            // Warning message visible
        BLOCKING_ELEMENT,   // Dialog, popup, or overlay blocking interaction
        UNEXPECTED_STATE,   // Screen doesn't match expected state
        LOADING_INDICATOR,  // Loading indicator present (may be expected or unexpected)
        SUCCESS_INDICATOR,  // Success message or confirmation visible
        MISSING_ELEMENT,    // Expected element not found
        ACCESSIBILITY_ISSUE // Accessibility problem detected
    }
    
    enum class Severity {
        CRITICAL,   // Blocks user from proceeding
        HIGH,       // Significant issue, user would likely report
        MEDIUM,     // Noticeable issue, may affect UX
        LOW,        // Minor issue or informational
        POSITIVE    // Good state (success indicators)
    }
}

/**
 * Collection of observations about a screen state.
 */
data class ScreenEvaluation(
    val observations: List<ScreenObservation>,
    val overallState: EvaluationState,
    val summary: String,
    val hasKeyboard: Boolean = false, // VISUALLY detected: keyboard is visible on screen
    val blockingElements: List<String> = emptyList() // VISUALLY detected: elements blocking interaction
) {
    enum class EvaluationState {
        PASS,           // Screen matches expectations, no blocking issues
        PASS_WITH_ISSUES, // Task completed but with notable issues
        FAIL,           // Blocking issues prevent task completion
        UNCERTAIN      // Cannot determine state reliably
    }
    
    /**
     * Get observations by severity.
     */
    fun getObservationsBySeverity(severity: ScreenObservation.Severity): List<ScreenObservation> {
        return observations.filter { it.severity == severity }
    }
    
    /**
     * Get critical blocking observations.
     */
    fun getBlockingIssues(): List<ScreenObservation> {
        return observations.filter { 
            it.severity == ScreenObservation.Severity.CRITICAL ||
            it.type == ScreenObservation.ObservationType.BLOCKING_ELEMENT ||
            it.type == ScreenObservation.ObservationType.ERROR
        }
    }
    
    /**
     * Check if screen has blocking issues.
     */
    fun hasBlockingIssues(): Boolean {
        return getBlockingIssues().isNotEmpty()
    }
}



