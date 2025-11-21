package com.electricsheep.testautomation.vision

import org.slf4j.LoggerFactory
import java.io.File

/**
 * Detects and extracts text from screenshots using OCR (Optical Character Recognition).
 * 
 * This mimics human text reading behavior - humans read text on screens to understand
 * error messages, screen names, button labels, etc. This component does the same.
 * 
 * Uses Tesseract OCR engine via command-line (simpler, no Java dependency issues).
 * TODO: Migrate to tess4j Java wrapper once dependency resolution is fixed.
 */
class TextDetector {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    // Check if Tesseract is available
    private val tesseractAvailable: Boolean = run {
        try {
            val process = ProcessBuilder("tesseract", "--version")
                .redirectErrorStream(true)
                .start()
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Text region with location information.
     */
    data class TextRegion(
        val text: String,
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int,
        val confidence: Double
    )
    
    /**
     * Extracted text and detected patterns from a screenshot.
     */
    data class ExtractedText(
        val fullText: String,
        val errorMessages: List<String>,
        val screenIndicators: List<String>,
        val buttonLabels: List<String>,
        val loadingIndicators: List<String>,
        val textRegions: List<TextRegion> = emptyList() // Text with bounding boxes
    )
    
    /**
     * Extract text from screenshot and detect patterns.
     * 
     * @param screenshot The screenshot file to analyze
     * @return ExtractedText with full text and detected patterns
     */
    fun extractText(screenshot: File): ExtractedText {
        return try {
            if (!tesseractAvailable) {
                logger.warn("Tesseract not available - OCR disabled. Install with: brew install tesseract")
                return ExtractedText(
                    fullText = "",
                    errorMessages = emptyList(),
                    screenIndicators = emptyList(),
                    buttonLabels = emptyList(),
                    loadingIndicators = emptyList(),
                    textRegions = emptyList()
                )
            }
            
            logger.debug("Extracting text from screenshot: ${screenshot.name}")
            logger.debug("   Screenshot path: ${screenshot.absolutePath}")
            logger.debug("   Screenshot exists: ${screenshot.exists()}")
            logger.debug("   Screenshot size: ${screenshot.length()} bytes")
            
            if (!screenshot.exists()) {
                logger.error("❌ Screenshot file does not exist: ${screenshot.absolutePath}")
                return ExtractedText(
                    fullText = "",
                    errorMessages = emptyList(),
                    screenIndicators = emptyList(),
                    buttonLabels = emptyList(),
                    loadingIndicators = emptyList(),
                    textRegions = emptyList()
                )
            }
            
            if (screenshot.length() == 0L) {
                logger.error("❌ Screenshot file is empty: ${screenshot.absolutePath}")
                return ExtractedText(
                    fullText = "",
                    errorMessages = emptyList(),
                    screenIndicators = emptyList(),
                    buttonLabels = emptyList(),
                    loadingIndicators = emptyList(),
                    textRegions = emptyList()
                )
            }
            
            // Extract text using Tesseract command-line
            // First, get plain text
            val tempOutput = File.createTempFile("ocr_output", ".txt")
            tempOutput.deleteOnExit()
            
            val tesseractOutputFile = tempOutput.absolutePath.replace(".txt", "") // Tesseract adds .txt automatically
            
            logger.debug("   Running tesseract: tesseract ${screenshot.absolutePath} $tesseractOutputFile -l eng")
            
            val process = ProcessBuilder(
                "tesseract",
                screenshot.absolutePath,
                tesseractOutputFile,
                "-l", "eng"
            )
                .redirectErrorStream(true)
                .start()
            
            // Capture stderr for debugging
            val errorOutput = StringBuilder()
            process.errorStream.bufferedReader().use { reader ->
                reader.lineSequence().forEach { line ->
                    errorOutput.appendLine(line)
                }
            }
            
            val exitCode = process.waitFor()
            
            if (exitCode != 0) {
                logger.error("❌ Tesseract OCR failed with exit code: $exitCode")
                logger.error("   Error output: ${errorOutput.toString().take(500)}")
                return ExtractedText(
                    fullText = "",
                    errorMessages = emptyList(),
                    screenIndicators = emptyList(),
                    buttonLabels = emptyList(),
                    loadingIndicators = emptyList(),
                    textRegions = emptyList()
                )
            }
            
            // Read extracted text
            val outputFile = File("$tesseractOutputFile.txt")
            val fullText = if (outputFile.exists()) {
                outputFile.readText().trim()
            } else {
                logger.warn("⚠️  Tesseract output file not found: ${outputFile.absolutePath}")
                ""
            }
            
            // Also extract text with bounding boxes (TSV format)
            val textRegions = extractTextRegions(screenshot)
            
            logger.info("✅ OCR extracted ${fullText.length} characters of text from screenshot")
            if (fullText.isEmpty()) {
                logger.warn("⚠️  OCR returned empty text - screenshot may not contain readable text or OCR quality is poor")
            }
            
            // Detect patterns in extracted text
            val errorMessages = findErrorMessages(fullText)
            val screenIndicators = findScreenIndicators(fullText)
            val buttonLabels = findButtonLabels(fullText)
            val loadingIndicators = findLoadingIndicators(fullText)
            
            ExtractedText(
                fullText = fullText,
                errorMessages = errorMessages,
                screenIndicators = screenIndicators,
                buttonLabels = buttonLabels,
                loadingIndicators = loadingIndicators,
                textRegions = textRegions
            )
        } catch (e: Exception) {
            logger.warn("Failed to extract text from screenshot: ${e.message}")
            // Return empty result on failure
            ExtractedText(
                fullText = "",
                errorMessages = emptyList(),
                screenIndicators = emptyList(),
                buttonLabels = emptyList(),
                loadingIndicators = emptyList(),
                textRegions = emptyList()
            )
        }
    }
    
    /**
     * Find error messages in text.
     * Looks for common error keywords and patterns.
     */
    private fun findErrorMessages(text: String): List<String> {
        val errorKeywords = listOf(
            "error", "invalid", "required", "cannot", "must", "failed",
            "incorrect", "wrong", "missing", "not found", "unable"
        )
        
        return text.lines()
            .filter { line ->
                val lowerLine = line.lowercase().trim()
                lowerLine.isNotEmpty() && 
                errorKeywords.any { lowerLine.contains(it, ignoreCase = true) }
            }
            .map { it.trim() }
            .distinct()
    }
    
    /**
     * Find screen name indicators in text.
     * Looks for common screen names and navigation indicators.
     */
    private fun findScreenIndicators(text: String): List<String> {
        val screenKeywords = listOf(
            "mood management", "mood tracking", "sign in", "sign up", "create account",
            "landing", "home", "settings", "profile", "history"
        )
        
        return text.lines()
            .filter { line ->
                val lowerLine = line.lowercase().trim()
                lowerLine.isNotEmpty() &&
                screenKeywords.any { lowerLine.contains(it, ignoreCase = true) }
            }
            .map { it.trim() }
            .distinct()
    }
    
    /**
     * Find button labels in text.
     * Looks for common button text patterns.
     */
    private fun findButtonLabels(text: String): List<String> {
        val buttonKeywords = listOf(
            "save", "cancel", "submit", "create", "delete", "edit",
            "back", "next", "continue", "confirm", "ok", "yes", "no",
            "sign in", "sign up", "login", "account", "get started",
            "add", "new", "view", "history", "settings", "profile"
        )
        
        // Extract button labels from text
        val buttons = mutableListOf<String>()
        
        // Method 1: Look for lines containing button keywords
        text.lines().forEach { line ->
            val lowerLine = line.lowercase().trim()
            if (lowerLine.isNotEmpty() && buttonKeywords.any { lowerLine.contains(it, ignoreCase = true) }) {
                // Extract the button text (take first few words, up to 5 words)
                val words = line.trim().split(Regex("\\s+")).take(5)
                if (words.isNotEmpty()) {
                    buttons.add(words.joinToString(" "))
                }
            }
        }
        
        // Method 2: Look for standalone button keywords in text
        buttonKeywords.forEach { keyword ->
            val regex = Regex("\\b$keyword\\b", RegexOption.IGNORE_CASE)
            if (regex.find(text) != null && !buttons.any { it.equals(keyword, ignoreCase = true) }) {
                buttons.add(keyword.capitalize())
            }
        }
        
        return buttons.distinct()
    }
    
    /**
     * Find loading indicators in text.
     * Looks for loading-related text.
     */
    private fun findLoadingIndicators(text: String): List<String> {
        val loadingKeywords = listOf(
            "loading", "please wait", "processing", "saving", "uploading"
        )
        
        return text.lines()
            .filter { line ->
                val lowerLine = line.lowercase().trim()
                lowerLine.isNotEmpty() &&
                loadingKeywords.any { lowerLine.contains(it, ignoreCase = true) }
            }
            .map { it.trim() }
            .distinct()
    }
    
    /**
     * Extract text regions with bounding boxes using Tesseract TSV output.
     * This allows us to detect interactive elements (cards, buttons) based on text location and size.
     */
    private fun extractTextRegions(screenshot: File): List<TextRegion> {
        if (!tesseractAvailable) {
            return emptyList()
        }
        
        return try {
            val tempTsv = File.createTempFile("ocr_tsv", ".tsv")
            tempTsv.deleteOnExit()
            
            val tesseractTsvFile = tempTsv.absolutePath.replace(".tsv", "")
            
            // Run Tesseract with TSV output (includes bounding boxes)
            val process = ProcessBuilder(
                "tesseract",
                screenshot.absolutePath,
                tesseractTsvFile,
                "-l", "eng",
                "tsv" // TSV output format includes bounding boxes
            )
                .redirectErrorStream(true)
                .start()
            
            val exitCode = process.waitFor()
            
            if (exitCode != 0) {
                logger.debug("Tesseract TSV extraction failed, falling back to text-only")
                return emptyList()
            }
            
            // Read TSV file
            val tsvFile = File("$tesseractTsvFile.tsv")
            if (!tsvFile.exists()) {
                logger.debug("TSV output file not found, falling back to text-only")
                return emptyList()
            }
            
            val lines = tsvFile.readLines()
            if (lines.size < 2) {
                return emptyList()
            }
            
            // Parse TSV (skip header line)
            val regions = mutableListOf<TextRegion>()
            lines.drop(1).forEach { line ->
                val parts = line.split("\t")
                if (parts.size >= 12) {
                    try {
                        val level = parts[0].toIntOrNull() ?: return@forEach
                        // Level 5 = word level (good for detecting interactive elements)
                        if (level == 5) {
                            val text = parts[11].trim()
                            if (text.isNotEmpty() && text.length > 2) { // Filter out very short text
                                val x = parts[6].toIntOrNull() ?: return@forEach
                                val y = parts[7].toIntOrNull() ?: return@forEach
                                val width = parts[8].toIntOrNull() ?: return@forEach
                                val height = parts[9].toIntOrNull() ?: return@forEach
                                val confidence = parts[10].toDoubleOrNull() ?: 0.0
                                
                                regions.add(
                                    TextRegion(
                                        text = text,
                                        x = x,
                                        y = y,
                                        width = width,
                                        height = height,
                                        confidence = confidence
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) {
                        // Skip invalid lines
                    }
                }
            }
            
            logger.debug("Extracted ${regions.size} text regions with bounding boxes")
            regions
        } catch (e: Exception) {
            logger.debug("Failed to extract text regions: ${e.message}")
            emptyList()
        }
    }
}
