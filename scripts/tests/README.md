# Emulator Management Scripts - Unit Tests

This directory contains unit tests for the emulator management scripts.

## Quick Start

**New to this?** Start here: [QUICKSTART.md](QUICKSTART.md)

```bash
# 1. Run setup (checks dependencies)
./scripts/tests/setup.sh

# 2. Run tests
./scripts/tests/run_tests.sh
```

## Test Framework

We use **Bats (Bash Automated Testing System)** for testing.

### Prerequisites

- **bats-core** - Testing framework
- **jq** - JSON parser (for lock files)
- **bash** 4.0+ - Shell interpreter

### Installation

**Automatic (Recommended):**
```bash
./scripts/tests/setup.sh
```

**Manual:**

```bash
# macOS
brew install bats-core jq

# Linux (Ubuntu/Debian)
sudo apt-get install bats jq

# Or see: https://github.com/bats-core/bats-core#installation
```

### Running Tests

**Using the test runner (Recommended):**
```bash
# Run all tests
./scripts/tests/run_tests.sh

# Run with verbose output
./scripts/tests/run_tests.sh --verbose

# Run specific test file
./scripts/tests/run_tests.sh test_emulator_lock_manager.bats

# Run setup first, then tests
./scripts/tests/run_tests.sh --setup
```

**Using bats directly:**
```bash
# Run all tests
bats scripts/tests/

# Run specific test file
bats scripts/tests/test_emulator_lock_manager.bats

# With verbose output
bats --verbose scripts/tests/
```

## Test Structure

- `test_emulator_lock_manager.bats` - Tests for lock manager
- `test_emulator_discovery.bats` - Tests for discovery service
- `test_helpers.bash` - Shared test helper functions

## Test Coverage

- Lock acquisition and release
- Stale lock detection
- Lock file format validation
- Feature name detection
- AVD name generation
- Error handling

