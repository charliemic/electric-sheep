package com.electricsheep.testautomation.reporting

import org.slf4j.LoggerFactory
import java.io.File

/**
 * Standalone utility to generate PDF from AI-enhanced report.
 */
fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("GeneratePDF")
    
    // Try multiple possible paths (relative to where the script runs from)
    val possiblePaths = listOf(
        "../test-automation/test-results/reports/ai_enhanced_report.md",
        "test-automation/test-results/reports/ai_enhanced_report.md",
        "test-results/reports/ai_enhanced_report.md",
        "../test-automation/test-automation/test-results/reports/ai_enhanced_report.md"
    )
    
    val reportFile = possiblePaths.map { File(it) }.firstOrNull { it.exists() }
    
    if (reportFile == null) {
        // Try absolute path from repo root
        val repoRoot = File(System.getProperty("user.dir"))
        val absolutePath = File(repoRoot, "test-automation/test-automation/test-results/reports/ai_enhanced_report.md")
        if (absolutePath.exists()) {
            val pdfFile = File(absolutePath.parentFile, "${absolutePath.nameWithoutExtension}.pdf")
            val reportContent = absolutePath.readText()
            val pdfGenerator = PDFReportGenerator()
            pdfGenerator.generatePDF(reportContent, pdfFile)
            logger.info("✅ PDF generated: ${pdfFile.absolutePath}")
            println("✅ PDF generated: ${pdfFile.absolutePath}")
            return
        }
        logger.error("Report file not found")
        println("Error: Report file not found")
        return
    }
    
    val pdfFile = File(reportFile.parentFile, "${reportFile.nameWithoutExtension}.pdf")
    
    logger.info("Reading report from: ${reportFile.absolutePath}")
    val reportContent = reportFile.readText()
    
    val pdfGenerator = PDFReportGenerator()
    
    try {
        pdfGenerator.generatePDF(reportContent, pdfFile)
        logger.info("✅ PDF generated successfully: ${pdfFile.absolutePath}")
        println("✅ PDF generated: ${pdfFile.absolutePath}")
    } catch (e: Exception) {
        logger.error("❌ Error generating PDF: ${e.message}", e)
        println("❌ Error generating PDF: ${e.message}")
        e.printStackTrace()
    }
}
