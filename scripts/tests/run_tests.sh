#!/bin/bash

# Test runner for emulator management scripts
# Usage: ./scripts/tests/run_tests.sh [test-file] [options]
#
# Options:
#   --verbose, -v    Verbose output
#   --setup          Run setup first
#   --help, -h       Show help

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Parse arguments
VERBOSE=""
SETUP_FIRST=""
TEST_FILE=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --verbose|-v)
            VERBOSE="--verbose"
            shift
            ;;
        --setup)
            SETUP_FIRST="true"
            shift
            ;;
        --help|-h)
            echo "Test runner for emulator management scripts"
            echo ""
            echo "Usage: $0 [test-file] [options]"
            echo ""
            echo "Options:"
            echo "  --verbose, -v    Verbose output"
            echo "  --setup          Run setup first"
            echo "  --help, -h       Show this help"
            echo ""
            echo "Examples:"
            echo "  $0                                    # Run all tests"
            echo "  $0 test_emulator_lock_manager.bats   # Run specific test file"
            echo "  $0 --verbose                         # Run all tests with verbose output"
            echo "  $0 --setup                            # Run setup, then all tests"
            exit 0
            ;;
        *)
            if [ -z "$TEST_FILE" ]; then
                TEST_FILE="$1"
            fi
            shift
            ;;
    esac
done

# Run setup if requested
if [ -n "$SETUP_FIRST" ]; then
    echo -e "${BLUE}Running setup...${NC}"
    "$SCRIPT_DIR/setup.sh"
    echo ""
fi

# Check if bats is installed
if ! command -v bats &> /dev/null; then
    echo -e "${RED}❌ bats is not installed${NC}"
    echo ""
    echo -e "${YELLOW}Run setup first:${NC}"
    echo "  ./scripts/tests/setup.sh"
    echo ""
    echo "Or install manually:"
    echo "  macOS: brew install bats-core"
    echo "  Linux: See https://github.com/bats-core/bats-core#installation"
    exit 1
fi

# Check if jq is installed (required for lock manager)
if ! command -v jq &> /dev/null; then
    echo -e "${RED}❌ jq is not installed (required for lock manager)${NC}"
    echo ""
    echo -e "${YELLOW}Run setup first:${NC}"
    echo "  ./scripts/tests/setup.sh"
    echo ""
    echo "Or install manually:"
    echo "  macOS: brew install jq"
    echo "  Linux: sudo apt-get install jq"
    exit 1
fi

# Show test summary
echo -e "${BLUE}🧪 Running Emulator Management Tests${NC}"
echo ""

# Count test files
TEST_COUNT=$(find "$SCRIPT_DIR" -name "*.bats" | wc -l | tr -d ' ')
echo -e "${BLUE}Found ${TEST_COUNT} test file(s)${NC}"
echo ""

# Run tests
if [ -n "$TEST_FILE" ]; then
    # Run specific test file
    if [[ "$TEST_FILE" != /* ]]; then
        # Relative path - assume it's in test directory
        TEST_FILE="$SCRIPT_DIR/$TEST_FILE"
    fi
    
    if [ ! -f "$TEST_FILE" ]; then
        echo -e "${RED}❌ Test file not found: $TEST_FILE${NC}"
        exit 1
    fi
    
    echo -e "${BLUE}Running: $(basename "$TEST_FILE")${NC}"
    echo ""
    bats $VERBOSE "$TEST_FILE"
else
    # Run all tests
    echo -e "${BLUE}Running all tests...${NC}"
    echo ""
    
    # Find all .bats files
    TEST_FILES=("$SCRIPT_DIR"/*.bats)
    
    if [ ! -e "${TEST_FILES[0]}" ]; then
        echo -e "${RED}❌ No test files found in $SCRIPT_DIR${NC}"
        exit 1
    fi
    
    # Run each test file
    for test_file in "${TEST_FILES[@]}"; do
        if [ -f "$test_file" ]; then
            bats $VERBOSE "$test_file"
        fi
    done
fi

TEST_EXIT_CODE=$?

echo ""
if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✓ All tests passed!${NC}"
else
    echo -e "${RED}✗ Some tests failed${NC}"
fi

exit $TEST_EXIT_CODE

