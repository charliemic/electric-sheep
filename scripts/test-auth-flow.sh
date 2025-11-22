#!/bin/bash
# Quick test script for authentication flow
#
# This script helps verify the authentication implementation is working.
# It checks:
# 1. Dependencies are installed
# 2. Environment variables are set
# 3. Server can start (syntax check)
#
# Usage:
#   ./scripts/test-auth-flow.sh

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}Testing Authentication Flow${NC}"
echo ""

# Check dependencies
echo "1️⃣  Checking dependencies..."
cd "$(dirname "$0")/../scripts/metrics"

if [ ! -d "node_modules" ]; then
    echo -e "${YELLOW}   Installing dependencies...${NC}"
    npm install
else
    echo -e "${GREEN}   ✓ Dependencies installed${NC}"
fi

# Check for required packages
if [ ! -d "node_modules/@fastify/rate-limit" ]; then
    echo -e "${RED}   ✗ @fastify/rate-limit not found${NC}"
    echo -e "${YELLOW}   Installing...${NC}"
    npm install @fastify/rate-limit
fi

echo -e "${GREEN}   ✓ All dependencies present${NC}"
echo ""

# Check environment variables
echo "2️⃣  Checking environment variables..."
if [ -z "${SUPABASE_URL:-}" ]; then
    echo -e "${YELLOW}   ⚠️  SUPABASE_URL not set (required for auth)${NC}"
else
    echo -e "${GREEN}   ✓ SUPABASE_URL is set${NC}"
fi

if [ -z "${SUPABASE_ANON_KEY:-}" ]; then
    echo -e "${YELLOW}   ⚠️  SUPABASE_ANON_KEY not set (required for auth)${NC}"
else
    echo -e "${GREEN}   ✓ SUPABASE_ANON_KEY is set${NC}"
fi

if [ -z "${SUPABASE_SECRET_KEY:-}" ] && [ -z "${SUPABASE_SERVICE_ROLE_KEY:-}" ]; then
    echo -e "${YELLOW}   ⚠️  SUPABASE_SECRET_KEY not set (required for admin user creation)${NC}"
else
    echo -e "${GREEN}   ✓ SUPABASE_SECRET_KEY is set${NC}"
fi
echo ""

# Syntax check
echo "3️⃣  Checking server syntax..."
if node -c dashboard-server-fastify.js 2>/dev/null; then
    echo -e "${GREEN}   ✓ Server syntax is valid${NC}"
else
    echo -e "${RED}   ✗ Server syntax error${NC}"
    exit 1
fi

# Check auth middleware syntax
if node -c auth-middleware.js 2>/dev/null; then
    echo -e "${GREEN}   ✓ Auth middleware syntax is valid${NC}"
else
    echo -e "${RED}   ✗ Auth middleware syntax error${NC}"
    exit 1
fi
echo ""

# Summary
echo -e "${GREEN}✓ All checks passed!${NC}"
echo ""
echo "Next steps:"
echo "  1. Set environment variables if needed:"
echo "     export SUPABASE_URL='https://xxx.supabase.co'"
echo "     export SUPABASE_ANON_KEY='eyJxxx...'"
echo "     export SUPABASE_SECRET_KEY='sb_secret_xxx'"
echo ""
echo "  2. Create an admin user:"
echo "     ./scripts/create-admin-user.sh admin@example.com 'Admin User'"
echo ""
echo "  3. Start the dashboard:"
echo "     cd scripts/metrics && npm start"
echo ""
echo "  4. Test login:"
echo "     Open http://localhost:8080/login"
echo "     Sign in with admin credentials"
echo "     Try accessing http://localhost:8080/author (should work for admin)"

