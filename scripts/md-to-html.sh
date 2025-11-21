#!/bin/bash
# Convert Markdown to HTML for sharing
# Generates clean HTML that can be shared via Google Drive or Google Sites

set -e

INPUT_FILE="${1:-docs/learning/A_WEEK_WITH_AI_CODING.md}"
CLOUDFILES="${CLOUDFILES:-$HOME/CloudFiles}"

if [ ! -f "$INPUT_FILE" ]; then
    echo "Error: Input file not found: $INPUT_FILE"
    exit 1
fi

# Get title from first heading
TITLE=$(grep -m1 '^# ' "$INPUT_FILE" | sed 's/^# //' || basename "$INPUT_FILE" .md | sed 's/_/ /g')
# Create shorter filename
SHORT_TITLE=$(echo "$TITLE" | sed 's/:.*//' | awk '{for(i=1;i<=5;i++) if($i) printf "%s ", tolower($i)}' | sed 's/ $//' | sed 's/ /-/g' | sed 's/--*/-/g')
OUTPUT_FILE="$CLOUDFILES/$SHORT_TITLE.html"

echo "Converting: $INPUT_FILE → $OUTPUT_FILE"

# Check if pandoc is available (better conversion)
if command -v pandoc &> /dev/null; then
    pandoc "$INPUT_FILE" -o "$OUTPUT_FILE" \
        --standalone \
        --css=https://cdn.jsdelivr.net/npm/water.css@2/out/water.css \
        --metadata title="$TITLE"
    echo "✓ HTML file created with pandoc"
else
    # Fallback: use Python script
    python3 scripts/md-to-html.py "$INPUT_FILE" "$OUTPUT_FILE" "$TITLE"
fi

echo ""
echo "✓ HTML file created in CloudFiles"
echo "  File: $OUTPUT_FILE"
echo ""
echo "Sharing options:"
echo "1. Google Drive: Upload to Drive, right-click → Share"
echo "2. Google Sites: Create site, embed HTML or link to Drive file"
echo "3. Direct link: Share the Drive file link (viewers can download HTML)"

