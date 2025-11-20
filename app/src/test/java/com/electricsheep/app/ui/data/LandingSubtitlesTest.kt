package com.electricsheep.app.ui.data

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for LandingSubtitles.
 * Tests quote collection and random selection functionality.
 */
class LandingSubtitlesTest {
    
    @Test
    fun `quotes list should not be empty`() {
        // Arrange & Act
        val quotes = LandingSubtitles.quotes
        
        // Assert
        assertTrue("Quotes list should not be empty", quotes.isNotEmpty())
    }
    
    @Test
    fun `quotes list should contain Personal Utilities`() {
        // Arrange & Act
        val quotes = LandingSubtitles.quotes
        
        // Assert
        assertTrue(
            "Quotes should contain 'Personal Utilities'",
            quotes.contains("Personal Utilities")
        )
    }
    
    @Test
    fun `getRandomQuote should return a quote from the collection`() {
        // Arrange
        val quotes = LandingSubtitles.quotes
        
        // Act
        val randomQuote = LandingSubtitles.getRandomQuote()
        
        // Assert
        assertTrue(
            "Random quote should be from the quotes collection",
            quotes.contains(randomQuote)
        )
    }
    
    @Test
    fun `getRandomQuote should return different quotes on multiple calls`() {
        // Arrange
        val quotes = LandingSubtitles.quotes
        val iterations = 100
        val uniqueQuotes = mutableSetOf<String>()
        
        // Act - Call getRandomQuote multiple times
        repeat(iterations) {
            uniqueQuotes.add(LandingSubtitles.getRandomQuote())
        }
        
        // Assert - With 50+ quotes, we should get at least a few different ones
        // This test verifies randomness (not deterministic)
        assertTrue(
            "Should return different quotes across multiple calls",
            uniqueQuotes.size > 1
        )
    }
    
    @Test
    fun `all quotes should be non-empty strings`() {
        // Arrange & Act
        val quotes = LandingSubtitles.quotes
        
        // Assert
        quotes.forEach { quote ->
            assertTrue(
                "Quote should not be empty: '$quote'",
                quote.isNotBlank()
            )
        }
    }
    
    @Test
    fun `quotes should have reasonable length for subtitle display`() {
        // Arrange & Act
        val quotes = LandingSubtitles.quotes
        val maxReasonableLength = 200 // Characters
        
        // Assert
        quotes.forEach { quote ->
            assertTrue(
                "Quote should be reasonable length for subtitle: '$quote'",
                quote.length <= maxReasonableLength
            )
        }
    }
}

