#!/bin/bash
# Convert Markdown to DOCX and sync to Google Drive CloudFiles folder
# Direct workflow: Markdown → DOCX → Google Drive → Google Docs

set -e

INPUT_FILE="${1:-docs/learning/A_WEEK_WITH_AI_CODING.md}"
CLOUDFILES="${CLOUDFILES:-$HOME/CloudFiles}"

if [ ! -f "$INPUT_FILE" ]; then
    echo "Error: Input file not found: $INPUT_FILE"
    exit 1
fi

# Get title from first heading, create shorter filename
FULL_TITLE=$(grep -m1 '^# ' "$INPUT_FILE" | sed 's/^# //' || basename "$INPUT_FILE" .md | sed 's/_/ /g')
# Create shorter, more natural filename (first 4-5 words, lowercase, hyphens)
# Remove subtitle after colon, take first few words
SHORT_TITLE=$(echo "$FULL_TITLE" | sed 's/:.*//' | awk '{for(i=1;i<=5;i++) if($i) printf "%s ", tolower($i)}' | sed 's/ $//' | sed 's/ /-/g' | sed 's/--*/-/g')
OUTPUT_FILE="$CLOUDFILES/$SHORT_TITLE.docx"

echo "Converting: $INPUT_FILE → $OUTPUT_FILE"
python3 scripts/convert-to-docx.py "$INPUT_FILE" "$OUTPUT_FILE"

echo ""
echo "✓ DOCX file created in CloudFiles"
echo "  File: $OUTPUT_FILE"
echo ""
echo "Next steps:"
echo "1. Wait for Google Drive to sync (check Drive icon in menu bar)"
echo "2. Go to https://drive.google.com"
echo "3. Find '$TITLE.docx'"
echo "4. Right-click → Open with → Google Docs"
echo "   (or double-click to open in Google Docs)"
echo ""
echo "The file will be converted to a native Google Doc automatically!"

