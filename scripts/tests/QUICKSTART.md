# Quick Start: Emulator Management Testing

Get up and running with the emulator management test suite in 2 minutes.

## 1. Run Setup

```bash
./scripts/tests/setup.sh
```

This will:
- ✅ Check for required dependencies (bats, jq)
- ✅ Offer to install missing dependencies (macOS with Homebrew)
- ✅ Verify all test files exist
- ✅ Check script permissions

## 2. Run Tests

```bash
# Run all tests
./scripts/tests/run_tests.sh

# Run with verbose output
./scripts/tests/run_tests.sh --verbose

# Run specific test file
./scripts/tests/run_tests.sh test_emulator_lock_manager.bats
```

## That's It! 🎉

You're ready to run tests. For more details, see:
- [README.md](README.md) - Full documentation
- [../docs/development/EMULATOR_MANAGEMENT_TESTING.md](../../docs/development/EMULATOR_MANAGEMENT_TESTING.md) - Complete testing guide

## Troubleshooting

### "bats is not installed"

**macOS:**
```bash
brew install bats-core
```

**Linux:**
```bash
# See: https://github.com/bats-core/bats-core#installation
```

### "jq is not installed"

**macOS:**
```bash
brew install jq
```

**Linux:**
```bash
sudo apt-get install jq
```

### "Permission denied"

```bash
chmod +x scripts/tests/*.sh
chmod +x scripts/tests/*.bats
```

### Still having issues?

Run setup with verbose output:
```bash
bash -x ./scripts/tests/setup.sh
```

