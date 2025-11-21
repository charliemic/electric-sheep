#!/bin/bash
# Add one day's worth of mood data for test users (yesterday's data) via PostgREST API
# This script is idempotent - safe to run multiple times
#
# Prerequisites:
#   - SUPABASE_URL environment variable
#   - SUPABASE_SECRET_KEY environment variable (for PostgREST API access)
#   - jq installed
#
# Usage:
#   SUPABASE_URL=https://xxx.supabase.co SUPABASE_SECRET_KEY=xxx ./add-daily-data-postgrest.sh

set -euo pipefail

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

# Source shared libraries
if [ -f "${PROJECT_ROOT}/scripts/lib/supabase-postgrest.sh" ]; then
    source "${PROJECT_ROOT}/scripts/lib/supabase-postgrest.sh"
else
    echo -e "${RED}Error: supabase-postgrest.sh library not found${NC}" >&2
    exit 1
fi

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check prerequisites
if ! postgrest_validate_env; then
    exit 1
fi

# Test users configuration (matches TestUserFixtures.kt)
declare -A TEST_USERS=(
    ["test-user-tech-novice-high-stable"]="test-novice-high-stable@electric-sheep.test|high_stable"
    ["test-user-tech-novice-low-stable"]="test-novice-low-stable@electric-sheep.test|low_stable"
    ["test-user-tech-novice-high-unstable"]="test-novice-high-unstable@electric-sheep.test|high_unstable"
    ["test-user-tech-novice-low-unstable"]="test-novice-low-unstable@electric-sheep.test|low_unstable"
    ["test-user-tech-savvy-high-stable"]="test-savvy-high-stable@electric-sheep.test|high_stable"
    ["test-user-tech-savvy-low-stable"]="test-savvy-low-stable@electric-sheep.test|low_stable"
    ["test-user-tech-savvy-high-unstable"]="test-savvy-high-unstable@electric-sheep.test|high_unstable"
    ["test-user-tech-savvy-low-unstable"]="test-savvy-low-unstable@electric-sheep.test|low_unstable"
)

# Calculate yesterday's date and timestamp
YESTERDAY_DATE=$(date -u -d "yesterday" +"%Y-%m-%d" 2>/dev/null || date -u -v-1d +"%Y-%m-%d" 2>/dev/null || echo "")
if [ -z "$YESTERDAY_DATE" ]; then
    echo -e "${RED}Error: Could not calculate yesterday's date${NC}"
    exit 1
fi

# Convert to Unix timestamp (milliseconds)
YESTERDAY_TIMESTAMP=$(date -u -d "$YESTERDAY_DATE" +"%s" 2>/dev/null || date -u -j -f "%Y-%m-%d" "$YESTERDAY_DATE" +"%s" 2>/dev/null || echo "")
if [ -z "$YESTERDAY_TIMESTAMP" ]; then
    echo -e "${RED}Error: Could not convert date to timestamp${NC}"
    exit 1
fi
YESTERDAY_TIMESTAMP_MS=$((YESTERDAY_TIMESTAMP * 1000))

echo -e "${GREEN}Adding mood data for date: ${YESTERDAY_DATE} (timestamp: ${YESTERDAY_TIMESTAMP_MS})${NC}"

# Function to generate mood score (simplified version matching SQL function logic)
generate_mood_score() {
    local pattern="$1"
    local day_offset="$2"
    local seed="${3:-$day_offset}"
    
    # Use a simple deterministic calculation
    # This matches the SQL function generate_mood_score logic
    case "$pattern" in
        "high_stable")
            # High stable: 7-9 range, low variance
            local base=8.0
            local variance=$(echo "scale=2; (s($day_offset * 0.1) * 0.5) + (($seed % 100) / 100.0 * 0.5 - 0.25)" | bc -l 2>/dev/null || echo "0")
            local score=$(echo "$base + $variance" | bc -l 2>/dev/null | awk '{printf "%.0f", $1}')
            ;;
        "low_stable")
            # Low stable: 2-4 range, low variance
            local base=3.0
            local variance=$(echo "scale=2; (s($day_offset * 0.1) * 0.5) + (($seed % 100) / 100.0 * 0.5 - 0.25)" | bc -l 2>/dev/null || echo "0")
            local score=$(echo "$base + $variance" | bc -l 2>/dev/null | awk '{printf "%.0f", $1}')
            ;;
        "high_unstable")
            # High unstable: 4-10 range, high variance
            local base=7.0
            local variance=$(echo "scale=2; (s($day_offset * 0.5) * 2.5) + (($seed % 200) / 100.0 - 1.0)" | bc -l 2>/dev/null || echo "0")
            local score=$(echo "$base + $variance" | bc -l 2>/dev/null | awk '{printf "%.0f", $1}')
            ;;
        "low_unstable")
            # Low unstable: 1-6 range, high variance
            local base=3.5
            local variance=$(echo "scale=2; (s($day_offset * 0.5) * 2.5) + (($seed % 200) / 100.0 - 1.0)" | bc -l 2>/dev/null || echo "0")
            local score=$(echo "$base + $variance" | bc -l 2>/dev/null | awk '{printf "%.0f", $1}')
            ;;
        *)
            local score=5
            ;;
    esac
    
    # Clamp to valid range (1-10)
    if [ "$score" -lt 1 ]; then
        score=1
    elif [ "$score" -gt 10 ]; then
        score=10
    fi
    
    echo "$score"
}

