package com.electricsheep.testautomation.monitoring

import com.electricsheep.testautomation.perception.AttentionManager
import io.appium.java_client.android.AndroidDriver
import org.openqa.selenium.OutputType
import org.slf4j.LoggerFactory
import java.io.File
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Continuously monitors the screen and reports state changes.
 * 
 * Runs in parallel with Action Executor, mimicking human observation behavior.
 * 
 * Responsibilities:
 * - Capture screenshots at regular intervals
 * - Detect UI state changes (loading, errors, screen transitions) using VISUAL ANALYSIS ONLY
 * - Report state to State Manager
 * - Provide real-time visual feedback
 * 
 * CRITICAL: This component uses VISUAL CUES (screenshots) for all state detection.
 * It does NOT use Appium's internal element queries or DOM structures.
 * All analysis is based on what can be "seen" in screenshots, not internal element properties.
 * 
 * See: .cursor/rules/visual-first-principle.mdc for the architectural principle.
 */
class ScreenMonitor(
    private val driver: AndroidDriver,
    private val stateManager: StateManager,
    private val screenshotDir: File,
    private val monitoringIntervalMs: Long = 1000, // Default: 1 second
    private val testStartTime: Long = System.currentTimeMillis(),
    private val attentionManager: AttentionManager? = null, // Optional attention manager
    private val screenEvaluator: ScreenEvaluator? = null // Optional screen evaluator for visual analysis
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var monitoringJob: Job? = null
    private var isMonitoring = false
    private var lastState: ScreenState? = null
    private val _isMonitoringFlow = MutableStateFlow(false)
    val isMonitoringFlow: StateFlow<Boolean> = _isMonitoringFlow.asStateFlow()
    private var currentAction: String? = null // Track current action for context
    private var currentIntent: String? = null // Track current intent/goal
    private var currentGoal: String? = null // Track current goal for attention
    
    /**
     * Start continuous monitoring in a parallel coroutine.
     */
    fun startMonitoring(scope: CoroutineScope) {
        if (isMonitoring) {
            logger.warn("Monitoring already started")
            return
        }
        
        isMonitoring = true
        _isMonitoringFlow.value = true
        logger.info("Starting screen monitoring (interval: ${monitoringIntervalMs}ms)")
        
        monitoringJob = scope.launch {
            try {
                while (isMonitoring) {
                    // Capture and analyze screen state
                    val currentState = captureAndAnalyzeState()
                    
                    // Only log meaningful state changes (errors, loading, screen names)
                    if (currentState.hasErrors && currentState.errorMessages.isNotEmpty()) {
                        logger.info("⚠️  Error on screen: ${currentState.errorMessages.first()}")
                    } else if (currentState.isLoading) {
                        logger.debug("⏳ Screen is loading...")
                    }
                    
                    // Report state change if detected
                    if (currentState.hasChangedFrom(lastState)) {
                        val fromScreen = lastState?.screenName ?: "unknown screen"
                        val toScreen = currentState.screenName ?: "unknown screen"
                        if (fromScreen != toScreen) {
                            logger.info("🔄 Screen changed: $fromScreen → $toScreen")
                        }
                        if (currentState.hasErrors && currentState.errorMessages.isNotEmpty()) {
                            logger.warn("⚠️ Monitor detected errors: ${currentState.errorMessages.joinToString("; ")}")
                        }
                        if (currentState.isLoading) {
                            logger.debug("⏳ Monitor detected loading state")
                        }
                        stateManager.onStateChanged(currentState, lastState)
                        lastState = currentState
                    }
                    
                    delay(monitoringIntervalMs)
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Expected when stopping - not an error
                logger.debug("Screen monitoring cancelled (normal shutdown)")
            } catch (e: Exception) {
                logger.error("Error in screen monitoring", e)
            } finally {
                _isMonitoringFlow.value = false
                logger.debug("Screen monitoring stopped")
            }
        }
    }
    
    /**
     * Update current action context (for linking screenshots to actions).
     */
    fun setCurrentAction(action: String?) {
        currentAction = action
        logger.debug("Monitor: Current action context set to: $action")
    }
    
    /**
     * Update current intent context (for linking screenshots to intents).
     */
    fun setCurrentIntent(intent: String?) {
        currentIntent = intent
        logger.debug("Monitor: Current intent context set to: $intent")
    }
    
    /**
     * Update current goal context (for attention-based focus).
     */
    fun setCurrentGoal(goal: String?) {
        currentGoal = goal
        logger.debug("Monitor: Current goal context set to: $goal")
    }
    
    /**
     * Stop monitoring.
     */
    fun stopMonitoring() {
        if (!isMonitoring) {
            return
        }
        
        logger.info("Stopping screen monitoring")
        isMonitoring = false
        monitoringJob?.cancel()
        monitoringJob = null
        _isMonitoringFlow.value = false
    }
    
    /**
     * Capture screenshot and analyze current state.
     */
    private suspend fun captureAndAnalyzeState(): ScreenState {
        return try {
            // Capture screenshot
            val screenshot = captureScreenshot()
            
            // Analyze state (basic detection for now, can be enhanced with AI)
            val state = analyzeState(screenshot)
            
            // Log state if it's meaningful
            if (state.screenName != null || state.isLoading || state.hasErrors) {
                logger.debug("Monitor detected: screen=${state.screenName ?: "unknown"}, loading=${state.isLoading}, errors=${state.hasErrors}")
            }
            
            state
        } catch (e: org.openqa.selenium.StaleElementReferenceException) {
            // Stale elements are expected during UI transitions - not an error
            logger.debug("Stale elements detected (UI is changing) - will retry on next cycle")
            ScreenState(
                screenName = null,
                isLoading = false,
                hasErrors = false,
                screenshot = null,
                testStartTime = testStartTime,
                currentAction = currentAction,
                currentIntent = currentIntent
            )
        } catch (e: org.openqa.selenium.WebDriverException) {
            // Driver session issues - likely shutting down
            if (isMonitoring) {
                logger.debug("Driver session issue during monitoring: ${e.message}")
            }
            ScreenState(
                screenName = null,
                isLoading = false,
                hasErrors = false,
                screenshot = null,
                testStartTime = testStartTime,
                currentAction = currentAction,
                currentIntent = currentIntent
            )
        } catch (e: kotlinx.coroutines.CancellationException) {
            // Expected when stopping monitoring
            throw e
        } catch (e: Exception) {
            logger.warn("Failed to capture/analyze state: ${e.message}")
            // Return minimal state on error
            ScreenState(
                screenName = null,
                isLoading = false,
                hasErrors = false,
                screenshot = null,
                testStartTime = testStartTime,
                currentAction = currentAction,
                currentIntent = currentIntent
            )
        }
    }
    
    /**
     * Capture screenshot from driver with context metadata.
     */
    private suspend fun captureScreenshot(): File? {
        return try {
            if (driver.sessionId == null) {
                logger.debug("Driver session is null, cannot capture screenshot")
                return null
            }
            
            val screenshot = driver.getScreenshotAs(OutputType.FILE)
            val timestamp = System.currentTimeMillis()
            val relativeTime = timestamp - testStartTime
            
            // Build filename with context
            val contextParts = mutableListOf<String>()
            if (currentAction != null) {
                contextParts.add("action_${currentAction?.replace(" ", "_")?.take(20)}")
            }
            if (currentIntent != null) {
                contextParts.add("intent_${currentIntent?.replace(" ", "_")?.take(20)}")
            }
            val contextSuffix = if (contextParts.isNotEmpty()) {
                "_${contextParts.joinToString("_")}"
            } else {
                ""
            }
            
            val outputFile = File(screenshotDir, "monitor_${relativeTime}ms${contextSuffix}_${timestamp}.png")
            screenshot.copyTo(outputFile, overwrite = true)
            
            logger.debug("Monitor screenshot: ${outputFile.name} (action: $currentAction, intent: $currentIntent)")
            outputFile
        } catch (e: Exception) {
            logger.debug("Failed to capture screenshot: ${e.message}")
            null
        }
    }
    
    /**
     * Analyze screen state from screenshot using VISUAL ANALYSIS ONLY.
     * 
     * CRITICAL: This method uses visual cues (screenshots) only, NOT Appium element queries.
     * All state detection is based on what can be "seen" in the screenshot.
     * 
     * For detailed visual analysis, the ScreenEvaluator should be used.
     * This method provides basic state tracking based on screenshot availability.
     */
    private suspend fun analyzeState(screenshot: File?): ScreenState {
        return withContext(Dispatchers.IO) {
            try {
                if (screenshot == null || !screenshot.exists()) {
                    logger.debug("No screenshot available for visual analysis")
                    return@withContext ScreenState(
                        screenName = null,
                        isLoading = false,
                        hasErrors = false,
                        screenshot = null,
                        testStartTime = testStartTime,
                        currentAction = currentAction,
                        currentIntent = currentIntent
                    )
                }
                
<<<<<<< HEAD
                // Visual analysis: Use ScreenEvaluator if available for detailed visual analysis
                val evaluation = if (screenEvaluator != null) {
                    try {
                        screenEvaluator.evaluateScreen(screenshot, expectedState = currentIntent)
                    } catch (e: Exception) {
                        logger.debug("Screen evaluation failed: ${e.message}")
                        null
                    }
                } else {
                    null
                }
                
                // Extract state from evaluation (visual-first: what we "see" on screen)
                val isLoading = evaluation?.observations?.any { 
                    it.type == ScreenObservation.ObservationType.LOADING_INDICATOR 
                } ?: false
                val hasErrors = evaluation?.observations?.any { 
                    it.type == ScreenObservation.ObservationType.ERROR 
                } ?: false
                val errorMessages = evaluation?.observations
                    ?.filter { it.type == ScreenObservation.ObservationType.ERROR }
                    ?.map { it.message } ?: emptyList()
                val visibleElements = evaluation?.observations
                    ?.filter { it.element != null }
                    ?.mapNotNull { it.element } ?: emptyList()
                
                // Extract screen name from observations (look for screen indicators)
                val screenName = evaluation?.observations
                    ?.firstOrNull { it.type == ScreenObservation.ObservationType.UNEXPECTED_STATE }
                    ?.message
                    ?.substringAfter("but detected: ")
                    ?.substringBefore(",")
                    ?: null
                
                // Extract keyboard and blocking elements from evaluation
                val hasKeyboard = evaluation?.hasKeyboard ?: false
                val blockingElements = evaluation?.blockingElements ?: emptyList()
                
                ScreenState(
                    screenName = screenName,
                    isLoading = isLoading,
                    hasErrors = hasErrors,
                    errorMessages = errorMessages,
                    visibleElements = visibleElements,
                    screenshot = screenshot,
                    testStartTime = testStartTime,
                    currentAction = currentAction,
                    currentIntent = currentIntent,
                    hasKeyboard = hasKeyboard,
                    blockingElements = blockingElements
                )
            } catch (e: Exception) {
                logger.warn("Error analyzing state: ${e.message}")
                ScreenState(
                    screenName = null,
                    isLoading = false,
                    hasErrors = false,
                    screenshot = screenshot,
                    testStartTime = testStartTime,
                    currentAction = currentAction,
                    currentIntent = currentIntent
                )
            }
        }
    }
    
    /**
     * Pause monitoring temporarily (e.g., during action execution).
     */
    fun pauseMonitoring() {
        // Could implement pause/resume if needed
        // For now, monitoring continues but can be filtered
    }
    
    /**
     * Resume monitoring.
     */
    fun resumeMonitoring() {
        // Could implement pause/resume if needed
    }
}

