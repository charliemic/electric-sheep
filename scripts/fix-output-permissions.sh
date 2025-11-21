#!/bin/bash

# Fix permissions for all output files and directories
# Ensures user has read/write access to all generated files

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "🔧 Fixing permissions for output files and directories..."
echo ""

# Fix permissions for test output directories
if [ -d "$PROJECT_ROOT/test-automation/test-results" ]; then
    echo "📁 Fixing test-results directory..."
    chmod -R u+rwX "$PROJECT_ROOT/test-automation/test-results"
    find "$PROJECT_ROOT/test-automation/test-results" -type f -exec chmod 644 {} \;
    find "$PROJECT_ROOT/test-automation/test-results" -type d -exec chmod 755 {} \;
fi

# Fix permissions for screenshots
if [ -d "$PROJECT_ROOT/test-automation/test-results/screenshots" ]; then
    echo "📸 Fixing screenshots directory..."
    chmod -R u+rwX "$PROJECT_ROOT/test-automation/test-results/screenshots"
    find "$PROJECT_ROOT/test-automation/test-results/screenshots" -type f -exec chmod 644 {} \;
fi

# Fix permissions for reports
if [ -d "$PROJECT_ROOT/test-automation/test-results/reports" ]; then
    echo "📄 Fixing reports directory..."
    chmod -R u+rwX "$PROJECT_ROOT/test-automation/test-results/reports"
    find "$PROJECT_ROOT/test-automation/test-results/reports" -type f -exec chmod 644 {} \;
fi

# Fix permissions for logs
if [ -d "$PROJECT_ROOT/test-automation/test-results/logs" ]; then
    echo "📝 Fixing logs directory..."
    chmod -R u+rwX "$PROJECT_ROOT/test-automation/test-results/logs"
    find "$PROJECT_ROOT/test-automation/test-results/logs" -type f -exec chmod 644 {} \;
fi

# Fix permissions for /tmp files we create
echo "🔧 Fixing /tmp files..."
chmod 644 /tmp/cognitive-journey.md 2>/dev/null || true
chmod 644 /tmp/test-journey-narrative.md 2>/dev/null || true
chmod 644 /tmp/test-visual-journey.html 2>/dev/null || true
chmod 644 /tmp/test-final-run-*.log 2>/dev/null || true
chmod 644 /tmp/test-narrative-*.md 2>/dev/null || true
chmod 644 /tmp/human-readable-test-narrative.log 2>/dev/null || true
chmod 644 /tmp/streaming-test.log 2>/dev/null || true

echo ""
echo "✅ Permissions fixed!"
echo ""
echo "📋 Summary:"
echo "   - All test-results directories: 755 (directories), 644 (files)"
echo "   - All /tmp output files: 644"
echo "   - User has read/write access to all generated files"

