package com.electricsheep.testautomation.actions

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Logs test actions with precise timestamps for video annotation.
 * 
 * This logger records all actions executed during test automation with timestamps
 * relative to video start time, enabling frame-accurate video annotation.
 */
class ActionLogger(
    private val videoStartTime: Long = System.currentTimeMillis(),
    private val logFile: File
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val actions = mutableListOf<ActionLogEntry>()
    private val objectMapper: ObjectMapper = jacksonObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
    }
    
    /**
     * Log an action with its result and optional coordinates.
     * 
     * @param action The action that was executed
     * @param result The result of executing the action
     * @param coordinates Optional coordinates (x, y) for tap/swipe actions
     */
    fun logAction(
        action: HumanAction,
        result: ActionResult,
        coordinates: Pair<Int, Int>? = null
    ) {
        val timestamp = System.currentTimeMillis() - videoStartTime
        val description = generateDescription(action, result)
        val target = extractTarget(action)
        
        val entry = ActionLogEntry(
            timestampMs = timestamp,
            action = action::class.simpleName ?: "Unknown",
            description = description,
            target = target,
            screenshot = result.screenshot?.absolutePath,
            coordinates = coordinates?.let { listOf(it.first, it.second) },
            success = result is ActionResult.Success,
            error = (result as? ActionResult.Failure)?.error
        )
        
        actions.add(entry)
        logger.debug("Logged action: $description at ${timestamp}ms")
    }
    
    /**
     * Export action log to JSON file.
     * 
     * @return The log file
     */
    fun export(): File {
        try {
            logFile.parentFile?.mkdirs()
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(logFile, actions)
            logger.info("Exported ${actions.size} actions to ${logFile.absolutePath}")
            return logFile
        } catch (e: Exception) {
            logger.error("Failed to export action log", e)
            throw e
        }
    }
    
    /**
     * Get the video start time (for reference).
     */
    fun getVideoStartTime(): Long = videoStartTime
    
    /**
     * Generate a human-readable description of the action.
     */
    private fun generateDescription(action: HumanAction, result: ActionResult): String {
        return when (action) {
            is HumanAction.Tap -> {
                val target = action.target ?: action.accessibilityId ?: "element"
                if (result is ActionResult.Success) {
                    "Tapping on $target"
                } else {
                    "Failed to tap on $target"
                }
            }
            is HumanAction.TypeText -> {
                val target = action.target ?: action.accessibilityId ?: "field"
                val preview = action.text.take(20) + if (action.text.length > 20) "..." else ""
                if (result is ActionResult.Success) {
                    "Typing into $target: $preview"
                } else {
                    "Failed to type into $target"
                }
            }
            is HumanAction.Swipe -> {
                if (result is ActionResult.Success) {
                    "Swiping ${action.direction.name.lowercase()}"
                } else {
                    "Failed to swipe ${action.direction.name.lowercase()}"
                }
            }
            is HumanAction.WaitFor -> {
                val condition = when (val c = action.condition) {
                    is WaitCondition.ElementVisible -> "element ${c.target} to be visible"
                    is WaitCondition.ElementEnabled -> "element ${c.target} to be enabled"
                    is WaitCondition.ScreenChanged -> "screen to change to ${c.expectedScreen}"
                    is WaitCondition.LoadingComplete -> "loading to complete"
                    is WaitCondition.TextAppears -> "text '${c.text}' to appear"
                }
                if (result is ActionResult.Success) {
                    "Waiting for $condition"
                } else {
                    "Timeout waiting for $condition"
                }
            }
            is HumanAction.NavigateBack -> {
                if (result is ActionResult.Success) {
                    "Navigating back"
                } else {
                    "Failed to navigate back"
                }
            }
            is HumanAction.CaptureState -> {
                "Capturing current state"
            }
            is HumanAction.Verify -> {
                val condition = when (val c = action.condition) {
                    is VerifyCondition.ElementPresent -> "element ${c.target} is present"
                    is VerifyCondition.TextPresent -> "text '${c.text}' is present"
                    is VerifyCondition.ScreenIs -> "screen is ${c.screenName}"
                    is VerifyCondition.Authenticated -> "authentication state is ${c.expected}"
                }
                if (result is ActionResult.Success) {
                    "Verifying $condition"
                } else {
                    "Verification failed: $condition"
                }
            }
        }
    }
    
    /**
     * Extract target description from action.
     */
    private fun extractTarget(action: HumanAction): String? {
        return when (action) {
            is HumanAction.Tap -> action.target ?: action.accessibilityId
            is HumanAction.TypeText -> action.target ?: action.accessibilityId
            is HumanAction.Swipe -> null
            is HumanAction.WaitFor -> when (val c = action.condition) {
                is WaitCondition.ElementVisible -> c.target
                is WaitCondition.ElementEnabled -> c.target
                is WaitCondition.ScreenChanged -> c.expectedScreen
                is WaitCondition.LoadingComplete -> null
                is WaitCondition.TextAppears -> null
            }
            is HumanAction.NavigateBack -> null
            is HumanAction.CaptureState -> null
            is HumanAction.Verify -> when (val c = action.condition) {
                is VerifyCondition.ElementPresent -> c.target
                is VerifyCondition.TextPresent -> null
                is VerifyCondition.ScreenIs -> c.screenName
                is VerifyCondition.Authenticated -> null
            }
        }
    }
}

/**
 * Data class representing a single action log entry.
 */
data class ActionLogEntry(
    val timestampMs: Long,
    val action: String,
    val description: String,
    val target: String?,
    val screenshot: String?,
    val coordinates: List<Int>?,  // [x, y] for tap/swipe actions
    val success: Boolean,
    val error: String? = null
)

