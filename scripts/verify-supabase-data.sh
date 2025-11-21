#!/bin/bash
# Verify test data exists in Supabase
# This script queries Supabase to confirm test users and mood data exist

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  Supabase Test Data Verification${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

# Check if Supabase is linked
if ! supabase status >/dev/null 2>&1; then
    echo -e "${RED}❌ Supabase is not linked${NC}"
    echo "   Link first: supabase link --project-ref <ref>"
    exit 1
fi

echo -e "${GREEN}✅ Supabase is linked${NC}"
echo ""

# Query 1: Count test users
echo -e "${YELLOW}Query 1: Counting test users...${NC}"
USER_COUNT=$(supabase db execute "
  SELECT COUNT(*) as count
  FROM auth.users
  WHERE email LIKE 'test-%@electric-sheep.test';
" 2>&1 | grep -E "^\s*[0-9]+" | head -1 | tr -d ' ' || echo "0")

if [ "$USER_COUNT" -eq 8 ]; then
    echo -e "${GREEN}✅ Found 8 test users (expected)${NC}"
elif [ "$USER_COUNT" -gt 0 ]; then
    echo -e "${YELLOW}⚠️  Found $USER_COUNT test users (expected 8)${NC}"
else
    echo -e "${RED}❌ No test users found${NC}"
fi

echo ""

# Query 2: List test users with mood entry counts
echo -e "${YELLOW}Query 2: Test users and mood entry counts...${NC}"
supabase db execute "
  SELECT 
    u.email,
    COUNT(m.id) as entry_count,
    MIN(DATE(to_timestamp(m.timestamp / 1000))) as earliest_date,
    MAX(DATE(to_timestamp(m.timestamp / 1000))) as latest_date
  FROM auth.users u
  LEFT JOIN public.moods m ON m.user_id = u.id
  WHERE u.email LIKE 'test-%@electric-sheep.test'
  GROUP BY u.email
  ORDER BY u.email;
" 2>&1 | grep -v "Manage Postgres" | grep -v "Usage:" | grep -v "Available" || echo "No results"

echo ""

# Query 3: Total mood entries
echo -e "${YELLOW}Query 3: Total mood entries...${NC}"
TOTAL_ENTRIES=$(supabase db execute "
  SELECT COUNT(*) as count
  FROM public.moods m
  INNER JOIN auth.users u ON m.user_id = u.id
  WHERE u.email LIKE 'test-%@electric-sheep.test';
" 2>&1 | grep -E "^\s*[0-9]+" | head -1 | tr -d ' ' || echo "0")

EXPECTED_ENTRIES=240  # 8 users × 30 days
if [ "$TOTAL_ENTRIES" -eq "$EXPECTED_ENTRIES" ]; then
    echo -e "${GREEN}✅ Found $TOTAL_ENTRIES mood entries (expected $EXPECTED_ENTRIES)${NC}"
elif [ "$TOTAL_ENTRIES" -gt 0 ]; then
    echo -e "${YELLOW}⚠️  Found $TOTAL_ENTRIES mood entries (expected $EXPECTED_ENTRIES)${NC}"
else
    echo -e "${RED}❌ No mood entries found${NC}"
fi

echo ""

# Query 4: Check generate_mood_score function
echo -e "${YELLOW}Query 4: Checking generate_mood_score function...${NC}"
FUNCTION_EXISTS=$(supabase db execute "
  SELECT EXISTS(
    SELECT 1 
    FROM pg_proc p
    JOIN pg_namespace n ON p.pronamespace = n.oid
    WHERE n.nspname = 'public' 
    AND p.proname = 'generate_mood_score'
  );
" 2>&1 | grep -E "^\s*(t|true|1)" | head -1 || echo "")

if [ -n "$FUNCTION_EXISTS" ]; then
    echo -e "${GREEN}✅ generate_mood_score function exists${NC}"
    
    # Test the function
    TEST_SCORE=$(supabase db execute "SELECT generate_mood_score('high_stable', 0, 0) as score;" 2>&1 | grep -E "^\s*[0-9]+" | head -1 | tr -d ' ' || echo "")
    if [ -n "$TEST_SCORE" ] && [ "$TEST_SCORE" -ge 1 ] && [ "$TEST_SCORE" -le 10 ]; then
        echo -e "${GREEN}✅ Function works! Test score: $TEST_SCORE${NC}"
    else
        echo -e "${YELLOW}⚠️  Function exists but test returned: $TEST_SCORE${NC}"
    fi
else
    echo -e "${RED}❌ generate_mood_score function not found${NC}"
fi

echo ""

# Summary
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  Verification Summary${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

if [ "$USER_COUNT" -eq 8 ] && [ "$TOTAL_ENTRIES" -eq "$EXPECTED_ENTRIES" ] && [ -n "$FUNCTION_EXISTS" ]; then
    echo -e "${GREEN}✅ All checks passed! Test data is present in Supabase.${NC}"
    exit 0
else
    echo -e "${YELLOW}⚠️  Some checks did not pass. Review output above.${NC}"
    exit 1
fi

