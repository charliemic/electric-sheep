package com.electricsheep.testautomation.actions

/**
 * Represents a human-like action that can be performed on the app.
 * These actions abstract away Appium internals and correlate to what a human would do.
 */
sealed class HumanAction {
    /**
     * Tap on an element identified by its accessibility label or text.
     * This is what a human would do - "tap the sign in button"
     */
    data class Tap(
        val target: String, // Human-readable description: "Sign in button", "Email field"
        val accessibilityId: String? = null, // Preferred: accessibility ID
        val text: String? = null, // Fallback: text content
        val description: String? = null // What this action does (for logging)
    ) : HumanAction()
    
    /**
     * Type text into a field.
     * Human task: "Enter email address"
     */
    data class TypeText(
        val target: String, // "Email field", "Password field"
        val text: String,
        val accessibilityId: String? = null,
        val clearFirst: Boolean = true // Human would clear field first
    ) : HumanAction()
    
    /**
     * Swipe in a direction.
     * Human task: "Scroll down", "Swipe left"
     */
    data class Swipe(
        val direction: SwipeDirection,
        val description: String? = null
    ) : HumanAction()
    
    /**
     * Wait for a condition to be met.
     * Human task: "Wait for the screen to load", "Wait for button to appear"
     */
    data class WaitFor(
        val condition: WaitCondition,
        val timeoutSeconds: Int = 10,
        val description: String? = null
    ) : HumanAction()
    
    /**
     * Navigate back.
     * Human task: "Go back"
     */
    object NavigateBack : HumanAction()
    
    /**
     * Take a screenshot for analysis.
     * Used by the planner to understand current state.
     */
    object CaptureState : HumanAction()
    
    /**
     * Verify a condition is met.
     * Human task: "Check if I'm signed in", "Verify mood was saved"
     */
    data class Verify(
        val condition: VerifyCondition,
        val description: String? = null
    ) : HumanAction()
}

enum class SwipeDirection {
    UP, DOWN, LEFT, RIGHT
}

sealed class WaitCondition {
    data class ElementVisible(val target: String, val accessibilityId: String? = null) : WaitCondition()
    data class ElementEnabled(val target: String, val accessibilityId: String? = null) : WaitCondition()
    data class ScreenChanged(val expectedScreen: String) : WaitCondition()
    data class LoadingComplete(val loadingIndicator: String? = null) : WaitCondition()
    data class TextAppears(val text: String) : WaitCondition()
}

sealed class VerifyCondition {
    data class ElementPresent(val target: String, val accessibilityId: String? = null) : VerifyCondition()
    data class TextPresent(val text: String) : VerifyCondition()
    data class ScreenIs(val screenName: String) : VerifyCondition()
    data class Authenticated(val expected: Boolean = true) : VerifyCondition()
}