# Process each test user
created_count=0
skipped_count=0
error_count=0

for user_id in "${!TEST_USERS[@]}"; do
    IFS='|' read -r email pattern <<< "${TEST_USERS[$user_id]}"
    
    echo -n "Processing ${email}... "
    
    # Check if data already exists for yesterday
    # Query via PostgREST to check if entry exists
    if postgrest_query "moods" "user_id=eq.${user_id}&timestamp=eq.${YESTERDAY_TIMESTAMP_MS}" "id"; then
        local existing_count=$(echo "$POSTGREST_QUERY_RESULT" | jq 'length' 2>/dev/null || echo "0")
        if [ "$existing_count" -gt 0 ]; then
            echo -e "${YELLOW}SKIPPED (data already exists)${NC}"
            skipped_count=$((skipped_count + 1))
            continue
        fi
    fi
    
    # Calculate day offset (days since baseline start, 30 days ago)
    # For simplicity, use a fixed calculation
    BASELINE_START_DATE=$(date -u -d "30 days ago" +"%Y-%m-%d" 2>/dev/null || date -u -v-30d +"%Y-%m-%d" 2>/dev/null || echo "")
    DAY_OFFSET=$(($(($(date -u -d "$YESTERDAY_DATE" +"%s" 2>/dev/null || date -u -j -f "%Y-%m-%d" "$YESTERDAY_DATE" +"%s" 2>/dev/null) - $(date -u -d "$BASELINE_START_DATE" +"%s" 2>/dev/null || date -u -j -f "%Y-%m-%d" "$BASELINE_START_DATE" +"%s" 2>/dev/null))) / 86400))
    
    # Generate mood score
    SEED=$((YESTERDAY_TIMESTAMP + ${#user_id}))
    MOOD_SCORE=$(generate_mood_score "$pattern" "$DAY_OFFSET" "$SEED")
    
    # Generate UUID for mood entry
    MOOD_ID=$(uuidgen 2>/dev/null || cat /proc/sys/kernel/random/uuid 2>/dev/null || echo "")
    if [ -z "$MOOD_ID" ]; then
        # Fallback: generate deterministic ID from user_id and timestamp
        MOOD_ID=$(echo -n "${user_id}${YESTERDAY_TIMESTAMP_MS}" | sha256sum | cut -c1-36 | sed 's/\([0-9a-f]\{8\}\)\([0-9a-f]\{4\}\)\([0-9a-f]\{4\}\)\([0-9a-f]\{4\}\)\([0-9a-f]\{12\}\)/\1-\2-\3-\4-\5/')
    fi
    
    # Build JSON payload
    JSON_PAYLOAD=$(jq -n \
        --arg id "$MOOD_ID" \
        --arg user_id "$user_id" \
        --argjson score "$MOOD_SCORE" \
        --argjson timestamp "$YESTERDAY_TIMESTAMP_MS" \
        '{
            id: $id,
            user_id: $user_id,
            score: $score,
            timestamp: $timestamp,
            created_at: $timestamp,
            updated_at: $timestamp
        }')
    
    # Upsert via PostgREST (using shared library)
    if postgrest_upsert "moods" "$JSON_PAYLOAD" "user_id=eq.${user_id}&timestamp=eq.${YESTERDAY_TIMESTAMP_MS}" "false"; then
        if [ "$POSTGREST_RESULT" = "inserted" ]; then
            echo -e "${GREEN}CREATED (score: ${MOOD_SCORE})${NC}"
            created_count=$((created_count + 1))
        else
            echo -e "${GREEN}UPDATED (score: ${MOOD_SCORE})${NC}"
            created_count=$((created_count + 1))
        fi
    else
        echo -e "${RED}FAILED${NC}"
        error_count=$((error_count + 1))
    fi
done

# Summary
echo ""
echo -e "${GREEN}Summary:${NC}"
echo "  Created/Updated: ${created_count}"
echo "  Skipped: ${skipped_count}"
echo "  Errors: ${error_count}"

if [ $error_count -gt 0 ]; then
    exit 1
fi

echo -e "${GREEN}Daily data update complete!${NC}"



