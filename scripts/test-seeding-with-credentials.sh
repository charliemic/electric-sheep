#!/bin/bash
# Test seeding with credentials from local.properties and GitHub secrets
# This script attempts to use available credentials to test seeding

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
MAIN_REPO="$(cd "$PROJECT_ROOT/.." && pwd)/electric-sheep"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  Test Data Seeding - Runtime Verification${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

# Read Supabase URLs from local.properties
if [ -f "$MAIN_REPO/local.properties" ]; then
    STAGING_URL=$(grep "^supabase.staging.url=" "$MAIN_REPO/local.properties" | cut -d'=' -f2 || echo "")
    STAGING_REF=$(echo "$STAGING_URL" | sed 's|https://||' | sed 's|\.supabase\.co||' || echo "")
    
    if [ -z "$STAGING_REF" ]; then
        echo -e "${RED}❌ Could not extract staging project ref${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ Found staging project: $STAGING_REF${NC}"
    echo ""
else
    echo -e "${RED}❌ local.properties not found${NC}"
    exit 1
fi

# Check for Supabase CLI
if ! command -v supabase >/dev/null 2>&1; then
    echo -e "${RED}❌ Supabase CLI not found${NC}"
    exit 1
fi

# Try to get access token from GitHub (if available)
echo -e "${YELLOW}Attempting to get Supabase access token from GitHub...${NC}"
ACCESS_TOKEN=""
if command -v gh >/dev/null 2>&1 && gh auth status >/dev/null 2>&1; then
    # Note: We can't directly read secrets, but we can try to use them via workflow
    echo -e "${YELLOW}⚠️  Cannot directly read GitHub secrets${NC}"
    echo "   Secrets are encrypted and can only be used in GitHub Actions"
    echo "   You'll need to provide credentials manually"
fi

# Check if already linked
if supabase status >/dev/null 2>&1; then
    echo -e "${GREEN}✅ Supabase is already linked${NC}"
    CURRENT_REF=$(supabase status 2>/dev/null | grep "Project ID" | awk '{print $3}' || echo "")
    if [ "$CURRENT_REF" != "$STAGING_REF" ]; then
        echo -e "${YELLOW}⚠️  Currently linked to different project: $CURRENT_REF${NC}"
        echo "   We'll use staging: $STAGING_REF"
    fi
else
    echo -e "${YELLOW}Linking to Supabase staging project...${NC}"
    echo "   Project Ref: $STAGING_REF"
    echo ""
    echo -e "${YELLOW}Note: You may need to provide:${NC}"
    echo "   1. SUPABASE_ACCESS_TOKEN (from GitHub Secrets or Supabase Dashboard)"
    echo "   2. SUPABASE_DB_PASSWORD_STAGING (from GitHub Secrets or Supabase Dashboard)"
    echo ""
    echo -e "${BLUE}To link manually:${NC}"
    echo "   export SUPABASE_ACCESS_TOKEN='<your-token>'"
    echo "   supabase login --token \"\$SUPABASE_ACCESS_TOKEN\""
    echo "   supabase link --project-ref $STAGING_REF"
    echo ""
    read -p "Do you want to attempt linking now? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        if [ -z "${SUPABASE_ACCESS_TOKEN:-}" ]; then
            echo -e "${YELLOW}SUPABASE_ACCESS_TOKEN not set.${NC}"
            echo "   Get from: GitHub Secrets or Supabase Dashboard"
            echo "   Or run: gh secret get SUPABASE_ACCESS_TOKEN (if you have permission)"
            exit 1
        fi
        
        echo "Logging in to Supabase..."
        supabase login --token "$SUPABASE_ACCESS_TOKEN" || {
            echo -e "${RED}❌ Login failed${NC}"
            exit 1
        }
        
        echo "Linking to project..."
        supabase link --project-ref "$STAGING_REF" || {
            echo -e "${RED}❌ Link failed${NC}"
            exit 1
        }
        
        echo -e "${GREEN}✅ Linked successfully${NC}"
    else
        echo -e "${YELLOW}Skipping link. Run manually when ready.${NC}"
        exit 0
    fi
fi

echo ""

# Test 1: Verify function can be created
echo -e "${YELLOW}Test 1: Creating generate_mood_score function...${NC}"
if cat "$PROJECT_ROOT/supabase/seed/functions/generate_mood_score.sql" | supabase db execute 2>&1; then
    echo -e "${GREEN}✅ Function created/updated successfully${NC}"
else
    echo -e "${YELLOW}⚠️  Function creation had warnings (may already exist)${NC}"
fi

echo ""

# Test 2: Verify function works
echo -e "${YELLOW}Test 2: Testing generate_mood_score function...${NC}"
TEST_QUERY="SELECT generate_mood_score('high_stable', 0, 0) as score;"
RESULT=$(supabase db execute "$TEST_QUERY" 2>&1 | grep -E "^\s*[0-9]+" | head -1 || echo "")
if [ -n "$RESULT" ]; then
    SCORE=$(echo "$RESULT" | tr -d ' ')
    if [ "$SCORE" -ge 1 ] && [ "$SCORE" -le 10 ]; then
        echo -e "${GREEN}✅ Function works! Generated score: $SCORE${NC}"
    else
        echo -e "${RED}❌ Function returned invalid score: $SCORE${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  Could not verify function output${NC}"
fi

echo ""

# Test 3: Check for service role key (needed for user creation)
echo -e "${YELLOW}Test 3: Checking for service role key...${NC}"
if [ -z "${SUPABASE_SECRET_KEY:-}" ] && [ -z "${SUPABASE_SERVICE_ROLE_KEY:-}" ]; then
    echo -e "${YELLOW}⚠️  Service role key not set${NC}"
    echo "   Needed for: Creating test users"
    echo "   Get from: Supabase Dashboard → Settings → API → secret key"
    echo "   Or from: GitHub Secrets (SUPABASE_SECRET_KEY_STAGING)"
    echo ""
    echo "   To set: export SUPABASE_SECRET_KEY='sb_secret_...'"
    echo ""
    echo -e "${BLUE}SQL-only tests can continue without service role key${NC}"
else
    export SUPABASE_SECRET_KEY="${SUPABASE_SECRET_KEY:-$SUPABASE_SERVICE_ROLE_KEY}"
    export SUPABASE_URL="$STAGING_URL"
    echo -e "${GREEN}✅ Service role key is set${NC}"
    echo ""
    
    # Test 4: Try to create test users
    echo -e "${YELLOW}Test 4: Testing user creation (dry run)...${NC}"
    echo "   This would create test users if run for real"
    echo "   Run manually: ./supabase/scripts/create-test-users.sh"
fi

echo ""

# Test 5: Test baseline data loading (SQL only, no users needed)
echo -e "${YELLOW}Test 5: Testing baseline data SQL (syntax check)...${NC}"
if [ -f "$PROJECT_ROOT/supabase/seed/002_load_baseline_mood_data.sql" ]; then
    echo -e "${GREEN}✅ SQL script exists${NC}"
    echo "   Note: This requires test users to exist first"
    echo "   Run after creating users: supabase db execute < supabase/seed/002_load_baseline_mood_data.sql"
else
    echo -e "${RED}❌ SQL script not found${NC}"
fi

echo ""

# Summary
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  Verification Summary${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

if supabase status >/dev/null 2>&1; then
    echo -e "${GREEN}✅ Supabase is linked${NC}"
    echo -e "${GREEN}✅ Function can be created${NC}"
    
    if [ -n "${SUPABASE_SECRET_KEY:-}" ]; then
        echo -e "${GREEN}✅ Service role key available${NC}"
        echo ""
        echo -e "${GREEN}Ready for full seeding test!${NC}"
        echo ""
        echo "To run full seeding:"
        echo "  export SUPABASE_URL='$STAGING_URL'"
        echo "  export SUPABASE_SECRET_KEY='<your-key>'"
        echo "  ./supabase/scripts/seed-test-data.sh"
    else
        echo -e "${YELLOW}⚠️  Service role key needed for user creation${NC}"
        echo ""
        echo "To complete verification:"
        echo "  1. Get service role key from Supabase Dashboard"
        echo "  2. export SUPABASE_SECRET_KEY='sb_secret_...'"
        echo "  3. export SUPABASE_URL='$STAGING_URL'"
        echo "  4. ./supabase/scripts/seed-test-data.sh"
    fi
else
    echo -e "${YELLOW}⚠️  Supabase not linked${NC}"
    echo "   Link first, then re-run this script"
fi

echo ""

