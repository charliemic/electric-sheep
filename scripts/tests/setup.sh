#!/bin/bash

# Setup script for emulator management testing environment
# Checks and installs required dependencies for running tests
# Usage: ./scripts/tests/setup.sh

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔧 Setting up Emulator Management Testing Environment${NC}"
echo ""

# Track what needs to be installed
MISSING_DEPS=()
INSTALL_COMMANDS=()

# Check for bats
if ! command -v bats &> /dev/null; then
    MISSING_DEPS+=("bats-core")
    if [[ "$OSTYPE" == "darwin"* ]]; then
        INSTALL_COMMANDS+=("brew install bats-core")
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        INSTALL_COMMANDS+=("See: https://github.com/bats-core/bats-core#installation")
    else
        INSTALL_COMMANDS+=("See: https://github.com/bats-core/bats-core#installation")
    fi
    echo -e "${RED}✗${NC} bats-core not found"
else
    BATS_VERSION=$(bats --version 2>/dev/null || echo "unknown")
    echo -e "${GREEN}✓${NC} bats-core installed ($BATS_VERSION)"
fi

# Check for jq
if ! command -v jq &> /dev/null; then
    MISSING_DEPS+=("jq")
    if [[ "$OSTYPE" == "darwin"* ]]; then
        INSTALL_COMMANDS+=("brew install jq")
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        INSTALL_COMMANDS+=("sudo apt-get install jq")
    else
        INSTALL_COMMANDS+=("See: https://stedolan.github.io/jq/download/")
    fi
    echo -e "${RED}✗${NC} jq not found"
else
    JQ_VERSION=$(jq --version 2>/dev/null || echo "unknown")
    echo -e "${GREEN}✓${NC} jq installed ($JQ_VERSION)"
fi

# Check for bash (should always be available, but check version)
if ! command -v bash &> /dev/null; then
    echo -e "${RED}✗${NC} bash not found (this is unusual!)"
    exit 1
else
    BASH_VERSION=$(bash --version | head -1 | awk '{print $4}' | cut -d'(' -f1)
    echo -e "${GREEN}✓${NC} bash installed ($BASH_VERSION)"
fi

echo ""

# If dependencies are missing, show installation instructions
if [ ${#MISSING_DEPS[@]} -gt 0 ]; then
    echo -e "${YELLOW}⚠${NC} Missing dependencies detected:"
    echo ""
    
    for i in "${!MISSING_DEPS[@]}"; do
        echo -e "${BLUE}${MISSING_DEPS[$i]}${NC}"
        echo "  ${INSTALL_COMMANDS[$i]}"
        echo ""
    done
    
    # Offer to install on macOS
    if [[ "$OSTYPE" == "darwin"* ]] && command -v brew &> /dev/null; then
        echo -e "${BLUE}Would you like to install missing dependencies now? (y/n)${NC}"
        read -r response
        if [[ "$response" =~ ^[Yy]$ ]]; then
            echo ""
            for i in "${!MISSING_DEPS[@]}"; do
                if [[ "${INSTALL_COMMANDS[$i]}" == brew* ]]; then
                    echo -e "${BLUE}Installing ${MISSING_DEPS[$i]}...${NC}"
                    eval "${INSTALL_COMMANDS[$i]}"
                fi
            done
            echo ""
            echo -e "${GREEN}✓${NC} Dependencies installed!"
            echo ""
            echo -e "${BLUE}Run this script again to verify installation.${NC}"
            exit 0
        fi
    fi
    
    echo -e "${YELLOW}Please install the missing dependencies and run this script again.${NC}"
    exit 1
fi

# Verify test files exist
echo -e "${BLUE}Checking test files...${NC}"
TEST_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [ ! -f "$TEST_DIR/test_emulator_lock_manager.bats" ]; then
    echo -e "${RED}✗${NC} test_emulator_lock_manager.bats not found"
    exit 1
fi
echo -e "${GREEN}✓${NC} test_emulator_lock_manager.bats"

if [ ! -f "$TEST_DIR/test_emulator_discovery.bats" ]; then
    echo -e "${RED}✗${NC} test_emulator_discovery.bats not found"
    exit 1
fi
echo -e "${GREEN}✓${NC} test_emulator_discovery.bats"

if [ ! -f "$TEST_DIR/test_helpers.bash" ]; then
    echo -e "${RED}✗${NC} test_helpers.bash not found"
    exit 1
fi
echo -e "${GREEN}✓${NC} test_helpers.bash"

# Verify scripts exist
echo ""
echo -e "${BLUE}Checking emulator management scripts...${NC}"
SCRIPTS_DIR="$(cd "$TEST_DIR/.." && pwd)"

if [ ! -f "$SCRIPTS_DIR/emulator-lock-manager.sh" ]; then
    echo -e "${RED}✗${NC} emulator-lock-manager.sh not found"
    exit 1
fi
echo -e "${GREEN}✓${NC} emulator-lock-manager.sh"

if [ ! -f "$SCRIPTS_DIR/emulator-discovery.sh" ]; then
    echo -e "${RED}✗${NC} emulator-discovery.sh not found"
    exit 1
fi
echo -e "${GREEN}✓${NC} emulator-discovery.sh"

# Check script permissions
if [ ! -x "$SCRIPTS_DIR/emulator-lock-manager.sh" ]; then
    echo -e "${YELLOW}⚠${NC} Making emulator-lock-manager.sh executable..."
    chmod +x "$SCRIPTS_DIR/emulator-lock-manager.sh"
fi

if [ ! -x "$SCRIPTS_DIR/emulator-discovery.sh" ]; then
    echo -e "${YELLOW}⚠${NC} Making emulator-discovery.sh executable..."
    chmod +x "$SCRIPTS_DIR/emulator-discovery.sh"
fi

echo ""
echo -e "${GREEN}✓${NC} Setup complete! All dependencies and files are ready."
echo ""
echo -e "${BLUE}Next steps:${NC}"
echo "  Run tests: ./scripts/tests/run_tests.sh"
echo "  Or: bats scripts/tests/"

