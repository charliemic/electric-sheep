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
    
    # Check for either SUPABASE_SECRET_KEY or SUPABASE_SERVICE_ROLE_KEY (for compatibility)
    if [ -z "${SUPABASE_SECRET_KEY:-}" ] && [ -z "${SUPABASE_SERVICE_ROLE_KEY:-}" ]; then
        echo -e "${RED}Error: SUPABASE_SECRET_KEY or SUPABASE_SERVICE_ROLE_KEY environment variable is not set${NC}" >&2
        echo -e "${YELLOW}Note: Use secret key (sb_secret_...) for Admin API access${NC}" >&2
        return 1
    fi
    
    # Use SUPABASE_SECRET_KEY if set, otherwise fall back to SUPABASE_SERVICE_ROLE_KEY
    if [ -z "${SUPABASE_SECRET_KEY:-}" ]; then
        SUPABASE_SECRET_KEY="${SUPABASE_SERVICE_ROLE_KEY}"
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

# Update user metadata (e.g., role, display_name)
#
# Arguments:
#   $1: User ID (UUID string) or email
#   $2: User metadata JSON object (as string, e.g., '{"role":"admin","display_name":"Admin User"}')
#
# Returns:
#   0 on success, 1 on failure
auth_admin_update_user_metadata() {
    local user_identifier="$1"
    local user_metadata="$2"
    
    if ! auth_admin_validate_env; then
        return 1
    fi
    
    if [ -z "$user_metadata" ]; then
        echo -e "${RED}Error: User metadata is required${NC}" >&2
        return 1
    fi
    
    # First, get the user ID if email was provided
    local user_id="$user_identifier"
    if [[ "$user_identifier" == *"@"* ]]; then
        # It's an email, need to get user ID first
        local api_url="${SUPABASE_URL}/auth/v1/admin/users?email=eq.${user_identifier}"
        local response=$(curl -s -w "\n%{http_code}" -X GET \
            -H "apikey: ${SUPABASE_SECRET_KEY}" \
            -H "Authorization: Bearer ${SUPABASE_SECRET_KEY}" \
            -H "Content-Type: application/json" \
            "$api_url" 2>/dev/null || echo "ERROR")
        
        local http_code=$(echo "$response" | tail -n1)
        local body=$(echo "$response" | sed '$d')
        
        if [ "$http_code" = "200" ]; then
            user_id=$(echo "$body" | jq -r '.users[0].id' 2>/dev/null || echo "")
            if [ -z "$user_id" ] || [ "$user_id" = "null" ]; then
                echo -e "${RED}Error: User not found: ${user_identifier}${NC}" >&2
                return 1
            fi
        else
            echo -e "${RED}Error finding user ${user_identifier}: ${body}${NC}" >&2
            return 1
        fi
    fi
    
    # Update user metadata
    local api_url="${SUPABASE_URL}/auth/v1/admin/users/${user_id}"
    
    # Get existing user data to preserve current metadata
    local get_response=$(curl -s -w "\n%{http_code}" -X GET \
        -H "apikey: ${SUPABASE_SECRET_KEY}" \
        -H "Authorization: Bearer ${SUPABASE_SECRET_KEY}" \
        -H "Content-Type: application/json" \
        "$api_url" 2>/dev/null || echo "ERROR")
    
    local get_http_code=$(echo "$get_response" | tail -n1)
    local get_body=$(echo "$get_response" | sed '$d')
    
    if [ "$get_http_code" != "200" ]; then
        echo -e "${RED}Error fetching user: ${get_body}${NC}" >&2
        return 1
    fi
    
    # Extract existing metadata (response format: {"user_metadata": {...}} or {"user": {"user_metadata": {...}}})
    local existing_metadata=$(echo "$get_body" | jq -r '.user_metadata // .user.user_metadata // {}' 2>/dev/null || echo "{}")
    
    # Merge metadata (new values override existing)
    local merged_metadata=$(echo "$existing_metadata" | jq -c ". + ${user_metadata}" 2>/dev/null || echo "$user_metadata")
    
    local response=$(curl -s -w "\n%{http_code}" -X PUT \
        -H "apikey: ${SUPABASE_SECRET_KEY}" \
        -H "Authorization: Bearer ${SUPABASE_SECRET_KEY}" \
        -H "Content-Type: application/json" \
        -d "{\"user_metadata\":${merged_metadata}}" \
        "$api_url" 2>/dev/null || echo "ERROR")
    
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ]; then
        echo -e "${GREEN}User metadata updated successfully${NC}"
        return 0  # Success
    else
        echo -e "${RED}Error updating user metadata: ${body}${NC}" >&2
        return 1  # Error
    fi
}

# Set user role (admin or user)
#
# Arguments:
#   $1: User ID (UUID string) or email
#   $2: Role ("admin" or "user")
#
# Returns:
#   0 on success, 1 on failure
auth_admin_set_user_role() {
    local user_identifier="$1"
    local role="$2"
    
    if [ -z "$role" ]; then
        echo -e "${RED}Error: Role is required (admin or user)${NC}" >&2
        return 1
    fi
    
    if [ "$role" != "admin" ] && [ "$role" != "user" ]; then
        echo -e "${RED}Error: Role must be 'admin' or 'user'${NC}" >&2
        return 1
    fi
    
    auth_admin_update_user_metadata "$user_identifier" "{\"role\":\"${role}\"}"
}



