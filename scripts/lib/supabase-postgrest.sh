#!/bin/bash
# Shared library for Supabase PostgREST API operations
# Provides reusable functions for upsert operations via PostgREST API
#
# Usage:
#   source scripts/lib/supabase-postgrest.sh
#   postgrest_upsert "moods" "$JSON_PAYLOAD" "user_id=eq.xxx&timestamp=eq.yyy"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Validate required environment variables
postgrest_validate_env() {
    if [ -z "${SUPABASE_URL:-}" ]; then
        echo -e "${RED}Error: SUPABASE_URL environment variable is not set${NC}" >&2
        return 1
    fi
    
    if [ -z "${SUPABASE_SECRET_KEY:-}" ]; then
        echo -e "${RED}Error: SUPABASE_SECRET_KEY environment variable is not set${NC}" >&2
        echo -e "${YELLOW}Note: Use secret key (sb_secret_...) for PostgREST API access${NC}" >&2
        return 1
    fi
    
    return 0
}

# Upsert a record via PostgREST API
# Uses PATCH-then-POST pattern (same as feature flags)
#
# Arguments:
#   $1: Table name (e.g., "moods", "feature_flags")
#   $2: JSON payload (string)
#   $3: Filter query string for PATCH (e.g., "user_id=eq.xxx&timestamp=eq.yyy")
#   $4: Verbose mode (optional, "true" or "false")
#
# Returns:
#   0 on success, 1 on failure
#   Sets POSTGREST_RESULT variable to "updated", "inserted", or "failed"
postgrest_upsert() {
    local table_name="$1"
    local json_payload="$2"
    local filter_query="$3"
    local verbose="${4:-false}"
    
    if ! postgrest_validate_env; then
        return 1
    fi
    
    local api_url="${SUPABASE_URL}/rest/v1/${table_name}"
    
    # Try PATCH first (update existing record)
    if [ -n "$filter_query" ]; then
        local patch_url="${api_url}?${filter_query}"
    else
        local patch_url="${api_url}"
    fi
    
    if [ "$verbose" = "true" ]; then
        echo -e "${YELLOW}Attempting PATCH to ${patch_url}${NC}" >&2
    fi
    
    local patch_response=$(curl -s -w "\n%{http_code}" -X PATCH \
        -H "apikey: ${SUPABASE_SECRET_KEY}" \
        -H "Authorization: Bearer ${SUPABASE_SECRET_KEY}" \
        -H "Content-Type: application/json" \
        -H "Prefer: return=representation" \
        -d "$json_payload" \
        "$patch_url" 2>/dev/null || echo "ERROR")
    
    local patch_http_code=$(echo "$patch_response" | tail -n1)
    local patch_body=$(echo "$patch_response" | sed '$d')
    
    if [ "$patch_http_code" = "200" ] || [ "$patch_http_code" = "204" ]; then
        # Update successful
        POSTGREST_RESULT="updated"
        if [ "$verbose" = "true" ]; then
            echo -e "${GREEN}✓ Updated record in ${table_name}${NC}" >&2
        fi
        return 0
    elif [ "$patch_http_code" = "404" ]; then
        # Record doesn't exist, try to insert
        if [ "$verbose" = "true" ]; then
            echo -e "${YELLOW}Record not found, attempting insert...${NC}" >&2
        fi
        
        local post_response=$(curl -s -w "\n%{http_code}" -X POST \
            -H "apikey: ${SUPABASE_SECRET_KEY}" \
            -H "Authorization: Bearer ${SUPABASE_SECRET_KEY}" \
            -H "Content-Type: application/json" \
            -H "Prefer: return=representation" \
            -d "$json_payload" \
            "$api_url" 2>/dev/null || echo "ERROR")
        
        local post_http_code=$(echo "$post_response" | tail -n1)
        local post_body=$(echo "$post_response" | sed '$d')
        
        if [ "$post_http_code" = "201" ]; then
            POSTGREST_RESULT="inserted"
            if [ "$verbose" = "true" ]; then
                echo -e "${GREEN}✓ Inserted record into ${table_name}${NC}" >&2
            fi
            return 0
        else
            POSTGREST_RESULT="failed"
            echo -e "${RED}✗ Failed to insert into ${table_name} (HTTP ${post_http_code})${NC}" >&2
            if [ -n "$post_body" ]; then
                echo "$post_body" >&2
            fi
            return 1
        fi
    else
        # PATCH failed with unexpected error
        POSTGREST_RESULT="failed"
        echo -e "${RED}✗ Failed to update ${table_name} (HTTP ${patch_http_code})${NC}" >&2
        if [ -n "$patch_body" ]; then
            echo "$patch_body" >&2
        fi
        return 1
    fi
}

# Insert a record via PostgREST API (no upsert)
#
# Arguments:
#   $1: Table name
#   $2: JSON payload
#   $3: Verbose mode (optional)
#
# Returns:
#   0 on success, 1 on failure
postgrest_insert() {
    local table_name="$1"
    local json_payload="$2"
    local verbose="${3:-false}"
    
    if ! postgrest_validate_env; then
        return 1
    fi
    
    local api_url="${SUPABASE_URL}/rest/v1/${table_name}"
    
    if [ "$verbose" = "true" ]; then
        echo -e "${YELLOW}Inserting into ${table_name}...${NC}" >&2
    fi
    
    local response=$(curl -s -w "\n%{http_code}" -X POST \
        -H "apikey: ${SUPABASE_SECRET_KEY}" \
        -H "Authorization: Bearer ${SUPABASE_SECRET_KEY}" \
        -H "Content-Type: application/json" \
        -H "Prefer: return=representation" \
        -d "$json_payload" \
        "$api_url" 2>/dev/null || echo "ERROR")
    
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "201" ]; then
        if [ "$verbose" = "true" ]; then
            echo -e "${GREEN}✓ Inserted record into ${table_name}${NC}" >&2
        fi
        return 0
    else
        echo -e "${RED}✗ Failed to insert into ${table_name} (HTTP ${http_code})${NC}" >&2
        if [ -n "$body" ]; then
            echo "$body" >&2
        fi
        return 1
    fi
}

# Query records via PostgREST API
#
# Arguments:
#   $1: Table name
#   $2: Filter query string (optional, e.g., "user_id=eq.xxx")
#   $3: Select columns (optional, e.g., "id,score,timestamp")
#
# Returns:
#   JSON response in POSTGREST_QUERY_RESULT variable
#   0 on success, 1 on failure
postgrest_query() {
    local table_name="$1"
    local filter_query="${2:-}"
    local select_columns="${3:-*}"
    
    if ! postgrest_validate_env; then
        return 1
    fi
    
    local api_url="${SUPABASE_URL}/rest/v1/${table_name}"
    local query_params="select=${select_columns}"
    
    if [ -n "$filter_query" ]; then
        query_params="${query_params}&${filter_query}"
    fi
    
    api_url="${api_url}?${query_params}"
    
    local response=$(curl -s -w "\n%{http_code}" -X GET \
        -H "apikey: ${SUPABASE_SECRET_KEY}" \
        -H "Authorization: Bearer ${SUPABASE_SECRET_KEY}" \
        -H "Content-Type: application/json" \
        -H "Prefer: return=representation" \
        "$api_url" 2>/dev/null || echo "ERROR")
    
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ]; then
        POSTGREST_QUERY_RESULT="$body"
        return 0
    else
        echo -e "${RED}✗ Failed to query ${table_name} (HTTP ${http_code})${NC}" >&2
        if [ -n "$body" ]; then
            echo "$body" >&2
        fi
        return 1
    fi
}



