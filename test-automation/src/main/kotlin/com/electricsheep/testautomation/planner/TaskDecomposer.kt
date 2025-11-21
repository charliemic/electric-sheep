package com.electricsheep.testautomation.planner

import com.electricsheep.testautomation.perception.GoalManager
import org.slf4j.LoggerFactory

/**
 * Decomposes high-level tasks into abstract goals.
 * 
 * **Key Principle**: This layer translates natural language tasks into abstract,
 * app-agnostic goals that the AdaptivePlanner can work with.
 * 
 * **Separation of Concerns**:
 * - This layer knows about task semantics (what "sign up" means)
 * - AdaptivePlanner is agnostic and works with abstract goals
 * - GoalManager manages goal state, not task semantics
 */
class TaskDecomposer {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * Decompose a task into abstract goals.
     * 
     * @param task Natural language task description
     * @return List of abstract goals with priorities
     */
    fun decomposeTask(task: String): List<AbstractGoal> {
        val taskLower = task.lowercase()
        val goals = mutableListOf<AbstractGoal>()
        
        logger.info("🔍 Decomposing task: $task")
        
        // Detect authentication requirement
        if (taskLower.contains("sign up") || taskLower.contains("create account") || 
            taskLower.contains("sign in") || taskLower.contains("login")) {
            goals.add(AbstractGoal(
                id = "authenticate",
                type = AbstractGoalType.AUTHENTICATE,
                description = "Authenticate user (sign up or sign in)",
                priority = 10, // High priority - needed first
                parentGoalId = null
            ))
        }
        
        // Detect data entry requirement
        if (taskLower.contains("add") || taskLower.contains("create") || 
            taskLower.contains("enter") || taskLower.contains("submit")) {
            val dataType = when {
                taskLower.contains("mood") -> "mood"
                taskLower.contains("note") -> "note"
                taskLower.contains("entry") -> "entry"
                else -> "data"
            }
            
            goals.add(AbstractGoal(
                id = "add_data_entry",
                type = AbstractGoalType.ADD_DATA_ENTRY,
                description = "Add $dataType entry",
                priority = 8,
                parentGoalId = "authenticate", // Usually requires authentication first
                metadata = mapOf("dataType" to dataType) // Pass context down to planner
            ))
        }
        
        // Detect navigation requirement
        if (taskLower.contains("navigate") || taskLower.contains("go to") || 
            taskLower.contains("open")) {
            goals.add(AbstractGoal(
                id = "navigate_to_feature",
                type = AbstractGoalType.NAVIGATE_TO_FEATURE,
                description = "Navigate to required feature",
                priority = 9,
                parentGoalId = null
            ))
        }
        
        // Detect viewing requirement
        if (taskLower.contains("view") || taskLower.contains("see") || 
            taskLower.contains("check") || taskLower.contains("verify")) {
            goals.add(AbstractGoal(
                id = "view_data",
                type = AbstractGoalType.VIEW_DATA,
                description = "View or verify data",
                priority = 5,
                parentGoalId = null
            ))
        }
        
        // Sort by priority (highest first)
        val sortedGoals = goals.sortedByDescending { it.priority }
        
        logger.info("📋 Decomposed into ${sortedGoals.size} abstract goals:")
        sortedGoals.forEachIndexed { idx, goal ->
            logger.info("   ${idx + 1}. ${goal.type.name}: ${goal.description} (priority: ${goal.priority})")
        }
        
        return sortedGoals
    }
    
    /**
     * Abstract goal types - app-agnostic.
     */
    enum class AbstractGoalType {
        AUTHENTICATE,        // Sign up, sign in, login
        ADD_DATA_ENTRY,      // Add any form entry (mood, note, etc.)
        UPDATE_DATA_ENTRY,   // Edit existing entry
        DELETE_DATA_ENTRY,   // Delete entry
        VIEW_DATA,           // View list, history, details
        NAVIGATE_TO_FEATURE, // Navigate to any feature
        SEARCH,              // Search for content
        FILTER,              // Filter data
        UNKNOWN
    }
    
    /**
     * Abstract goal representation.
     */
    data class AbstractGoal(
        val id: String,
        val type: AbstractGoalType,
        val description: String,
        val priority: Int,
        val parentGoalId: String? = null,
        val metadata: Map<String, Any> = emptyMap() // Optional: "dataType" -> "mood"
    )
}

