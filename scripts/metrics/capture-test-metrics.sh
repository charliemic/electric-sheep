#!/bin/bash

# Capture test metrics and store them
# Usage: ./scripts/metrics/capture-test-metrics.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# Run tests and capture metrics
START_TIME=$(date +%s)
TEST_OUTPUT=$(cd "$PROJECT_ROOT" && ./gradlew test --no-daemon 2>&1)
TEST_EXIT_CODE=$?
END_TIME=$(date +%s)
EXECUTION_TIME=$((END_TIME - START_TIME))

# Parse test results from output (macOS-compatible grep)
# Look for test summary in various formats
TOTAL_TESTS="0"
PASSED_TESTS="0"
FAILED_TESTS="0"
SKIPPED_TESTS="0"

# Try to find test counts from Gradle output
# Format: "18 tests completed, 2 failed" or similar
TEST_SUMMARY=$(echo "$TEST_OUTPUT" | grep -iE "tests? completed|BUILD" | tail -1)

if echo "$TEST_SUMMARY" | grep -qiE "tests? completed"; then
    TOTAL_TESTS=$(echo "$TEST_SUMMARY" | grep -oE "[0-9]+ tests? completed" | grep -oE "[0-9]+" | head -1)
    FAILED_TESTS=$(echo "$TEST_SUMMARY" | grep -oE "[0-9]+ failed" | grep -oE "[0-9]+" | head -1)
    PASSED_TESTS=$((TOTAL_TESTS - FAILED_TESTS))
fi

# If still no tests found, check if build was successful (all tests passed)
if [ "$TOTAL_TESTS" = "0" ] && echo "$TEST_OUTPUT" | grep -q "BUILD SUCCESSFUL"; then
    # Count test files to estimate
    TEST_FILES=$(find "$PROJECT_ROOT/app/src/test" -name "*Test.kt" 2>/dev/null | wc -l | tr -d ' ')
    if [ "$TEST_FILES" -gt 0 ]; then
        # Rough estimate - actual count would require parsing test reports
        TOTAL_TESTS="estimated"
        PASSED_TESTS="estimated"
    fi
fi

# Ensure we have valid numbers
TOTAL_TESTS=${TOTAL_TESTS:-0}
PASSED_TESTS=${PASSED_TESTS:-0}
FAILED_TESTS=${FAILED_TESTS:-0}
SKIPPED_TESTS=${SKIPPED_TESTS:-0}

# Try to get coverage if available
COVERAGE_PERCENTAGE="null"
if echo "$TEST_OUTPUT" | grep -q "coverage"; then
    COVERAGE_PERCENTAGE=$(echo "$TEST_OUTPUT" | grep -oE "[0-9]+\.[0-9]+%" | head -1 | sed 's/%//' || echo "null")
fi

# Extract test failures if any
TEST_FAILURES="[]"
if [ "$FAILED_TESTS" != "0" ] && [ "$FAILED_TESTS" != "" ] && [ "$FAILED_TESTS" -gt 0 ] 2>/dev/null; then
    FAILURE_LIST=$(echo "$TEST_OUTPUT" | grep -A 2 "FAILED" | grep -oE "com\.[a-zA-Z0-9_]+\.[a-zA-Z0-9_]+\.[a-zA-Z0-9_]+\.[a-zA-Z0-9_]+" | head -5)
    if [ -n "$FAILURE_LIST" ]; then
        TEST_FAILURES=$(echo "$FAILURE_LIST" | while read line; do echo "\"$line\""; done | tr '\n' ',' | sed 's/,$//' | sed 's/^/[/' | sed 's/$/]/')
    fi
fi

# Create metrics JSON
METRICS_JSON=$(cat <<EOF
{
  "timestamp": "$TIMESTAMP",
  "success": $([ $TEST_EXIT_CODE -eq 0 ] && echo "true" || echo "false"),
  "executionTimeSeconds": $EXECUTION_TIME,
  "totalTests": $TOTAL_TESTS,
  "passedTests": $PASSED_TESTS,
  "failedTests": $FAILED_TESTS,
  "skippedTests": $SKIPPED_TESTS,
  "coveragePercentage": $COVERAGE_PERCENTAGE,
  "testFailures": $TEST_FAILURES,
  "exitCode": $TEST_EXIT_CODE
}
EOF
)

# Store metrics
echo "$METRICS_JSON" > "$PROJECT_ROOT/.test-info.tmp"

# Also output for immediate use
echo "$METRICS_JSON"

exit $TEST_EXIT_CODE

