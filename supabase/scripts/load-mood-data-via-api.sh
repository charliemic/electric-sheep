#!/bin/bash
# Load mood data via PostgREST API (HTTP REST, same pattern as user creation)
# This bypasses the need for supabase link and db execute
#
# Prerequisites:
#   - SUPABASE_URL environment variable
#   - SUPABASE_SECRET_KEY environment variable (for PostgREST API access)
#   - curl and jq installed
#   - Test users must exist (run create-test-users.sh first)
#
# Usage:
#   SUPABASE_URL=https://xxx.supabase.co SUPABASE_SECRET_KEY=xxx ./load-mood-data-via-api.sh

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

# Source shared libraries
if [ -f "${PROJECT_ROOT}/scripts/lib/supabase-auth-admin.sh" ]; then
    source "${PROJECT_ROOT}/scripts/lib/supabase-auth-admin.sh"
else
    echo -e "${RED}Error: supabase-auth-admin.sh library not found${NC}" >&2
    exit 1
fi

if [ -f "${PROJECT_ROOT}/scripts/lib/supabase-postgrest.sh" ]; then
    source "${PROJECT_ROOT}/scripts/lib/supabase-postgrest.sh"
else
    echo -e "${RED}Error: supabase-postgrest.sh library not found${NC}" >&2
    exit 1
fi

# Check prerequisites
if ! auth_admin_validate_env; then
    exit 1
fi

if ! postgrest_validate_env; then
    exit 1
fi

# Test users configuration (matches TestUserFixtures.kt)
declare -A TEST_USERS=(
    ["test-novice-high-stable@electric-sheep.test"]="high_stable"
    ["test-novice-low-stable@electric-sheep.test"]="low_stable"
    ["test-novice-high-unstable@electric-sheep.test"]="high_unstable"
    ["test-novice-low-unstable@electric-sheep.test"]="low_unstable"
    ["test-savvy-high-stable@electric-sheep.test"]="high_stable"
    ["test-savvy-low-stable@electric-sheep.test"]="low_stable"
    ["test-savvy-high-unstable@electric-sheep.test"]="high_unstable"
    ["test-savvy-low-unstable@electric-sheep.test"]="low_unstable"
)

# Configuration
DAYS_TO_GENERATE="${DAYS_TO_GENERATE:-30}"
ENTRIES_PER_DAY="${ENTRIES_PER_DAY:-1}"

echo -e "${GREEN}Loading mood data via PostgREST API...${NC}"
echo "Supabase URL: ${SUPABASE_URL}"
echo "Generating ${DAYS_TO_GENERATE} days of data per user"
echo ""

# Function to generate mood score (matches TestUserFixtures.kt logic)
generate_mood_score() {
    local pattern="$1"
    local day_offset="$2"
    local entry_index="$3"
    
    case "$pattern" in
        "high_stable")
            # High stable: 7-9, mostly 8
            echo $((7 + (day_offset % 3)))
            ;;
        "low_stable")
            # Low stable: 2-4, mostly 3
            echo $((2 + (day_offset % 3)))
            ;;
        "high_unstable")
            # High unstable: 3-9, varies
            echo $((3 + ((day_offset * 7 + entry_index * 3) % 7)))
            ;;
        "low_unstable")
            # Low unstable: 1-7, varies
            echo $((1 + ((day_offset * 5 + entry_index * 2) % 7)))
            ;;
        *)
            echo 5  # Default
            ;;
    esac
}

# Function to get user UUID by email
get_user_uuid() {
    local email="$1"
    
    local api_url="${SUPABASE_URL}/auth/v1/admin/users?email=eq.${email}"
    
    local response=$(curl -s -w "\n%{http_code}" -X GET \
        -H "apikey: ${SUPABASE_SECRET_KEY}" \
        -H "Authorization: Bearer ${SUPABASE_SECRET_KEY}" \
        -H "Content-Type: application/json" \
        "$api_url" 2>/dev/null || echo "ERROR")
    
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ]; then
        local user_id=$(echo "$body" | jq -r '.users[0].id' 2>/dev/null || echo "")
        if [ -n "$user_id" ] && [ "$user_id" != "null" ]; then
            echo "$user_id"
            return 0
        fi
    fi
    
    return 1
}

