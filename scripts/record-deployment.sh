#!/bin/bash
# Record deployment event to metrics schema
# Usage: ./scripts/record-deployment.sh <deployment_id> <environment> <status> <commit_sha> <branch_name> [pr_number] [deployed_by]
#
# Environments: development, staging, production
# Status: success, failure, rollback
# Example: ./scripts/record-deployment.sh "deploy-123" "production" "success" "abc123" "main" 42 "ci-bot"

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
    echo "Usage: $0 <deployment_id> <environment> <status> <commit_sha> <branch_name> [pr_number] [deployed_by]" >&2
    echo "Environments: development, staging, production" >&2
    echo "Status: success, failure, rollback" >&2
    exit 1
fi

DEPLOYMENT_ID="$1"
ENVIRONMENT="$2"
STATUS="$3"
COMMIT_SHA="$4"
BRANCH_NAME="$5"
PR_NUMBER="${6:-}"
DEPLOYED_BY="${7:-}"

# Validate environment
if [[ ! "$ENVIRONMENT" =~ ^(development|staging|production)$ ]]; then
    echo -e "${RED}Error: Invalid environment: $ENVIRONMENT${NC}" >&2
    echo "Valid environments: development, staging, production" >&2
    exit 1
fi

# Validate status
if [[ ! "$STATUS" =~ ^(success|failure|rollback)$ ]]; then
    echo -e "${RED}Error: Invalid status: $STATUS${NC}" >&2
    echo "Valid statuses: success, failure, rollback" >&2
    exit 1
fi

# Build JSON payload
JSON_PAYLOAD=$(cat <<EOF
{
  "deployment_id": "$DEPLOYMENT_ID",
  "environment": "$ENVIRONMENT",
  "status": "$STATUS",
  "commit_sha": "$COMMIT_SHA",
  "branch_name": "$BRANCH_NAME",
  "pr_number": ${PR_NUMBER:-null},
  "deployed_by": $(echo "$DEPLOYED_BY" | jq -R .)
}
EOF
)

# Insert into metrics.deployment_events (PostgREST uses schema-qualified table name)
if postgrest_insert "metrics.deployment_events" "$JSON_PAYLOAD" "true"; then
    echo -e "${GREEN}✓ Recorded deployment: $DEPLOYMENT_ID ($ENVIRONMENT/$STATUS)${NC}"
    exit 0
else
    echo -e "${RED}✗ Failed to record deployment${NC}" >&2
    exit 1
fi

