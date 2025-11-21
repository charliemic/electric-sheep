#!/bin/bash
# Convert Markdown to both DOCX and HTML, sync to CloudFiles
# Maintains parallel formats for different sharing needs

set -e

INPUT_FILE="${1:-docs/learning/A_WEEK_WITH_AI_CODING.md}"

echo "Converting to both formats..."
echo ""

# Generate DOCX
./scripts/md-to-google-doc.sh "$INPUT_FILE"

echo ""

# Generate HTML
./scripts/md-to-html.sh "$INPUT_FILE"

echo ""
echo "✓ Both formats created in CloudFiles"
echo ""
echo "DOCX: For editing in Google Docs"
echo "HTML: For sharing/viewing in browser"

