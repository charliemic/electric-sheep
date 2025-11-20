package com.electricsheep.testautomation

import com.electricsheep.testautomation.actions.ActionExecutor
import com.electricsheep.testautomation.planner.TaskPlanner
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Main entry point for test automation.
 * 
 * Usage:
 *   java -jar test-automation.jar --task "Sign up and add mood value" --context "tech_novice persona"
 */
fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("Main")
    
    // Parse arguments
    fun getArg(key: String, default: String? = null): String? {
        val index = args.indexOf("--$key")
        return if (index >= 0 && index < args.size - 1) {
            args[index + 1]
        } else {
            default
        }
    }
    
    val task = getArg("task") ?: "Sign up and add mood value"
    val context = getArg("context")
    val deviceName = getArg("device") ?: "emulator-5554"
    val appiumUrl = getArg("appium-url") ?: "http://localhost:4723"
    val aiApiKey = getArg("ai-api-key") ?: System.getenv("OPENAI_API_KEY")
    
    logger.info("Starting test automation")
    logger.info("  Task: $task")
    logger.info("  Context: ${context ?: "none"}")
    logger.info("  Device: $deviceName")
    
    // Setup
    val screenshotDir = File("test-results/screenshots").apply { mkdirs() }
    val driverManager = AppiumDriverManager()
    val driver = driverManager.createAndroidDriver(
        appiumServerUrl = appiumUrl,
        deviceName = deviceName
    )
    
    try {
        // Create executor and planner
        val actionExecutor = ActionExecutor(driver, screenshotDir)
        val taskPlanner = TaskPlanner(
            actionExecutor = actionExecutor,
            aiApiKey = aiApiKey,
            aiModel = "gpt-4-vision-preview"
        )
        
        // Execute task
        val result = kotlinx.coroutines.runBlocking {
            taskPlanner.planAndExecute(
                task = task,
                context = context,
                maxIterations = 10
            )
        }
        
        // Report results
        when (result) {
            is com.electricsheep.testautomation.planner.TaskResult.Success -> {
                logger.info("✅ Task completed successfully!")
                logger.info("  Execution steps: ${result.executionHistory.size}")
            }
            is com.electricsheep.testautomation.planner.TaskResult.Failure -> {
                logger.error("❌ Task failed: ${result.error}")
                logger.error("  Execution steps: ${result.executionHistory.size}")
                System.exit(1)
            }
        }
    } finally {
        driverManager.quitDriver(driver)
    }
}

