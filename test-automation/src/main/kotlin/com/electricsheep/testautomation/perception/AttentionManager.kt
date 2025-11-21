package com.electricsheep.testautomation.perception

import org.slf4j.LoggerFactory

/**
 * Manages attention and focus using attention models from cognitive psychology.
 * 
 * Determines what areas of the screen to focus on based on:
 * - Current goal
 * - Expected action
 * - Salience (what stands out)
 * - Context (where we are in the task)
 */
class AttentionManager {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * Represents a focus area on the screen.
     */
    data class FocusArea(
        val name: String,
        val description: String,
        val priority: Int, // Higher = more important
        val elementTypes: List<String> // e.g., "input", "button", "error", "loading"
    )
    
    /**
     * Determine focus areas based on context.
     * 
     * @param currentGoal Current goal we're working toward
     * @param nextAction Next action we're about to perform
     * @param screenState Current screen state
     * @return List of focus areas, ordered by priority
     */
    fun determineFocusAreas(
        currentGoal: String?,
        nextAction: String?,
        screenState: String?
    ): List<FocusArea> {
        val focusAreas = mutableListOf<FocusArea>()
        
        // Determine focus based on action type
        when {
            nextAction?.contains("Type", ignoreCase = true) == true -> {
                // When typing, focus on input fields
                focusAreas.add(
                    FocusArea(
                        name = "Input Fields",
                        description = "Focus on input fields for typing",
                        priority = 10,
                        elementTypes = listOf("input", "textfield", "edittext")
                    )
                )
                focusAreas.add(
                    FocusArea(
                        name = "Error Zones",
                        description = "Check for validation errors",
                        priority = 8,
                        elementTypes = listOf("error", "validation", "message")
                    )
                )
            }
            
            nextAction?.contains("Tap", ignoreCase = true) == true -> {
                // When tapping, focus on buttons and interactive elements
                focusAreas.add(
                    FocusArea(
                        name = "Interactive Elements",
                        description = "Focus on buttons and clickable elements",
                        priority = 10,
                        elementTypes = listOf("button", "clickable", "interactive")
                    )
                )
                focusAreas.add(
                    FocusArea(
                        name = "Blocking Elements",
                        description = "Check for dialogs or overlays",
                        priority = 9,
                        elementTypes = listOf("dialog", "popup", "overlay", "modal")
                    )
                )
            }
            
            nextAction?.contains("Wait", ignoreCase = true) == true -> {
                // When waiting, focus on loading indicators
                focusAreas.add(
                    FocusArea(
                        name = "Loading Indicators",
                        description = "Focus on loading spinners and progress",
                        priority = 10,
                        elementTypes = listOf("loading", "progress", "spinner", "indicator")
                    )
                )
                focusAreas.add(
                    FocusArea(
                        name = "State Changes",
                        description = "Watch for screen transitions",
                        priority = 8,
                        elementTypes = listOf("screen", "navigation", "transition")
                    )
                )
            }
            
            else -> {
                // Default: general observation
                focusAreas.add(
                    FocusArea(
                        name = "General Screen",
                        description = "General screen observation",
                        priority = 5,
                        elementTypes = listOf("all")
                    )
                )
            }
        }
        
        // Always check for errors (high priority)
        focusAreas.add(
            FocusArea(
                name = "Error Detection",
                description = "Always check for errors",
                priority = 9,
                elementTypes = listOf("error", "warning", "alert")
            )
        )
        
        // Sort by priority (highest first)
        val sortedAreas = focusAreas.sortedByDescending { it.priority }
        
        logger.debug("Determined focus areas: ${sortedAreas.joinToString { it.name }}")
        
        return sortedAreas
    }
    
    /**
     * Get salience indicators (what stands out on screen).
     * 
     * @param screenState Current screen state
     * @return List of salient elements (errors, changes, important elements)
     */
    fun getSalientElements(screenState: String?): List<String> {
        val salient = mutableListOf<String>()
        
        // Errors are always salient
        if (screenState?.contains("error", ignoreCase = true) == true) {
            salient.add("error")
        }
        
        // Loading indicators are salient
        if (screenState?.contains("loading", ignoreCase = true) == true) {
            salient.add("loading")
        }
        
        // Changes are salient
        if (screenState?.contains("change", ignoreCase = true) == true) {
            salient.add("change")
        }
        
        return salient
    }
    
    /**
     * Determine if we should shift attention.
     * 
     * @param currentFocus Current focus areas
     * @param newContext New context (goal, action, state)
     * @return True if attention should shift
     */
    fun shouldShiftAttention(
        currentFocus: List<FocusArea>,
        newContext: String
    ): Boolean {
        // Shift attention if:
        // 1. New error detected
        if (newContext.contains("error", ignoreCase = true)) {
            return true
        }
        
        // 2. Goal changed
        if (newContext.contains("goal", ignoreCase = true)) {
            return true
        }
        
        // 3. Screen changed significantly
        if (newContext.contains("screen", ignoreCase = true) && 
            newContext.contains("change", ignoreCase = true)) {
            return true
        }
        
        return false
    }
}



