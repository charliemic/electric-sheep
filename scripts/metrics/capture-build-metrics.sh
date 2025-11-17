#!/bin/bash

# Capture build metrics and store them
# Usage: ./scripts/metrics/capture-build-metrics.sh [build-type]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
BUILD_TYPE=${1:-debug}
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# Run build and capture metrics
START_TIME=$(date +%s)
BUILD_OUTPUT=$(cd "$PROJECT_ROOT" && ./gradlew assemble${BUILD_TYPE^} --no-daemon 2>&1)
BUILD_EXIT_CODE=$?
END_TIME=$(date +%s)
EXECUTION_TIME=$((END_TIME - START_TIME))

# Get Gradle and Java versions
GRADLE_VERSION=$(cd "$PROJECT_ROOT" && ./gradlew --version 2>/dev/null | grep "Gradle" | head -1 | awk '{print $2}' || echo "unknown")
JAVA_VERSION=$(java -version 2>&1 | head -1 | awk -F '"' '{print $2}' || echo "unknown")

# Count warnings and errors from build output
WARNING_COUNT=$(echo "$BUILD_OUTPUT" | grep -i "warning" | wc -l | tr -d ' ')
ERROR_COUNT=$(echo "$BUILD_OUTPUT" | grep -i "error" | wc -l | tr -d ' ')

# Create metrics JSON
METRICS_JSON=$(cat <<EOF
{
  "timestamp": "$TIMESTAMP",
  "buildType": "$BUILD_TYPE",
  "success": $([ $BUILD_EXIT_CODE -eq 0 ] && echo "true" || echo "false"),
  "executionTimeSeconds": $EXECUTION_TIME,
  "gradleVersion": "$GRADLE_VERSION",
  "javaVersion": "$JAVA_VERSION",
  "warningCount": $WARNING_COUNT,
  "errorCount": $ERROR_COUNT,
  "exitCode": $BUILD_EXIT_CODE
}
EOF
)

# Store metrics
echo "$METRICS_JSON" > "$PROJECT_ROOT/.build-info.tmp"

# Also output for immediate use
echo "$METRICS_JSON"

exit $BUILD_EXIT_CODE

