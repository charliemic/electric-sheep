package com.electricsheep.testautomation.logging

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Instant

/**
 * Generates machine-readable structured logs (JSON) for debugging.
 * 
 * This logger creates structured JSON logs with all technical details,
 * timestamps, state information, and error details for debugging.
 */
class MachineReadableLogger(
    val outputFile: File
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val logs = mutableListOf<LogEntry>()
    private val objectMapper: ObjectMapper = jacksonObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
    }
    
    data class LogEntry(
        val timestamp: Long,
        val level: String,
        val component: String,
        val message: String,
        val data: Map<String, Any?>? = null,
        val error: ErrorDetails? = null
    )
    
    data class ErrorDetails(
        val type: String,
        val message: String,
        val stackTrace: List<String>? = null
    )
    
    init {
        outputFile.parentFile?.mkdirs()
    }
    
    /**
     * Log a debug message with optional data.
     */
    fun debug(component: String, message: String, data: Map<String, Any?>? = null) {
        addLog("DEBUG", component, message, data)
    }
    
    /**
     * Log an info message with optional data.
     */
    fun info(component: String, message: String, data: Map<String, Any?>? = null) {
        addLog("INFO", component, message, data)
    }
    
    /**
     * Log a warning with optional data.
     */
    fun warn(component: String, message: String, data: Map<String, Any?>? = null) {
        addLog("WARN", component, message, data)
    }
    
    /**
     * Log an error with exception details.
     */
    fun error(component: String, message: String, exception: Throwable? = null, data: Map<String, Any?>? = null) {
        val errorDetails = exception?.let {
            ErrorDetails(
                type = it.javaClass.simpleName,
                message = it.message ?: "Unknown error",
                stackTrace = it.stackTrace.take(20).map { it.toString() }
            )
        }
        addLog("ERROR", component, message, data, errorDetails)
    }
    
    /**
     * Log state change.
     */
    fun stateChange(
        component: String,
        fromState: String?,
        toState: String,
        stateData: Map<String, Any?>? = null
    ) {
        addLog("STATE", component, "State changed: $fromState → $toState", mapOf(
            "fromState" to fromState,
            "toState" to toState
        ) + (stateData ?: emptyMap()))
    }
    
    /**
     * Log action execution.
     */
    fun action(
        component: String,
        actionType: String,
        actionDetails: Map<String, Any?>,
        result: String,
        resultData: Map<String, Any?>? = null
    ) {
        addLog("ACTION", component, "$actionType: $result", mapOf(
            "actionType" to actionType,
            "actionDetails" to actionDetails,
            "result" to result
        ) + (resultData ?: emptyMap()))
    }
    
    /**
     * Log screen observation.
     */
    fun observation(
        component: String,
        observationType: String,
        observationData: Map<String, Any?>
    ) {
        addLog("OBSERVATION", component, "Observed: $observationType", observationData)
    }
    
    private fun addLog(
        level: String,
        component: String,
        message: String,
        data: Map<String, Any?>? = null,
        error: ErrorDetails? = null
    ) {
        logs.add(LogEntry(
            timestamp = System.currentTimeMillis(),
            level = level,
            component = component,
            message = message,
            data = data,
            error = error
        ))
    }
    
    /**
     * Finalize and save logs as JSON.
     */
    fun finalize() {
        val logData = mapOf(
            "metadata" to mapOf(
                "startTime" to logs.firstOrNull()?.timestamp,
                "endTime" to System.currentTimeMillis(),
                "totalEntries" to logs.size
            ),
            "logs" to logs
        )
        
        val json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(logData)
        outputFile.writeText(json)
        logger.info("Machine-readable log saved: ${outputFile.absolutePath}")
    }
}

