package com.electricsheep.testautomation.planner

import com.electricsheep.testautomation.actions.ActionResult
import com.electricsheep.testautomation.actions.HumanAction
import org.slf4j.LoggerFactory

/**
 * Detects when the system is stuck in a loop trying the same thing repeatedly.
 * 
 * **Human Process**: "I've tried this 3-4 times and it's not working. I should give up."
 * 
 * Tracks:
 * - Repeated attempts at the same action
 * - Repeated attempts to achieve the same goal
 * - Patterns of failure (same error, same action)
 * 
 * Makes human-like decision to stop when stuck.
 */
class StuckDetector(
    private val maxRepeatedActionAttempts: Int = 3, // How many times to try same action before giving up
    private val maxRepeatedGoalAttempts: Int = 4   // How many iterations trying same goal before giving up
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * Tracks recent action attempts with their outcomes.
     */
    data class ActionAttempt(
        val action: HumanAction,
        val result: ActionResult,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    private val recentAttempts = mutableListOf<ActionAttempt>()
    private val recentGoals = mutableListOf<String>() // Track what we're trying to achieve
    
    /**
     * Record an action attempt.
     */
    fun recordAttempt(action: HumanAction, result: ActionResult, goalDescription: String? = null) {
        recentAttempts.add(ActionAttempt(action, result))
        
        // Keep only last 10 attempts for analysis
        if (recentAttempts.size > 10) {
            recentAttempts.removeAt(0)
        }
        
        // Track goal if provided
        if (goalDescription != null) {
            recentGoals.add(goalDescription)
            if (recentGoals.size > 10) {
                recentGoals.removeAt(0)
            }
        }
    }
    
    /**
     * Check if we're stuck trying the same action repeatedly.
     * 
     * **Human Decision**: "I've tried tapping this button 3 times and it keeps failing. It's not working."
     */
    fun isStuckOnAction(): Boolean {
        if (recentAttempts.size < maxRepeatedActionAttempts) {
            return false
        }
        
        // Look at the last N attempts
        val lastAttempts = recentAttempts.takeLast(maxRepeatedActionAttempts)
        
        // Check if they're all the same action type and target
        val firstAttempt = lastAttempts.first()
        val actionKey = getActionKey(firstAttempt.action)
        
        val allSameAction = lastAttempts.all { attempt ->
            getActionKey(attempt.action) == actionKey
        }
        
        if (!allSameAction) {
            return false
        }
        
        // Check if they all failed
        val allFailed = lastAttempts.all { attempt ->
            attempt.result is ActionResult.Failure
        }
        
        if (allFailed) {
            logger.warn("🔄 STUCK DETECTED: Tried '${actionKey}' $maxRepeatedActionAttempts times, all failed")
            logger.warn("💭 HUMAN DECISION: This isn't working. I should stop trying.")
            return true
        }
        
        return false
    }
    
    /**
     * Check if we're stuck trying to achieve the same goal.
     * 
     * **Human Decision**: "I've been trying to get to the mood screen for 4 iterations. It's not happening."
     */
    fun isStuckOnGoal(): Boolean {
        if (recentGoals.size < maxRepeatedGoalAttempts) {
            return false
        }
        
        // Look at the last N goals
        val lastGoals = recentGoals.takeLast(maxRepeatedGoalAttempts)
        
        // Check if they're all the same goal
        val firstGoal = lastGoals.first()
        val allSameGoal = lastGoals.all { it == firstGoal }
        
        if (allSameGoal) {
            logger.warn("🔄 STUCK DETECTED: Tried to achieve '$firstGoal' for $maxRepeatedGoalAttempts iterations")
            logger.warn("💭 HUMAN DECISION: I keep trying the same thing. It's not working. I should stop.")
            return true
        }
        
        return false
    }
    
    /**
     * Check if we're stuck in a failure loop (same error pattern).
     * 
     * **Human Decision**: "I keep getting the same error. This approach isn't working."
     */
    fun isStuckInFailureLoop(): Boolean {
        if (recentAttempts.size < maxRepeatedActionAttempts) {
            return false
        }
        
        val lastAttempts = recentAttempts.takeLast(maxRepeatedActionAttempts)
        val failures = lastAttempts.filter { it.result is ActionResult.Failure }
        
        if (failures.size < maxRepeatedActionAttempts) {
            return false
        }
        
        // Check if they all have similar error messages
        val errorMessages = failures.mapNotNull { (it.result as? ActionResult.Failure)?.error }
        if (errorMessages.size < maxRepeatedActionAttempts) {
            return false
        }
        
        // Check if errors are similar (same keywords)
        val firstError = errorMessages.first().lowercase()
        val errorKeywords = extractErrorKeywords(firstError)
        
        val allSimilarErrors = errorMessages.all { error ->
            val errorLower = error.lowercase()
            errorKeywords.any { keyword -> errorLower.contains(keyword) }
        }
        
        if (allSimilarErrors) {
            logger.warn("🔄 STUCK DETECTED: Getting same error pattern repeatedly")
            logger.warn("💭 HUMAN DECISION: This error keeps happening. I should stop.")
            return true
        }
        
        return false
    }
    
    /**
     * Check if we're stuck overall (any of the stuck conditions).
     */
    fun isStuck(): Boolean {
        return isStuckOnAction() || isStuckOnGoal() || isStuckInFailureLoop()
    }
    
    /**
     * Get a human-like reason for why we're stuck.
     */
    fun getStuckReason(): String {
        return when {
            isStuckOnAction() -> {
                val lastAttempts = recentAttempts.takeLast(maxRepeatedActionAttempts)
                val actionKey = getActionKey(lastAttempts.first().action)
                "Tried '$actionKey' $maxRepeatedActionAttempts times without success"
            }
            isStuckOnGoal() -> {
                val lastGoals = recentGoals.takeLast(maxRepeatedGoalAttempts)
                "Tried to achieve '${lastGoals.first()}' for $maxRepeatedGoalAttempts iterations without progress"
            }
            isStuckInFailureLoop() -> {
                "Getting the same error pattern repeatedly - approach isn't working"
            }
            else -> "Unknown stuck condition"
        }
    }
    
    /**
     * Reset tracking (e.g., after successful action or goal change).
     */
    fun reset() {
        recentAttempts.clear()
        recentGoals.clear()
    }
    
    /**
     * Reset action tracking but keep goal tracking (e.g., after successful action).
     */
    fun resetActionTracking() {
        recentAttempts.clear()
    }
    
    /**
     * Get a unique key for an action (for comparison).
     */
    private fun getActionKey(action: HumanAction): String {
        return when (action) {
            is HumanAction.Tap -> "Tap:${action.target}"
            is HumanAction.TypeText -> "Type:${action.target}"
            is HumanAction.Swipe -> "Swipe:${action.direction}"
            is HumanAction.WaitFor -> "Wait:${action.condition}"
            is HumanAction.Verify -> "Verify:${action.condition}"
            is HumanAction.NavigateBack -> "NavigateBack"
            is HumanAction.CaptureState -> "CaptureState"
        }
    }
    
    /**
     * Extract keywords from error message for pattern matching.
     */
    private fun extractErrorKeywords(error: String): List<String> {
        val keywords = mutableListOf<String>()
        val lowerError = error.lowercase()
        
        // Common error patterns
        if (lowerError.contains("not found") || lowerError.contains("could not find")) {
            keywords.add("not found")
        }
        if (lowerError.contains("not clickable") || lowerError.contains("not visible")) {
            keywords.add("not clickable")
        }
        if (lowerError.contains("timeout")) {
            keywords.add("timeout")
        }
        if (lowerError.contains("keyboard")) {
            keywords.add("keyboard")
        }
        if (lowerError.contains("blocked") || lowerError.contains("blocking")) {
            keywords.add("blocked")
        }
        
        return keywords
    }
}

