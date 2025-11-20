#!/bin/bash
# Seed test data: Create users and load baseline mood data
# This script runs all seed scripts in order
#
# Prerequisites:
#   - Supabase CLI installed and configured
#   - SUPABASE_URL and SUPABASE_SECRET_KEY for user creation
#   - Database connection (via supabase link)
#
# Usage:
#   export SUPABASE_URL=https://xxx.supabase.co
#   export SUPABASE_SECRET_KEY=sb_secret_xxx
#   supabase link --project-ref xxx
#   ./seed-test-data.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SEED_DIR="$(cd "${SCRIPT_DIR}/../seed" && pwd)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Seeding test data...${NC}"

# Step 1: Create test users (via Admin API)
echo -e "${YELLOW}Step 1: Creating test users...${NC}"
if [ -f "${SCRIPT_DIR}/create-test-users.sh" ]; then
    # Support both SUPABASE_SECRET_KEY (new) and SUPABASE_SERVICE_ROLE_KEY (old) for compatibility
    if [ -z "${SUPABASE_SECRET_KEY:-}" ] && [ -n "${SUPABASE_SERVICE_ROLE_KEY:-}" ]; then
        export SUPABASE_SECRET_KEY="${SUPABASE_SERVICE_ROLE_KEY}"
    fi
    
    if [ -z "${SUPABASE_URL:-}" ] || [ -z "${SUPABASE_SECRET_KEY:-}" ]; then
        echo -e "${YELLOW}Warning: SUPABASE_URL or SUPABASE_SECRET_KEY not set. Skipping user creation.${NC}"
        echo -e "${YELLOW}Set these environment variables to create test users.${NC}"
    else
        bash "${SCRIPT_DIR}/create-test-users.sh"
    fi
else
    echo -e "${RED}Error: create-test-users.sh not found${NC}"
    exit 1
fi

# Step 2: Load baseline mood data (via SQL)
echo -e "${YELLOW}Step 2: Loading baseline mood data...${NC}"

# Check if supabase is linked
if ! supabase status >/dev/null 2>&1; then
    echo -e "${RED}Error: Supabase is not linked. Run 'supabase link' first.${NC}"
    exit 1
fi

# Run seed SQL scripts in order
for seed_file in "${SEED_DIR}"/00*.sql; do
    if [ -f "$seed_file" ]; then
        echo -e "${GREEN}Running $(basename "$seed_file")...${NC}"
        # Use psql via supabase db execute or direct connection
        # For now, we'll use supabase db execute if available
        if command -v supabase >/dev/null 2>&1; then
            # Execute SQL file
            supabase db execute < "$seed_file" || {
                echo -e "${YELLOW}Warning: Failed to execute $seed_file. Continuing...${NC}"
            }
        else
            echo -e "${RED}Error: Supabase CLI not found. Cannot execute SQL scripts.${NC}"
            exit 1
        fi
    fi
done

echo -e "${GREEN}Test data seeding complete!${NC}"

