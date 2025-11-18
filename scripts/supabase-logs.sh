#!/bin/bash
# Read-only utility to fetch Supabase logs for debugging
# Uses Management API to access logs without making any changes

set -u  # Fail on undefined variables

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
PROJECT_REF=""
ACCESS_TOKEN=""
LOG_TYPE="api"  # api, auth, postgres, storage, functions
LIMIT=100
START_TIME=""
END_TIME=""

# Help function
show_help() {
    cat << EOF
Usage: $0 [OPTIONS]

Read-only utility to fetch Supabase logs for debugging.

Options:
    -p, --project-ref REF     Supabase project reference ID (required)
    -t, --token TOKEN         Supabase Personal Access Token (required, or set SUPABASE_ACCESS_TOKEN)
    -l, --log-type TYPE       Log type: api, auth, postgres, storage, functions (default: api)
    -n, --limit N             Number of log entries to fetch (default: 100, max: 1000)
    -s, --start-time TIME     Start time (ISO 8601 format, e.g., 2024-01-01T00:00:00Z)
    -e, --end-time TIME       End time (ISO 8601 format, e.g., 2024-01-01T23:59:59Z)
    -h, --help                Show this help message

Environment Variables:
    SUPABASE_ACCESS_TOKEN     Personal Access Token (alternative to -t)
    SUPABASE_PROJECT_REF      Project reference (alternative to -p)

Examples:
    # Fetch last 100 API logs
    $0 -p mvuzvoyvijsdqsfqjgpd -t sbp_...

    # Fetch auth logs from last hour
    $0 -p mvuzvoyvijsdqsfqjgpd --log-type auth --limit 50

    # Fetch logs with time range
    $0 -p mvuzvoyvijsdqsfqjgpd -s "2024-01-01T00:00:00Z" -e "2024-01-01T23:59:59Z"

Note: This script is READ-ONLY and does not make any changes to your Supabase project.
EOF
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -p|--project-ref)
            PROJECT_REF="$2"
            shift 2
            ;;
        -t|--token)
            ACCESS_TOKEN="$2"
            shift 2
            ;;
        -l|--log-type)
            LOG_TYPE="$2"
            shift 2
            ;;
        -n|--limit)
            LIMIT="$2"
            shift 2
            ;;
        -s|--start-time)
            START_TIME="$2"
            shift 2
            ;;
        -e|--end-time)
            END_TIME="$2"
            shift 2
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            show_help
            exit 1
            ;;
    esac
done

# Get values from environment if not provided
PROJECT_REF="${PROJECT_REF:-${SUPABASE_PROJECT_REF:-}}"
ACCESS_TOKEN="${ACCESS_TOKEN:-${SUPABASE_ACCESS_TOKEN:-}}"

# Validate required parameters
if [ -z "$PROJECT_REF" ]; then
    echo -e "${RED}Error: Project reference is required${NC}"
    echo "Use -p/--project-ref or set SUPABASE_PROJECT_REF environment variable"
    exit 1
fi

if [ -z "$ACCESS_TOKEN" ]; then
    echo -e "${RED}Error: Access token is required${NC}"
    echo "Use -t/--token or set SUPABASE_ACCESS_TOKEN environment variable"
    exit 1
fi

# Validate log type
VALID_LOG_TYPES=("api" "auth" "postgres" "storage" "functions")
if [[ ! " ${VALID_LOG_TYPES[@]} " =~ " ${LOG_TYPE} " ]]; then
    echo -e "${RED}Error: Invalid log type: $LOG_TYPE${NC}"
    echo "Valid types: ${VALID_LOG_TYPES[*]}"
    exit 1
fi

# Validate limit
if [ "$LIMIT" -lt 1 ] || [ "$LIMIT" -gt 1000 ]; then
    echo -e "${RED}Error: Limit must be between 1 and 1000${NC}"
    exit 1
fi

# Management API base URL
API_BASE="https://api.supabase.com/v1/projects/${PROJECT_REF}"

echo -e "${BLUE}Fetching ${LOG_TYPE} logs for project: ${PROJECT_REF}${NC}"
echo -e "${BLUE}Limit: ${LIMIT} entries${NC}"
if [ -n "$START_TIME" ]; then
    echo -e "${BLUE}Start time: ${START_TIME}${NC}"
fi
if [ -n "$END_TIME" ]; then
    echo -e "${BLUE}End time: ${END_TIME}${NC}"
fi
echo ""

# Build query parameters
QUERY_PARAMS="limit=${LIMIT}"
if [ -n "$START_TIME" ]; then
    QUERY_PARAMS="${QUERY_PARAMS}&start_time=${START_TIME}"
fi
if [ -n "$END_TIME" ]; then
    QUERY_PARAMS="${QUERY_PARAMS}&end_time=${END_TIME}"
fi

# Try different log endpoints based on Supabase Management API
# Note: Supabase Management API may have different endpoints for logs
LOG_ENDPOINTS=(
    "${API_BASE}/logs/${LOG_TYPE}?${QUERY_PARAMS}"
    "${API_BASE}/telemetry/logs/${LOG_TYPE}?${QUERY_PARAMS}"
    "${API_BASE}/logs?type=${LOG_TYPE}&${QUERY_PARAMS}"
)

SUCCESS=false
for ENDPOINT in "${LOG_ENDPOINTS[@]}"; do
    echo -e "${YELLOW}Trying endpoint: ${ENDPOINT}${NC}"
    
    RESPONSE=$(curl -s -w "\n%{http_code}" \
        -X GET \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -H "Content-Type: application/json" \
        "${ENDPOINT}" 2>&1)
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | head -n-1)
    
    if [ "$HTTP_CODE" = "200" ]; then
        echo -e "${GREEN}✓ Successfully fetched logs${NC}"
        echo ""
        # Pretty print JSON if jq is available, otherwise just print
        if command -v jq &> /dev/null; then
            echo "$BODY" | jq '.'
        else
            echo "$BODY"
        fi
        SUCCESS=true
        break
    elif [ "$HTTP_CODE" = "404" ]; then
        echo -e "${YELLOW}  Endpoint not found (404), trying next...${NC}"
        continue
    elif [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "403" ]; then
        echo -e "${RED}  Authentication failed (${HTTP_CODE})${NC}"
        echo -e "${RED}  Check your access token and permissions${NC}"
        exit 1
    else
        echo -e "${YELLOW}  HTTP ${HTTP_CODE}, trying next endpoint...${NC}"
        if [ "$HTTP_CODE" != "000" ]; then
            echo -e "${YELLOW}  Response: ${BODY}${NC}"
        fi
        continue
    fi
done

if [ "$SUCCESS" = false ]; then
    echo -e "${RED}✗ Failed to fetch logs from any endpoint${NC}"
    echo ""
    echo -e "${YELLOW}Alternative: Use Supabase Dashboard${NC}"
    echo "1. Go to: https://supabase.com/dashboard/project/${PROJECT_REF}/logs"
    echo "2. Navigate to the 'Logs' section in the left sidebar"
    echo "3. Filter by log type: ${LOG_TYPE}"
    echo ""
    echo -e "${YELLOW}Or use Supabase CLI (if available):${NC}"
    echo "  supabase logs --project-ref ${PROJECT_REF} --type ${LOG_TYPE}"
    echo ""
    echo -e "${YELLOW}Note: Supabase may not expose logs via Management API yet.${NC}"
    echo "Check: https://supabase.com/docs/reference/api for latest API documentation"
    exit 1
fi

