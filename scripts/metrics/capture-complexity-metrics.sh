#!/bin/bash

# Capture code complexity metrics
# Uses cloc for basic metrics, can be extended with other tools

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
METRICS_DIR="$PROJECT_ROOT/development-metrics"
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# Count source files and lines
SOURCE_FILES=$(find "$PROJECT_ROOT/app/src/main" -name "*.kt" 2>/dev/null | wc -l | tr -d ' ')
TEST_FILES=$(find "$PROJECT_ROOT/app/src/test" -name "*.kt" 2>/dev/null | wc -l | tr -d ' ')

# Count lines of code (simple count, can be enhanced with cloc if available)
MAIN_LINES=$(find "$PROJECT_ROOT/app/src/main" -name "*.kt" -exec wc -l {} + 2>/dev/null | tail -1 | awk '{print $1}' || echo "0")
TEST_LINES=$(find "$PROJECT_ROOT/app/src/test" -name "*.kt" -exec wc -l {} + 2>/dev/null | tail -1 | awk '{print $1}' || echo "0")

# Count classes and functions (simple grep-based approach)
CLASS_COUNT=$(find "$PROJECT_ROOT/app/src/main" -name "*.kt" -exec grep -h "^class \|^object \|^interface " {} + 2>/dev/null | wc -l | tr -d ' ')
FUNCTION_COUNT=$(find "$PROJECT_ROOT/app/src/main" -name "*.kt" -exec grep -h "^fun \|^suspend fun " {} + 2>/dev/null | wc -l | tr -d ' ')

# Create metrics JSON
METRICS_JSON=$(cat <<EOF
{
  "timestamp": "$TIMESTAMP",
  "sourceFiles": $SOURCE_FILES,
  "testFiles": $TEST_FILES,
  "mainLinesOfCode": $MAIN_LINES,
  "testLinesOfCode": $TEST_LINES,
  "totalLinesOfCode": $((MAIN_LINES + TEST_LINES)),
  "classCount": $CLASS_COUNT,
  "functionCount": $FUNCTION_COUNT,
  "testToSourceRatio": $(awk "BEGIN {printf \"%.2f\", $TEST_FILES / ($SOURCE_FILES + 0.0001)}")
}
EOF
)

# Store metrics
echo "$METRICS_JSON" > "$METRICS_DIR/complexity/complexity_${TIMESTAMP}.json"

echo "$METRICS_JSON"

