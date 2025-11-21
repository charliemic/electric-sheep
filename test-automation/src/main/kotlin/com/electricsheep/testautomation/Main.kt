package com.electricsheep.testautomation

import com.electricsheep.testautomation.actions.ActionExecutor
import com.electricsheep.testautomation.actions.ContinuousInteractionLoop
import com.electricsheep.testautomation.actions.VisualAdaptiveRecovery
import com.electricsheep.testautomation.ai.OllamaService
import com.electricsheep.testautomation.monitoring.ScreenEvaluator
import com.electricsheep.testautomation.monitoring.ScreenMonitor
import com.electricsheep.testautomation.monitoring.StateManager
import com.electricsheep.testautomation.perception.GoalManager
import com.electricsheep.testautomation.planner.PersonaManager
import com.electricsheep.testautomation.planner.TaskPlanner
import com.electricsheep.testautomation.planner.TaskResult
import com.electricsheep.testautomation.vision.PatternRecognizer
import com.electricsheep.testautomation.vision.TextDetector
import com.electricsheep.testautomation.logging.CognitiveNarrativeLogger
import com.electricsheep.testautomation.logging.MachineReadableLogger
import org.slf4j.LoggerFactory
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

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
    val testResultsDir = File("test-results").apply { 
        mkdirs()
        setReadable(true, false)  // User readable, not group/other
        setWritable(true, false)  // User writable, not group/other
        setExecutable(true, false) // User executable, not group/other
    }
    val screenshotDir = File(testResultsDir, "screenshots").apply { 
        mkdirs()
        setReadable(true, false)
        setWritable(true, false)
        setExecutable(true, false)
    }
    val reportDir = File(testResultsDir, "reports").apply { 
        mkdirs()
        setReadable(true, false)
        setWritable(true, false)
        setExecutable(true, false)
    }
    val logDir = File(testResultsDir, "logs").apply { 
        mkdirs()
        setReadable(true, false)
        setWritable(true, false)
        setExecutable(true, false)
    }
    
    // Initialize dual logging system
    val timestamp = System.currentTimeMillis()
    val narrativeLogger = CognitiveNarrativeLogger(
        File(logDir, "narrative_${timestamp}.txt")
    )
    val machineLogger = MachineReadableLogger(
        File(logDir, "machine_${timestamp}.json")
    )
    
    val driverManager = AppiumDriverManager()
    val driver = driverManager.createAndroidDriver(
        appiumServerUrl = appiumUrl,
        deviceName = deviceName
    )
    
    // Create coroutine scope for continuous loop (outside try for cleanup)
    val coroutineScope = CoroutineScope(SupervisorJob())
    
    // Create monitoring components (outside try for cleanup)
    val stateManager = StateManager()
    
    // Create visual analysis components (visual-first principle)
    val textDetector = TextDetector()
    val patternRecognizer = PatternRecognizer(templateDir = null) // No templates for now
    val ollamaService = OllamaService() // For affordance detection (LLaVA)
    val screenEvaluator = ScreenEvaluator(
        aiVisionAPI = null, // Using hybrid visual analysis (OCR + Pattern Recognition)
        textDetector = textDetector,
        patternRecognizer = patternRecognizer,
        ollamaService = ollamaService // For AI-powered affordance detection
    )
    logger.info("✅ Visual analysis components initialized (OCR + Pattern Recognition)")
    
    val screenMonitor = ScreenMonitor(
        driver = driver,
        stateManager = stateManager,
        screenshotDir = screenshotDir,
        monitoringIntervalMs = 500, // 500ms for faster feedback
        screenEvaluator = screenEvaluator // Pass evaluator for visual analysis
    )
    
    try {
        // Create Ollama service (self-hosted LLM)
        val ollamaBaseUrl = System.getenv("OLLAMA_BASE_URL") ?: "http://localhost:11434"
        val ollamaModel = System.getenv("OLLAMA_MODEL") ?: "llama3.2"
        val ollamaService = OllamaService(baseUrl = ollamaBaseUrl, defaultModel = ollamaModel)
        logger.info("Ollama service initialized (baseUrl: $ollamaBaseUrl, model: $ollamaModel)")
        
        // Create persona manager with Ollama service
        val personaManager = PersonaManager(ollamaService = ollamaService)
        
        // Create goal manager
        val goalManager = GoalManager()
        
        // Create visual adaptive recovery (visual-first error recovery)
        val visualAdaptiveRecovery = VisualAdaptiveRecovery(
            stateManager = stateManager,
            driver = driver, // Required for last-resort fallbacks
            textDetector = textDetector,
            patternRecognizer = patternRecognizer
        )
        logger.info("✅ Visual adaptive recovery initialized (visual-first with last-resort fallbacks)")
        
        // Create continuous interaction loop (human-like continuous feedback)
        val continuousLoop = ContinuousInteractionLoop(
            screenMonitor = screenMonitor,
            stateManager = stateManager,
            goalManager = goalManager,
            coroutineScope = coroutineScope,
            visualAdaptiveRecovery = visualAdaptiveRecovery // Pass adaptive recovery
        )
        
        // Create executor with continuous loop and visual detection components
        val actionExecutor = ActionExecutor(
            driver = driver,
            screenshotDir = screenshotDir,
            visualActionExecutor = null, // Can be added later
            continuousLoop = continuousLoop,
            stateManager = stateManager, // For visual-first state detection
            screenEvaluator = screenEvaluator, // For visual-first screen evaluation
            textDetector = textDetector // For visual-first text detection
        )
        logger.info("✅ Action executor initialized with visual-first detection support")
        
        // Set action executor in continuous loop (after creation to avoid circular dependency)
        continuousLoop.setActionExecutor(actionExecutor)
        
        // Start screen monitoring
        screenMonitor.startMonitoring(coroutineScope)
        logger.info("✅ Continuous screen monitoring started (interval: 500ms)")
        
        // Create planner with adaptive planning components
        val taskPlanner = TaskPlanner(
            actionExecutor = actionExecutor,
            aiApiKey = aiApiKey,
            aiModel = "gpt-4-vision-preview",
            personaManager = personaManager, // Pass persona manager with Ollama service
            screenEvaluator = screenEvaluator, // For adaptive planning
            textDetector = textDetector // For adaptive planning
        )
        logger.info("✅ Task planner initialized with adaptive planning support")
        
        // Execute task
        val result = kotlinx.coroutines.runBlocking {
            taskPlanner.planAndExecute(
                task = task,
                context = context,
                maxIterations = 10
            )
        }
        
        // Finalize logs
        narrativeLogger.finalize(task, result is TaskResult.Success)
        machineLogger.finalize()
        
        // Report results
        when (result) {
            is TaskResult.Success -> {
                logger.info("")
                logger.info("✅ Task completed successfully!")
                logger.info("  Execution steps: ${result.executionHistory.size}")
                logger.info("  Screenshots: ${screenshotDir.absolutePath}")
                logger.info("  Reports: ${reportDir.absolutePath}")
                logger.info("  Narrative log: ${narrativeLogger.outputFile.absolutePath}")
                logger.info("  Machine log: ${machineLogger.outputFile.absolutePath}")
            }
            is TaskResult.Failure -> {
                logger.error("")
                logger.error("❌ Task failed: ${result.error}")
                logger.error("  Execution steps: ${result.executionHistory.size}")
                logger.error("  Screenshots: ${screenshotDir.absolutePath}")
                logger.error("  Reports: ${reportDir.absolutePath}")
                logger.error("  Narrative log: ${narrativeLogger.outputFile.absolutePath}")
                logger.error("  Machine log: ${machineLogger.outputFile.absolutePath}")
                System.exit(1)
            }
        }
    } finally {
        logger.info("")
        logger.info("🧹 Starting cleanup...")
        
        // 1. Stop screen monitoring
        try {
            screenMonitor.stopMonitoring()
            logger.info("✅ Stopped screen monitoring")
        } catch (e: Exception) {
            logger.warn("Failed to stop screen monitoring: ${e.message}")
        }
        
        // 2. Cancel coroutine scope
        try {
            coroutineScope.cancel()
            logger.info("✅ Cancelled coroutine scope")
        } catch (e: Exception) {
            logger.warn("Failed to cancel coroutine scope: ${e.message}")
        }
        
        // 3. Quit driver
        try {
            driverManager.quitDriver(driver)
            logger.info("✅ Driver closed")
        } catch (e: Exception) {
            logger.warn("Failed to quit driver: ${e.message}")
        }
        
        // 4. Release emulator lock if we acquired it
        if (acquiredDevice != null) {
            try {
                val discoveryScript = File("../scripts/emulator-discovery.sh")
                if (discoveryScript.exists()) {
                    val process = ProcessBuilder("bash", discoveryScript.absolutePath, "release", acquiredDevice)
                        .directory(File(".."))
                        .start()
                    process.waitFor()
                    logger.info("✅ Released emulator lock: $acquiredDevice")
                }
            } catch (e: Exception) {
                logger.warn("Failed to release emulator lock: ${e.message}")
            }
        }
        
        // 5. Clean up old screenshots and temporary files (keep recent ones)
        try {
            cleanupOldScreenshots(screenshotDir, keepRecentCount = 50, maxAgeHours = 24)
            cleanupTemporaryFiles(screenshotDir, maxAgeHours = 24)
            logger.info("✅ Cleaned up old screenshots and temporary files")
        } catch (e: Exception) {
            logger.warn("Failed to clean up screenshots: ${e.message}")
        }
        
        // 6. Clean up old reports (keep recent ones)
        try {
            cleanupOldReports(reportDir, keepRecentCount = 10, maxAgeDays = 7)
            logger.info("✅ Cleaned up old reports")
        } catch (e: Exception) {
            logger.warn("Failed to clean up reports: ${e.message}")
        }
        
        logger.info("")
        logger.info("✅ Cleanup complete")
    }
}

