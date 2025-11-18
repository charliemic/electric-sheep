#!/bin/bash
# Comprehensive Supabase debugging utility (READ-ONLY)
# Fetches logs, project info, and database status for debugging

set -u  # Fail on undefined variables

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Default values
PROJECT_REF=""
ACCESS_TOKEN=""
VERBOSE=false

# Help function
show_help() {
    cat << EOF
Usage: $0 [OPTIONS]

Comprehensive READ-ONLY debugging utility for Supabase projects.
Fetches logs, project information, and database status.

Options:
    -p, --project-ref REF     Supabase project reference ID (required)
    -t, --token TOKEN         Supabase Personal Access Token (required, or set SUPABASE_ACCESS_TOKEN)
    -v, --verbose             Show detailed output
    -h, --help                Show this help message

Environment Variables:
    SUPABASE_ACCESS_TOKEN     Personal Access Token (alternative to -t)
    SUPABASE_PROJECT_REF      Project reference (alternative to -p)

Examples:
    # Full debug report
    $0 -p mvuzvoyvijsdqsfqjgpd -t sbp_...

    # Verbose output
    $0 -p mvuzvoyvijsdqsfqjgpd --verbose

Note: This script is READ-ONLY and does not make any changes.
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
        -v|--verbose)
            VERBOSE=true
            shift
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
    exit 1
fi

if [ -z "$ACCESS_TOKEN" ]; then
    echo -e "${RED}Error: Access token is required${NC}"
    exit 1
fi

API_BASE="https://api.supabase.com/v1/projects/${PROJECT_REF}"

echo -e "${CYAN}═══════════════════════════════════════════════════════════${NC}"
echo -e "${CYAN}  Supabase Debug Report (READ-ONLY)${NC}"
echo -e "${CYAN}  Project: ${PROJECT_REF}${NC}"
echo -e "${CYAN}═══════════════════════════════════════════════════════════${NC}"
echo ""

# Function to make API call
api_call() {
    local endpoint="$1"
    local description="$2"
    
    if [ "$VERBOSE" = true ]; then
        echo -e "${YELLOW}Fetching: ${description}${NC}"
        echo -e "${YELLOW}  Endpoint: ${endpoint}${NC}"
    fi
    
    RESPONSE=$(curl -s -w "\n%{http_code}" \
        -X GET \
        -H "Authorization: Bearer ${ACCESS_TOKEN}" \
        -H "Content-Type: application/json" \
        "${endpoint}" 2>&1)
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    BODY=$(echo "$RESPONSE" | head -n-1)
    
    if [ "$HTTP_CODE" = "200" ]; then
        echo -e "${GREEN}✓ ${description}${NC}"
        if [ "$VERBOSE" = true ]; then
            if command -v jq &> /dev/null; then
                echo "$BODY" | jq '.' | head -20
            else
                echo "$BODY" | head -20
            fi
        fi
        return 0
    else
        echo -e "${RED}✗ ${description} (HTTP ${HTTP_CODE})${NC}"
        if [ "$VERBOSE" = true ] && [ "$HTTP_CODE" != "404" ]; then
            echo "$BODY" | head -5
        fi
        return 1
    fi
}

# 1. Project Information
echo -e "${BLUE}1. Project Information${NC}"
api_call "${API_BASE}" "Project details"
echo ""

# 2. Database Status
echo -e "${BLUE}2. Database Status${NC}"
api_call "${API_BASE}/database" "Database status"
api_call "${API_BASE}/database/backups" "Database backups"
echo ""

# 3. API Logs (recent)
echo -e "${BLUE}3. Recent API Logs${NC}"
api_call "${API_BASE}/logs/api?limit=10" "API logs (last 10)"
echo ""

# 4. Auth Logs (recent)
echo -e "${BLUE}4. Recent Auth Logs${NC}"
api_call "${API_BASE}/logs/auth?limit=10" "Auth logs (last 10)"
echo ""

# 5. Database Logs (recent)
echo -e "${BLUE}5. Recent Database Logs${NC}"
api_call "${API_BASE}/logs/postgres?limit=10" "Postgres logs (last 10)"
echo ""

# 6. Storage Status
echo -e "${BLUE}6. Storage Status${NC}"
api_call "${API_BASE}/storage" "Storage information"
echo ""

# 7. Functions Status
echo -e "${BLUE}7. Edge Functions Status${NC}"
api_call "${API_BASE}/functions" "Edge functions list"
echo ""

# 8. Health Check
echo -e "${BLUE}8. Health Check${NC}"
api_call "${API_BASE}/health" "Project health"
echo ""

echo -e "${CYAN}═══════════════════════════════════════════════════════════${NC}"
echo -e "${CYAN}  Debug Report Complete${NC}"
echo -e "${CYAN}═══════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "${YELLOW}For more detailed logs, use:${NC}"
echo "  ./scripts/supabase-logs.sh -p ${PROJECT_REF} -t <token> --log-type <type>"
echo ""
echo -e "${YELLOW}Or access Supabase Dashboard:${NC}"
echo "  https://supabase.com/dashboard/project/${PROJECT_REF}/logs"

