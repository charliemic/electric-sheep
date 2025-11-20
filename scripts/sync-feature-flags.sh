#!/bin/bash
# Sync feature flags from flags.yaml to Supabase
# This script reads flags.yaml and upserts flags into the feature_flags table

# Don't use set -e here - we handle errors explicitly
# Use set -u to catch undefined variables, but allow commands to fail
set -u  # Fail on undefined variables (safer than set -e)

# Add verbose mode
VERBOSE="${VERBOSE:-false}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if required tools are installed
if ! command -v yq &> /dev/null; then
    echo -e "${RED}Error: yq is required but not installed.${NC}"
    echo "Install with: brew install yq (macOS) or see https://github.com/mikefarah/yq"
    exit 1
fi

if ! command -v psql &> /dev/null; then
    echo -e "${RED}Error: psql is required but not installed.${NC}"
    echo "Install PostgreSQL client tools"
    exit 1
fi

# Parse command line arguments
FLAGS_FILE=""
while [[ $# -gt 0 ]]; do
    case $1 in
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        *)
            if [ -z "$FLAGS_FILE" ]; then
                FLAGS_FILE="$1"
            fi
            shift
            ;;
    esac
done

# Default to feature-flags/flags.yaml if not provided
FLAGS_FILE="${FLAGS_FILE:-feature-flags/flags.yaml}"

# Check if flags.yaml exists
if [ ! -f "$FLAGS_FILE" ]; then
    echo -e "${RED}Error: Flags file not found: $FLAGS_FILE${NC}"
    exit 1
fi

# Get Supabase connection details from environment or config
SUPABASE_URL="${SUPABASE_URL:-}"
SUPABASE_DB_URL="${SUPABASE_DB_URL:-}"
SUPABASE_SERVICE_ROLE_KEY="${SUPABASE_SERVICE_ROLE_KEY:-}"

# Try to get from Supabase CLI if available
if command -v supabase &> /dev/null; then
    if [ -z "$SUPABASE_DB_URL" ]; then
        # Try to get from linked project
        PROJECT_REF=$(supabase status 2>/dev/null | grep "API URL" | awk '{print $3}' | sed 's|https://||' | sed 's|\.supabase\.co||' || echo "")
        if [ -n "$PROJECT_REF" ]; then
            echo -e "${YELLOW}Note: Using Supabase CLI linked project${NC}"
        fi
    fi
fi

# Validate required environment variables
if [ -z "$SUPABASE_DB_URL" ] && [ -z "$SUPABASE_URL" ]; then
    echo -e "${RED}Error: SUPABASE_DB_URL or SUPABASE_URL must be set${NC}"
    echo "Set SUPABASE_DB_URL (PostgreSQL connection string) or SUPABASE_URL + SUPABASE_SECRET_KEY"
    exit 1
fi

# If using Supabase URL, construct DB URL (this is a simplified approach)
# In production, you should use the direct database connection string
if [ -n "$SUPABASE_URL" ] && [ -z "$SUPABASE_DB_URL" ]; then
    echo -e "${YELLOW}Warning: Direct database connection (SUPABASE_DB_URL) is recommended for flag sync${NC}"
    echo "Using PostgREST API instead..."
    USE_API=true
else
    USE_API=false
fi

echo -e "${GREEN}Syncing feature flags from $FLAGS_FILE${NC}"

# Verify database connection before proceeding
if [ "$USE_API" = false ] && [ -n "$SUPABASE_DB_URL" ]; then
    echo -e "${YELLOW}Testing database connection...${NC}"
    set +u
    CONNECTION_TEST=$(psql "$SUPABASE_DB_URL" -c "SELECT 1;" 2>&1)
    CONNECTION_EXIT=$?
    set -u
    if [ $CONNECTION_EXIT -ne 0 ]; then
        echo -e "${YELLOW}⚠ Database connection failed, falling back to PostgREST API${NC}"
        echo -e "${YELLOW}Error: $CONNECTION_TEST${NC}"
        echo -e "${YELLOW}Using PostgREST API instead...${NC}"
        USE_API=true
    else
        echo -e "${GREEN}✓ Database connection successful${NC}"
    fi
fi

# Count flags
FLAG_COUNT=$(yq '.flags | length' "$FLAGS_FILE")
echo "Found $FLAG_COUNT flag(s) to sync"

# Process each flag
SUCCESS_COUNT=0
ERROR_COUNT=0

if [ "$USE_API" = true ]; then
    # Use PostgREST API (requires SUPABASE_URL and SUPABASE_SECRET_KEY)
    # Note: SUPABASE_SERVICE_ROLE_KEY env var accepts both secret keys and service_role keys
    if [ -z "$SUPABASE_SERVICE_ROLE_KEY" ]; then
        echo -e "${RED}Error: SUPABASE_SECRET_KEY required for API sync${NC}"
        echo "Set SUPABASE_SECRET_KEY environment variable (or SUPABASE_SERVICE_ROLE_KEY for compatibility)"
        exit 1
    fi
    
    for i in $(seq 0 $((FLAG_COUNT - 1))); do
        KEY=$(yq ".flags[$i].key" "$FLAGS_FILE")
        VALUE_TYPE=$(yq ".flags[$i].value_type" "$FLAGS_FILE")
        ENABLED=$(yq ".flags[$i].enabled" "$FLAGS_FILE")
        DESCRIPTION=$(yq ".flags[$i].description // \"\"" "$FLAGS_FILE")
        SEGMENT_ID=$(yq ".flags[$i].segment_id // \"\"" "$FLAGS_FILE")
        USER_ID=$(yq ".flags[$i].user_id // \"\"" "$FLAGS_FILE")
        
        # Get value based on type
        if [ "$VALUE_TYPE" = "boolean" ]; then
            VALUE=$(yq ".flags[$i].boolean_value" "$FLAGS_FILE")
            JSON_PAYLOAD=$(cat <<EOF
{
  "key": "$KEY",
  "value_type": "$VALUE_TYPE",
  "boolean_value": $VALUE,
  "enabled": $ENABLED,
  "description": "$DESCRIPTION",
  "segment_id": ${SEGMENT_ID:-null},
  "user_id": ${USER_ID:-null}
}
EOF
)
        elif [ "$VALUE_TYPE" = "string" ]; then
            VALUE=$(yq ".flags[$i].string_value" "$FLAGS_FILE")
            JSON_PAYLOAD=$(cat <<EOF
{
  "key": "$KEY",
  "value_type": "$VALUE_TYPE",
  "string_value": "$VALUE",
  "enabled": $ENABLED,
  "description": "$DESCRIPTION",
  "segment_id": ${SEGMENT_ID:-null},
  "user_id": ${USER_ID:-null}
}
EOF
)
        elif [ "$VALUE_TYPE" = "int" ]; then
            VALUE=$(yq ".flags[$i].int_value" "$FLAGS_FILE")
            JSON_PAYLOAD=$(cat <<EOF
{
  "key": "$KEY",
  "value_type": "$VALUE_TYPE",
  "int_value": $VALUE,
  "enabled": $ENABLED,
  "description": "$DESCRIPTION",
  "segment_id": ${SEGMENT_ID:-null},
  "user_id": ${USER_ID:-null}
}
EOF
)
        fi
        
        # Upsert using PostgREST: Try PATCH (update) first, then POST (insert) if not found
        # This is more reliable than using POST with merge-duplicates
        PATCH_RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH \
            -H "apikey: $SUPABASE_SERVICE_ROLE_KEY" \
            -H "Authorization: Bearer $SUPABASE_SERVICE_ROLE_KEY" \
            -H "Content-Type: application/json" \
            -H "Prefer: return=representation" \
            -d "$JSON_PAYLOAD" \
            "$SUPABASE_URL/rest/v1/feature_flags?key=eq.$KEY")
        
        PATCH_HTTP_CODE=$(echo "$PATCH_RESPONSE" | tail -n1)
        
        if [ "$PATCH_HTTP_CODE" = "200" ] || [ "$PATCH_HTTP_CODE" = "204" ]; then
            # Update successful
            echo -e "${GREEN}✓ Updated: $KEY${NC}"
            SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
        elif [ "$PATCH_HTTP_CODE" = "404" ]; then
            # Flag doesn't exist, try to insert
            if [ "$VERBOSE" = "true" ]; then
                echo -e "${YELLOW}Flag $KEY not found, attempting insert...${NC}"
            fi
            
            POST_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST \
                -H "apikey: $SUPABASE_SERVICE_ROLE_KEY" \
                -H "Authorization: Bearer $SUPABASE_SERVICE_ROLE_KEY" \
                -H "Content-Type: application/json" \
                -H "Prefer: return=representation" \
                -d "$JSON_PAYLOAD" \
                "$SUPABASE_URL/rest/v1/feature_flags")
            
            POST_HTTP_CODE=$(echo "$POST_RESPONSE" | tail -n1)
            
            if [ "$POST_HTTP_CODE" = "201" ]; then
                echo -e "${GREEN}✓ Inserted: $KEY${NC}"
                SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
            else
                echo -e "${RED}✗ Failed to insert: $KEY (HTTP $POST_HTTP_CODE)${NC}"
                echo "$POST_RESPONSE" | head -n-1
                ERROR_COUNT=$((ERROR_COUNT + 1))
            fi
        else
            # PATCH failed with unexpected error
            echo -e "${RED}✗ Failed to update: $KEY (HTTP $PATCH_HTTP_CODE)${NC}"
            echo "$PATCH_RESPONSE" | head -n-1
            ERROR_COUNT=$((ERROR_COUNT + 1))
        fi
    done
