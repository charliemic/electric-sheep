#!/usr/bin/env bash
# Parse test results from JUnit XML format and output JSON
# Usage: ./scripts/parse-test-results.sh <test-results-directory>
# Output: JSON with total, passed, failed, skipped counts

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

TEST_RESULTS_DIR="${1:-}"

if [ -z "$TEST_RESULTS_DIR" ]; then
    echo -e "${RED}Error: Test results directory not provided${NC}" >&2
    echo "Usage: $0 <test-results-directory>" >&2
    exit 1
fi

if [ ! -d "$TEST_RESULTS_DIR" ]; then
    echo -e "${YELLOW}Warning: Test results directory not found: $TEST_RESULTS_DIR${NC}" >&2
    echo '{"total": 0, "passed": 0, "failed": 0, "skipped": 0}'
    exit 0
fi

# Check if jq is available (for JSON output)
if ! command -v jq &> /dev/null; then
    echo -e "${YELLOW}Warning: jq not found, using basic parsing${NC}" >&2
    USE_JQ=false
else
    USE_JQ=true
fi

# Initialize counters
TOTAL=0
PASSED=0
FAILED=0
SKIPPED=0

# Parse all XML test result files
# JUnit XML format: <testsuite tests="X" failures="Y" skipped="Z">
find "$TEST_RESULTS_DIR" -name "*.xml" -type f | while read -r xml_file; do
    # Extract test counts from XML
    # Handle both <testsuite> and <testsuites> formats
    TESTS=$(grep -o 'tests="[0-9]*"' "$xml_file" 2>/dev/null | grep -o '[0-9]*' | head -1 || echo "0")
    FAILURES=$(grep -o 'failures="[0-9]*"' "$xml_file" 2>/dev/null | grep -o '[0-9]*' | head -1 || echo "0")
    ERRORS=$(grep -o 'errors="[0-9]*"' "$xml_file" 2>/dev/null | grep -o '[0-9]*' | head -1 || echo "0")
    SKIP=$(grep -o 'skipped="[0-9]*"' "$xml_file" 2>/dev/null | grep -o '[0-9]*' | head -1 || echo "0")
    
    # Convert to integers (handle empty values)
    TESTS=${TESTS:-0}
    FAILURES=${FAILURES:-0}
    ERRORS=${ERRORS:-0}
    SKIP=${SKIP:-0}
    
    # Accumulate counts
    TOTAL=$((TOTAL + TESTS))
    FAILED=$((FAILED + FAILURES + ERRORS))
    SKIPPED=$((SKIPPED + SKIP))
done

# Calculate passed tests
PASSED=$((TOTAL - FAILED - SKIPPED))

# Ensure non-negative values
if [ $PASSED -lt 0 ]; then
    PASSED=0
fi

# Output JSON
if [ "$USE_JQ" = true ]; then
    jq -n \
        --argjson total "$TOTAL" \
        --argjson passed "$PASSED" \
        --argjson failed "$FAILED" \
        --argjson skipped "$SKIPPED" \
        '{total: $total, passed: $passed, failed: $failed, skipped: $skipped}'
else
    # Basic JSON output without jq
    echo "{\"total\": $TOTAL, \"passed\": $PASSED, \"failed\": $FAILED, \"skipped\": $SKIPPED}"
fi
