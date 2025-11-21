package com.electricsheep.testautomation.perception

import org.slf4j.LoggerFactory

/**
 * Manages goals hierarchically using Perceptual Control Theory (PCT).
 * 
 * Tracks goal states, calculates errors (difference between goal and current state),
 * and provides feedback for action planning.
 */
class GoalManager {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    private val goals = mutableListOf<Goal>()
    private var currentGoal: Goal? = null
    
    /**
     * Represents a goal with reference state and current state.
     */
    data class Goal(
        val id: String,
        val description: String,
        val referenceState: GoalState, // What we want
        var currentState: GoalState = GoalState.UNKNOWN, // What we have
        val parentGoalId: String? = null, // For hierarchical goals
        val priority: Int = 0 // Higher = more important
    ) {
        /**
         * Calculate error: difference between reference and current state.
         * Returns 0 if goal is achieved, >0 if not achieved.
         */
        fun calculateError(): Int {
            return if (currentState == referenceState) {
                0 // Goal achieved
            } else {
                1 // Goal not achieved (can be enhanced with more granular error calculation)
            }
        }
        
        /**
         * Check if goal is achieved.
         */
        fun isAchieved(): Boolean = calculateError() == 0
    }
    
    /**
     * Represents the state of a goal.
     */
    enum class GoalState {
        UNKNOWN,        // Goal state not yet determined
        IN_PROGRESS,    // Working toward goal
        ACHIEVED,       // Goal completed
        FAILED,         // Goal cannot be achieved
        BLOCKED         // Goal blocked by error or unexpected state
    }
    
    /**
     * Add a goal to the hierarchy.
     */
    fun addGoal(goal: Goal) {
        goals.add(goal)
        logger.debug("Added goal: ${goal.description} (id: ${goal.id})")
        
        // If this is the first goal or has higher priority, make it current
        if (currentGoal == null || goal.priority > currentGoal!!.priority) {
            currentGoal = goal
            logger.debug("Set current goal: ${goal.description}")
        }
    }
    
    /**
     * Update current state of a goal.
     */
    fun updateGoalState(goalId: String, newState: GoalState) {
        val goal = goals.find { it.id == goalId }
        if (goal != null) {
            val oldState = goal.currentState
            goal.currentState = newState
            logger.debug("Updated goal ${goal.description}: $oldState → $newState")
            
            // If current goal is achieved, move to next priority goal
            if (goal == currentGoal && goal.isAchieved()) {
                selectNextGoal()
            }
        } else {
            logger.warn("Goal not found: $goalId")
        }
    }
    
    /**
     * Get current goal (highest priority unachieved goal).
     */
    fun getCurrentGoal(): Goal? = currentGoal
    
    /**
     * Get all goals.
     */
    fun getAllGoals(): List<Goal> = goals.toList()
    
    /**
     * Get goal error (difference between reference and current state).
     */
    fun getGoalError(): Int {
        return currentGoal?.calculateError() ?: 0
    }
    
    /**
     * Check if main goal is achieved.
     */
    fun isMainGoalAchieved(): Boolean {
        val mainGoal = goals.firstOrNull { it.parentGoalId == null }
        return mainGoal?.isAchieved() ?: false
    }
    
    /**
     * Select next priority goal.
     */
    private fun selectNextGoal() {
        val unachievedGoals = goals.filter { !it.isAchieved() }
        currentGoal = unachievedGoals.maxByOrNull { it.priority }
        currentGoal?.let {
            logger.debug("Selected next goal: ${it.description}")
        }
    }
    
    /**
     * Reset all goals (for new test run).
     */
    fun reset() {
        goals.clear()
        currentGoal = null
        logger.debug("Goal manager reset")
    }
}



