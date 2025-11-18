#!/bin/bash
# Test script to diagnose feature flag SQL issues
# This script tests the SQL syntax and database connection

set -u

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

if [ -z "${SUPABASE_DB_URL:-}" ]; then
    echo -e "${RED}Error: SUPABASE_DB_URL must be set${NC}"
    exit 1
fi

echo -e "${YELLOW}Testing feature flag SQL operations...${NC}"
echo ""

# Test 1: Connection
echo -e "${YELLOW}1. Testing database connection...${NC}"
set +u
CONNECTION_TEST=$(psql "$SUPABASE_DB_URL" -c "SELECT version();" 2>&1)
CONNECTION_EXIT=$?
set -u
if [ $CONNECTION_EXIT -eq 0 ]; then
    echo -e "${GREEN}✓ Connection successful${NC}"
    echo "$CONNECTION_TEST" | head -3
else
    echo -e "${RED}✗ Connection failed${NC}"
    echo "$CONNECTION_TEST"
    exit 1
fi
echo ""

# Test 2: Check if table exists
echo -e "${YELLOW}2. Checking if feature_flags table exists...${NC}"
set +u
TABLE_CHECK=$(psql "$SUPABASE_DB_URL" -c "\d public.feature_flags" 2>&1)
TABLE_EXIT=$?
set -u
if [ $TABLE_EXIT -eq 0 ]; then
    echo -e "${GREEN}✓ Table exists${NC}"
    echo "$TABLE_CHECK" | head -20
else
    echo -e "${RED}✗ Table does not exist or cannot be accessed${NC}"
    echo "$TABLE_CHECK"
    echo ""
    echo -e "${YELLOW}Possible causes:${NC}"
    echo "  - Migration not run yet"
    echo "  - Wrong database/schema"
    echo "  - RLS blocking access (but direct psql should bypass RLS)"
    exit 1
fi
echo ""

# Test 3: Check unique constraint on key
echo -e "${YELLOW}3. Checking unique constraint on 'key' column...${NC}"
set +u
CONSTRAINT_CHECK=$(psql "$SUPABASE_DB_URL" -c "
SELECT 
    conname as constraint_name,
    contype as constraint_type
FROM pg_constraint
WHERE conrelid = 'public.feature_flags'::regclass
AND contype = 'u';
" 2>&1)
CONSTRAINT_EXIT=$?
set -u
if [ $CONSTRAINT_EXIT -eq 0 ]; then
    echo -e "${GREEN}✓ Unique constraint check${NC}"
    echo "$CONSTRAINT_CHECK"
    if echo "$CONSTRAINT_CHECK" | grep -q "key"; then
        echo -e "${GREEN}  ✓ Unique constraint on 'key' found${NC}"
    else
        echo -e "${YELLOW}  ⚠ Unique constraint on 'key' not found (may use implicit UNIQUE)${NC}"
    fi
else
    echo -e "${RED}✗ Failed to check constraints${NC}"
    echo "$CONSTRAINT_CHECK"
fi
echo ""

# Test 4: Test INSERT with sample data
echo -e "${YELLOW}4. Testing INSERT statement...${NC}"
TEST_SQL=$(cat <<EOF
INSERT INTO public.feature_flags (
    key, value_type, boolean_value, enabled, description, segment_id, user_id, version
) VALUES (
    'test_flag_' || extract(epoch from now())::text, 'boolean', false, true, 'Test flag', 
    NULL, NULL, 1
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
EOF
)

set +u
INSERT_TEST=$(psql "$SUPABASE_DB_URL" -c "$TEST_SQL" 2>&1)
INSERT_EXIT=$?
set -u
if [ $INSERT_EXIT -eq 0 ]; then
    echo -e "${GREEN}✓ INSERT test successful${NC}"
    echo "$INSERT_TEST" | head -5
else
    echo -e "${RED}✗ INSERT test failed${NC}"
    echo "$INSERT_TEST"
    echo ""
    echo -e "${YELLOW}SQL that failed:${NC}"
    echo "$TEST_SQL"
fi
echo ""

# Test 5: Check RLS policies
echo -e "${YELLOW}5. Checking RLS policies...${NC}"
set +u
RLS_CHECK=$(psql "$SUPABASE_DB_URL" -c "
SELECT 
    schemaname,
    tablename,
    policyname,
    permissive,
    roles,
    cmd,
    qual
FROM pg_policies
WHERE tablename = 'feature_flags';
" 2>&1)
RLS_EXIT=$?
set -u
if [ $RLS_EXIT -eq 0 ]; then
    echo -e "${GREEN}✓ RLS policies check${NC}"
    echo "$RLS_CHECK"
else
    echo -e "${YELLOW}⚠ Could not check RLS policies${NC}"
    echo "$RLS_CHECK"
fi
echo ""

# Test 6: Check current user/role
echo -e "${YELLOW}6. Checking database user and role...${NC}"
set +u
USER_CHECK=$(psql "$SUPABASE_DB_URL" -c "SELECT current_user, current_role, session_user;" 2>&1)
USER_EXIT=$?
set -u
if [ $USER_EXIT -eq 0 ]; then
    echo -e "${GREEN}✓ User check${NC}"
    echo "$USER_CHECK"
else
    echo -e "${RED}✗ Failed to check user${NC}"
    echo "$USER_CHECK"
fi
echo ""

echo -e "${CYAN}═══════════════════════════════════════════════════════════${NC}"
echo -e "${CYAN}  Diagnostic Complete${NC}"
echo -e "${CYAN}═══════════════════════════════════════════════════════════${NC}"

