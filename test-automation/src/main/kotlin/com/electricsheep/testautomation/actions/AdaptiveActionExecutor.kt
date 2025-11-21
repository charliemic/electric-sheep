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
 * Adaptive action executor that tries multiple strategies when actions fail.
 * 
 * **Human-like behavior**: When humans hit obstacles, they adapt:
 * 1. Try the obvious approach first
 * 2. If blocked, dismiss keyboard/overlays
 * 3. If still blocked, scroll to bring element into view
 * 4. If still blocked, try alternative element locators
 * 5. If still blocked, report the issue
 * 
 * **Adaptive Strategies**:
 * - Keyboard dismissal (if keyboard is blocking)
 * - Scroll to element (if element is off-screen)
 * - Alternative locators (if primary locator fails)
 * - Retry with delay (if element appears after animation)
 */
class AdaptiveActionExecutor(
    private val baseExecutor: ActionExecutor,
    private val driver: AndroidDriver
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val wait = WebDriverWait(driver, Duration.ofSeconds(5))
    
    /**
     * Execute tap with adaptive recovery strategies.
     */
    suspend fun executeTapWithAdaptation(action: HumanAction.Tap): ActionResult {
        logger.info("Trying to tap: ${action.target}")
        
        // Strategy 1: Try direct tap
        val directResult = tryDirectTap(action)
        if (directResult is ActionResult.Success) {
            return directResult
        }
        
        logger.info("Direct tap failed, trying adaptive strategies...")
        
        // Strategy 2: Dismiss keyboard and retry
        logger.info("💡 Dismissing keyboard and retrying...")
        dismissKeyboard()
        val keyboardDismissedResult = tryDirectTap(action)
        if (keyboardDismissedResult is ActionResult.Success) {
            return keyboardDismissedResult
        }
        
        // Strategy 3: Scroll to element and retry
        logger.info("💡 Scrolling to find element and retrying...")
        scrollToElement(action)
        val scrollResult = tryDirectTap(action)
        if (scrollResult is ActionResult.Success) {
            return scrollResult
        }
        
        // Strategy 4: Try alternative locators
        logger.info("💡 Trying alternative ways to find element...")
        val alternativeResult = tryAlternativeLocators(action)
        if (alternativeResult is ActionResult.Success) {
            return alternativeResult
        }
        
        // Strategy 5: Wait a bit and retry (for animations)
        logger.info("💡 Waiting for UI to settle and retrying...")
        Thread.sleep(500)
        val retryResult = tryDirectTap(action)
        if (retryResult is ActionResult.Success) {
            return retryResult
        }
        
        // All strategies failed
        logger.warn("❌ All adaptive strategies failed for: ${action.target}")
        return ActionResult.Failure(
            action = action,
            error = "Could not tap ${action.target} after trying: direct tap, keyboard dismissal, scroll, alternative locators, and retry",
            screenshot = baseExecutor.captureScreenshot()
        )
    }
    
    /**
     * Execute type text with adaptive recovery strategies.
     */
    suspend fun executeTypeTextWithAdaptation(action: HumanAction.TypeText): ActionResult {
        logger.info("Trying to type into: ${action.target}")
        
        // Strategy 1: Try direct type
        val directResult = tryDirectTypeText(action)
        if (directResult is ActionResult.Success) {
            return directResult
        }
        
        logger.info("Direct type failed, trying adaptive strategies...")
        
        // Strategy 2: Dismiss any existing keyboard, then focus field
        logger.info("💡 Dismissing keyboard and focusing field...")
        dismissKeyboard()
        Thread.sleep(300)
        val keyboardDismissedResult = tryDirectTypeText(action)
        if (keyboardDismissedResult is ActionResult.Success) {
            return keyboardDismissedResult
        }
        
        // Strategy 3: Scroll to field and retry
        logger.info("💡 Scrolling to find field and retrying...")
        scrollToTextField(action)
        val scrollResult = tryDirectTypeText(action)
        if (scrollResult is ActionResult.Success) {
            return scrollResult
        }
        
        // Strategy 4: Try alternative locators
        logger.info("💡 Trying alternative ways to find field...")
        val alternativeResult = tryAlternativeTypeTextLocators(action)
        if (alternativeResult is ActionResult.Success) {
            return alternativeResult
        }
        
        // All strategies failed
        logger.warn("❌ All adaptive strategies failed for typing into: ${action.target}")
        return ActionResult.Failure(
            action = action,
            error = "Could not type into ${action.target} after trying: direct type, keyboard dismissal, scroll, and alternative locators",
            screenshot = baseExecutor.captureScreenshot()
        )
    }
    
    /**
     * Try direct tap without adaptation.
     */
    private fun tryDirectTap(action: HumanAction.Tap): ActionResult {
        return try {
            baseExecutor.executeTapWithAppium(action)
        } catch (e: Exception) {
            ActionResult.Failure(
                action = action,
                error = "Direct tap failed: ${e.message}",
                screenshot = baseExecutor.captureScreenshot()
            )
        }
    }
    
    /**
     * Try direct type text without adaptation.
     */
    private suspend fun tryDirectTypeText(action: HumanAction.TypeText): ActionResult {
        return try {
            baseExecutor.executeTypeTextWithAppium(action)
        } catch (e: Exception) {
            ActionResult.Failure(
                action = action,
                error = "Direct type failed: ${e.message}",
                screenshot = baseExecutor.captureScreenshot()
            )
        }
    }
    
    /**
     * Dismiss keyboard if present.
     * NOTE: This is deprecated in favor of VisualAdaptiveRecovery which uses visual detection.
     */
    private fun dismissKeyboard() {
        try {
            // Try using Appium's hideKeyboard method (visual-first approach preferred)
            driver.hideKeyboard()
            Thread.sleep(200)
            logger.debug("Keyboard dismissed via hideKeyboard")
        } catch (e: Exception) {
            logger.debug("Could not dismiss keyboard: ${e.message}")
        }
    }
    
    /**
     * Scroll to bring element into view.
     */
    private fun scrollToElement(action: HumanAction.Tap) {
        try {
            // Try to find element first
            val element = findElementSafely(action.accessibilityId, action.text, action.target)
            if (element != null) {
                // Scroll element into view
                val location = element.location
                val size = driver.manage().window().size
                
                // If element is below visible area, scroll up
                if (location.y > size.height * 0.8) {
                    val startY = (size.height * 0.7).toInt()
                    val endY = (size.height * 0.3).toInt()
                    scrollVertically(startY, endY)
                }
                // If element is above visible area, scroll down
                else if (location.y < size.height * 0.2) {
                    val startY = (size.height * 0.3).toInt()
                    val endY = (size.height * 0.7).toInt()
                    scrollVertically(startY, endY)
                }
                
                Thread.sleep(300) // Allow scroll to complete
            }
        } catch (e: Exception) {
            logger.debug("Could not scroll to element: ${e.message}")
        }
    }
    
    /**
     * Scroll to bring text field into view.
     */
    private fun scrollToTextField(action: HumanAction.TypeText) {
        try {
            val element = findElementSafely(action.accessibilityId, null, action.target)
            if (element != null) {
                val location = element.location
                val size = driver.manage().window().size
                
                // Scroll to center the field
                if (location.y > size.height * 0.6 || location.y < size.height * 0.4) {
                    val startY = if (location.y > size.height / 2) {
                        (size.height * 0.7).toInt()
                    } else {
                        (size.height * 0.3).toInt()
                    }
                    val endY = (size.height * 0.5).toInt()
                    scrollVertically(startY, endY)
                    Thread.sleep(300)
                }
            }
        } catch (e: Exception) {
            logger.debug("Could not scroll to text field: ${e.message}")
        }
    }
    
    /**
     * Scroll vertically.
     */
    private fun scrollVertically(startY: Int, endY: Int) {
        val size = driver.manage().window().size
        val centerX = size.width / 2
        
        val touchAction = TouchAction(driver)
        touchAction.press(PointOption.point(centerX, startY))
            .waitAction(WaitOptions.waitOptions(Duration.ofMillis(200)))
            .moveTo(PointOption.point(centerX, endY))
            .release()
            .perform()
    }
    
    /**
     * Try alternative locators for tap action.
     */
    private fun tryAlternativeLocators(action: HumanAction.Tap): ActionResult {
        return try {
            // Try finding by partial text match
            if (!action.target.isNullOrBlank()) {
                val elements = driver.findElements(
                    By.xpath("//*[contains(@text, '${action.target}') or contains(@content-desc, '${action.target}')]")
                )
                val visibleElements = elements.filter { it.isDisplayed }
                if (visibleElements.isNotEmpty()) {
                    visibleElements.first().click()
                    return ActionResult.Success(
                        action = action,
                        message = "Successfully tapped ${action.target} using alternative locator",
                        screenshot = baseExecutor.captureScreenshot()
                    )
                }
            }
            
            ActionResult.Failure(
                action = action,
                error = "Alternative locators did not find element",
                screenshot = baseExecutor.captureScreenshot()
            )
        } catch (e: Exception) {
            ActionResult.Failure(
                action = action,
                error = "Alternative locators failed: ${e.message}",
                screenshot = baseExecutor.captureScreenshot()
            )
        }
    }
    
    /**
     * Try alternative locators for type text action.
     */
    private suspend fun tryAlternativeTypeTextLocators(action: HumanAction.TypeText): ActionResult {
        return try {
            // Extract field name from target (e.g., "Email field" -> "Email")
            val fieldName = action.target.replace(" field", "", ignoreCase = true)
                .replace("Field", "", ignoreCase = true)
                .trim()
            
            // Try finding by field name
            val elements = driver.findElements(
                By.xpath("//*[contains(@content-desc, '$fieldName') or contains(@hint, '$fieldName')]")
            )
            val visibleElements = elements.filter { it.isDisplayed }
            if (visibleElements.isNotEmpty()) {
                val element = visibleElements.first()
                element.click()
                Thread.sleep(300)
                element.clear()
                element.sendKeys(action.text)
                return ActionResult.Success(
                    action = action,
                    message = "Successfully typed into ${action.target} using alternative locator",
                    screenshot = baseExecutor.captureScreenshot()
                )
            }
            
            ActionResult.Failure(
                action = action,
                error = "Alternative locators did not find field",
                screenshot = baseExecutor.captureScreenshot()
            )
        } catch (e: Exception) {
            ActionResult.Failure(
                action = action,
                error = "Alternative locators failed: ${e.message}",
                screenshot = baseExecutor.captureScreenshot()
            )
        }
    }
    
    /**
     * Find element safely (returns null if not found).
     */
    private fun findElementSafely(
        accessibilityId: String?,
        text: String?,
        targetDescription: String
    ): WebElement? {
        return try {
            if (!accessibilityId.isNullOrBlank()) {
                wait.until(ExpectedConditions.presenceOfElementLocated(
                    AppiumBy.accessibilityId(accessibilityId)
                ))
            } else if (!text.isNullOrBlank()) {
                wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[@text='$text']")
                ))
            } else {
                wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[contains(@content-desc, '$targetDescription')]")
                ))
            }
        } catch (e: Exception) {
            null
        }
    }
}

