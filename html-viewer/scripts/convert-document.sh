#!/bin/bash
# Convert a document to HTML using the HTML Viewer tool
# This script is available both locally and in CI/CD pipelines

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
HTML_VIEWER_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
PROJECT_ROOT="$(cd "$HTML_VIEWER_DIR/.." && pwd)"

# Usage: convert-document.sh <input-file> <output-file> [options]
INPUT_FILE="${1}"
OUTPUT_FILE="${2}"
TITLE="${3:-$(basename "$INPUT_FILE" .md)}"
THEME="${4:-light}"

if [ -z "$INPUT_FILE" ] || [ -z "$OUTPUT_FILE" ]; then
  echo "Usage: $0 <input-file> <output-file> [title] [theme]"
  echo ""
  echo "Examples:"
  echo "  $0 docs/learning/A_WEEK_WITH_AI_CODING.md output.html"
  echo "  $0 docs/learning/A_WEEK_WITH_AI_CODING.md output.html 'My Document' dark"
  exit 1
fi

# Resolve paths relative to project root
if [ ! -f "$INPUT_FILE" ]; then
  # Try relative to project root
  INPUT_FILE="$PROJECT_ROOT/$INPUT_FILE"
fi

if [ ! -f "$INPUT_FILE" ]; then
  echo "Error: Input file not found: $INPUT_FILE"
  exit 1
fi

# Ensure output directory exists
OUTPUT_DIR=$(dirname "$OUTPUT_FILE")
if [ ! -d "$OUTPUT_DIR" ]; then
  mkdir -p "$OUTPUT_DIR"
fi

echo "📄 Converting: $INPUT_FILE"
echo "📤 Output: $OUTPUT_FILE"
echo "📝 Title: $TITLE"
echo "🎨 Theme: $THEME"

# Check if we're in a CI environment or have Node.js available
if command -v node &> /dev/null && [ -f "$HTML_VIEWER_DIR/package.json" ]; then
  # Use the HTML Viewer tool (Node.js/Astro)
  echo "Using HTML Viewer tool..."
  
  cd "$HTML_VIEWER_DIR"
  
  # Install dependencies if needed
  if [ ! -d "node_modules" ]; then
    echo "Installing dependencies..."
    npm ci --silent
  fi
  
  # Create a temporary markdown file with metadata
  TEMP_MD=$(mktemp)
  cat > "$TEMP_MD" <<EOF
---
title: "$TITLE"
theme: "$THEME"
---

$(cat "$INPUT_FILE")
EOF
  
  # Use Node.js to convert (we'll create a simple converter script)
  node -e "
    const fs = require('fs');
    const { marked } = require('marked');
    const md = fs.readFileSync('$TEMP_MD', 'utf-8');
    // Simple conversion - in production, use the full Astro tool
    const html = marked.parse(md);
    fs.writeFileSync('$OUTPUT_FILE', html);
  " 2>/dev/null || {
    # Fallback: use existing md-to-html.py if available
    echo "Falling back to Python converter..."
    if [ -f "$PROJECT_ROOT/scripts/md-to-html.py" ]; then
      python3 "$PROJECT_ROOT/scripts/md-to-html.py" "$INPUT_FILE" "$OUTPUT_FILE" "$TITLE"
    else
      echo "Error: No converter available. Install Node.js or use md-to-html.py"
      exit 1
    fi
  }
  
  rm -f "$TEMP_MD"
else
  # Fallback to existing Python script
  echo "Using Python converter (fallback)..."
  if [ -f "$PROJECT_ROOT/scripts/md-to-html.py" ]; then
    python3 "$PROJECT_ROOT/scripts/md-to-html.py" "$INPUT_FILE" "$OUTPUT_FILE" "$TITLE"
  else
    echo "Error: No converter available. Install Node.js or ensure md-to-html.py exists"
    exit 1
  fi
fi

echo "✅ Conversion complete: $OUTPUT_FILE"

