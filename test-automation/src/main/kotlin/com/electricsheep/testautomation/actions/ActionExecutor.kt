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
 */
class ActionExecutor(
    private val driver: AndroidDriver,
    private val screenshotDir: File
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val wait = WebDriverWait(driver, Duration.ofSeconds(30))
    
    /**
     * Execute a human action.
     * Returns ActionResult with success status and any relevant information.
     */
    suspend fun execute(action: HumanAction): ActionResult {
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
    
    private fun executeTap(action: HumanAction.Tap): ActionResult {
        logger.info("Tapping: ${action.target} (${action.description ?: ""})")
        
        val element = findElement(action.accessibilityId, action.text, action.target)
        element.click()
        
        return ActionResult.Success(
            action = action,
            message = "Successfully tapped ${action.target}",
            screenshot = captureScreenshot()
        )
    }
    
    private fun executeTypeText(action: HumanAction.TypeText): ActionResult {
        logger.info("Typing into ${action.target}: ${action.text.take(10)}...")
        
        val element = findElement(action.accessibilityId, null, action.target)
        
        // Click element first to focus it (required for Compose TextFields)
        element.click()
        Thread.sleep(300) // Brief wait for focus
        
        if (action.clearFirst) {
            // For Compose TextFields, select all and delete
            element.click()
            Thread.sleep(200)
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
    
    private fun executeSwipe(action: HumanAction.Swipe): ActionResult {
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
    
    private fun executeWaitFor(action: HumanAction.WaitFor): ActionResult {
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
                // Wait for screen title or key element to appear
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
            is WaitCondition.LoadingComplete -> {
                // Wait for loading indicators to disappear
                Thread.sleep(2000) // Give UI time to update
                val loadingElements = driver.findElements(
                    AppiumBy.xpath("//*[contains(@content-desc, 'loading') or contains(@text, 'Loading')]")
                )
                loadingElements.isEmpty()
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
    
    private fun executeNavigateBack(): ActionResult {
        logger.info("Navigating back")
        driver.navigate().back()
        
        return ActionResult.Success(
            action = HumanAction.NavigateBack,
            message = "Navigated back",
            screenshot = captureScreenshot()
        )
    }
    
    private fun executeCaptureState(): ActionResult {
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
    
    private fun executeVerify(action: HumanAction.Verify): ActionResult {
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
                try {
                    driver.findElement(By.xpath("//*[contains(@text, '${condition.text}')]"))
                    true
                } catch (e: Exception) {
                    false
                }
            }
            is VerifyCondition.ScreenIs -> {
                try {
                    val elements = driver.findElements(AppiumBy.accessibilityId(condition.screenName))
                    elements.isNotEmpty()
                } catch (e: Exception) {
                    false
                }
            }
            is VerifyCondition.Authenticated -> {
                // Check for authenticated state indicators
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
    
    private fun captureScreenshot(): File {
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

