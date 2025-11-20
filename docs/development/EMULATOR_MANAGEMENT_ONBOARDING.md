# Emulator Management - Developer Onboarding

**Last Updated**: 2025-11-20  
**Purpose**: Quick onboarding guide for developers working with emulator management scripts

## TL;DR

```bash
# 1. Setup (one-time)
./scripts/tests/setup.sh

# 2. Run tests
./scripts/tests/run_tests.sh

# That's it! 🎉
```

## What Is This?

The emulator management system prevents conflicts when multiple developers/agents work with Android emulators simultaneously. It uses file-based locking to ensure each agent gets its own emulator.

## Quick Start

### Step 1: Install Dependencies

**Option A: Automatic (Recommended)**
```bash
./scripts/tests/setup.sh
```

This will:
- Check for required tools (bats, jq)
- Offer to install missing dependencies (macOS)
- Verify all files are in place

**Option B: Manual**

**macOS:**
```bash
brew install bats-core jq
```

**Linux:**
```bash
sudo apt-get install bats jq
```

### Step 2: Run Tests

```bash
# Run all tests
./scripts/tests/run_tests.sh

# Or use Make (if available)
make test-emulator
```

## Common Tasks

### Running Tests

```bash
# All tests
./scripts/tests/run_tests.sh

# Verbose output
./scripts/tests/run_tests.sh --verbose

# Specific test file
./scripts/tests/run_tests.sh test_emulator_lock_manager.bats

# Setup + tests
./scripts/tests/run_tests.sh --setup
```

### Using Make (Optional)

If you prefer Make:

```bash
make test-emulator-setup      # Run setup
make test-emulator            # Run all tests
make test-emulator-verbose    # Verbose output
make test-emulator-file FILE=test_emulator_lock_manager.bats
```

### Writing New Tests

1. **Create test file**: `scripts/tests/test_my_feature.bats`
2. **Use template**:
   ```bash
   #!/usr/bin/env bats
   load 'test_helpers.bash'
   
   setup() {
       setup_test_env
   }
   
   teardown() {
       cleanup_test_env
   }
   
   @test "should do something" {
       run command_to_test
       [ "$status" -eq 0 ]
   }
   ```
3. **Run it**: `./scripts/tests/run_tests.sh test_my_feature.bats`

## File Structure

```
scripts/
├── emulator-lock-manager.sh      # Lock file management
├── emulator-discovery.sh         # Emulator discovery service
├── emulator-manager.sh           # Main emulator manager
└── tests/
    ├── setup.sh                  # Setup script
    ├── run_tests.sh              # Test runner
    ├── QUICKSTART.md             # Quick start guide
    ├── README.md                 # Full documentation
    ├── test_helpers.bash         # Shared test helpers
    ├── test_emulator_lock_manager.bats
    └── test_emulator_discovery.bats
```

## Dependencies

| Tool | Purpose | Install |
|------|---------|---------|
| **bats-core** | Test framework | `brew install bats-core` |
| **jq** | JSON parsing | `brew install jq` |
| **bash** 4.0+ | Shell interpreter | Usually pre-installed |

## Troubleshooting

### "bats: command not found"

```bash
# macOS
brew install bats-core

# Verify
bats --version
```

### "jq: command not found"

```bash
# macOS
brew install jq

# Verify
jq --version
```

### "Permission denied"

```bash
chmod +x scripts/tests/*.sh
chmod +x scripts/tests/*.bats
```

### Tests fail with "No such file or directory"

Make sure you're running from the project root:
```bash
cd /path/to/electric-sheep
./scripts/tests/run_tests.sh
```

### Setup script doesn't offer to install

The auto-install only works on macOS with Homebrew. On Linux or other systems, install manually:
```bash
# See installation instructions in setup.sh output
```

## Next Steps

- [Read the testing guide](EMULATOR_MANAGEMENT_TESTING.md) for detailed information
- [Check the architecture](EMULATOR_MANAGEMENT_ARCHITECTURE.md) to understand the design
- [Review implementation details](EMULATOR_MANAGEMENT_IMPLEMENTATION.md) for technical deep-dive

## Getting Help

1. **Check documentation**:
   - [QUICKSTART.md](../../scripts/tests/QUICKSTART.md) - Quick start
   - [README.md](../../scripts/tests/README.md) - Test documentation
   - [EMULATOR_MANAGEMENT_TESTING.md](EMULATOR_MANAGEMENT_TESTING.md) - Complete guide

2. **Run setup with verbose output**:
   ```bash
   bash -x ./scripts/tests/setup.sh
   ```

3. **Check test output**:
   ```bash
   ./scripts/tests/run_tests.sh --verbose
   ```

## Related Documentation

- [EMULATOR_MANAGEMENT_ARCHITECTURE.md](EMULATOR_MANAGEMENT_ARCHITECTURE.md) - System architecture
- [EMULATOR_MANAGEMENT_IMPLEMENTATION.md](EMULATOR_MANAGEMENT_IMPLEMENTATION.md) - Implementation details
- [EMULATOR_MANAGEMENT_TESTING.md](EMULATOR_MANAGEMENT_TESTING.md) - Testing guide
- [EMULATOR_SCRIPT_REVIEW.md](EMULATOR_SCRIPT_REVIEW.md) - Script review

