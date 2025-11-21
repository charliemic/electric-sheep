package com.electricsheep.testautomation.planner

import com.electricsheep.testautomation.actions.HumanAction
import com.electricsheep.testautomation.monitoring.ScreenEvaluator
import com.electricsheep.testautomation.monitoring.ScreenObservation
import com.electricsheep.testautomation.vision.TextDetector
import org.slf4j.LoggerFactory
import java.io.File

// Import types from TaskPlanner
import com.electricsheep.testautomation.planner.TaskPlanner.PlanResult
import com.electricsheep.testautomation.planner.TaskPlanner.ExecutionStep

/**
 * Adaptive planner that works out how to achieve intent based on visual observation.
 * 
 * **Key Principle**: Instead of fixed pathways, this planner:
 * 1. Observes what's on screen (visual analysis)
 * 2. Matches current state to goal
 * 3. Generates next action based on what's visible
 * 4. Adapts to different screen states
 * 5. Tries alternative pathways when stuck
 * 
 * This mimics how humans work out how to achieve a task - by observing and adapting.
 */
class AdaptivePlanner(
    private val screenEvaluator: ScreenEvaluator,
    private val textDetector: TextDetector?,
    private val personaManager: PersonaManager
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * Generate a plan based on visual observation of current screen.
     * 
     * @param task The goal to achieve
     * @param screenshot Current screen state
     * @param persona User persona (affects behavior)
     * @param history Previous actions attempted
     * @param errorMessages Any errors currently visible
     * @return Plan with next actions to take
     */
    suspend fun generatePlan(
        task: String,
        screenshot: File,
        persona: Persona?,
        history: List<ExecutionStep>,
        errorMessages: List<String>
    ): PlanResult {
        logger.info("🧠 ADAPTIVE PLANNING: Observing screen to work out how to achieve: $task")
        
        // Step 1: VISUAL OBSERVATION - What do we see?
        val evaluation = screenEvaluator.evaluateScreen(screenshot)
        val visibleText = textDetector?.extractText(screenshot)?.fullText?.lowercase() ?: ""
        val visibleButtons = textDetector?.extractText(screenshot)?.buttonLabels ?: emptyList()
        
        val screenName = evaluation.observations.firstOrNull { 
            it.type == ScreenObservation.ObservationType.UNEXPECTED_STATE 
        }?.message?.substringAfter("but detected: ")?.substringBefore(",") 
        logger.info("👁️  OBSERVED: Screen=${screenName ?: "unknown"}, Errors=${evaluation.observations.filter { it.type == ScreenObservation.ObservationType.ERROR }.size}, Buttons=${visibleButtons.size}")
        
        // Step 2: ORIENT - Match current state to goal
        val goalState = determineGoalState(task)
        val currentState = determineCurrentState(evaluation, visibleText, visibleButtons)
        val gap = identifyGap(goalState, currentState, task)
        
        logger.info("🎯 GOAL: $goalState, CURRENT: $currentState, GAP: $gap")
        
        // Step 3: DECIDE - Generate next action based on gap
        val actions = mutableListOf<HumanAction>()
        
        // Handle errors first (if any)
        if (errorMessages.isNotEmpty() || evaluation.observations.any { it.type == ScreenObservation.ObservationType.ERROR }) {
            val errorActions = handleErrors(evaluation, errorMessages, persona)
            actions.addAll(errorActions)
            if (errorActions.isNotEmpty()) {
                return PlanResult.Success(actions)
            }
        }
        
        // Generate actions to close the gap
        val gapActions = generateActionsForGap(gap, currentState, visibleButtons, visibleText, task, persona, history)
        actions.addAll(gapActions)
        
        if (actions.isEmpty()) {
            logger.warn("⚠️  Could not generate actions for gap: $gap")
            return PlanResult.Failure("Could not determine next action for gap: $gap")
        }
        
        logger.info("📋 Generated ${actions.size} adaptive actions based on visual observation")
        return PlanResult.Success(actions)
    }
    
    /**
     * Determine what state we need to be in to achieve the goal.
     */
    private fun determineGoalState(task: String): GoalState {
        val taskLower = task.lowercase()
        return when {
            taskLower.contains("sign up") || taskLower.contains("create account") -> {
                if (taskLower.contains("mood")) {
                    GoalState.SIGNED_UP_WITH_MOOD_ADDED
                } else {
                    GoalState.SIGNED_UP
                }
            }
            taskLower.contains("sign in") || taskLower.contains("login") -> GoalState.SIGNED_IN
            taskLower.contains("add mood") || taskLower.contains("mood value") -> GoalState.MOOD_ADDED
            taskLower.contains("mood") -> GoalState.VIEWING_MOOD
            else -> GoalState.UNKNOWN
        }
    }
    
    /**
     * Determine current state from visual observation.
     */
    private fun determineCurrentState(
        evaluation: com.electricsheep.testautomation.monitoring.ScreenEvaluation,
        visibleText: String,
        visibleButtons: List<String>
    ): CurrentState {
        // Detect screen type from visible text
        val screenType = when {
            visibleText.contains("sign in") || visibleText.contains("sign up") || visibleText.contains("create account") -> ScreenType.SIGN_IN_SCREEN
            visibleText.contains("mood") && (visibleText.contains("score") || visibleText.contains("enter")) -> ScreenType.MOOD_INPUT_SCREEN
            visibleText.contains("mood history") || visibleText.contains("mood management") -> ScreenType.MOOD_MANAGEMENT_SCREEN
            visibleText.contains("landing") || visibleText.contains("welcome") -> ScreenType.LANDING_SCREEN
            else -> ScreenType.UNKNOWN
        }
        
        // Detect authentication state
        val isAuthenticated = visibleText.contains("mood history") || 
                             visibleText.contains("sign out") || 
                             visibleText.contains("profile")
        
        // Detect form state
        val hasEmailField = visibleText.contains("email") || visibleButtons.any { it.contains("email", ignoreCase = true) }
        val hasPasswordField = visibleText.contains("password") || visibleButtons.any { it.contains("password", ignoreCase = true) }
        val hasMoodField = visibleText.contains("mood score") || visibleText.contains("enter") && visibleText.contains("mood")
        
        return CurrentState(
            screenType = screenType,
            isAuthenticated = isAuthenticated,
            hasEmailField = hasEmailField,
            hasPasswordField = hasPasswordField,
            hasMoodField = hasMoodField,
            visibleButtons = visibleButtons
        )
    }
    
    /**
     * Identify the gap between current state and goal state.
     */
    private fun identifyGap(goalState: GoalState, currentState: CurrentState, task: String): Gap {
        return when (goalState) {
            GoalState.SIGNED_UP_WITH_MOOD_ADDED -> {
                when {
                    !currentState.isAuthenticated && currentState.screenType != ScreenType.SIGN_IN_SCREEN -> {
                        Gap.NAVIGATE_TO_SIGN_IN
                    }
                    !currentState.isAuthenticated && currentState.screenType == ScreenType.SIGN_IN_SCREEN -> {
                        if (!currentState.hasEmailField || !currentState.hasPasswordField) {
                            Gap.SHOW_EMAIL_PASSWORD_FORM
                        } else {
                            Gap.FILL_SIGN_UP_FORM
                        }
                    }
                    currentState.isAuthenticated && currentState.screenType != ScreenType.MOOD_INPUT_SCREEN -> {
                        Gap.NAVIGATE_TO_MOOD_INPUT
                    }
                    currentState.isAuthenticated && currentState.screenType == ScreenType.MOOD_INPUT_SCREEN -> {
                        if (!currentState.hasMoodField) {
                            Gap.FIND_MOOD_FIELD
                        } else {
                            Gap.ADD_MOOD_VALUE
                        }
                    }
                    else -> Gap.UNKNOWN
                }
            }
            GoalState.SIGNED_UP -> {
                when {
                    !currentState.isAuthenticated && currentState.screenType != ScreenType.SIGN_IN_SCREEN -> {
                        Gap.NAVIGATE_TO_SIGN_IN
                    }
                    !currentState.isAuthenticated && currentState.screenType == ScreenType.SIGN_IN_SCREEN -> {
                        if (!currentState.hasEmailField || !currentState.hasPasswordField) {
                            Gap.SHOW_EMAIL_PASSWORD_FORM
                        } else {
                            Gap.FILL_SIGN_UP_FORM
                        }
                    }
                    currentState.isAuthenticated -> {
                        Gap.VERIFY_SIGN_UP_SUCCESS
                    }
                    else -> Gap.UNKNOWN
                }
            }
            GoalState.MOOD_ADDED -> {
                when {
                    !currentState.isAuthenticated -> {
                        Gap.SIGN_IN_FIRST
                    }
                    currentState.screenType != ScreenType.MOOD_INPUT_SCREEN -> {
                        Gap.NAVIGATE_TO_MOOD_INPUT
                    }
                    currentState.hasMoodField -> {
                        Gap.ADD_MOOD_VALUE
                    }
                    else -> {
                        Gap.FIND_MOOD_FIELD
                    }
                }
            }
            else -> Gap.UNKNOWN
        }
    }
    
    /**
     * Generate actions to close the identified gap.
     */
    private suspend fun generateActionsForGap(
        gap: Gap,
        currentState: CurrentState,
        visibleButtons: List<String>,
        visibleText: String,
        task: String,
        persona: Persona?,
        history: List<ExecutionStep>
    ): List<HumanAction> {
        val actions = mutableListOf<HumanAction>()
        
        when (gap) {
            Gap.NAVIGATE_TO_SIGN_IN -> {
                // Look for sign in button or mood management (which leads to sign in)
                val signInButton = visibleButtons.firstOrNull { 
                    it.contains("sign in", ignoreCase = true) || 
                    it.contains("mood management", ignoreCase = true) ||
                    it.contains("mood", ignoreCase = true)
                }
                if (signInButton != null) {
                    actions.add(HumanAction.Tap(
                        target = signInButton,
                        accessibilityId = signInButton
                    ))
                }
            }
            
            Gap.SHOW_EMAIL_PASSWORD_FORM -> {
                // Look for "Show email and password sign in" or similar
                val formButton = visibleButtons.firstOrNull {
                    it.contains("email", ignoreCase = true) && it.contains("password", ignoreCase = true) ||
                    it.contains("show", ignoreCase = true) && it.contains("email", ignoreCase = true)
                }
                if (formButton != null) {
                    actions.add(HumanAction.Tap(
                        target = formButton,
                        accessibilityId = formButton
                    ))
                }
            }
            
            Gap.FILL_SIGN_UP_FORM -> {
                // Generate email and password based on persona
                val email = personaManager.generateEmail(persona)
                val password = personaManager.generatePassword(persona)
                
                // Find email field
                val emailField = if (currentState.hasEmailField) {
                    "Email field"
                } else {
                    visibleButtons.firstOrNull { it.contains("email", ignoreCase = true) } ?: "Email field"
                }
                
                // Find password field
                val passwordField = if (currentState.hasPasswordField) {
                    "Password field"
                } else {
                    visibleButtons.firstOrNull { it.contains("password", ignoreCase = true) } ?: "Password field"
                }
                
                actions.add(HumanAction.TypeText(
                    target = emailField,
                    text = email,
                    accessibilityId = "Email address input field"
                ))
                
                actions.add(HumanAction.TypeText(
                    target = passwordField,
                    text = password,
                    accessibilityId = "Password input field"
                ))
                
                // Find create account button
                val createButton = visibleButtons.firstOrNull {
                    it.contains("create", ignoreCase = true) || 
                    it.contains("sign up", ignoreCase = true)
                }
                if (createButton != null) {
                    actions.add(HumanAction.Tap(
                        target = createButton,
                        accessibilityId = createButton
                    ))
                    actions.add(HumanAction.WaitFor(
                        condition = com.electricsheep.testautomation.actions.WaitCondition.LoadingComplete(),
                        timeoutSeconds = 25
                    ))
                }
            }
            
            Gap.NAVIGATE_TO_MOOD_INPUT -> {
                // Look for mood-related button
                val moodButton = visibleButtons.firstOrNull {
                    it.contains("mood", ignoreCase = true) && 
                    (it.contains("add", ignoreCase = true) || it.contains("enter", ignoreCase = true))
                }
                if (moodButton != null) {
                    actions.add(HumanAction.Tap(
                        target = moodButton,
                        accessibilityId = moodButton
                    ))
                }
            }
            
            Gap.ADD_MOOD_VALUE -> {
                // Generate mood score based on persona
                val moodScore = when (persona?.averageTechnicalSkill ?: 5) {
                    in 0..3 -> "7"
                    in 4..7 -> "6"
                    else -> "8"
                }
                
                actions.add(HumanAction.TypeText(
                    target = "Mood score field",
                    text = moodScore,
                    accessibilityId = "Mood score input field. Enter a number between 1 and 10"
                ))
                
                // Find save button
                val saveButton = visibleButtons.firstOrNull {
                    it.contains("save", ignoreCase = true) || 
                    it.contains("submit", ignoreCase = true)
                }
                if (saveButton != null) {
                    actions.add(HumanAction.Tap(
                        target = saveButton,
                        accessibilityId = saveButton
                    ))
                    actions.add(HumanAction.WaitFor(
                        condition = com.electricsheep.testautomation.actions.WaitCondition.LoadingComplete(),
                        timeoutSeconds = 15
                    ))
                }
            }
            
            Gap.VERIFY_SIGN_UP_SUCCESS -> {
                actions.add(HumanAction.Verify(
                    condition = com.electricsheep.testautomation.actions.VerifyCondition.Authenticated(true)
                ))
            }
            
            Gap.SIGN_IN_FIRST -> {
                // Need to sign in first - this is a sub-goal
                // For now, return empty and let the planner handle it in next iteration
            }
            
            Gap.FIND_MOOD_FIELD -> {
                // Look for mood input - might need to scroll or navigate
                // For now, try to find it visually
            }
            
            Gap.UNKNOWN -> {
                logger.warn("Unknown gap - cannot generate actions")
            }
        }
        
        return actions
    }
    
    /**
     * Handle errors visible on screen.
     */
    private suspend fun handleErrors(
        evaluation: com.electricsheep.testautomation.monitoring.ScreenEvaluation,
        errorMessages: List<String>,
        persona: Persona?
    ): List<HumanAction> {
        val actions = mutableListOf<HumanAction>()
        val allErrors = errorMessages + evaluation.observations
            .filter { it.type == ScreenObservation.ObservationType.ERROR }
            .map { it.message }
        
        if (allErrors.isEmpty()) return actions
        
        val errorText = allErrors.joinToString(" ").lowercase()
        logger.info("💡 Handling errors: $errorText")
        
        // Email error - generate better email
        if (errorText.contains("email") && (errorText.contains("invalid") || errorText.contains("format"))) {
            val betterEmail = personaManager.generateEmail(persona)
            actions.add(HumanAction.TypeText(
                target = "Email field",
                text = betterEmail,
                accessibilityId = "Email address input field",
                clearFirst = true
            ))
        }
        
        // Password error - generate stronger password
        if (errorText.contains("password") && (errorText.contains("weak") || errorText.contains("short"))) {
            val strongerPassword = "SecurePass123!@#"
            actions.add(HumanAction.TypeText(
                target = "Password field",
                text = strongerPassword,
                accessibilityId = "Password input field",
                clearFirst = true
            ))
        }
        
        // After fixing errors, retry
        if (actions.isNotEmpty()) {
            actions.add(HumanAction.Tap(
                target = "Create Account button",
                accessibilityId = "Create account with email and password"
            ))
            actions.add(HumanAction.WaitFor(
                condition = com.electricsheep.testautomation.actions.WaitCondition.LoadingComplete(),
                timeoutSeconds = 25
            ))
        }
        
        return actions
    }
    
    // Data classes for state representation
    enum class GoalState {
        SIGNED_UP,
        SIGNED_UP_WITH_MOOD_ADDED,
        SIGNED_IN,
        MOOD_ADDED,
        VIEWING_MOOD,
        UNKNOWN
    }
    
    enum class ScreenType {
        LANDING_SCREEN,
        SIGN_IN_SCREEN,
        MOOD_MANAGEMENT_SCREEN,
        MOOD_INPUT_SCREEN,
        UNKNOWN
    }
    
    data class CurrentState(
        val screenType: ScreenType,
        val isAuthenticated: Boolean,
        val hasEmailField: Boolean,
        val hasPasswordField: Boolean,
        val hasMoodField: Boolean,
        val visibleButtons: List<String>
    )
    
    enum class Gap {
        NAVIGATE_TO_SIGN_IN,
        SHOW_EMAIL_PASSWORD_FORM,
        FILL_SIGN_UP_FORM,
        NAVIGATE_TO_MOOD_INPUT,
        ADD_MOOD_VALUE,
        VERIFY_SIGN_UP_SUCCESS,
        SIGN_IN_FIRST,
        FIND_MOOD_FIELD,
        UNKNOWN
    }
}

