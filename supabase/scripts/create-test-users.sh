#!/bin/bash
# Create test users in Supabase Auth via Admin API
# This script is idempotent - safe to run multiple times
#
# Prerequisites:
#   - SUPABASE_URL environment variable
#   - SUPABASE_SECRET_KEY environment variable (for Admin API access)
#   - curl and jq installed
#
# Usage:
#   SUPABASE_URL=https://xxx.supabase.co SUPABASE_SECRET_KEY=xxx ./create-test-users.sh

set -euo pipefail

# Colors for output (define early for error messages)
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

# Source shared library
if [ -f "${PROJECT_ROOT}/scripts/lib/supabase-auth-admin.sh" ]; then
    source "${PROJECT_ROOT}/scripts/lib/supabase-auth-admin.sh"
else
    echo -e "${RED}Error: supabase-auth-admin.sh library not found${NC}" >&2
    exit 1
fi

# Check prerequisites (using shared library function)
if ! auth_admin_validate_env; then
    exit 1
fi

# Test users configuration (matches TestUserFixtures.kt)
declare -A TEST_USERS=(
    ["test-user-tech-novice-high-stable"]="test-novice-high-stable@electric-sheep.test|Tech Novice - High Stable Mood"
    ["test-user-tech-novice-low-stable"]="test-novice-low-stable@electric-sheep.test|Tech Novice - Low Stable Mood"
    ["test-user-tech-novice-high-unstable"]="test-novice-high-unstable@electric-sheep.test|Tech Novice - High Unstable Mood"
    ["test-user-tech-novice-low-unstable"]="test-novice-low-unstable@electric-sheep.test|Tech Novice - Low Unstable Mood"
    ["test-user-tech-savvy-high-stable"]="test-savvy-high-stable@electric-sheep.test|Tech Savvy - High Stable Mood"
    ["test-user-tech-savvy-low-stable"]="test-savvy-low-stable@electric-sheep.test|Tech Savvy - Low Stable Mood"
    ["test-user-tech-savvy-high-unstable"]="test-savvy-high-unstable@electric-sheep.test|Tech Savvy - High Unstable Mood"
    ["test-user-tech-savvy-low-unstable"]="test-savvy-low-unstable@electric-sheep.test|Tech Savvy - Low Unstable Mood"
)

# Default password for all test users (can be overridden)
TEST_PASSWORD="${TEST_PASSWORD:-test-password-123}"

echo -e "${GREEN}Creating test users in Supabase Auth...${NC}"
echo "Supabase URL: ${SUPABASE_URL}"

created_count=0
skipped_count=0
error_count=0

# Note: User existence check and creation functions are now in shared library

# Process each test user
for user_id in "${!TEST_USERS[@]}"; do
    IFS='|' read -r email display_name <<< "${TEST_USERS[$user_id]}"
    
    echo -n "Processing ${email}... "
    
    # Check if user already exists (using shared library)
    if auth_admin_user_exists "$user_id" "$email"; then
        echo -e "${YELLOW}SKIPPED (already exists)${NC}"
        skipped_count=$((skipped_count + 1))
        continue
    fi
    
    # Create user (using shared library)
    if auth_admin_create_user "$user_id" "$email" "$TEST_PASSWORD" "$display_name"; then
        echo -e "${GREEN}CREATED${NC}"
        created_count=$((created_count + 1))
    else
        echo -e "${RED}FAILED${NC}"
        error_count=$((error_count + 1))
    fi
done

# Summary
echo ""
echo -e "${GREEN}Summary:${NC}"
echo "  Created: ${created_count}"
echo "  Skipped: ${skipped_count}"
echo "  Errors: ${error_count}"

if [ $error_count -gt 0 ]; then
    exit 1
fi

echo -e "${GREEN}Test users setup complete!${NC}"
echo -e "${YELLOW}Note: All test users have password: ${TEST_PASSWORD}${NC}"

