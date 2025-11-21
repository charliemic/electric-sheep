#!/bin/bash

# Helper script to analyze test screenshots using Cursor's vision capabilities
#
# Usage:
#   ./scripts/analyze-screenshot-in-cursor.sh [screenshot-path]
#   ./scripts/analyze-screenshot-in-cursor.sh  # Opens latest evaluation screenshot

set -e

SCREENSHOT_DIR="test-automation/test-results/screenshots"

if [ -n "$1" ]; then
    SCREENSHOT="$1"
else
    # Find latest evaluation screenshot
    SCREENSHOT=$(find "$SCREENSHOT_DIR" -name "evaluation_*.png" -type f 2>/dev/null | sort -r | head -1)
    
    if [ -z "$SCREENSHOT" ]; then
        # Fall back to latest screenshot
        SCREENSHOT=$(find "$SCREENSHOT_DIR" -name "screenshot_*.png" -type f 2>/dev/null | sort -r | head -1)
    fi
fi

if [ -z "$SCREENSHOT" ] || [ ! -f "$SCREENSHOT" ]; then
    echo "❌ No screenshot found"
    echo "   Run a test first to generate screenshots"
    exit 1
fi

echo "📸 Analyzing screenshot: $(basename "$SCREENSHOT")"
echo ""

# Check for corresponding prompt file
PROMPT_FILE="${SCREENSHOT%.*}_cursor_prompt.txt"
if [ -f "$PROMPT_FILE" ]; then
    echo "📝 Prompt file found: $(basename "$PROMPT_FILE")"
    echo ""
    echo "Prompt content:"
    echo "─────────────────────────────────────────────────────────"
    cat "$PROMPT_FILE"
    echo "─────────────────────────────────────────────────────────"
    echo ""
fi

# Open screenshot in default app (can be opened in Cursor)
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "🖼️  Opening screenshot..."
    open "$SCREENSHOT"
    
    if [ -f "$PROMPT_FILE" ]; then
        echo "📝 Opening prompt file..."
        open -a "Cursor" "$PROMPT_FILE" 2>/dev/null || open "$PROMPT_FILE"
    fi
else
    echo "📸 Screenshot: $SCREENSHOT"
    if [ -f "$PROMPT_FILE" ]; then
        echo "📝 Prompt: $PROMPT_FILE"
    fi
fi

echo ""
echo "💡 In Cursor:"
echo "   1. Open the screenshot file"
echo "   2. Use Cursor's chat to analyze it"
echo "   3. Copy the prompt above and paste it in Cursor chat"
echo ""
echo "Example Cursor prompt:"
echo "   'Analyze this mobile app screenshot. Look for errors, blocking elements, and UI issues.'"



