# Emulator Management - Testing Guide

**Last Updated**: 2025-11-20  
**Status**: Test Infrastructure Complete

## Test Framework: Bats (Bash Automated Testing System)

We use **Bats** for unit testing our bash scripts. Bats is a TAP-compliant testing framework that allows writing tests in bash.

### Why Bats?

- ✅ Native bash testing (no language barrier)
- ✅ TAP-compliant (works with CI/CD)
- ✅ Simple, readable test syntax
- ✅ Good for system-level script testing
- ✅ Active maintenance and community

### Installation

```bash
# macOS
brew install bats-core

# Linux (Ubuntu/Debian)
# See: https://github.com/bats-core/bats-core#installation
```

## Test Structure

```
scripts/tests/
├── README.md                          # Test documentation
├── run_tests.sh                       # Test runner script
├── test_helpers.bash                  # Shared test helper functions
├── test_emulator_lock_manager.bats   # Lock manager tests
└── test_emulator_discovery.bats       # Discovery service tests
```

## Running Tests

### Run All Tests

```bash
./scripts/tests/run_tests.sh
```

### Run Specific Test File

```bash
bats scripts/tests/test_emulator_lock_manager.bats
```

### Run with Verbose Output

```bash
bats --verbose scripts/tests/
```

## Test Coverage

### Lock Manager Tests (`test_emulator_lock_manager.bats`)

✅ **Lock Acquisition**
- Creates lock file
- Validates JSON structure
- Includes required fields
- Prevents duplicate locks

✅ **Lock Release**
- Removes lock file
- Handles non-existent locks gracefully

✅ **Availability Checking**
- Returns true for unlocked devices
- Returns false for locked devices

✅ **Lock Listing**
- Shows no locks when empty
- Lists active locks

✅ **Stale Lock Cleanup**
- Removes locks from dead processes
- Keeps locks from active processes

✅ **Lock Information**
- Shows lock details
- Handles missing locks

✅ **Error Handling**
- Validates required parameters
- Provides helpful error messages

✅ **Concurrency**
- Multiple devices can be locked simultaneously
- Lock isolation between devices

✅ **Lock File Format**
- Contains agent ID
- Contains timestamp (ISO 8601)
- Valid JSON structure

### Discovery Service Tests (`test_emulator_discovery.bats`)

✅ **Basic Functionality**
- Script exists and is executable
- Requires lock manager

✅ **Lock Integration**
- Release command works
- Cleanup command works

⚠️ **Integration Tests** (Require mocking)
- Feature name detection (requires git mocking)
- AVD name generation (requires function extraction)
- Full acquisition flow (requires emulator-manager mocking)

## Test Helpers

The `test_helpers.bash` file provides:

- `setup_test_env()` - Creates isolated test environment
- `cleanup_test_env()` - Cleans up test environment
- `create_mock_lock()` - Creates test lock files
- `lock_file_exists()` - Checks if lock exists
- `assert_lock_structure()` - Validates lock JSON structure
- `assert_lock_field()` - Validates specific lock field values

## Test Isolation

Each test:
- Uses temporary directories (`TEST_LOCK_DIR`, `TEST_TMP_DIR`)
- Isolates lock files from system
- Cleans up after execution
- Doesn't interfere with other tests

## Continuous Integration

Tests can be integrated into CI/CD:

```yaml
# Example GitHub Actions
- name: Install Bats
  run: brew install bats-core

- name: Run Tests
  run: ./scripts/tests/run_tests.sh
```

## Writing New Tests

### Test Template

```bash
#!/usr/bin/env bats

load 'test_helpers.bash'

setup() {
    setup_test_env
    # Setup test-specific environment
}

teardown() {
    cleanup_test_env
}

@test "test description" {
    # Arrange
    # Act
    run command_to_test
    
    # Assert
    [ "$status" -eq 0 ]
    [[ "$output" == *"expected"* ]]
}
```

### Best Practices

1. **One assertion per test** (when possible)
2. **Use descriptive test names** (`@test "should do X when Y"`)
3. **Isolate tests** (don't depend on other tests)
4. **Clean up** (use teardown for cleanup)
5. **Test edge cases** (empty input, missing files, etc.)

## Known Limitations

1. **Integration Tests**: Some tests require mocking external commands (git, adb, emulator)
2. **Function Testing**: Testing internal functions requires extracting them or using `source`
3. **Emulator Mocking**: Full discovery flow requires emulator-manager mocking

## Future Improvements

- [ ] Add integration tests with mocked emulator-manager
- [ ] Extract functions for better unit testing
- [ ] Add performance tests
- [ ] Add concurrency stress tests
- [ ] Add CI/CD integration

## Related Documentation

- `docs/development/EMULATOR_MANAGEMENT_ARCHITECTURE.md` - Architecture design
- `docs/development/EMULATOR_MANAGEMENT_IMPLEMENTATION.md` - Implementation details
- `scripts/tests/README.md` - Test directory README

