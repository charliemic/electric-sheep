package com.electricsheep.testautomation.util

import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.util.concurrent.TimeUnit

/**
 * Utility for cleaning up test artifacts (screenshots, temporary files, etc.)
 * after test execution.
 */
class TestCleanup {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * Clean up old screenshots, keeping only the most recent ones.
     * 
     * @param screenshotDir Directory containing screenshots
     * @param keepRecentCount Number of recent screenshots to keep (default: 50)
     * @param maxAgeHours Maximum age in hours for screenshots to keep (default: 24)
     */
    fun cleanupScreenshots(
        screenshotDir: File,
        keepRecentCount: Int = 50,
        maxAgeHours: Long = 24
    ) {
        if (!screenshotDir.exists() || !screenshotDir.isDirectory) {
            logger.debug("Screenshot directory does not exist: ${screenshotDir.absolutePath}")
            return
        }
        
        val screenshots = screenshotDir.listFiles { _, name ->
            name.endsWith(".png", ignoreCase = true)
        }?.toList() ?: emptyList()
        
        if (screenshots.isEmpty()) {
            logger.debug("No screenshots to clean up")
            return
        }
        
        // Sort by modification time (newest first)
        val sortedScreenshots = screenshots.sortedByDescending { it.lastModified() }
        
        val maxAgeMillis = TimeUnit.HOURS.toMillis(maxAgeHours)
        val cutoffTime = System.currentTimeMillis() - maxAgeMillis
        
        var deletedCount = 0
        var keptCount = 0
        
        sortedScreenshots.forEachIndexed { index, screenshot ->
            val shouldKeep = when {
                // Keep the most recent N screenshots
                index < keepRecentCount -> true
                // Keep screenshots newer than maxAgeHours
                screenshot.lastModified() > cutoffTime -> true
                // Delete old screenshots
                else -> false
            }
            
            if (shouldKeep) {
                keptCount++
            } else {
                try {
                    if (screenshot.delete()) {
                        deletedCount++
                    } else {
                        logger.warn("Failed to delete screenshot: ${screenshot.absolutePath}")
                    }
                } catch (e: Exception) {
                    logger.warn("Error deleting screenshot ${screenshot.absolutePath}: ${e.message}")
                }
            }
        }
        
        logger.info("Screenshot cleanup: Deleted $deletedCount old screenshots, kept $keptCount recent screenshots")
    }
    
    /**
     * Clean up temporary files (e.g., cursor prompt files, temporary analysis files).
     * 
     * @param screenshotDir Directory to clean (typically the screenshot directory)
     * @param maxAgeHours Maximum age in hours for temporary files (default: 24)
     */
    fun cleanupTemporaryFiles(
        screenshotDir: File,
        maxAgeHours: Long = 24
    ) {
        if (!screenshotDir.exists() || !screenshotDir.isDirectory) {
            return
        }
        
        val maxAgeMillis = TimeUnit.HOURS.toMillis(maxAgeHours)
        val cutoffTime = System.currentTimeMillis() - maxAgeMillis
        
        val tempFiles = screenshotDir.listFiles { _, name ->
            name.endsWith("_cursor_prompt.txt", ignoreCase = true) ||
            name.endsWith(".tmp", ignoreCase = true) ||
            name.startsWith("temp_", ignoreCase = true)
        }?.toList() ?: emptyList()
        
        var deletedCount = 0
        tempFiles.forEach { file ->
            if (file.lastModified() < cutoffTime) {
                try {
                    if (file.delete()) {
                        deletedCount++
                    }
                } catch (e: Exception) {
                    logger.debug("Error deleting temp file ${file.absolutePath}: ${e.message}")
                }
            }
        }
        
        if (deletedCount > 0) {
            logger.info("Cleaned up $deletedCount temporary files")
        }
    }
    
    /**
     * Clean up old test reports, keeping only the most recent ones.
     * 
     * @param reportDir Directory containing test reports
     * @param keepRecentCount Number of recent reports to keep (default: 10)
     * @param maxAgeDays Maximum age in days for reports to keep (default: 7)
     */
    fun cleanupReports(
        reportDir: File,
        keepRecentCount: Int = 10,
        maxAgeDays: Long = 7
    ) {
        if (!reportDir.exists() || !reportDir.isDirectory) {
            return
        }
        
        val reports = reportDir.listFiles { _, name ->
            name.startsWith("test_report_") && name.endsWith(".txt")
        }?.toList() ?: emptyList()
        
        if (reports.isEmpty()) {
            return
        }
        
        // Sort by modification time (newest first)
        val sortedReports = reports.sortedByDescending { it.lastModified() }
        
        val maxAgeMillis = TimeUnit.DAYS.toMillis(maxAgeDays)
        val cutoffTime = System.currentTimeMillis() - maxAgeMillis
        
        var deletedCount = 0
        var keptCount = 0
        
        sortedReports.forEachIndexed { index, report ->
            val shouldKeep = when {
                // Keep the most recent N reports
                index < keepRecentCount -> true
                // Keep reports newer than maxAgeDays
                report.lastModified() > cutoffTime -> true
                // Delete old reports
                else -> false
            }
            
            if (shouldKeep) {
                keptCount++
            } else {
                try {
                    if (report.delete()) {
                        deletedCount++
                    } else {
                        logger.warn("Failed to delete report: ${report.absolutePath}")
                    }
                } catch (e: Exception) {
                    logger.warn("Error deleting report ${report.absolutePath}: ${e.message}")
                }
            }
        }
        
        if (deletedCount > 0) {
            logger.info("Report cleanup: Deleted $deletedCount old reports, kept $keptCount recent reports")
        }
    }
    
    /**
     * Perform all cleanup operations.
     */
    fun cleanupAll(
        screenshotDir: File,
        reportDir: File? = null,
        keepRecentScreenshots: Int = 50,
        keepRecentReports: Int = 10
    ) {
        logger.info("Starting test cleanup...")
        cleanupScreenshots(screenshotDir, keepRecentScreenshots)
        cleanupTemporaryFiles(screenshotDir)
        reportDir?.let { cleanupReports(it, keepRecentReports) }
        logger.info("Test cleanup completed")
    }
}



