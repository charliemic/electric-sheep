#!/bin/bash

# Capture accessibility metrics from lint output
# Usage: ./scripts/metrics/capture-accessibility-metrics.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
METRICS_DIR="$PROJECT_ROOT/development-metrics"
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# Run accessibility lint check
LINT_OUTPUT=$(cd "$PROJECT_ROOT" && ./gradlew lint -Pandroid.lint.checkAccessibility=true --no-daemon 2>&1 || true)

# Count accessibility violations
# Look for accessibility-related warnings/errors in lint output
ACCESSIBILITY_VIOLATIONS=$(echo "$LINT_OUTPUT" | grep -i "accessibility\|ContentDescription\|TouchTargetSize\|IconColors\|TextContrast" | wc -l | tr -d ' ')

# Count by severity if available
ERROR_COUNT=$(echo "$LINT_OUTPUT" | grep -i "error.*accessibility" | wc -l | tr -d ' ')
WARNING_COUNT=$(echo "$LINT_OUTPUT" | grep -i "warning.*accessibility" | wc -l | tr -d ' ')

# Create metrics JSON
METRICS_JSON=$(cat <<EOF
{
  "timestamp": "$TIMESTAMP",
  "totalViolations": $ACCESSIBILITY_VIOLATIONS,
  "errorCount": $ERROR_COUNT,
  "warningCount": $WARNING_COUNT
}
EOF
)

# Store metrics
echo "$METRICS_JSON" > "$METRICS_DIR/accessibility/accessibility_${TIMESTAMP}.json"

echo "$METRICS_JSON"

