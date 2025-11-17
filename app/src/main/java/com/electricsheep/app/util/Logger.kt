package com.electricsheep.app.util

import android.util.Log

/**
 * Centralised logging utility for the application.
 * 
 * Provides structured logging with appropriate levels and tagging.
 * Designed to be easily extensible for future external log streaming.
 */
object Logger {
    private const val DEFAULT_TAG = "ElectricSheep"
    
    /**
     * Log levels matching Android's Log levels
     * Priority values match android.util.Log constants
     */
    enum class Level(val priority: Int) {
        VERBOSE(2),  // Log.VERBOSE
        DEBUG(3),    // Log.DEBUG
        INFO(4),     // Log.INFO
        WARN(5),     // Log.WARN
        ERROR(6)     // Log.ERROR
    }
    
    /**
     * Log a verbose message (detailed debugging information)
     * Use for very detailed diagnostic information that is usually only interesting during development
     */
    fun verbose(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        log(Level.VERBOSE, tag, message, throwable)
    }
    
    /**
     * Log a debug message (development debugging)
     * Use for diagnostic information that is helpful during development but not needed in production
     */
    fun debug(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        log(Level.DEBUG, tag, message, throwable)
    }
    
    /**
     * Log an info message (general information)
     * Use for general informational messages about app flow and state
     */
    fun info(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        log(Level.INFO, tag, message, throwable)
    }
    
    /**
     * Log a warning message (potential issues)
     * Use for situations that are unusual but not errors, or recoverable errors
     */
    fun warn(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        log(Level.WARN, tag, message, throwable)
    }
    
    /**
     * Log an error message (errors and exceptions)
     * Use for errors and exceptions that indicate problems
     */
    fun error(tag: String = DEFAULT_TAG, message: String, throwable: Throwable? = null) {
        log(Level.ERROR, tag, message, throwable)
    }
    
    private fun log(level: Level, tag: String, message: String, throwable: Throwable?) {
        when (level) {
            Level.VERBOSE -> if (throwable != null) Log.v(tag, message, throwable) else Log.v(tag, message)
            Level.DEBUG -> if (throwable != null) Log.d(tag, message, throwable) else Log.d(tag, message)
            Level.INFO -> if (throwable != null) Log.i(tag, message, throwable) else Log.i(tag, message)
            Level.WARN -> if (throwable != null) Log.w(tag, message, throwable) else Log.w(tag, message)
            Level.ERROR -> if (throwable != null) Log.e(tag, message, throwable) else Log.e(tag, message)
        }
        
        // Future: Add external log streaming here
        // streamToExternalService(level, tag, message, throwable)
    }
}

