package com.electricsheep.app.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Utility for formatting dates and times in human-readable format.
 */
object DateFormatter {
    
    /**
     * Format a timestamp (milliseconds since epoch) to a human-readable date and time string.
     * Uses the system's default locale and timezone.
     * 
     * @param timestamp Milliseconds since epoch
     * @return Formatted date and time string (e.g., "17 Nov 2024, 15:30")
     */
    fun formatDateTime(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        return dateTime.format(formatter)
    }
    
    /**
     * Format a timestamp to a date-only string.
     * 
     * @param timestamp Milliseconds since epoch
     * @return Formatted date string (e.g., "17 Nov 2024")
     */
    fun formatDate(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        return dateTime.format(formatter)
    }
    
    /**
     * Format a timestamp to a time-only string.
     * 
     * @param timestamp Milliseconds since epoch
     * @return Formatted time string (e.g., "15:30")
     */
    fun formatTime(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        return dateTime.format(formatter)
    }
}

