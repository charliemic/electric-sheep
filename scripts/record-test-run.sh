#!/bin/bash
# Record test run results to metrics schema
# Usage: ./scripts/record-test-run.sh <run_id> <total> <passed> <failed> <skipped> [execution_time] [test_type] [branch] [commit] [pr_number]
#
# Example: ./scripts/record-test-run.sh "test-run-123" 150 145 3 2 120 "all" "feature/add-x" "abc123" 42

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Source shared library
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/lib/supabase-postgrest.sh"

# Validate arguments
if [ $# -lt 5 ]; then
    echo -e "${RED}Error: Missing required arguments${NC}" >&2
    echo "Usage: $0 <run_id> <total> <passed> <failed> <skipped> [execution_time] [test_type] [branch] [commit] [pr_number]" >&2
    exit 1
fi

RUN_ID="$1"
TOTAL_TESTS="$2"
PASSED_TESTS="$3"
FAILED_TESTS="$4"
SKIPPED_TESTS="$5"
EXECUTION_TIME="${6:-null}"
TEST_TYPE="${7:-all}"
BRANCH_NAME="${8:-}"
COMMIT_SHA="${9:-}"
PR_NUMBER="${10:-}"

# Validate numeric arguments
for arg in "$TOTAL_TESTS" "$PASSED_TESTS" "$FAILED_TESTS" "$SKIPPED_TESTS"; do
    if ! [[ "$arg" =~ ^[0-9]+$ ]]; then
        echo -e "${RED}Error: Test counts must be numeric: $arg${NC}" >&2
        exit 1
    fi
done

# Validate test counts
if [ $((PASSED_TESTS + FAILED_TESTS + SKIPPED_TESTS)) -gt "$TOTAL_TESTS" ]; then
    echo -e "${RED}Error: Sum of passed/failed/skipped ($((PASSED_TESTS + FAILED_TESTS + SKIPPED_TESTS))) exceeds total ($TOTAL_TESTS)${NC}" >&2
    exit 1
fi

# Validate test type
if [[ ! "$TEST_TYPE" =~ ^(unit|integration|e2e|all)$ ]]; then
    echo -e "${RED}Error: Invalid test_type: $TEST_TYPE${NC}" >&2
    echo "Valid types: unit, integration, e2e, all" >&2
    exit 1
fi

# Build JSON payload
JSON_PAYLOAD=$(cat <<EOF
{
  "run_id": "$RUN_ID",
  "total_tests": $TOTAL_TESTS,
  "passed_tests": $PASSED_TESTS,
  "failed_tests": $FAILED_TESTS,
  "skipped_tests": $SKIPPED_TESTS,
  "execution_time_seconds": $EXECUTION_TIME,
  "test_type": "$TEST_TYPE",
  "branch_name": $(echo "$BRANCH_NAME" | jq -R .),
  "commit_sha": $(echo "$COMMIT_SHA" | jq -R .),
  "pr_number": ${PR_NUMBER:-null}
}
EOF
)

# Insert into metrics.test_runs (PostgREST uses schema-qualified table name)
if postgrest_insert "metrics.test_runs" "$JSON_PAYLOAD" "true"; then
    # Calculate pass rate for display
    if [ "$TOTAL_TESTS" -gt 0 ]; then
        PASS_RATE=$(echo "scale=2; $PASSED_TESTS * 100 / $TOTAL_TESTS" | bc)
        echo -e "${GREEN}✓ Recorded test run: $RUN_ID (Pass rate: ${PASS_RATE}%)${NC}"
    else
        echo -e "${GREEN}✓ Recorded test run: $RUN_ID${NC}"
    fi
    exit 0
else
    echo -e "${RED}✗ Failed to record test run${NC}" >&2
    exit 1
fi

