#!/bin/bash

# Capture all available metrics
# This is the main script to run after development work

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "📊 Collecting development metrics..."

# Capture complexity metrics
echo "  - Code complexity..."
"$SCRIPT_DIR/capture-complexity-metrics.sh" > /dev/null

# Capture accessibility metrics
echo "  - Accessibility..."
"$SCRIPT_DIR/capture-accessibility-metrics.sh" > /dev/null

# Note: Build and test metrics should be captured during their execution
# This script is for metrics that don't require running builds/tests

echo "✅ Metrics collection complete"

