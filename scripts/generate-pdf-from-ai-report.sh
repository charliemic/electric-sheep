#!/bin/bash
# Generate PDF from AI-enhanced report using the PDF generator

cd "$(dirname "$0")/.." || exit 1

REPORT_FILE="test-automation/test-automation/test-results/reports/ai_enhanced_report.md"
OUTPUT_PDF="test-automation/test-automation/test-results/reports/ai_enhanced_report.pdf"

if [ ! -f "$REPORT_FILE" ]; then
    echo "Error: Report file not found: $REPORT_FILE"
    exit 1
fi

# Use a simple approach: convert markdown to text, then use the PDF generator
# For now, we'll use the existing PDF generator with the markdown content as text
cd test-automation || exit 1

# Create a simple Kotlin script to generate PDF
cat > /tmp/generate_pdf.kt << 'EOF'
import com.electricsheep.testautomation.reporting.PDFReportGenerator
import java.io.File

fun main() {
    val reportFile = File("test-automation/test-automation/test-results/reports/ai_enhanced_report.md")
    val pdfFile = File("test-automation/test-automation/test-results/reports/ai_enhanced_report.pdf")
    
    val reportContent = reportFile.readText()
    val pdfGenerator = PDFReportGenerator()
    pdfGenerator.generatePDF(reportContent, pdfFile)
    println("PDF generated: ${pdfFile.absolutePath}")
}
EOF

# For now, let's use a simpler approach - just open the markdown and let the user convert it
# Or we can use pandoc if available
if command -v pandoc &> /dev/null; then
    pandoc "$REPORT_FILE" -o "$OUTPUT_PDF" --pdf-engine=wkhtmltopdf 2>/dev/null || \
    pandoc "$REPORT_FILE" -o "$OUTPUT_PDF" 2>/dev/null || \
    echo "Pandoc not available or failed. Opening markdown file instead."
    if [ -f "$OUTPUT_PDF" ]; then
        open "$OUTPUT_PDF"
    else
        open "$REPORT_FILE"
    fi
else
    echo "Pandoc not available. Opening markdown file."
    open "$REPORT_FILE"
fi



