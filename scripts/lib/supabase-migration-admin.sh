#!/bin/bash
# Shared library for Supabase Migration/DDL operations via HTTP API
# Uses supabase db push (HTTP-based) instead of direct SQL execution
#
# Usage:
#   source scripts/lib/supabase-migration-admin.sh
#   migration_admin_execute_sql_file "$sql_file" "$migration_name"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Validate required environment variables
migration_admin_validate_env() {
    # For supabase db push, we need:
    # - Supabase CLI installed
    # - Project linked (supabase link)
    # - Access token (for Management API)
    
    if ! command -v supabase >/dev/null 2>&1; then
        echo -e "${RED}Error: Supabase CLI is not installed${NC}" >&2
        return 1
    fi
    
    # Check if project is linked
    if ! supabase status >/dev/null 2>&1; then
        echo -e "${RED}Error: Supabase project is not linked. Run 'supabase link' first.${NC}" >&2
        return 1
    fi
    
    return 0
}

# Execute SQL file via supabase db push (HTTP-based migration)
#
# Arguments:
#   $1: Path to SQL file
#   $2: Migration name (optional, defaults to timestamp-based name)
#
# Returns:
#   0 if successful, 1 on error
#
# Note: This creates a temporary migration file and pushes it via HTTP API
migration_admin_execute_sql_file() {
    local sql_file="$1"
    local migration_name="${2:-}"
    
    if ! migration_admin_validate_env; then
        return 1
    fi
    
    if [ ! -f "$sql_file" ]; then
        echo -e "${RED}Error: SQL file not found: $sql_file${NC}" >&2
        return 1
    fi
    
    # Generate migration name if not provided
    if [ -z "$migration_name" ]; then
        migration_name="temp_$(basename "$sql_file" .sql)_$(date +%Y%m%d%H%M%S)"
    fi
    
    # Create temporary migration file
    local temp_migration_dir="supabase/migrations"
    mkdir -p "$temp_migration_dir"
    
    local migration_file="${temp_migration_dir}/${migration_name}.sql"
    
    # Copy SQL content to migration file
    cp "$sql_file" "$migration_file"
    
    echo -e "${YELLOW}Created temporary migration: $migration_file${NC}"
    
    # Push migration via HTTP API (supabase db push)
    echo -e "${YELLOW}Pushing migration via supabase db push...${NC}"
    if supabase db push; then
        echo -e "${GREEN}✓ Migration executed successfully${NC}"
        
        # Clean up temporary migration file (optional - you may want to keep it)
        # rm "$migration_file"
        
        return 0
    else
        echo -e "${RED}✗ Failed to push migration${NC}" >&2
        return 1
    fi
}

# Execute SQL string via temporary migration file
#
# Arguments:
#   $1: SQL query string
#   $2: Migration name (optional)
#
# Returns:
#   0 if successful, 1 on error
migration_admin_execute_sql() {
    local sql_query="$1"
    local migration_name="${2:-}"
    
    if [ -z "$sql_query" ]; then
        echo -e "${RED}Error: SQL query is required${NC}" >&2
        return 1
    fi
    
    # Generate migration name if not provided
    if [ -z "$migration_name" ]; then
        migration_name="temp_sql_$(date +%Y%m%d%H%M%S)"
    fi
    
    # Create temporary SQL file
    local temp_dir=$(mktemp -d)
    local temp_sql_file="${temp_dir}/${migration_name}.sql"
    
    echo "$sql_query" > "$temp_sql_file"
    
    # Execute via migration file
    local result=0
    migration_admin_execute_sql_file "$temp_sql_file" "$migration_name" || result=1
    
    # Clean up temp file
    rm -rf "$temp_dir"
    
    return $result
}

