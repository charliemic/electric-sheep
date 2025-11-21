#!/bin/bash
# Verify test data seeding infrastructure
# This script attempts to verify seeding works using available credentials

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
MAIN_REPO="$(cd "$PROJECT_ROOT/.." && pwd)/electric-sheep"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  Test Data Seeding Verification${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

# Step 1: Check for Supabase URLs
echo -e "${YELLOW}Step 1: Checking for Supabase URLs...${NC}"
if [ -f "$MAIN_REPO/local.properties" ]; then
    PROD_URL=$(grep "^supabase.url=" "$MAIN_REPO/local.properties" | cut -d'=' -f2 || echo "")
    STAGING_URL=$(grep "^supabase.staging.url=" "$MAIN_REPO/local.properties" | cut -d'=' -f2 || echo "")
    
    if [ -n "$PROD_URL" ]; then
        echo -e "${GREEN}✅ Production URL found: $PROD_URL${NC}"
        PROD_REF=$(echo "$PROD_URL" | sed 's|https://||' | sed 's|\.supabase\.co||')
        echo "   Project Ref: $PROD_REF"
    else
        echo -e "${RED}❌ Production URL not found${NC}"
    fi
    
    if [ -n "$STAGING_URL" ]; then
        echo -e "${GREEN}✅ Staging URL found: $STAGING_URL${NC}"
        STAGING_REF=$(echo "$STAGING_URL" | sed 's|https://||' | sed 's|\.supabase\.co||')
        echo "   Project Ref: $STAGING_REF"
    else
        echo -e "${YELLOW}⚠️  Staging URL not found${NC}"
    fi
else
    echo -e "${RED}❌ local.properties not found at $MAIN_REPO/local.properties${NC}"
fi

echo ""

# Step 2: Check for Supabase CLI
echo -e "${YELLOW}Step 2: Checking Supabase CLI...${NC}"
if command -v supabase >/dev/null 2>&1; then
    VERSION=$(supabase --version 2>&1 | head -1)
    echo -e "${GREEN}✅ Supabase CLI available: $VERSION${NC}"
else
    echo -e "${RED}❌ Supabase CLI not found${NC}"
    echo "   Install: https://supabase.com/docs/guides/cli"
    exit 1
fi

echo ""

# Step 3: Check for GitHub CLI and secrets
echo -e "${YELLOW}Step 3: Checking GitHub Secrets...${NC}"
if command -v gh >/dev/null 2>&1; then
    if gh auth status >/dev/null 2>&1; then
        echo -e "${GREEN}✅ GitHub CLI authenticated${NC}"
        
        # List available secrets (we can't read values, but can check if they exist)
        echo "   Checking for Supabase secrets..."
        SECRETS=$(gh secret list 2>&1 | grep -i supabase || echo "")
        if [ -n "$SECRETS" ]; then
            echo -e "${GREEN}✅ Supabase secrets found:${NC}"
            echo "$SECRETS" | sed 's/^/   /'
        else
            echo -e "${YELLOW}⚠️  No Supabase secrets found in GitHub${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️  GitHub CLI not authenticated${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  GitHub CLI not available${NC}"
fi

echo ""

# Step 4: Check if Supabase is linked
echo -e "${YELLOW}Step 4: Checking Supabase link status...${NC}"
if supabase status >/dev/null 2>&1; then
    echo -e "${GREEN}✅ Supabase is linked${NC}"
    supabase status | head -5
else
    echo -e "${YELLOW}⚠️  Supabase is not linked${NC}"
    if [ -n "${STAGING_REF:-}" ]; then
        echo "   To link staging: supabase link --project-ref $STAGING_REF"
    fi
    if [ -n "${PROD_REF:-}" ]; then
        echo "   To link production: supabase link --project-ref $PROD_REF"
    fi
fi

echo ""

# Step 5: Check for service role key
echo -e "${YELLOW}Step 5: Checking for service role key...${NC}"
if [ -n "${SUPABASE_SECRET_KEY:-}" ]; then
    echo -e "${GREEN}✅ SUPABASE_SECRET_KEY environment variable is set${NC}"
    echo "   Key starts with: $(echo "$SUPABASE_SECRET_KEY" | cut -c1-15)..."
elif [ -n "${SUPABASE_SERVICE_ROLE_KEY:-}" ]; then
    echo -e "${GREEN}✅ SUPABASE_SERVICE_ROLE_KEY environment variable is set${NC}"
    echo "   Key starts with: $(echo "$SUPABASE_SERVICE_ROLE_KEY" | cut -c1-15)..."
    export SUPABASE_SECRET_KEY="$SUPABASE_SERVICE_ROLE_KEY"
else
    echo -e "${YELLOW}⚠️  Service role key not found in environment${NC}"
    echo "   Set SUPABASE_SECRET_KEY or SUPABASE_SERVICE_ROLE_KEY"
    echo "   Get from: Supabase Dashboard → Settings → API → secret key"
fi

echo ""

# Step 6: Verification summary
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  Verification Summary${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

READY=true

if [ -z "${PROD_URL:-}" ] && [ -z "${STAGING_URL:-}" ]; then
    echo -e "${RED}❌ No Supabase URLs found${NC}"
    READY=false
fi

if ! command -v supabase >/dev/null 2>&1; then
    echo -e "${RED}❌ Supabase CLI not available${NC}"
    READY=false
fi

if [ -z "${SUPABASE_SECRET_KEY:-}" ]; then
    echo -e "${YELLOW}⚠️  Service role key not set (needed for user creation)${NC}"
    echo "   You can still test SQL scripts if Supabase is linked"
fi

if [ "$READY" = true ]; then
    echo -e "${GREEN}✅ Ready for testing!${NC}"
    echo ""
    echo "Next steps:"
    echo "  1. Link Supabase: supabase link --project-ref <ref>"
    echo "  2. Set service role key: export SUPABASE_SECRET_KEY=sb_secret_..."
    echo "  3. Run seeding: ./supabase/scripts/seed-test-data.sh"
else
    echo -e "${RED}❌ Not ready - fix issues above${NC}"
fi

echo ""

