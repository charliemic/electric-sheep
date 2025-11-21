package com.electricsheep.testautomation

import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.options.UiAutomator2Options
import org.openqa.selenium.remote.DesiredCapabilities
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.Duration

/**
 * Manages Appium driver lifecycle with best practices.
 */
class AppiumDriverManager {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * Create and configure Android driver with best practices.
     */
    fun createAndroidDriver(
        appiumServerUrl: String = "http://localhost:4723",
        appPackage: String = "com.electricsheep.app",
        appActivity: String = ".MainActivity",
        deviceName: String = "emulator-5554",
        platformVersion: String? = null // Auto-detect from device
    ): AndroidDriver {
        logger.info("Creating Android driver...")
        logger.info("  Appium Server: $appiumServerUrl")
        logger.info("  App Package: $appPackage")
        logger.info("  Device: $deviceName")
        
        val optionsBuilder = UiAutomator2Options()
            .setAppPackage(appPackage)
            .setAppActivity(appActivity)
            .setDeviceName(deviceName)
            .setAutomationName("UiAutomator2")
            .setNoReset(false) // Reset app state for clean test
            .setFullReset(false) // Don't reinstall app
            .setNewCommandTimeout(Duration.ofSeconds(300)) // 5 minutes
            .setUiautomator2ServerLaunchTimeout(Duration.ofSeconds(60))
            .setUiautomator2ServerInstallTimeout(Duration.ofSeconds(60))
        
        // Only set platform version if specified (otherwise auto-detect)
        val options = if (platformVersion != null) {
            optionsBuilder.setPlatformVersion(platformVersion)
        } else {
            optionsBuilder
        }
        
        val driver = AndroidDriver(URL(appiumServerUrl), options)
        
        // Set implicit wait (best practice: use explicit waits instead, but set a reasonable default)
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5))
        
        logger.info("Android driver created successfully")
        return driver
    }
    
    /**
     * Quit driver and cleanup.
     */
    fun quitDriver(driver: AndroidDriver) {
        try {
            logger.info("Quitting driver...")
            
            // Check if driver session is still active
            try {
                driver.sessionId // This will throw if session is already closed
                driver.quit()
                logger.info("Driver quit successfully")
            } catch (e: org.openqa.selenium.WebDriverException) {
                // Session already closed or invalid
                logger.debug("Driver session already closed: ${e.message}")
                logger.info("Driver already closed, skipping quit")
            } catch (e: Exception) {
                // Try to quit anyway, but don't fail if it doesn't work
                try {
                    driver.quit()
                    logger.info("Driver quit after session check")
                } catch (e2: Exception) {
                    logger.debug("Driver quit failed (session may be closed): ${e2.message}")
                }
            }
        } catch (e: Exception) {
            logger.warn("Error during driver cleanup: ${e.message}")
            // Don't throw - cleanup should be best-effort
        }
    }
}

