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
    
    // Acquire emulator using discovery service if not provided
    val deviceName = getArg("device") ?: run {
        try {
            val discoveryScript = File("../scripts/emulator-discovery.sh")
            if (!discoveryScript.exists()) {
                logger.warn("Discovery script not found, falling back to default device")
                "emulator-5554"
            } else {
                val process = ProcessBuilder("bash", discoveryScript.absolutePath, "acquire")
                    .directory(File(".."))
                    .redirectErrorStream(true)
                    .start()
                val result = process.inputStream.bufferedReader().readText().trim()
                val exitCode = process.waitFor()
                if (exitCode == 0 && result.isNotEmpty()) {
                    logger.info("Acquired emulator via discovery service: $result")
                    result
                } else {
                    logger.warn("Discovery service failed, falling back to default device")
                    "emulator-5554"
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to acquire emulator via discovery service: ${e.message}, using default")
            "emulator-5554"
        }
    }
    
    val appiumUrl = getArg("appium-url") ?: "http://localhost:4723"
    val aiApiKey = getArg("ai-api-key") ?: System.getenv("OPENAI_API_KEY")
    
    // Track acquired device for cleanup
    val acquiredDevice = if (getArg("device") == null) deviceName else null
    
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
        
        // Release emulator lock if we acquired it
        if (acquiredDevice != null) {
            try {
                val discoveryScript = File("../scripts/emulator-discovery.sh")
                if (discoveryScript.exists()) {
                    val process = ProcessBuilder("bash", discoveryScript.absolutePath, "release", acquiredDevice)
                        .directory(File(".."))
                        .start()
                    process.waitFor()
                    logger.info("Released emulator lock: $acquiredDevice")
                }
            } catch (e: Exception) {
                logger.warn("Failed to release emulator lock: ${e.message}")
            }
        }
    }
}

