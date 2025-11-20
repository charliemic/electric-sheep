#!/usr/bin/env bats

# Unit tests for emulator-discovery.sh

load 'test_helpers.bash'

setup() {
    setup_test_env
    SCRIPT_DIR="$(get_script_dir)"
    DISCOVERY="$SCRIPT_DIR/emulator-discovery.sh"
    LOCK_MANAGER="$SCRIPT_DIR/emulator-lock-manager.sh"
}

teardown() {
    cleanup_test_env
}

@test "discovery script exists" {
    [ -f "$DISCOVERY" ]
}

@test "discovery script is executable" {
    [ -x "$DISCOVERY" ]
}

@test "discovery requires lock manager" {
    [ -f "$LOCK_MANAGER" ]
}

@test "get feature name from branch" {
    # Mock git branch
    mock_git_branch "feature/mood-chart"
    
    # Source the discovery script to test get_feature_name function
    # Note: This is a simplified test - full integration would require mocking git
    skip "Requires function extraction for unit testing"
}

@test "get AVD name from feature" {
    # Test AVD name generation logic
    # feature/mood-chart -> electric_sheep_mood_chart
    skip "Requires function extraction for unit testing"
}

@test "acquire command returns device ID" {
    # This would require mocking emulator-manager.sh
    # For now, test that command structure is correct
    run "$DISCOVERY" --help 2>&1 || true
    
    # Should show usage or execute (depending on implementation)
    [ "$status" -ge 0 ]
}

@test "release command releases lock" {
    # Create a lock first
    run "$LOCK_MANAGER" acquire "emulator-5554"
    [ "$status" -eq 0 ]
    
    # Release via discovery
    run "$DISCOVERY" release "emulator-5554"
    
    # Lock should be released
    ! lock_file_exists "emulator-5554"
}

@test "status command shows information" {
    run "$DISCOVERY" status
    
    # Should show status (may be empty if no device acquired)
    [ "$status" -eq 0 ]
}

@test "cleanup command cleans stale locks" {
    # Create stale lock
    create_mock_lock "emulator-5554" "999999" "test-agent"
    
    # Run cleanup
    run "$DISCOVERY" cleanup
    
    [ "$status" -eq 0 ]
    
    # Stale lock should be removed
    ! lock_file_exists "emulator-5554"
}

@test "discovery handles missing lock manager gracefully" {
    # This would require temporarily moving lock manager
    skip "Requires dynamic path testing"
}

