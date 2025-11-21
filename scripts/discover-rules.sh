#!/bin/bash

# Rule Discovery Script
# Helps discover relevant cursor rules for a given task

set -e

RULES_DIR=".cursor/rules"
SEARCH_TERM="${1:-}"

if [ -z "$SEARCH_TERM" ]; then
    echo "Usage: $0 <search-term>"
    echo ""
    echo "Examples:"
    echo "  $0 error"
    echo "  $0 branch"
    echo "  $0 test"
    echo "  $0 accessibility"
    echo ""
    echo "Available rules:"
    ls -1 "$RULES_DIR"/*.mdc 2>/dev/null | sed 's|.*/||' | sed 's|\.mdc||' | sed 's/^/  - /'
    exit 0
fi

echo "🔍 Searching cursor rules for: '$SEARCH_TERM'"
echo ""

FOUND=0

for rule_file in "$RULES_DIR"/*.mdc; do
    if [ ! -f "$rule_file" ]; then
        continue
    fi
    
    rule_name=$(basename "$rule_file" .mdc)
    
    # Search in file content (case-insensitive)
    if grep -qi "$SEARCH_TERM" "$rule_file"; then
        echo "📋 $rule_name"
        echo "   File: $rule_file"
        
        # Extract key points (lines with ✅ or CRITICAL)
        KEY_POINTS=$(grep -iE "(CRITICAL|✅|REQUIRED|MUST|ALWAYS)" "$rule_file" | head -3 | sed 's/^/   → /')
        if [ -n "$KEY_POINTS" ]; then
            echo "$KEY_POINTS"
        fi
        
        # Show first few lines of description
        FIRST_LINE=$(head -5 "$rule_file" | grep -v "^---" | grep -v "^$" | head -1)
        if [ -n "$FIRST_LINE" ]; then
            echo "   $FIRST_LINE"
        fi
        
        echo ""
        FOUND=$((FOUND + 1))
    fi
done

if [ $FOUND -eq 0 ]; then
    echo "❌ No rules found matching '$SEARCH_TERM'"
    echo ""
    echo "Available rules:"
    ls -1 "$RULES_DIR"/*.mdc 2>/dev/null | sed 's|.*/||' | sed 's|\.mdc||' | sed 's/^/  - /'
else
    echo "✅ Found $FOUND matching rule(s)"
    echo ""
    echo "💡 Tip: Read the full rule file for complete guidance"
fi

