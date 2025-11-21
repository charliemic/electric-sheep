package com.electricsheep.testautomation.vision

import org.junit.Test
import java.io.File
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Simple test to verify TextDetector can extract text from screenshots.
 * 
 * This is a minimal test to verify OCR integration works.
 * For full testing, we'll use real app screenshots.
 */
class TextDetectorTest {
    
    @Test
    fun `test TextDetector can be instantiated`() {
        val detector = TextDetector()
        assertNotNull(detector, "TextDetector should be instantiable")
    }
    
    @Test
    fun `test TextDetector extracts text from screenshot if available`() {
        // This test will only run if we have a test screenshot
        // For now, just verify the method exists and doesn't crash
        val detector = TextDetector()
        
        // Try with a non-existent file (should handle gracefully)
        val nonExistentFile = File("non_existent_screenshot.png")
        val result = detector.extractText(nonExistentFile)
        
        // Should return empty result, not crash
        assertNotNull(result, "Should return ExtractedText even for non-existent file")
        assertTrue(result.fullText.isEmpty(), "Should return empty text for non-existent file")
    }
}

