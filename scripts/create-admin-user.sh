#!/bin/bash
# Create or promote a user to admin role
#
# Usage:
#   ./scripts/create-admin-user.sh <email> [display_name] [password]
#
# Examples:
#   ./scripts/create-admin-user.sh admin@example.com "Admin User"
#   ./scripts/create-admin-user.sh admin@example.com "Admin User" "secure-password"
#
# Environment Variables:
#   SUPABASE_URL - Supabase project URL
#   SUPABASE_SECRET_KEY - Supabase service role key (sb_secret_...)

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Source the auth admin library
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${SCRIPT_DIR}/lib/supabase-auth-admin.sh"

# Check arguments
if [ $# -lt 1 ]; then
    echo -e "${RED}Usage: $0 <email> [display_name] [password]${NC}" >&2
    echo "" >&2
    echo "Examples:" >&2
    echo "  $0 admin@example.com \"Admin User\"" >&2
    echo "  $0 admin@example.com \"Admin User\" \"secure-password\"" >&2
    exit 1
fi

EMAIL="$1"
DISPLAY_NAME="${2:-Admin User}"
PASSWORD="${3:-}"

# Validate email format
if [[ ! "$EMAIL" =~ ^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$ ]]; then
    echo -e "${RED}Error: Invalid email format: ${EMAIL}${NC}" >&2
    exit 1
fi

# Check if user exists
echo -e "${YELLOW}Checking if user exists...${NC}"
if auth_admin_user_exists "" "$EMAIL"; then
    echo -e "${GREEN}User exists: ${EMAIL}${NC}"
    
    # Update role to admin
    echo -e "${YELLOW}Setting role to admin...${NC}"
    if auth_admin_set_user_role "$EMAIL" "admin"; then
        echo -e "${GREEN}✓ User ${EMAIL} is now an admin${NC}"
        exit 0
    else
        echo -e "${RED}✗ Failed to set admin role${NC}" >&2
        exit 1
    fi
else
    echo -e "${YELLOW}User does not exist. Creating new admin user...${NC}"
    
    # Generate password if not provided
    if [ -z "$PASSWORD" ]; then
        # Generate a secure random password
        PASSWORD=$(openssl rand -base64 16 | tr -d "=+/" | cut -c1-16)
        echo -e "${YELLOW}Generated password: ${PASSWORD}${NC}"
        echo -e "${YELLOW}⚠️  Save this password securely!${NC}"
    fi
    
    # Create user with admin role
    if auth_admin_create_user "" "$EMAIL" "$PASSWORD" "$DISPLAY_NAME"; then
        echo -e "${GREEN}User created: ${EMAIL}${NC}"
        
        # Set admin role
        echo -e "${YELLOW}Setting role to admin...${NC}"
        if auth_admin_set_user_role "$AUTH_ADMIN_USER_ID" "admin"; then
            echo -e "${GREEN}✓ Admin user created successfully!${NC}"
            echo ""
            echo -e "${GREEN}Credentials:${NC}"
            echo "  Email: ${EMAIL}"
            echo "  Password: ${PASSWORD}"
            echo "  Role: admin"
            echo ""
            echo -e "${YELLOW}⚠️  Save these credentials securely!${NC}"
            exit 0
        else
            echo -e "${RED}✗ User created but failed to set admin role${NC}" >&2
            echo -e "${YELLOW}You can manually set the role using:${NC}" >&2
            echo "  auth_admin_set_user_role \"${AUTH_ADMIN_USER_ID}\" \"admin\"" >&2
            exit 1
        fi
    else
        echo -e "${RED}✗ Failed to create user${NC}" >&2
        exit 1
    fi
fi

