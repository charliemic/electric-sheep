package com.electricsheep.testautomation.actions

import io.appium.java_client.AppiumBy
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.TouchAction
import io.appium.java_client.touch.WaitOptions
import io.appium.java_client.touch.offset.PointOption
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Duration

/**
 * Executes human-like actions using Appium.
 * This layer abstracts away Appium internals - the planner doesn't need to know
 * about Appium's API, just human tasks.
 * 
 * **Visual-First Enhancement**: Can use visual detection for element finding
 * (like human visual search) instead of Appium element queries.
 * 
 * **Continuous Loop Enhancement**: Can use continuous feedback during actions
 * (like human continuous perception-action loop).
 */
class ActionExecutor(
    private val driver: AndroidDriver,
    private val screenshotDir: File,
    private val visualActionExecutor: com.electricsheep.testautomation.actions.visual.VisualActionExecutor? = null,
    private val continuousLoop: ContinuousInteractionLoop? = null,
    private val stateManager: com.electricsheep.testautomation.monitoring.StateManager? = null,
    private val screenEvaluator: com.electricsheep.testautomation.monitoring.ScreenEvaluator? = null,
    private val textDetector: com.electricsheep.testautomation.vision.TextDetector? = null
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val wait = WebDriverWait(driver, Duration.ofSeconds(30))
    
    /**
     * Use visual-first actions when available, fallback to Appium queries.
     */
    private val useVisualActions: Boolean = visualActionExecutor != null
    
    /**
     * Use continuous feedback loop when available.
     */
    private val useContinuousLoop: Boolean = continuousLoop != null
    
    /**
     * Use visual detection for state queries (visual-first principle).
     */
    private val useVisualDetection: Boolean = stateManager != null && screenEvaluator != null && textDetector != null
    
    /**
     * Execute a human action.
     * Returns ActionResult with success status and any relevant information.
     * 
     * **Continuous Loop**: If enabled, executes action with continuous feedback
     * (human-like: continuous perception ←→ action ←→ feedback).
     */
    suspend fun execute(action: HumanAction): ActionResult {
        // Use continuous loop if available (human-like continuous feedback)
        if (useContinuousLoop && shouldUseContinuousLoop(action)) {
            return continuousLoop!!.executeWithContinuousFeedback(action)
        }
        
        // Fallback to standard execution
        return try {
            when (action) {
                is HumanAction.Tap -> executeTap(action)
                is HumanAction.TypeText -> executeTypeText(action)
                is HumanAction.Swipe -> executeSwipe(action)
                is HumanAction.WaitFor -> executeWaitFor(action)
                is HumanAction.NavigateBack -> executeNavigateBack()
                is HumanAction.CaptureState -> executeCaptureState()
                is HumanAction.Verify -> executeVerify(action)
            }
        } catch (e: Exception) {
            logger.error("Failed to execute action: ${action::class.simpleName}", e)
            ActionResult.Failure(
                action = action,
                error = e.message ?: "Unknown error",
                screenshot = captureScreenshot()
            )
        }
    }
    
    /**
     * Determine if action should use continuous loop.
     */
    private fun shouldUseContinuousLoop(action: HumanAction): Boolean {
        // Use continuous loop for interactive actions (tap, type, swipe)
        // Skip for non-interactive actions (capture, verify, wait)
        return when (action) {
            is HumanAction.Tap -> true
            is HumanAction.TypeText -> true
            is HumanAction.Swipe -> true
            is HumanAction.NavigateBack -> true
            else -> false // Capture, Verify, WaitFor don't need continuous feedback
        }
    }
    
    private suspend fun executeTap(action: HumanAction.Tap): ActionResult {
        logger.info("Tapping: ${action.target} (${action.description ?: ""})")
        
        // Use visual-first approach if available (human-like: visual search → tap)
        if (useVisualActions && action.text != null) {
            try {
                val visualResult = visualActionExecutor!!.tapButton(action.text)
                when (visualResult) {
                    is com.electricsheep.testautomation.actions.visual.VisualActionExecutor.VisualActionResult.Success -> {
                        return ActionResult.Success(
                            action = action,
                            message = visualResult.message,
                            screenshot = visualResult.screenshot
                        )
                    }
                    is com.electricsheep.testautomation.actions.visual.VisualActionExecutor.VisualActionResult.Failure -> {
                        logger.warn("Visual tap failed: ${visualResult.message}, falling back to Appium")
                        // Fallback to Appium - continue below
                    }
                }
            } catch (e: Exception) {
                logger.warn("Visual tap error: ${e.message}, falling back to Appium", e)
                // Fallback to Appium - continue below
            }
        }
        
        // Fallback to Appium element queries
        return executeTapWithAppium(action)
    }
    
    /**
     * Execute tap using Appium element queries (fallback).
     * Made internal so ContinuousInteractionLoop can call it directly.
     */
    internal fun executeTapWithAppium(action: HumanAction.Tap): ActionResult {
        val element = findElement(action.accessibilityId, action.text, action.target)
        element.click()
        
        return ActionResult.Success(
            action = action,
            message = "Successfully tapped ${action.target}",
            screenshot = captureScreenshot()
        )
    }
    
    private suspend fun executeTypeText(action: HumanAction.TypeText): ActionResult {
        logger.info("Typing into ${action.target}: ${action.text.take(10)}...")
        
        // Use visual-first approach if available (human-like: visual search → type)
        if (useVisualActions && action.target.contains("field", ignoreCase = true)) {
            try {
                // Extract field label from target (e.g., "Email field" → "Email")
                val fieldLabel = action.target.replace(" field", "", ignoreCase = true)
                    .replace("Field", "", ignoreCase = true)
                    .trim()
                
                val visualResult = visualActionExecutor!!.typeText(action.text, fieldLabel)
                when (visualResult) {
                    is com.electricsheep.testautomation.actions.visual.VisualActionExecutor.VisualActionResult.Success -> {
                        return ActionResult.Success(
                            action = action,
                            message = visualResult.message,
                            screenshot = visualResult.screenshot
                        )
                    }
                    is com.electricsheep.testautomation.actions.visual.VisualActionExecutor.VisualActionResult.Failure -> {
                        logger.warn("Visual type failed: ${visualResult.message}, falling back to Appium")
                        // Fallback to Appium - continue below
                    }
                }
            } catch (e: Exception) {
                logger.warn("Visual type error: ${e.message}, falling back to Appium", e)
                // Fallback to Appium - continue below
            }
        }
        
        // Fallback to Appium element queries
        return executeTypeTextWithAppium(action)
    }
    
    /**
     * Execute type text using Appium element queries (fallback).
     * Made internal so ContinuousInteractionLoop can call it directly.
     */
    internal suspend fun executeTypeTextWithAppium(action: HumanAction.TypeText): ActionResult {
        val element = findElement(action.accessibilityId, null, action.target)
        
        // Click element first to focus it (required for Compose TextFields)
        element.click()
        // Event-driven wait: Wait for element to be focused/enabled (visual-first)
        if (useVisualDetection) {
            kotlinx.coroutines.runBlocking {
                stateManager!!.waitForState(
                    predicate = { state -> 
                        // Wait for keyboard to appear (indicates field is focused)
                        state.hasKeyboard || !state.isLoading
                    },
                    timeoutMs = 1000
                )
            }
        } else {
            // Fallback: Use WebDriverWait (event-driven, not fixed delay)
            wait.until(ExpectedConditions.elementToBeClickable(element))
        }
        
        if (action.clearFirst) {
            // For Compose TextFields, select all and delete
            element.click()
            // Event-driven wait: Brief wait for clear operation
            if (useVisualDetection) {
                kotlinx.coroutines.runBlocking {
                    kotlinx.coroutines.delay(100) // Minimal delay for clear operation
                }
            } else {
                wait.until { element.isEnabled }
            }
            // Use ADB input method as fallback for Compose
            try {
                element.clear()
            } catch (e: Exception) {
                logger.debug("Clear failed, using ADB input method")
            }
        }
        
        // Try sendKeys first, fallback to ADB if it fails
        try {
            element.sendKeys(action.text)
        } catch (e: Exception) {
            logger.debug("sendKeys failed, using ADB input: ${e.message}")
            // Fallback: Use ADB input text (works better with Compose)
            // ADB input text requires spaces to be %s, but @ and other chars work fine
            val deviceId = driver.capabilities.getCapability("deviceUDID") as? String ?: "emulator-5554"
            val escapedText = action.text.replace(" ", "%s")
            val adbCommand = arrayOf("adb", "-s", deviceId, "shell", "input", "text", escapedText)
            Runtime.getRuntime().exec(adbCommand).waitFor()
        }
        
        return ActionResult.Success(
            action = action,
            message = "Successfully typed into ${action.target}",
            screenshot = captureScreenshot()
        )
    }
    
    internal fun executeSwipe(action: HumanAction.Swipe): ActionResult {
        logger.info("Swiping ${action.direction}: ${action.description ?: ""}")
        
        val size = driver.manage().window().size
        val startX = size.width / 2
        val startY = size.height / 2
        val endX: Int
        val endY: Int
        
        when (action.direction) {
            SwipeDirection.UP -> {
                endX = startX
                endY = (size.height * 0.2).toInt()
            }
            SwipeDirection.DOWN -> {
                endX = startX
                endY = (size.height * 0.8).toInt()
            }
            SwipeDirection.LEFT -> {
                endX = (size.width * 0.2).toInt()
                endY = startY
            }
            SwipeDirection.RIGHT -> {
                endX = (size.width * 0.8).toInt()
                endY = startY
            }
        }
        
        // Use TouchAction for swipe (Appium Android)
        val touchAction = TouchAction(driver)
        touchAction.press(PointOption.point(startX, startY))
            .waitAction(WaitOptions.waitOptions(Duration.ofMillis(100)))
            .moveTo(PointOption.point(endX, endY))
            .release()
            .perform()
        
        return ActionResult.Success(
            action = action,
            message = "Successfully swiped ${action.direction}",
            screenshot = captureScreenshot()
        )
    }
    
    internal suspend fun executeWaitFor(action: HumanAction.WaitFor): ActionResult {
        logger.info("Waiting for: ${action.condition} (timeout: ${action.timeoutSeconds}s)")
        
        val waitCondition = WebDriverWait(driver, Duration.ofSeconds(action.timeoutSeconds.toLong()))
        
        val success = when (val condition = action.condition) {
            is WaitCondition.ElementVisible -> {
                try {
                    val element = findElement(condition.accessibilityId, null, condition.target)
                    waitCondition.until(ExpectedConditions.visibilityOf(element))
                    true
                } catch (e: Exception) {
                    false
                }
            }
            is WaitCondition.ElementEnabled -> {
                try {
                    val element = findElement(condition.accessibilityId, null, condition.target)
                    waitCondition.until(ExpectedConditions.elementToBeClickable(element))
                    true
                } catch (e: Exception) {
                    false
                }
            }
            is WaitCondition.TextAppears -> {
                try {
                    waitCondition.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//*[contains(@text, '${condition.text}')]")
                    ))
                    true
                } catch (e: Exception) {
                    false
                }
            }
            is WaitCondition.ScreenChanged -> {
                // Visual-first: Use state manager to detect screen change
                if (useVisualDetection) {
                    try {
                        kotlinx.coroutines.runBlocking {
                            val result = stateManager!!.waitForState(
                                predicate = { state -> 
                                    state.screenName == condition.expectedScreen ||
                                    state.visibleElements.contains(condition.expectedScreen)
                                },
                                timeoutMs = action.timeoutSeconds * 1000L
                            )
                            result != null
                        }
                    } catch (e: Exception) {
                        logger.debug("Visual screen detection failed, falling back to Appium: ${e.message}")
                        // Fallback to Appium
                        try {
                            waitCondition.until {
                                val elements = driver.findElements(AppiumBy.accessibilityId(condition.expectedScreen))
                                elements.isNotEmpty()
                            }
                            true
                        } catch (e2: Exception) {
                            false
                        }
                    }
                } else {
                    // Fallback: Use Appium element queries
                    try {
                        waitCondition.until {
                            val elements = driver.findElements(AppiumBy.accessibilityId(condition.expectedScreen))
                            elements.isNotEmpty()
                        }
                        true
                    } catch (e: Exception) {
                        false
                    }
                }
            }
            is WaitCondition.LoadingComplete -> {
                // Visual-first: Use state manager to detect loading completion
                if (useVisualDetection) {
                    try {
                        kotlinx.coroutines.runBlocking {
                            val result = stateManager!!.waitForState(
                                predicate = { state -> !state.isLoading },
                                timeoutMs = action.timeoutSeconds * 1000L
                            )
                            result != null
                        }
                    } catch (e: Exception) {
                        logger.debug("Visual loading detection failed, falling back to Appium: ${e.message}")
                        // Fallback: Check for loading elements via Appium
                        val loadingElements = driver.findElements(
                            AppiumBy.xpath("//*[contains(@content-desc, 'loading') or contains(@text, 'Loading')]")
                        )
                        loadingElements.isEmpty()
                    }
                } else {
                    // Fallback: Use Appium element queries
                    waitCondition.until {
                        val loadingElements = driver.findElements(
                            AppiumBy.xpath("//*[contains(@content-desc, 'loading') or contains(@text, 'Loading')]")
                        )
                        loadingElements.isEmpty()
                    }
                    true
                }
            }
        }
        
        return if (success) {
            ActionResult.Success(
                action = action,
                message = "Wait condition met: ${action.condition}",
                screenshot = captureScreenshot()
            )
        } else {
            ActionResult.Failure(
                action = action,
                error = "Timeout waiting for: ${action.condition}",
                screenshot = captureScreenshot()
            )
        }
    }
    
    internal fun executeNavigateBack(): ActionResult {
        logger.info("Navigating back")
        driver.navigate().back()
        
        return ActionResult.Success(
            action = HumanAction.NavigateBack,
            message = "Navigated back",
            screenshot = captureScreenshot()
        )
    }
    
    internal suspend fun executeCaptureState(): ActionResult {
        logger.info("Capturing current state")
        val screenshot = captureScreenshot()
        
        // Also get UI hierarchy for better understanding
        val elements = driver.findElements(By.xpath("//*"))
        val elementInfo = elements.take(50).map { element ->
            mapOf(
                "tag" to element.tagName,
                "text" to (element.text.take(100)),
                "contentDesc" to (element.getAttribute("content-desc")?.take(100)),
                "resourceId" to element.getAttribute("resource-id"),
                "visible" to element.isDisplayed
            )
        }
        
        // Extract error messages (prominent ones)
        val errorMessages = elements
            .filter { it.isDisplayed }
            .mapNotNull { element ->
                val text = element.text
                val contentDesc = element.getAttribute("content-desc") ?: ""
                val combined = "$text $contentDesc"
                
                // Look for error indicators
                if (combined.contains("error", ignoreCase = true) ||
                    combined.contains("invalid", ignoreCase = true) ||
                    combined.contains("required", ignoreCase = true) ||
                    combined.contains("must", ignoreCase = true) ||
                    combined.contains("cannot", ignoreCase = true)) {
                    text.ifBlank { contentDesc }.take(200)
                } else {
                    null
                }
            }
            .distinct()
            .take(5)
        
        return ActionResult.Success(
            action = HumanAction.CaptureState,
            message = "State captured",
            screenshot = screenshot,
            additionalData = mapOf(
                "elements" to elementInfo,
                "errorMessages" to errorMessages
            )
        )
    }
    
    internal suspend fun executeVerify(action: HumanAction.Verify): ActionResult {
        logger.info("Verifying: ${action.condition}")
        
        val success = when (val condition = action.condition) {
            is VerifyCondition.ElementPresent -> {
                try {
                    findElement(condition.accessibilityId, null, condition.target)
                    true
                } catch (e: Exception) {
                    false
                }
            }
            is VerifyCondition.TextPresent -> {
                // Visual-first: Use text detector to find text
                if (useVisualDetection && textDetector != null) {
                    try {
                        val screenshot = captureScreenshot()
                        val textResult = textDetector.extractText(screenshot)
                        textResult.fullText.contains(condition.text, ignoreCase = true)
                    } catch (e: Exception) {
                        logger.debug("Visual text verification failed, falling back to Appium: ${e.message}")
                        // Fallback to Appium
                        try {
                            driver.findElement(By.xpath("//*[contains(@text, '${condition.text}')]"))
                            true
                        } catch (e2: Exception) {
                            false
                        }
                    }
                } else {
                    // Fallback: Use Appium element queries
                    try {
                        driver.findElement(By.xpath("//*[contains(@text, '${condition.text}')]"))
                        true
                    } catch (e: Exception) {
                        false
                    }
                }
            }
            is VerifyCondition.ScreenIs -> {
                // Visual-first: Use state manager to check screen
                if (useVisualDetection) {
                    try {
                        kotlinx.coroutines.runBlocking {
                            val state = stateManager!!.getCurrentState()
                            state?.screenName == condition.screenName ||
                            state?.visibleElements?.contains(condition.screenName) == true
                        }
                    } catch (e: Exception) {
                        logger.debug("Visual screen verification failed, falling back to Appium: ${e.message}")
                        // Fallback to Appium
                        try {
                            val elements = driver.findElements(AppiumBy.accessibilityId(condition.screenName))
                            elements.isNotEmpty()
                        } catch (e2: Exception) {
                            false
                        }
                    }
                } else {
                    // Fallback: Use Appium element queries
                    try {
                        val elements = driver.findElements(AppiumBy.accessibilityId(condition.screenName))
                        elements.isNotEmpty()
                    } catch (e: Exception) {
                        false
                    }
                }
            }
            is VerifyCondition.Authenticated -> {
                // Visual-first: Use state manager and text detector
                if (useVisualDetection && textDetector != null) {
                    try {
                        kotlinx.coroutines.runBlocking {
                            val state = stateManager!!.getCurrentState()
                            val screenshot = captureScreenshot()
                            val textResult = textDetector.extractText(screenshot)
                            // Visual indicators of authentication (generic, not app-specific)
                            val authenticatedIndicators = listOf(
                                "sign out", "logout", "log out", "signed in", "profile", "settings",
                                "mood history", "mood management" // Context-specific but generic patterns
                            )
                            val unauthenticatedIndicators = listOf(
                                "sign in", "login", "create account", "sign up"
                            )
                            
                            val fullTextLower = textResult.fullText.lowercase()
                            val hasAuthIndicators = authenticatedIndicators.any { 
                                fullTextLower.contains(it, ignoreCase = true) 
                            }
                            val hasUnauthIndicators = unauthenticatedIndicators.any { 
                                fullTextLower.contains(it, ignoreCase = true) 
                            }
                            
                            // If we see auth indicators and no unauth indicators, we're authenticated
                            val isAuthenticated = hasAuthIndicators && !hasUnauthIndicators
                            
                            logger.info("   🔍 Visual auth check: authIndicators=$hasAuthIndicators, unauthIndicators=$hasUnauthIndicators, result=$isAuthenticated")
                            
                            isAuthenticated == condition.expected
                        }
                    } catch (e: Exception) {
                        logger.debug("Visual authentication verification failed, falling back to Appium: ${e.message}")
                        // Fallback to Appium
                        val authIndicators = listOf(
                            "Mood Management screen heading",
                            "signed in as",
                            "Sign out"
                        )
                        authIndicators.any { indicator ->
                            try {
                                driver.findElements(
                                    AppiumBy.xpath("//*[contains(@content-desc, '$indicator') or contains(@text, '$indicator')]")
                                ).isNotEmpty()
                            } catch (e2: Exception) {
                                false
                            }
                        } == condition.expected
                    }
                } else {
                    // Fallback: Use Appium element queries
                    val authIndicators = listOf(
                        "Mood Management screen heading",
                        "signed in as",
                        "Sign out"
                    )
                    authIndicators.any { indicator ->
                        try {
                            driver.findElements(
                                AppiumBy.xpath("//*[contains(@content-desc, '$indicator') or contains(@text, '$indicator')]")
                            ).isNotEmpty()
                        } catch (e: Exception) {
                            false
                        }
                    } == condition.expected
                }
            }
        }
        
        return if (success) {
            ActionResult.Success(
                action = action,
                message = "Verification passed: ${action.condition}",
                screenshot = captureScreenshot()
            )
        } else {
            ActionResult.Failure(
                action = action,
                error = "Verification failed: ${action.condition}",
                screenshot = captureScreenshot()
            )
        }
    }
    
    /**
     * Find element using best practices:
     * 1. Try accessibility ID first (most reliable)
     * 2. Fall back to text if available
     * 3. Use XPath only as last resort
     */
    private fun findElement(
        accessibilityId: String?,
        text: String?,
        targetDescription: String
    ): WebElement {
        // Priority 1: Accessibility ID (most reliable, deterministic)
        if (!accessibilityId.isNullOrBlank()) {
            try {
                return wait.until(ExpectedConditions.presenceOfElementLocated(
                    AppiumBy.accessibilityId(accessibilityId)
                ))
            } catch (e: Exception) {
                logger.debug("Accessibility ID not found: $accessibilityId, trying alternatives")
            }
        }
        
        // Priority 2: Text content (human-readable)
        if (!text.isNullOrBlank()) {
            try {
                return wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[@text='$text']")
                ))
            } catch (e: Exception) {
                logger.debug("Text not found: $text, trying XPath")
            }
        }
        
        // Priority 3: XPath with content description (last resort)
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(@content-desc, '$targetDescription')]")
            ))
        } catch (e: Exception) {
            throw Exception("Could not find element: $targetDescription (tried accessibility ID, text, and XPath)")
        }
    }
    
    internal fun captureScreenshot(): File {
        val screenshot = driver.getScreenshotAs(org.openqa.selenium.OutputType.FILE)
        val timestamp = System.currentTimeMillis()
        val outputFile = File(screenshotDir, "screenshot_$timestamp.png")
        screenshot.copyTo(outputFile, overwrite = true)
        return outputFile
    }
}

/**
 * Result of executing an action.
 */
sealed class ActionResult {
    abstract val action: HumanAction
    abstract val screenshot: File?
    
    data class Success(
        override val action: HumanAction,
        val message: String,
        override val screenshot: File?,
        val additionalData: Map<String, Any>? = null
    ) : ActionResult()
    
    data class Failure(
        override val action: HumanAction,
        val error: String,
        override val screenshot: File?
    ) : ActionResult()
}

