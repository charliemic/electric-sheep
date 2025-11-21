#!/bin/bash

# Check for Existing Artifacts
# Helps prevent duplication by searching for similar files/scripts/docs before creating new ones
#
# Usage:
#   ./scripts/check-existing-artifacts.sh <keyword>
#   ./scripts/check-existing-artifacts.sh "google-docs"
#   ./scripts/check-existing-artifacts.sh "emulator"

set -e

if [ $# -eq 0 ]; then
    echo "Usage: $0 <keyword>"
    echo ""
    echo "Searches for existing artifacts (scripts, docs, code) matching the keyword."
    echo "Use this BEFORE creating new files to avoid duplication."
    echo ""
    echo "Examples:"
    echo "  $0 google-docs"
    echo "  $0 emulator"
    echo "  $0 aws-setup"
    exit 1
fi

KEYWORD="$1"

echo "╔════════════════════════════════════════════════════════════╗"
echo "║      CHECKING FOR EXISTING ARTIFACTS: $KEYWORD            ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

# 1. File name search (scripts)
echo "📜 Scripts matching '$KEYWORD':"
echo "─────────────────────────────────────────────────────────────"
SCRIPTS=$(find scripts -type f \( -name "*.sh" -o -name "*.py" \) -iname "*${KEYWORD}*" 2>/dev/null | grep -v "/\.git/" || true)
if [ -n "$SCRIPTS" ]; then
    echo "$SCRIPTS" | while read -r script; do
        echo "  ✅ $script"
        # Show first few lines to understand purpose
        if [ -f "$script" ]; then
            HEAD=$(head -n 3 "$script" 2>/dev/null | grep -v "^#!/" | grep -v "^$" | head -n 1 || true)
            if [ -n "$HEAD" ]; then
                echo "     └─ ${HEAD:0:70}..."
            fi
        fi
    done
else
    echo "  (none found)"
fi
echo ""

# 2. File name search (documentation)
echo "📚 Documentation matching '$KEYWORD':"
echo "─────────────────────────────────────────────────────────────"
DOCS=$(find docs -type f -name "*.md" -iname "*${KEYWORD}*" 2>/dev/null | grep -v "/\.git/" || true)
if [ -n "$DOCS" ]; then
    echo "$DOCS" | while read -r doc; do
        echo "  ✅ $doc"
        # Show title if available
        if [ -f "$doc" ]; then
            TITLE=$(head -n 5 "$doc" 2>/dev/null | grep "^#" | head -n 1 | sed 's/^#* *//' || true)
            if [ -n "$TITLE" ]; then
                echo "     └─ ${TITLE:0:70}..."
            fi
        fi
    done
else
    echo "  (none found)"
fi
echo ""

# 3. Content search (grep in scripts)
echo "🔍 Scripts containing '$KEYWORD' (content search):"
echo "─────────────────────────────────────────────────────────────"
SCRIPT_CONTENT=$(grep -r -l "$KEYWORD" scripts/ --include="*.sh" --include="*.py" 2>/dev/null | grep -v "/\.git/" || true)
if [ -n "$SCRIPT_CONTENT" ]; then
    echo "$SCRIPT_CONTENT" | while read -r script; do
        echo "  ✅ $script"
        # Show context
        MATCH=$(grep -i "$KEYWORD" "$script" 2>/dev/null | head -n 1 | sed 's/^[[:space:]]*//' || true)
        if [ -n "$MATCH" ]; then
            echo "     └─ ${MATCH:0:70}..."
        fi
    done
else
    echo "  (none found)"
fi
echo ""

# 4. Content search (grep in docs)
echo "🔍 Documentation containing '$KEYWORD' (content search):"
echo "─────────────────────────────────────────────────────────────"
DOC_CONTENT=$(grep -r -l "$KEYWORD" docs/ --include="*.md" 2>/dev/null | grep -v "/\.git/" || true)
if [ -n "$DOC_CONTENT" ]; then
    echo "$DOC_CONTENT" | while read -r doc; do
        echo "  ✅ $doc"
        # Show context
        MATCH=$(grep -i "$KEYWORD" "$doc" 2>/dev/null | head -n 1 | sed 's/^[[:space:]]*//' || true)
        if [ -n "$MATCH" ]; then
            echo "     └─ ${MATCH:0:70}..."
        fi
    done
else
    echo "  (none found)"
fi
echo ""

# 5. Code search (Kotlin files)
echo "💻 Code files matching '$KEYWORD':"
echo "─────────────────────────────────────────────────────────────"
CODE_FILES=$(find app/src -type f -name "*.kt" -iname "*${KEYWORD}*" 2>/dev/null | grep -v "/\.git/" || true)
if [ -n "$CODE_FILES" ]; then
    echo "$CODE_FILES" | head -n 10 | while read -r file; do
        echo "  ✅ $file"
    done
    REMAINING=$(echo "$CODE_FILES" | wc -l | tr -d ' ')
    if [ "$REMAINING" -gt 10 ]; then
        echo "  ... and $((REMAINING - 10)) more"
    fi
else
    echo "  (none found)"
fi
echo ""

# Summary
echo "╔════════════════════════════════════════════════════════════╗"
echo "║                    SUMMARY                                  ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

TOTAL_SCRIPTS=$(echo "$SCRIPTS" | grep -c . || echo "0")
TOTAL_DOCS=$(echo "$DOCS" | grep -c . || echo "0")
TOTAL_SCRIPT_CONTENT=$(echo "$SCRIPT_CONTENT" | grep -c . || echo "0")
TOTAL_DOC_CONTENT=$(echo "$DOC_CONTENT" | grep -c . || echo "0")
TOTAL_CODE=$(echo "$CODE_FILES" | grep -c . || echo "0")

TOTAL=$((TOTAL_SCRIPTS + TOTAL_DOCS + TOTAL_SCRIPT_CONTENT + TOTAL_DOC_CONTENT + TOTAL_CODE))

if [ "$TOTAL" -gt 0 ]; then
    echo "⚠️  Found $TOTAL existing artifact(s) matching '$KEYWORD'"
    echo ""
    echo "💡 Before creating a new artifact:"
    echo "   1. Review the existing artifacts above"
    echo "   2. Determine if you can extend an existing one instead"
    echo "   3. If creating new, document why existing cannot be used"
    echo "   4. See .cursor/rules/artifact-duplication.mdc for guidelines"
    echo ""
    echo "📖 Decision tree:"
    echo "   - Same purpose? → EXTEND existing"
    echo "   - Different purpose? → CREATE new (document why)"
    echo "   - Duplicates found? → CONSOLIDATE"
else
    echo "✅ No existing artifacts found matching '$KEYWORD'"
    echo ""
    echo "💡 You can proceed with creating new artifacts, but:"
    echo "   1. Verify search terms are appropriate"
    echo "   2. Check related directories manually"
    echo "   3. Consider semantic search for similar functionality"
fi

echo ""

