package com.electricsheep.app.ui.components

import com.electricsheep.app.data.model.Mood
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Unit tests for MoodChartDataProcessor.
 * Tests data aggregation and processing for different time intervals.
 */
class MoodChartDataProcessorTest {
    
    private val processor = MoodChartDataProcessor()
    
    @Test
    fun `should return empty list when processing empty daily data`() {
        // Arrange
        val moods = emptyList<Mood>()
        
        // Act
        val result = processor.processDailyData(moods)
        
        // Assert
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `should process single mood entry for daily view`() {
        // Arrange
        val timestamp = System.currentTimeMillis()
        val mood = Mood(
            id = "1",
            userId = "user-1",
            score = 5,
            timestamp = timestamp
        )
        
        // Act
        val result = processor.processDailyData(listOf(mood))
        
        // Assert
        assertEquals(1, result.size)
        assertEquals(0f, result[0].x, 0.01f)
        assertEquals(5f, result[0].y, 0.01f)
        assertNotNull(result[0].label)
    }
    
    @Test
    fun `should average multiple moods on same day for daily view`() {
        // Arrange
        val baseTime = System.currentTimeMillis()
        val date = Instant.ofEpochMilli(baseTime)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        
        val moods = listOf(
            Mood(id = "1", userId = "user-1", score = 5, timestamp = baseTime),
            Mood(id = "2", userId = "user-1", score = 7, timestamp = baseTime + 1000),
            Mood(id = "3", userId = "user-1", score = 3, timestamp = baseTime + 2000)
        )
        
        // Act
        val result = processor.processDailyData(moods)
        
        // Assert
        assertEquals(1, result.size)
        assertEquals(5f, result[0].y, 0.01f) // (5 + 7 + 3) / 3 = 5
    }
    
    @Test
    fun `should group moods by day for daily view`() {
        // Arrange
        val baseTime = System.currentTimeMillis()
        val today = Instant.ofEpochMilli(baseTime)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val tomorrow = today.plusDays(1)
        
        val moods = listOf(
            Mood(id = "1", userId = "user-1", score = 5, timestamp = baseTime),
            Mood(
                id = "2",
                userId = "user-1",
                score = 7,
                timestamp = tomorrow.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
        )
        
        // Act
        val result = processor.processDailyData(moods)
        
        // Assert
        assertEquals(2, result.size)
        assertEquals(5f, result[0].y, 0.01f)
        assertEquals(7f, result[1].y, 0.01f)
        assertEquals(0f, result[0].x, 0.01f)
        assertEquals(1f, result[1].x, 0.01f)
    }
    
    @Test
    fun `should sort daily data chronologically`() {
        // Arrange
        val baseTime = System.currentTimeMillis()
        val today = Instant.ofEpochMilli(baseTime)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val tomorrow = today.plusDays(1)
        val dayAfter = today.plusDays(2)
        
        val moods = listOf(
            Mood(
                id = "3",
                userId = "user-1",
                score = 9,
                timestamp = dayAfter.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            ),
            Mood(id = "1", userId = "user-1", score = 5, timestamp = baseTime),
            Mood(
                id = "2",
                userId = "user-1",
                score = 7,
                timestamp = tomorrow.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
        )
        
        // Act
        val result = processor.processDailyData(moods)
        
        // Assert
        assertEquals(3, result.size)
        assertEquals(5f, result[0].y, 0.01f) // Today
        assertEquals(7f, result[1].y, 0.01f) // Tomorrow
        assertEquals(9f, result[2].y, 0.01f) // Day after
    }
    
    @Test
    fun `should return empty list when processing empty weekly data`() {
        // Arrange
        val moods = emptyList<Mood>()
        
        // Act
        val result = processor.processWeeklyData(moods)
        
        // Assert
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `should process single mood entry for weekly view`() {
        // Arrange
        val timestamp = System.currentTimeMillis()
        val mood = Mood(
            id = "1",
            userId = "user-1",
            score = 5,
            timestamp = timestamp
        )
        
        // Act
        val result = processor.processWeeklyData(listOf(mood))
        
        // Assert
        assertEquals(1, result.size)
        assertEquals(0f, result[0].x, 0.01f)
        assertEquals(5f, result[0].y, 0.01f)
        assertNotNull(result[0].label)
    }
    
    @Test
    fun `should average multiple moods in same week for weekly view`() {
        // Arrange
        val baseTime = System.currentTimeMillis()
        val date = Instant.ofEpochMilli(baseTime)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        
        // Create moods on different days of the same week
        val moods = listOf(
            Mood(id = "1", userId = "user-1", score = 5, timestamp = baseTime),
            Mood(
                id = "2",
                userId = "user-1",
                score = 7,
                timestamp = date.plusDays(1)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            ),
            Mood(
                id = "3",
                userId = "user-1",
                score = 3,
                timestamp = date.plusDays(2)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            )
        )
        
        // Act
        val result = processor.processWeeklyData(moods)
        
        // Assert
        assertEquals(1, result.size)
        assertEquals(5f, result[0].y, 0.01f) // (5 + 7 + 3) / 3 = 5
    }
    
    @Test
    fun `should return empty list when processing empty monthly data`() {
        // Arrange
        val moods = emptyList<Mood>()
        
        // Act
        val result = processor.processMonthlyData(moods)
        
        // Assert
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `should process single mood entry for monthly view`() {
        // Arrange
        val timestamp = System.currentTimeMillis()
        val mood = Mood(
            id = "1",
            userId = "user-1",
            score = 5,
            timestamp = timestamp
        )
        
        // Act
        val result = processor.processMonthlyData(listOf(mood))
        
        // Assert
        assertEquals(1, result.size)
        assertEquals(0f, result[0].x, 0.01f)
        assertEquals(5f, result[0].y, 0.01f)
        assertNotNull(result[0].label)
        assertTrue(result[0].label.contains("202") || result[0].label.contains("2024") || result[0].label.contains("2025"))
    }
    
    @Test
    fun `should average multiple moods in same month for monthly view`() {
        // Arrange
        // Use a fixed date in the middle of a month to ensure all moods stay in same month
        val baseDate = LocalDate.of(2024, 6, 15) // June 15, 2024
        val baseTime = baseDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        
        // Create moods on different days of the same month
        val moods = listOf(
            Mood(id = "1", userId = "user-1", score = 5, timestamp = baseTime),
            Mood(
                id = "2",
                userId = "user-1",
                score = 7,
                timestamp = baseDate.plusDays(10)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            ),
            Mood(
                id = "3",
                userId = "user-1",
                score = 3,
                timestamp = baseDate.plusDays(15)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            )
        )
        
        // Act
        val result = processor.processMonthlyData(moods)
        
        // Assert
        assertEquals(1, result.size)
        assertEquals(5f, result[0].y, 0.01f) // (5 + 7 + 3) / 3 = 5
    }
    
    @Test
    fun `should group moods by month for monthly view`() {
        // Arrange
        val baseTime = System.currentTimeMillis()
        val date = Instant.ofEpochMilli(baseTime)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val nextMonth = date.plusMonths(1)
        
        val moods = listOf(
            Mood(id = "1", userId = "user-1", score = 5, timestamp = baseTime),
            Mood(
                id = "2",
                userId = "user-1",
                score = 7,
                timestamp = nextMonth
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            )
        )
        
        // Act
        val result = processor.processMonthlyData(moods)
        
        // Assert
        assertEquals(2, result.size)
        assertEquals(5f, result[0].y, 0.01f)
        assertEquals(7f, result[1].y, 0.01f)
        assertEquals(0f, result[0].x, 0.01f)
        assertEquals(1f, result[1].x, 0.01f)
    }
    
    @Test
    fun `should handle boundary scores correctly`() {
        // Arrange
        val timestamp = System.currentTimeMillis()
        val moods = listOf(
            Mood(id = "1", userId = "user-1", score = 1, timestamp = timestamp),
            Mood(id = "2", userId = "user-1", score = 10, timestamp = timestamp + 1000)
        )
        
        // Act
        val result = processor.processDailyData(moods)
        
        // Assert
        assertEquals(1, result.size)
        assertEquals(5.5f, result[0].y, 0.01f) // (1 + 10) / 2 = 5.5
    }
    
    @Test
    fun `should handle large number of mood entries efficiently`() {
        // Arrange
        val baseTime = System.currentTimeMillis()
        val moods = (1..100).map { index ->
            Mood(
                id = index.toString(),
                userId = "user-1",
                score = (index % 10) + 1, // Scores 1-10
                timestamp = baseTime + (index * 1000L)
            )
        }
        
        // Act
        val result = processor.processDailyData(moods)
        
        // Assert
        assertTrue(result.isNotEmpty())
        // All entries should be on the same day (within 100 seconds)
        assertTrue(result.size <= 2) // Might span 2 days if crossing midnight
    }
}

