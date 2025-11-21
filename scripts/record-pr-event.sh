#!/bin/bash
# Record PR lifecycle event to metrics schema
# Usage: ./scripts/record-pr-event.sh <event_type> <pr_number> [pr_title] [branch_name] [author]
#
# Event types: created, reviewed, merged, closed, reopened
# Example: ./scripts/record-pr-event.sh merged 42 "Add feature X" "feature/add-x" "developer"

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
if [ $# -lt 2 ]; then
    echo -e "${RED}Error: Missing required arguments${NC}" >&2
    echo "Usage: $0 <event_type> <pr_number> [pr_title] [branch_name] [author]" >&2
    echo "Event types: created, reviewed, merged, closed, reopened" >&2
    exit 1
fi

EVENT_TYPE="$1"
PR_NUMBER="$2"
PR_TITLE="${3:-}"
BRANCH_NAME="${4:-}"
AUTHOR="${5:-}"

# Validate event type
if [[ ! "$EVENT_TYPE" =~ ^(created|reviewed|merged|closed|reopened)$ ]]; then
    echo -e "${RED}Error: Invalid event_type: $EVENT_TYPE${NC}" >&2
    echo "Valid types: created, reviewed, merged, closed, reopened" >&2
    exit 1
fi

# Validate PR number
if ! [[ "$PR_NUMBER" =~ ^[0-9]+$ ]]; then
    echo -e "${RED}Error: PR number must be numeric: $PR_NUMBER${NC}" >&2
    exit 1
fi

# Calculate cycle_time_seconds if this is a merged event
CYCLE_TIME_SECONDS="null"
if [ "$EVENT_TYPE" = "merged" ]; then
    # Try to get created event timestamp and calculate cycle time
    # This would require querying existing events, so we'll leave it null
    # and calculate it in a separate step or via database function
    CYCLE_TIME_SECONDS="null"
fi

# Build JSON payload
JSON_PAYLOAD=$(cat <<EOF
{
  "pr_number": $PR_NUMBER,
  "pr_title": $(echo "$PR_TITLE" | jq -R .),
  "branch_name": $(echo "$BRANCH_NAME" | jq -R .),
  "event_type": "$EVENT_TYPE",
  "author": $(echo "$AUTHOR" | jq -R .),
  "cycle_time_seconds": $CYCLE_TIME_SECONDS
}
EOF
)

# Insert into metrics.pr_events (PostgREST uses schema-qualified table name)
if postgrest_insert "metrics.pr_events" "$JSON_PAYLOAD" "true"; then
    echo -e "${GREEN}✓ Recorded PR event: $EVENT_TYPE for PR #$PR_NUMBER${NC}"
    exit 0
else
    echo -e "${RED}✗ Failed to record PR event${NC}" >&2
    exit 1
fi

