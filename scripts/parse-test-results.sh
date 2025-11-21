#!/bin/bash
# Parse test results from Gradle XML output
# Usage: ./scripts/parse-test-results.sh [test-results-dir]
# Default: app/build/test-results/test

set -e

TEST_RESULTS_DIR="${1:-app/build/test-results/test}"

if [ ! -d "$TEST_RESULTS_DIR" ]; then
    echo "Error: Test results directory not found: $TEST_RESULTS_DIR" >&2
    exit 1
fi

# Initialize counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0
SKIPPED_TESTS=0

# Parse all XML files in test-results directory
for xml_file in "$TEST_RESULTS_DIR"/*.xml; do
    if [ ! -f "$xml_file" ]; then
        continue
    fi
    
    # Extract test counts from XML (using grep/sed as XML parsing)
    # Format: <testsuite tests="X" failures="Y" skipped="Z">
    TESTS=$(grep -oE 'tests="[0-9]+"' "$xml_file" | grep -oE '[0-9]+' | head -1 || echo "0")
    FAILURES=$(grep -oE 'failures="[0-9]+"' "$xml_file" | grep -oE '[0-9]+' | head -1 || echo "0")
    SKIPPED=$(grep -oE 'skipped="[0-9]+"' "$xml_file" | grep -oE '[0-9]+' | head -1 || echo "0")
    
    # Calculate passed tests
    PASSED=$((TESTS - FAILURES - SKIPPED))
    
    TOTAL_TESTS=$((TOTAL_TESTS + TESTS))
    PASSED_TESTS=$((PASSED_TESTS + PASSED))
    FAILED_TESTS=$((FAILED_TESTS + FAILURES))
    SKIPPED_TESTS=$((SKIPPED_TESTS + SKIPPED))
done

# Output as JSON for easy parsing
cat <<EOF
{
  "total": $TOTAL_TESTS,
  "passed": $PASSED_TESTS,
  "failed": $FAILED_TESTS,
  "skipped": $SKIPPED_TESTS
}
EOF

