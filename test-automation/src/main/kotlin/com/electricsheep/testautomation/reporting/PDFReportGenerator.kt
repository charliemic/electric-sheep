package com.electricsheep.testautomation.reporting

import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.TextAlignment
import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Generates PDF versions of test reports.
 */
class PDFReportGenerator {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * Convert a text report to PDF.
     */
    fun generatePDF(textReport: String, outputFile: File) {
        try {
            outputFile.parentFile?.mkdirs()
            val writer = PdfWriter(outputFile)
            val pdf = PdfDocument(writer)
            val document = Document(pdf)
            
            // Parse text report and convert to PDF
            val lines = textReport.lines()
            var inSection = false
            var currentSection = ""
            
            for (line in lines) {
                when {
                    // Title
                    line.startsWith("=".repeat(80)) && !inSection -> {
                        val titleIndex = lines.indexOf(line)
                        if (titleIndex >= 0 && titleIndex < lines.size - 1) {
                            val title = lines[titleIndex + 1]
                            if (title.contains("TEST EXECUTION REPORT", ignoreCase = true)) {
                                document.add(Paragraph(title).setFontSize(20f).setBold().setTextAlignment(TextAlignment.CENTER))
                                document.add(Paragraph(" ").setFontSize(12f))
                            }
                        }
                    }
                    
                    // Section headers
                    line.startsWith("─".repeat(80)) -> {
                        if (inSection && currentSection.isNotEmpty()) {
                            document.add(Paragraph(" ").setFontSize(8f))
                        }
                        inSection = true
                    }
                    
                    line.matches(Regex("[A-Z ]+")) && inSection -> {
                        currentSection = line
                        document.add(Paragraph(line).setFontSize(16f).setBold().setMarginTop(10f))
                    }
                    
                    // Regular content
                    line.isNotBlank() && !line.startsWith("=") && !line.startsWith("─") -> {
                        val paragraph = Paragraph()
                        
                        // Format emojis and special markers
                        when {
                            line.contains("✅") -> {
                                paragraph.add(Text("✅ ").setFontSize(12f))
                                paragraph.add(Text(line.replace("✅", "")).setFontSize(11f))
                            }
                            line.contains("❌") -> {
                                paragraph.add(Text("❌ ").setFontSize(12f))
                                paragraph.add(Text(line.replace("❌", "")).setFontSize(11f).setFontColor(ColorConstants.RED))
                            }
                            line.contains("⚠️") -> {
                                paragraph.add(Text("⚠️ ").setFontSize(12f))
                                paragraph.add(Text(line.replace("⚠️", "")).setFontSize(11f).setFontColor(ColorConstants.ORANGE))
                            }
                            line.startsWith("  ") -> {
                                // Indented content
                                paragraph.add(Text(line).setFontSize(10f))
                                paragraph.setMarginLeft(20f)
                            }
                            line.startsWith("•") || line.startsWith("-") -> {
                                // Bullet point
                                paragraph.add(Text("  • ").setFontSize(10f))
                                paragraph.add(Text(line.substringAfter("•").substringAfter("-").trim()).setFontSize(10f))
                            }
                            else -> {
                                paragraph.add(Text(line).setFontSize(11f))
                            }
                        }
                        
                        document.add(paragraph)
                    }
                }
            }
            
            document.close()
            logger.info("PDF report generated: ${outputFile.absolutePath}")
        } catch (e: Exception) {
            logger.error("Failed to generate PDF report: ${e.message}", e)
            throw e
        }
    }
}

