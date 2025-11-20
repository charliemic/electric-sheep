# CI/CD Test Debugging Guide

**Last Updated**: 2025-11-20  
**Purpose**: Debugging guide for emulator management test failures in CI

## Common Issues and Fixes

### Issue 1: Bats Installation

**Problem**: Ubuntu package `bats` may not be available or may be outdated.

**Fix Applied**:
- Try Ubuntu package first
- Fallback to GitHub release installation
- Verify installation with `bats --version`

**Workflow Code**:
```yaml
- name: Install bats-core
  run: |
    sudo apt-get update
    if sudo apt-get install -y bats 2>/dev/null; then
      echo "Installed bats from Ubuntu package"
    else
      # Fallback to GitHub install
      BATS_VERSION="1.10.0"
      curl -sSL "https://github.com/bats-core/bats-core/archive/v${BATS_VERSION}.tar.gz" -o /tmp/bats.tar.gz
      sudo tar -xz -C /tmp -f /tmp/bats.tar.gz
      sudo /tmp/bats-core-${BATS_VERSION}/install.sh /usr/local
      sudo rm -rf /tmp/bats-core-${BATS_VERSION} /tmp/bats.tar.gz
    fi
```

### Issue 2: Path Resolution in Tests

**Problem**: `get_script_dir()` function may not resolve paths correctly in CI.

**Fix Applied**:
- Improved path resolution logic
- Added fallback for when `BATS_TEST_DIRNAME` is not set
- Better handling of relative paths

**Test Helper Code**:
```bash
get_script_dir() {
    if [ -n "$BATS_TEST_DIRNAME" ]; then
        local test_dir="$(dirname "$BATS_TEST_DIRNAME")"
        echo "$(cd "$test_dir/.." && pwd)"
    else
        echo "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
    fi
}
```

### Issue 3: Glob Expansion

**Problem**: Bash glob patterns may not expand correctly in CI.

**Fix Applied**:
- Explicit iteration through test files
- Check if files exist before processing
- Better error messages

**Test Runner Code**:
```bash
TEST_FILES=("$SCRIPT_DIR"/*.bats)
if [ ! -e "${TEST_FILES[0]}" ]; then
    echo "No test files found"
    exit 1
fi
for test_file in "${TEST_FILES[@]}"; do
    if [ -f "$test_file" ]; then
        bats $VERBOSE "$test_file"
    fi
done
```

### Issue 4: File Permissions

**Problem**: Scripts may not be executable in CI.

**Fix Applied**:
- Make chmod commands more robust
- Add error handling
- Verify files exist before chmod

**Workflow Code**:
```yaml
- name: Make test scripts executable
  run: |
    chmod +x scripts/tests/*.sh scripts/tests/*.bats 2>/dev/null || true
    chmod +x scripts/emulator-*.sh 2>/dev/null || true
    ls -la scripts/tests/ || echo "Test directory check"
```

### Issue 5: Working Directory

**Problem**: Tests may run from wrong directory.

**Fix Applied**:
- Explicitly set working directory
- Add directory listing for debugging
- Use `$GITHUB_WORKSPACE` explicitly

**Workflow Code**:
```yaml
- name: Run emulator management tests
  run: |
    cd "$GITHUB_WORKSPACE"
    pwd
    ls -la scripts/tests/ || echo "Test directory listing"
    ./scripts/tests/run_tests.sh --verbose
```

## Debugging Steps

### 1. Check CI Logs

```bash
# View latest run
gh run list --limit 1

# View logs for specific run
gh run view <run-id> --log

# View logs for specific job
gh run view <run-id> --log --job <job-id>

# View only failed steps
gh run view <run-id> --log-failed
```

### 2. Reproduce Locally

```bash
# Use same environment as CI
docker run -it ubuntu:latest bash

# Install dependencies
apt-get update
apt-get install -y bats jq git

# Clone and test
git clone <repo>
cd <repo>
./scripts/tests/run_tests.sh
```

### 3. Check Common Issues

- **Bats version**: `bats --version`
- **jq version**: `jq --version`
- **Bash version**: `bash --version`
- **File permissions**: `ls -la scripts/tests/`
- **Path resolution**: `get_script_dir` output
- **Test file existence**: `ls scripts/tests/*.bats`

### 4. Add Debugging Output

Add to workflow:
```yaml
- name: Debug information
  run: |
    echo "Working directory: $(pwd)"
    echo "BATS_TEST_DIRNAME: ${BATS_TEST_DIRNAME:-not set}"
    echo "Script dir: $(cd scripts/tests && pwd)"
    echo "Test files:"
    ls -la scripts/tests/*.bats || echo "No .bats files"
    echo "Helper file:"
    ls -la scripts/tests/test_helpers.bash || echo "Helper not found"
```

## Known Limitations

1. **Bats Load Path**: The `load` statement looks for files relative to test file. Ensure `test_helpers.bash` is in same directory.

2. **Path Variables**: `BATS_TEST_DIRNAME` is set by bats. Don't rely on it being set before tests run.

3. **Environment Isolation**: Each test gets fresh environment. Use `setup()` and `teardown()` properly.

## Verification Checklist

Before considering CI fixed:

- [ ] Bats installs successfully
- [ ] jq installs successfully
- [ ] Test files are found
- [ ] Helper file loads correctly
- [ ] Script paths resolve correctly
- [ ] Tests execute (even if some fail)
- [ ] Error messages are clear

## Related Documentation

- [EMULATOR_MANAGEMENT_TESTING.md](EMULATOR_MANAGEMENT_TESTING.md) - Testing guide
- [CI_CD_EMULATOR_TESTS.md](CI_CD_EMULATOR_TESTS.md) - CI/CD integration
- `.github/workflows/build-and-test.yml` - Workflow configuration