else
    # Use direct PostgreSQL connection
    for i in $(seq 0 $((FLAG_COUNT - 1))); do
        KEY=$(yq ".flags[$i].key" "$FLAGS_FILE")
        VALUE_TYPE=$(yq ".flags[$i].value_type" "$FLAGS_FILE")
        ENABLED=$(yq ".flags[$i].enabled" "$FLAGS_FILE")
        DESCRIPTION=$(yq ".flags[$i].description // \"\"" "$FLAGS_FILE")
        SEGMENT_ID=$(yq ".flags[$i].segment_id // \"\"" "$FLAGS_FILE")
        USER_ID=$(yq ".flags[$i].user_id // \"\"" "$FLAGS_FILE")
        
        # Handle null values properly
        if [ "$SEGMENT_ID" = "null" ] || [ -z "$SEGMENT_ID" ] || [ "$SEGMENT_ID" = "\"\"" ]; then
            SEGMENT_ID_SQL="NULL"
        else
            SEGMENT_ID_SQL="'$SEGMENT_ID'"
        fi
        
        if [ "$USER_ID" = "null" ] || [ -z "$USER_ID" ] || [ "$USER_ID" = "\"\"" ]; then
            USER_ID_SQL="NULL"
        else
            USER_ID_SQL="'$USER_ID'"
        fi
        
        # Escape single quotes in description
        DESCRIPTION_ESCAPED=$(echo "$DESCRIPTION" | sed "s/'/''/g")
        
        # Build SQL based on value type
        if [ "$VALUE_TYPE" = "boolean" ]; then
            VALUE=$(yq ".flags[$i].boolean_value" "$FLAGS_FILE")
            SQL=$(cat <<EOF
INSERT INTO public.feature_flags (
    key, value_type, boolean_value, enabled, description, segment_id, user_id, version
) VALUES (
    '$KEY', '$VALUE_TYPE', $VALUE, $ENABLED, '$DESCRIPTION_ESCAPED', 
    $SEGMENT_ID_SQL, $USER_ID_SQL, 1
)
ON CONFLICT (key) DO UPDATE SET
    value_type = EXCLUDED.value_type,
    boolean_value = EXCLUDED.boolean_value,
    string_value = NULL,
    int_value = NULL,
    enabled = EXCLUDED.enabled,
    description = EXCLUDED.description,
    segment_id = EXCLUDED.segment_id,
    user_id = EXCLUDED.user_id,
    updated_at = NOW();
    -- Note: version is auto-incremented by trigger on UPDATE
EOF
)
        elif [ "$VALUE_TYPE" = "string" ]; then
            VALUE=$(yq ".flags[$i].string_value" "$FLAGS_FILE")
            # Escape single quotes in value
            VALUE_ESCAPED=$(echo "$VALUE" | sed "s/'/''/g")
            SQL=$(cat <<EOF
INSERT INTO public.feature_flags (
    key, value_type, string_value, enabled, description, segment_id, user_id, version
) VALUES (
    '$KEY', '$VALUE_TYPE', '$VALUE_ESCAPED', $ENABLED, '$DESCRIPTION_ESCAPED', 
    $SEGMENT_ID_SQL, $USER_ID_SQL, 1
)
ON CONFLICT (key) DO UPDATE SET
    value_type = EXCLUDED.value_type,
    boolean_value = NULL,
    string_value = EXCLUDED.string_value,
    int_value = NULL,
    enabled = EXCLUDED.enabled,
    description = EXCLUDED.description,
    segment_id = EXCLUDED.segment_id,
    user_id = EXCLUDED.user_id,
    updated_at = NOW();
    -- Note: version is auto-incremented by trigger on UPDATE
EOF
)
        elif [ "$VALUE_TYPE" = "int" ]; then
            VALUE=$(yq ".flags[$i].int_value" "$FLAGS_FILE")
            SQL=$(cat <<EOF
INSERT INTO public.feature_flags (
    key, value_type, int_value, enabled, description, segment_id, user_id, version
) VALUES (
    '$KEY', '$VALUE_TYPE', $VALUE, $ENABLED, '$DESCRIPTION_ESCAPED', 
    $SEGMENT_ID_SQL, $USER_ID_SQL, 1
)
ON CONFLICT (key) DO UPDATE SET
    value_type = EXCLUDED.value_type,
    boolean_value = NULL,
    string_value = NULL,
    int_value = EXCLUDED.int_value,
    enabled = EXCLUDED.enabled,
    description = EXCLUDED.description,
    segment_id = EXCLUDED.segment_id,
    user_id = EXCLUDED.user_id,
    updated_at = NOW();
    -- Note: version is auto-incremented by trigger on UPDATE
EOF
)
        fi
        
        # Execute SQL
        # Note: We use set +u temporarily to allow psql to fail without script exiting
        set +u
        echo -e "${YELLOW}Executing SQL for flag: $KEY${NC}"
        PSQL_OUTPUT=$(psql "$SUPABASE_DB_URL" -c "$SQL" 2>&1)
        PSQL_EXIT_CODE=$?
        set -u
        
        # Always show output for debugging
        if [ "$VERBOSE" = "true" ] || [ $PSQL_EXIT_CODE -ne 0 ]; then
            echo -e "${YELLOW}psql exit code: $PSQL_EXIT_CODE${NC}"
            if [ -n "$PSQL_OUTPUT" ]; then
                echo -e "${YELLOW}psql output:${NC}"
                echo "$PSQL_OUTPUT"
            fi
        fi
        
        if [ $PSQL_EXIT_CODE -eq 0 ]; then
            echo -e "${GREEN}✓ Synced: $KEY${NC}"
            SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
        else
            echo -e "${RED}✗ Failed: $KEY${NC}"
            echo -e "${RED}Error output: $PSQL_OUTPUT${NC}"
            echo -e "${YELLOW}SQL that failed:${NC}"
            echo "$SQL"
            echo ""
            ERROR_COUNT=$((ERROR_COUNT + 1))
        fi
    done
fi

echo ""
echo -e "${GREEN}Sync complete: $SUCCESS_COUNT succeeded, $ERROR_COUNT failed${NC}"

if [ $ERROR_COUNT -gt 0 ]; then
    exit 1
fi

