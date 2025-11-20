#!/usr/bin/env bash

# Test helper functions for emulator management tests

# Setup test environment
setup_test_env() {
    # Create temporary lock directory
    export TEST_LOCK_DIR=$(mktemp -d)
    export EMULATOR_LOCK_DIR="$TEST_LOCK_DIR"
    
    # Create temporary directory for test files
    export TEST_TMP_DIR=$(mktemp -d)
    
    # Mock git commands
    export TEST_GIT_BRANCH=""
    export TEST_WORKTREE=""
}

# Cleanup test environment
cleanup_test_env() {
    rm -rf "$TEST_LOCK_DIR" 2>/dev/null || true
    rm -rf "$TEST_TMP_DIR" 2>/dev/null || true
    unset EMULATOR_LOCK_DIR
    unset TEST_LOCK_DIR
    unset TEST_TMP_DIR
    unset TEST_GIT_BRANCH
    unset TEST_WORKTREE
}

# Mock git branch command
mock_git_branch() {
    local branch="$1"
    export TEST_GIT_BRANCH="$branch"
}

# Mock git worktree
mock_git_worktree() {
    local worktree="$1"
    export TEST_WORKTREE="$worktree"
}

# Get script directory
get_script_dir() {
    # BATS_TEST_DIRNAME is the test file path (e.g., /path/to/scripts/tests/test_file.bats)
    # We need to get the scripts/ directory (parent of tests/)
    if [ -n "$BATS_TEST_DIRNAME" ]; then
        # Get directory of test file (scripts/tests/), then go up one level
        local test_dir="$(dirname "$BATS_TEST_DIRNAME")"
        echo "$(cd "$test_dir/.." && pwd)"
    else
        # Fallback: assume we're in scripts/tests/ and go up one level
        echo "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
    fi
}

# Load script as library (for testing functions)
load_script() {
    local script_path="$1"
    source "$script_path"
}

# Create mock lock file
create_mock_lock() {
    local device_id="$1"
    local pid="${2:-$$}"
    local agent_id="${3:-test-agent-123}"
    local branch="${4:-feature/test}"
    local worktree="${5:-}"
    local avd_name="${6:-electric_sheep_test}"
    
    local lock_file="$TEST_LOCK_DIR/${device_id}.lock"
    
    jq -n \
        --arg device_id "$device_id" \
        --arg avd_name "$avd_name" \
        --arg pid "$pid" \
        --arg branch "$branch" \
        --arg worktree "$worktree" \
        --arg acquired_at "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
        --arg agent_id "$agent_id" \
        '{
            device_id: $device_id,
            avd_name: $avd_name,
            pid: ($pid | tonumber),
            branch: $branch,
            worktree: $worktree,
            acquired_at: $acquired_at,
            agent_id: $agent_id
        }' > "$lock_file"
    
    echo "$lock_file"
}

# Check if lock file exists
lock_file_exists() {
    local device_id="$1"
    [ -f "$TEST_LOCK_DIR/${device_id}.lock" ]
}

# Get lock file content
get_lock_content() {
    local device_id="$1"
    local lock_file="$TEST_LOCK_DIR/${device_id}.lock"
    
    if [ -f "$lock_file" ]; then
        cat "$lock_file"
    else
        echo ""
    fi
}

# Assert lock file has correct structure
assert_lock_structure() {
    local device_id="$1"
    local lock_file="$TEST_LOCK_DIR/${device_id}.lock"
    
    [ -f "$lock_file" ] || return 1
    
    # Check JSON structure
    jq -e '.device_id' "$lock_file" > /dev/null 2>&1 || return 1
    jq -e '.pid' "$lock_file" > /dev/null 2>&1 || return 1
    jq -e '.agent_id' "$lock_file" > /dev/null 2>&1 || return 1
    jq -e '.acquired_at' "$lock_file" > /dev/null 2>&1 || return 1
    
    return 0
}

# Assert lock file has specific field value
assert_lock_field() {
    local device_id="$1"
    local field="$2"
    local expected_value="$3"
    local lock_file="$TEST_LOCK_DIR/${device_id}.lock"
    
    [ -f "$lock_file" ] || return 1
    
    local actual_value=$(jq -r ".$field" "$lock_file")
    [ "$actual_value" = "$expected_value" ] || return 1
    
    return 0
}