/**
 * Clean up old screenshots, keeping only the most recent ones.
 */
private fun cleanupOldScreenshots(screenshotDir: File, keepRecentCount: Int, maxAgeHours: Long) {
    if (!screenshotDir.exists() || !screenshotDir.isDirectory) return
    
    val screenshots = screenshotDir.listFiles { _, name ->
        name.endsWith(".png", ignoreCase = true)
    }?.toList() ?: emptyList()
    
    if (screenshots.isEmpty()) return
    
    val sortedScreenshots = screenshots.sortedByDescending { it.lastModified() }
    val maxAgeMillis = java.util.concurrent.TimeUnit.HOURS.toMillis(maxAgeHours)
    val cutoffTime = System.currentTimeMillis() - maxAgeMillis
    
    var deletedCount = 0
    sortedScreenshots.forEachIndexed { index, screenshot ->
        val shouldKeep = index < keepRecentCount || screenshot.lastModified() > cutoffTime
        if (!shouldKeep) {
            try {
                if (screenshot.delete()) deletedCount++
            } catch (e: Exception) {
                // Ignore deletion errors
            }
        }
    }
    
    if (deletedCount > 0) {
        LoggerFactory.getLogger("Main").info("Deleted $deletedCount old screenshots")
    }
}

/**
 * Clean up temporary files.
 */