# Function to check if mood data exists for user
mood_data_exists() {
    local user_id="$1"
    
    local api_url="${SUPABASE_URL}/rest/v1/moods?user_id=eq.${user_id}&limit=1"
    
    local response=$(curl -s -w "\n%{http_code}" -X GET \
        -H "apikey: ${SUPABASE_SECRET_KEY}" \
        -H "Authorization: Bearer ${SUPABASE_SECRET_KEY}" \
        -H "Content-Type: application/json" \
        "$api_url" 2>/dev/null || echo "ERROR")
    
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ]; then
        local count=$(echo "$body" | jq 'length' 2>/dev/null || echo "0")
        if [ "$count" -gt 0 ]; then
            return 0  # Data exists
        fi
    fi
    
    return 1  # No data
}

# Function to generate timestamp for a day (milliseconds since epoch)
get_timestamp_for_day() {
    local day_offset="$1"
    local entry_index="$2"
    local entries_per_day="$3"
    
    # Use Python for cross-platform date calculation (more reliable than date command)
    python3 -c "
import datetime
from datetime import timezone

# Calculate target date (yesterday - day_offset)
target_date = datetime.date.today() - datetime.timedelta(days=1 + $day_offset)

# Get start of day in UTC (milliseconds)
day_start = int(datetime.datetime.combine(target_date, datetime.time.min).replace(tzinfo=timezone.utc).timestamp() * 1000)

# Add entry offset (spread entries throughout the day)
entry_offset = $entry_index * (24 * 60 * 60 * 1000 // $entries_per_day)

print(day_start + entry_offset)
"
}

total_inserted=0
total_skipped=0
total_errors=0

# Process each test user
for email in "${!TEST_USERS[@]}"; do
    pattern="${TEST_USERS[$email]}"
    
    echo -n "Processing ${email} (pattern: ${pattern})... "
    
    # Get user UUID
    user_id=$(get_user_uuid "$email")
    if [ -z "$user_id" ]; then
        echo -e "${RED}USER NOT FOUND${NC}"
        echo "   Run create-test-users.sh first to create test users"
        total_errors=$((total_errors + 1))
        continue
    fi
    
    # Check if data already exists
    if mood_data_exists "$user_id"; then
        echo -e "${YELLOW}SKIPPED (data exists)${NC}"
        total_skipped=$((total_skipped + 1))
        continue
    fi
    
    # Generate and insert mood entries
    inserted_count=0
    for day_offset in $(seq 0 $((DAYS_TO_GENERATE - 1))); do
        for entry_index in $(seq 0 $((ENTRIES_PER_DAY - 1))); do
            # Generate mood score
            score=$(generate_mood_score "$pattern" "$day_offset" "$entry_index")
            
            # Generate timestamp
            timestamp=$(get_timestamp_for_day "$day_offset" "$entry_index" "$ENTRIES_PER_DAY")
            
            # Create JSON payload
            json_payload="{\"user_id\":\"${user_id}\",\"score\":${score},\"timestamp\":${timestamp}}"
            
            # Insert via PostgREST API
            if postgrest_insert "moods" "$json_payload" "false"; then
                inserted_count=$((inserted_count + 1))
            else
                echo -e "\n${RED}Failed to insert mood entry${NC}" >&2
                total_errors=$((total_errors + 1))
            fi
        done
    done
    
    if [ $inserted_count -gt 0 ]; then
        echo -e "${GREEN}INSERTED ${inserted_count} entries${NC}"
        total_inserted=$((total_inserted + inserted_count))
    else
        echo -e "${RED}FAILED${NC}"
        total_errors=$((total_errors + 1))
    fi
done

echo ""
echo -e "${GREEN}Summary:${NC}"
echo "  Inserted: ${total_inserted} mood entries"
echo "  Skipped: ${total_skipped} users (data already exists)"
echo "  Errors: ${total_errors}"

if [ $total_errors -gt 0 ]; then
    exit 1
fi

