#!/bin/bash
# Shared library for Supabase Auth Admin API operations
# Provides reusable functions for user management via Admin API
#
# Usage:
#   source scripts/lib/supabase-auth-admin.sh
#   auth_admin_create_user "$user_id" "$email" "$password" "$display_name"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Validate required environment variables
auth_admin_validate_env() {
    if [ -z "${SUPABASE_URL:-}" ]; then
        echo -e "${RED}Error: SUPABASE_URL environment variable is not set${NC}" >&2
        return 1
    fi
    
    if [ -z "${SUPABASE_SECRET_KEY:-}" ]; then
        echo -e "${RED}Error: SUPABASE_SECRET_KEY environment variable is not set${NC}" >&2
        echo -e "${YELLOW}Note: Use secret key (sb_secret_...) for Admin API access${NC}" >&2
        return 1
    fi
    
    return 0
}

# Check if user exists in Supabase Auth
#
# Arguments:
#   $1: User ID (UUID string)
#   $2: Email (optional, for verification)
#
# Returns:
#   0 if user exists, 1 if not found
auth_admin_user_exists() {
    local user_id="$1"
    local email="${2:-}"
    
    if ! auth_admin_validate_env; then
        return 1
    fi
    
    local api_url="${SUPABASE_URL}/auth/v1/admin/users"
    
    # Query by user ID
    if [ -n "$user_id" ]; then
        api_url="${api_url}?id=eq.${user_id}"
    elif [ -n "$email" ]; then
        api_url="${api_url}?email=eq.${email}"
    else
        echo -e "${RED}Error: Must provide either user_id or email${NC}" >&2
        return 1
    fi
    
    local response=$(curl -s -w "\n%{http_code}" -X GET \
        -H "apikey: ${SUPABASE_SECRET_KEY}" \
        -H "Authorization: Bearer ${SUPABASE_SECRET_KEY}" \
        -H "Content-Type: application/json" \
        "$api_url" 2>/dev/null || echo "ERROR")
    
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ]; then
        # Check if user exists in response
        local user_count=$(echo "$body" | jq '.users | length' 2>/dev/null || echo "0")
        if [ "$user_count" -gt 0 ]; then
            # If email provided, verify it matches
            if [ -n "$email" ]; then
                local found_email=$(echo "$body" | jq -r '.users[0].email' 2>/dev/null || echo "")
                if [ "$found_email" = "$email" ]; then
                    return 0  # User exists with matching email
                fi
            else
                return 0  # User exists
            fi
        fi
    fi
    
    return 1  # User does not exist
}

# Create a user in Supabase Auth via Admin API
#
# Arguments:
#   $1: User ID (UUID string, optional - will be generated if not provided)
#   $2: Email
#   $3: Password
#   $4: Display name (optional)
#
# Returns:
#   0 on success, 1 on failure
#   Sets AUTH_ADMIN_USER_ID variable to created user's ID
auth_admin_create_user() {
    local user_id="${1:-}"
    local email="$2"
    local password="$3"
    local display_name="${4:-}"
    
    if ! auth_admin_validate_env; then
        return 1
    fi
    
    local api_url="${SUPABASE_URL}/auth/v1/admin/users"
    
    # Build JSON payload
    local json_payload="{"
    json_payload="${json_payload}\"email\":\"${email}\","
    json_payload="${json_payload}\"password\":\"${password}\","
    json_payload="${json_payload}\"email_confirm\":true"
    
    if [ -n "$user_id" ]; then
        json_payload="${json_payload},\"id\":\"${user_id}\""
    fi
    
    if [ -n "$display_name" ]; then
        json_payload="${json_payload},\"user_metadata\":{\"display_name\":\"${display_name}\"}"
    fi
    
    json_payload="${json_payload}}"
    
    local response=$(curl -s -w "\n%{http_code}" -X POST \
        -H "apikey: ${SUPABASE_SECRET_KEY}" \
        -H "Authorization: Bearer ${SUPABASE_SECRET_KEY}" \
        -H "Content-Type: application/json" \
        -d "$json_payload" \
        "$api_url" 2>/dev/null || echo "ERROR")
    
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ] || [ "$http_code" = "201" ]; then
        # Extract user ID from response
        AUTH_ADMIN_USER_ID=$(echo "$body" | jq -r '.id' 2>/dev/null || echo "$user_id")
        return 0  # Success
    else
        echo -e "${RED}Error creating user ${email}: ${body}${NC}" >&2
        return 1  # Error
    fi
}



