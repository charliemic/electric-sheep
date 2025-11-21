#!/bin/bash
# Shared library for Supabase Database Admin API operations
# Provides reusable functions for SQL execution via HTTP API
#
# Usage:
#   source scripts/lib/supabase-db-admin.sh
#   db_admin_execute_sql "$sql_query"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Validate required environment variables
db_admin_validate_env() {
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

# Execute SQL query via Supabase REST API (PostgREST)
#
# Arguments:
#   $1: SQL query string
#
# Returns:
#   0 if successful, 1 on error
#   Outputs result to stdout
db_admin_execute_sql() {
    local sql_query="$1"
    
    if ! db_admin_validate_env; then
        return 1
    fi
    
    if [ -z "$sql_query" ]; then
        echo -e "${RED}Error: SQL query is required${NC}" >&2
        return 1
    fi
    
    # Use Supabase REST API (PostgREST) to execute SQL
    # Note: For complex SQL (functions, DO blocks), we use the Management API
    # For simple queries, PostgREST works, but for DDL/DO blocks, we need Management API
    
    # Try Management API first (supports DDL and complex SQL)
    local api_url="${SUPABASE_URL}/rest/v1/rpc/exec_sql"
    
    # If Management API doesn't exist, fall back to direct PostgREST
    # For now, we'll use the Supabase Management API endpoint
    # This requires the project to have the exec_sql function, or we use a different approach
    
    # Alternative: Use Supabase's SQL execution via Management API
    # The actual endpoint may vary, so we'll use a more direct approach
    
    # For Supabase, we can use the PostgREST endpoint with proper headers
    # But for DDL/DO blocks, we need to use the Management API or CLI
    
    # Actually, the best approach is to use Supabase's SQL Editor API
    # But that's not publicly documented. Instead, we should use the CLI properly
    # OR use a different HTTP-based approach
    
    # Let's use the Supabase REST API with a stored procedure approach
    # Or better: use the Management API's SQL execution endpoint
    
    echo -e "${YELLOW}Warning: Direct SQL execution via HTTP API is not straightforward${NC}" >&2
    echo -e "${YELLOW}Consider using supabase db push for migrations or CLI for SQL execution${NC}" >&2
    return 1
}

# Execute SQL file via HTTP API
#
# Arguments:
#   $1: Path to SQL file
#
# Returns:
#   0 if successful, 1 on error
db_admin_execute_sql_file() {
    local sql_file="$1"
    
    if [ ! -f "$sql_file" ]; then
        echo -e "${RED}Error: SQL file not found: $sql_file${NC}" >&2
        return 1
    fi
    
    local sql_content=$(cat "$sql_file")
    db_admin_execute_sql "$sql_content"
}

