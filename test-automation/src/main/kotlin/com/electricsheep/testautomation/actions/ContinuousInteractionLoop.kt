package com.electricsheep.testautomation.actions

import com.electricsheep.testautomation.actions.HumanAction
import com.electricsheep.testautomation.actions.VisualAdaptiveRecovery
import com.electricsheep.testautomation.monitoring.ScreenMonitor
import com.electricsheep.testautomation.monitoring.ScreenState
import com.electricsheep.testautomation.monitoring.StateManager
import com.electricsheep.testautomation.perception.GoalManager
import org.slf4j.LoggerFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Continuous perception-action loop (human-like interaction).
 * 
 * **Human Process**: Continuous perception ←→ action ←→ feedback
 * 
 * **System Process**: 
 * - Start continuous monitoring before action
 * - Execute action while monitoring
 * - Real-time feedback updates goals
 * - Stop monitoring after action
 * 
 * **Key Features**:
 * - Real-time feedback during actions
 * - Immediate goal state updates
 * - Continuous adaptation
 * - Human-like interaction flow
 * 
 * **Complexity**: ⭐⭐ (Medium - orchestrates multiple components)
 * **Effectiveness**: High (enables real-time adaptation)
 */
class ContinuousInteractionLoop(
    private val screenMonitor: ScreenMonitor,
    private val stateManager: StateManager,
    private val goalManager: GoalManager,
    private val coroutineScope: CoroutineScope,
    private val visualAdaptiveRecovery: VisualAdaptiveRecovery? = null // Optional adaptive recovery
) {
    // ActionExecutor will be set when executing (avoids circular dependency)
    private var actionExecutor: ActionExecutor? = null
    
    /**
     * Set action executor (called after ActionExecutor is created).
     */
    fun setActionExecutor(executor: ActionExecutor) {
        this.actionExecutor = executor
    }
    private val logger = LoggerFactory.getLogger(javaClass)
    
    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()
    
    private var monitoringJob: Job? = null
    private val feedbackObservations = mutableListOf<ScreenState>()
    
    /**
     * Execute action with continuous feedback loop.
     * 
     * **Human Process**: 
     * 1. Start observing (continuous perception)
     * 2. Execute action (while observing)
     * 3. Get real-time feedback (during action)
     * 4. Update goals (based on feedback)
     * 5. Stop observing (after action)
     * 
     * **System Process**:
     * 1. Start continuous monitoring
     * 2. Execute action
     * 3. Process feedback during action
     * 4. Update goal states
     * 5. Stop monitoring
     */
    suspend fun executeWithContinuousFeedback(action: HumanAction): ActionResult {
        val actionDesc = when (action) {
            is HumanAction.Tap -> action.target
            is HumanAction.TypeText -> "${action.target}: ${action.text?.take(20)}"
            is HumanAction.Swipe -> "Swipe ${action.direction}"
            is HumanAction.WaitFor -> "Wait: ${action.condition}"
            is HumanAction.Verify -> "Verify: ${action.condition}"
            is HumanAction.NavigateBack -> "Navigate back"
            is HumanAction.CaptureState -> "Capture state"
        }
        logger.info("")
        logger.info("🧠 Want to: $actionDesc")
        
        // Clear previous feedback
        feedbackObservations.clear()
        
        // Start continuous monitoring (human: start observing)
        logger.info("👁️  Watching what happens...")
        val monitoringStarted = startContinuousMonitoring(action)
        if (!monitoringStarted) {
            logger.warn("Failed to start continuous monitoring, executing action without feedback")
            val executor = actionExecutor
                ?: throw IllegalStateException("ActionExecutor not set. Call setActionExecutor() first.")
            return executor.execute(action)
        }
        
        try {
            // Execute action while monitoring (human: act while observing)
            val executor = actionExecutor
            if (executor == null) {
                logger.warn("ActionExecutor not set, falling back to standard execution")
                // Fallback: would need to call actionExecutor directly, but we don't have it
                // This shouldn't happen if setActionExecutor() is called properly
                throw IllegalStateException("ActionExecutor not set. Call setActionExecutor() first.")
            }
            // Check if keyboard is blocking before executing action (human-like: see keyboard → dismiss it first)
            val currentState = stateManager.getCurrentState()
            if (currentState?.hasKeyboard == true && visualAdaptiveRecovery != null) {
                logger.info("💡 Keyboard is visible, checking if it's blocking...")
                val screenshot = currentState.screenshot
                if (screenshot != null && screenshot.exists()) {
                    val isBlocking = visualAdaptiveRecovery.isElementBlocked(action.toString())
                    if (isBlocking) {
                        logger.info("💡 Keyboard is blocking action. Attempting to dismiss visually...")
                        val dismissed = visualAdaptiveRecovery.dismissKeyboardVisually(screenshot, executor)
                        if (dismissed) {
                            logger.info("✅ Keyboard dismissed! Waiting a moment for UI to update...")
                            delay(300) // Allow UI to update after keyboard dismissal
                        } else {
                            logger.debug("Could not dismiss keyboard visually, proceeding anyway")
                        }
                    }
                }
            }
            
            logger.info("✋ Doing it now...")
            // CRITICAL: Call base executor methods directly to avoid infinite recursion
            // (executor.execute() would check useContinuousLoop and call us again!)
            val actionResult = when (action) {
                is HumanAction.Tap -> {
                    logger.debug("Executing tap via base method: ${action.target}")
                    executor.executeTapWithAppium(action)
                }
                is HumanAction.TypeText -> {
                    logger.debug("Executing type via base method: ${action.target}")
                    executor.executeTypeTextWithAppium(action)
                }
                is HumanAction.Swipe -> {
                    logger.debug("Executing swipe via base method: ${action.direction}")
                    executor.executeSwipe(action)
                }
                is HumanAction.WaitFor -> {
                    logger.debug("Executing wait via base method: ${action.condition}")
                    executor.executeWaitFor(action)
                }
                is HumanAction.NavigateBack -> {
                    logger.debug("Executing navigate back via base method")
                    executor.executeNavigateBack()
                }
                is HumanAction.CaptureState -> {
                    logger.debug("Executing capture state via base method")
                    executor.executeCaptureState()
                }
                is HumanAction.Verify -> {
                    logger.debug("Executing verify via base method: ${action.condition}")
                    executor.executeVerify(action)
                }
            }
            
            if (actionResult is ActionResult.Success) {
                logger.info("✅ Done!")
            } else {
                logger.info("❌ That didn't work: ${(actionResult as? ActionResult.Failure)?.error ?: "Unknown error"}")
            }
            
            // Brief wait to capture post-action state
            delay(300) // Allow UI to update
            
            // Check if keyboard appeared after typing and dismiss it if blocking next action
            // (Human: "I typed text, keyboard appeared, I should dismiss it before tapping button")
            // EVENT-DRIVEN: Wait for keyboard to appear (not fixed delay)
            logger.debug("Checking if action is TypeText: ${action is HumanAction.TypeText}, visualAdaptiveRecovery: ${visualAdaptiveRecovery != null}")
            if (action is HumanAction.TypeText && visualAdaptiveRecovery != null) {
                logger.info("💡 Checking for keyboard after typing...")
                
                // EVENT-DRIVEN: Wait for keyboard to appear in state (human: "I see the keyboard")
                // Poll state until keyboard is detected or timeout
                val postActionState = stateManager.waitForState(
                    predicate = { state -> 
                        // Wait until keyboard is detected visually
                        state.hasKeyboard == true && state.screenshot != null
                    },
                    timeoutMs = 3000 // Max 3 seconds for keyboard to appear
                ) ?: run {
                    // Fallback: Check current state if timeout (keyboard might already be there)
                    val current = stateManager.getCurrentState()
                    if (current?.hasKeyboard == true) current else null
                }
                
                logger.info("💡 Post-action state: hasKeyboard=${postActionState?.hasKeyboard}, screenshot=${postActionState?.screenshot?.exists()}")
                
                if (postActionState?.hasKeyboard == true && postActionState.screenshot != null) {
                    logger.info("💡 Keyboard appeared after typing. Dismissing it visually...")
                    val screenshot = postActionState.screenshot
                    if (screenshot.exists()) {
                        // Keyboard is always blocking when visible after typing - dismiss it
                        logger.info("💡 Keyboard is blocking future actions. Dismissing visually...")
                        val dismissed = visualAdaptiveRecovery.dismissKeyboardVisually(screenshot, executor)
                        if (dismissed) {
                            logger.info("✅ Keyboard dismissed after typing!")
                            
                            // EVENT-DRIVEN: Wait for keyboard to actually disappear (not fixed delay)
                            val keyboardDismissed = stateManager.waitForState(
                                predicate = { state -> !state.hasKeyboard },
                                timeoutMs = 2000
                            )
                            if (keyboardDismissed != null) {
                                logger.info("✅ Confirmed: Keyboard is gone!")
                            } else {
                                logger.debug("Keyboard dismissal: waiting for confirmation (may still be visible)")
                            }
                        } else {
                            logger.warn("⚠️  Could not dismiss keyboard, will try again if needed")
                        }
                    } else {
                        logger.warn("⚠️  Screenshot file doesn't exist: ${screenshot.absolutePath}")
                    }
                } else {
                    logger.debug("Keyboard not detected in state (hasKeyboard=${postActionState?.hasKeyboard}, screenshot=${postActionState?.screenshot?.exists()})")
                }
            }
            
            // Process accumulated feedback (human: process observations)
            if (feedbackObservations.isNotEmpty()) {
                logger.info("🔄 Noticed ${feedbackObservations.size} changes on screen")
            }
            processFeedback(feedbackObservations, action)
            
            return actionResult
        } catch (e: Exception) {
            logger.error("Error during action execution with continuous feedback: ${e.message}", e)
            // Still process any feedback we got
            processFeedback(feedbackObservations, action)
            throw e
        } finally {
            // Stop continuous monitoring (human: stop observing)
            stopContinuousMonitoring()
        }
    }
    
    /**
     * Start continuous monitoring for an action.
     */
    private suspend fun startContinuousMonitoring(action: HumanAction): Boolean {
        return try {
            logger.debug("Starting continuous monitoring for action: ${action::class.simpleName}")
            
            // Set action context in monitor
            screenMonitor.setCurrentAction(action.toString())
            
            // Get current goal for context
            val currentGoal = goalManager.getCurrentGoal()
            if (currentGoal != null) {
                screenMonitor.setCurrentGoal(currentGoal.description)
                logger.debug("Monitoring with goal context: ${currentGoal.description}")
            }
            
            // Start monitoring (if not already started)
            if (!screenMonitor.isMonitoringFlow.value) {
                screenMonitor.startMonitoring(coroutineScope)
            }
            
            // Set up feedback collection via StateManager listener
            val stateChangeListener: (ScreenState) -> Unit = { state ->
                registerStateObservation(state)
            }
            stateManager.addStateChangeListener(stateChangeListener)
            
            // Store listener for cleanup
            monitoringJob = coroutineScope.launch {
                // Keep job alive while monitoring
                while (_isActive.value) {
                    delay(100)
                }
                // Cleanup listener when done
                stateManager.removeStateChangeListener(stateChangeListener)
            }
            _isActive.value = true
            
            // Brief delay to ensure monitoring is active
            delay(100)
            
            logger.debug("Continuous monitoring started")
            true
        } catch (e: Exception) {
            logger.error("Failed to start continuous monitoring: ${e.message}", e)
            false
        }
    }
    
    
    /**
     * Process feedback observations and update goals.
     * 
     * **Human Process**: "Did my action work? What changed?"
     */
    private fun processFeedback(observations: List<ScreenState>, action: HumanAction) {
        if (observations.isEmpty()) {
            logger.debug("No feedback observations collected")
            return
        }
        
        logger.debug("Processing ${observations.size} feedback observations")
        
        // Get current goal
        val currentGoal = goalManager.getCurrentGoal()
        if (currentGoal == null) {
            logger.debug("No current goal to update from feedback")
            return
        }
        
        // Analyze feedback
        val latestState = observations.lastOrNull()
        if (latestState == null) {
            return
        }
        
        // Check for errors
        if (latestState.hasErrors && latestState.errorMessages.isNotEmpty()) {
            logger.warn("⚠️ Feedback detected errors: ${latestState.errorMessages.joinToString("; ")}")
            // Update goal state to BLOCKED if errors detected
            goalManager.updateGoalState(
                currentGoal.id,
                GoalManager.GoalState.BLOCKED
            )
            return
        }
        
        // Check for loading state
        if (latestState.isLoading) {
            logger.debug("⏳ Feedback detected loading state")
            // Goal is still in progress
            goalManager.updateGoalState(
                currentGoal.id,
                GoalManager.GoalState.IN_PROGRESS
            )
            return
        }
        
        // Check for screen changes (might indicate progress)
        val screenChanged = observations.size > 1 && 
            observations.last().screenName != observations.first().screenName
        
        if (screenChanged) {
            logger.info("🔄 Feedback detected screen change: ${observations.first().screenName} → ${latestState.screenName}")
            // Screen change might indicate progress toward goal
            // This is a simple heuristic - can be enhanced with goal-specific logic
            goalManager.updateGoalState(
                currentGoal.id,
                GoalManager.GoalState.IN_PROGRESS
            )
        }
        
        // Check if goal is achieved (goal-specific logic)
        val goalAchieved = checkGoalAchieved(currentGoal, latestState)
        if (goalAchieved) {
            logger.info("✅ Feedback indicates goal achieved: ${currentGoal.description}")
            goalManager.updateGoalState(
                currentGoal.id,
                GoalManager.GoalState.ACHIEVED
            )
        }
    }
    
    /**
     * Check if goal is achieved based on current state.
     * 
     * This is a simple heuristic - can be enhanced with goal-specific logic.
     */
    private fun checkGoalAchieved(goal: GoalManager.Goal, state: ScreenState): Boolean {
        // Simple heuristic: check if goal description matches screen name
        // This can be enhanced with more sophisticated goal matching
        val goalDescription = goal.description.lowercase()
        val screenName = state.screenName?.lowercase() ?: ""
        
        // Check for common goal patterns
        return when {
            goalDescription.contains("sign up") || goalDescription.contains("signup") -> {
                screenName.contains("mood") || screenName.contains("home") || 
                !screenName.contains("sign") && !screenName.contains("login")
            }
            goalDescription.contains("sign in") || goalDescription.contains("login") -> {
                screenName.contains("mood") || screenName.contains("home") || 
                !screenName.contains("sign") && !screenName.contains("login")
            }
            goalDescription.contains("mood") -> {
                screenName.contains("mood")
            }
            else -> false
        }
    }
    
    /**
     * Stop continuous monitoring.
     */
    private fun stopContinuousMonitoring() {
        logger.debug("Stopping continuous monitoring")
        _isActive.value = false
        monitoringJob?.cancel()
        monitoringJob = null
        // Don't stop ScreenMonitor itself (it might be used by other components)
        // Just clear our action context
        screenMonitor.setCurrentAction(null)
        screenMonitor.setCurrentGoal(null)
    }
    
    /**
     * Check if continuous loop is active.
     */
    fun isActive(): Boolean = _isActive.value
    
    /**
     * Register a state observation (called by StateManager or ScreenMonitor).
     */
    fun registerStateObservation(state: ScreenState) {
        if (_isActive.value) {
            feedbackObservations.add(state)
            logger.debug("Registered state observation: ${state.screenName} (total: ${feedbackObservations.size})")
        }
    }
    
    /**
     * Execute tap with visual-first adaptive recovery strategies.
     * 
     * **Visual-First Principle**: All adaptation is based on what we see, not implementation details.
     */
    private suspend fun executeTapWithAdaptation(executor: ActionExecutor, action: HumanAction.Tap): ActionResult {
        // Strategy 1: Try direct tap
        val directResult = try {
            executor.executeTapWithAppium(action)
        } catch (e: Exception) {
            logger.debug("Direct tap failed: ${e.message}")
            null
        }
        if (directResult is ActionResult.Success) {
            return directResult
        }
        
        logger.info("💡 That didn't work, let me see what's blocking it...")
        
        // Strategy 2: Check visually if keyboard is blocking (VISUAL DETECTION)
        val screenshot = executor.captureScreenshot()
        val currentState = stateManager.getCurrentState()
        
        // NOTE: Keyboard dismissal is now handled in executeWithContinuousFeedback()
        // This old code block has been removed - keyboard blocking is handled
        // by the visualAdaptiveRecovery passed to the constructor
        
        // Strategy 3: Wait and retry (for animations/loading)
        logger.info("💡 Waiting a moment and trying again...")
        Thread.sleep(500)
        val retryResult = try {
            executor.executeTapWithAppium(action)
        } catch (e: Exception) {
            null
        }
        if (retryResult is ActionResult.Success) {
            logger.info("✅ Worked after waiting!")
            return retryResult
        }
        
        // All strategies failed
        return directResult ?: ActionResult.Failure(
            action = action,
            error = "Could not tap ${action.target} - tried direct tap, keyboard dismissal, and retry",
            screenshot = screenshot
        )
    }
    
    /**
     * Execute type text with visual-first adaptive recovery strategies.
     * 
     * **Visual-First Principle**: All adaptation is based on what we see, not implementation details.
     */
    private suspend fun executeTypeTextWithAdaptation(executor: ActionExecutor, action: HumanAction.TypeText): ActionResult {
        // Strategy 1: Try direct type
        val directResult = try {
            executor.executeTypeTextWithAppium(action)
        } catch (e: Exception) {
            logger.debug("Direct type failed: ${e.message}")
            null
        }
        if (directResult is ActionResult.Success) {
            return directResult
        }
        
        logger.info("💡 That didn't work, let me see what's happening...")
        
        // Strategy 2: Check visually if keyboard is blocking or field is not visible
        val screenshot = executor.captureScreenshot()
        val currentState = stateManager.getCurrentState()
        
        // If keyboard is visible but we can't type, maybe we need to focus the field first
        if (currentState?.hasKeyboard == true) {
            logger.info("👁️  Keyboard is visible, but field might not be focused")
            // Try tapping the field first to focus it
            try {
                val focusAction = HumanAction.Tap(
                    target = action.target,
                    accessibilityId = action.accessibilityId,
                    description = "Focus ${action.target} for typing"
                )
                executor.executeTapWithAppium(focusAction)
                Thread.sleep(300)
                
                // Now try typing again
                val focusResult = try {
                    executor.executeTypeTextWithAppium(action)
                } catch (e: Exception) {
                    null
                }
                if (focusResult is ActionResult.Success) {
                    logger.info("✅ Worked after focusing the field!")
                    return focusResult
                }
            } catch (e: Exception) {
                logger.debug("Focus field failed: ${e.message}")
            }
        }
        
        // Strategy 3: Wait and retry (for animations/loading)
        logger.info("💡 Waiting a moment and trying again...")
        Thread.sleep(500)
        val retryResult = try {
            executor.executeTypeTextWithAppium(action)
        } catch (e: Exception) {
            null
        }
        if (retryResult is ActionResult.Success) {
            logger.info("✅ Worked after waiting!")
            return retryResult
        }
        
        // All strategies failed
        return directResult ?: ActionResult.Failure(
            action = action,
            error = "Could not type into ${action.target} - tried direct type, field focus, and retry",
            screenshot = screenshot
        )
    }
}