private fun cleanupTemporaryFiles(screenshotDir: File, maxAgeHours: Long) {
    if (!screenshotDir.exists() || !screenshotDir.isDirectory) return
    
    val maxAgeMillis = java.util.concurrent.TimeUnit.HOURS.toMillis(maxAgeHours)
    val cutoffTime = System.currentTimeMillis() - maxAgeMillis
    
    val tempFiles = screenshotDir.listFiles { _, name ->
        name.endsWith("_cursor_prompt.txt", ignoreCase = true) ||
        name.endsWith(".tmp", ignoreCase = true) ||
        name.startsWith("temp_", ignoreCase = true)
    }?.toList() ?: emptyList()
    
    tempFiles.forEach { file ->
        if (file.lastModified() < cutoffTime) {
            try {
                file.delete()
            } catch (e: Exception) {
                // Ignore deletion errors
            }
        }
    }
}

/**
 * Clean up old reports, keeping only the most recent ones.
 */
private fun cleanupOldReports(reportDir: File, keepRecentCount: Int, maxAgeDays: Long) {
    if (!reportDir.exists() || !reportDir.isDirectory) return
    
    val reports = reportDir.listFiles { _, name ->
        name.startsWith("test_report_") && (name.endsWith(".txt") || name.endsWith(".pdf"))
    }?.toList() ?: emptyList()
    
    if (reports.isEmpty()) return
    
    val sortedReports = reports.sortedByDescending { it.lastModified() }
    val maxAgeMillis = java.util.concurrent.TimeUnit.DAYS.toMillis(maxAgeDays)
    val cutoffTime = System.currentTimeMillis() - maxAgeMillis
    
    var deletedCount = 0
    sortedReports.forEachIndexed { index, report ->
        val shouldKeep = index < keepRecentCount || report.lastModified() > cutoffTime
        if (!shouldKeep) {
            try {
                if (report.delete()) deletedCount++
            } catch (e: Exception) {
                // Ignore deletion errors
            }
        }
    }
    
    if (deletedCount > 0) {
        LoggerFactory.getLogger("Main").info("Deleted $deletedCount old reports")
    }
}

