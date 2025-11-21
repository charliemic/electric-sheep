# CI/CD Integration: Emulator Management Tests

**Last Updated**: 2025-11-20  
**Purpose**: Documentation for CI/CD integration of emulator management tests

## Overview

The emulator management test suite is integrated into GitHub Actions CI/CD pipeline with the following characteristics:

- ✅ **Conditional Execution**: Only runs when relevant files change
- ✅ **Non-Blocking**: Failures don't prevent merge
- ✅ **Fast**: No Android SDK setup required
- ✅ **Isolated**: Separate job from app tests

## Workflow Configuration

### Job: `test-emulator-scripts`

**Location**: `.github/workflows/build-and-test.yml`

**Configuration**:
```yaml
test-emulator-scripts:
  needs: changes
  if: needs.changes.outputs.emulator-scripts == 'true'
  runs-on: ubuntu-latest
  continue-on-error: true  # Non-blocking
```

### File Change Detection

The workflow detects changes to:

- `scripts/emulator-*.sh` - Emulator management scripts
- `scripts/tests/**` - Test files
- `docs/development/EMULATOR_MANAGEMENT*.md` - Documentation
- `Makefile` - If it affects emulator tests

### Conditional Execution

**Runs when:**
- Emulator management scripts are modified
- Test files are added/modified
- Related documentation is updated

**Skips when:**
- Only app code changes (`app/**`)
- Only non-emulator scripts change
- Only other documentation changes

## Test Execution

### Steps

1. **Checkout code**
2. **Install dependencies**:
   - `bats-core` (testing framework)
   - `jq` (JSON parser)
3. **Verify dependencies**
4. **Make scripts executable**
5. **Run tests**: `./scripts/tests/run_tests.sh --verbose`
6. **Upload results** (if any artifacts generated)

### Dependencies

The CI job installs:
- `bats` - via `apt-get install bats`
- `jq` - via `apt-get install jq`

No Android SDK or Java setup required (unlike app tests).

## Non-Blocking Behavior

### Why Non-Blocking?

- Script tests are supplementary to app tests
- Failures in script tests shouldn't block app development
- Allows gradual adoption and refinement
- Scripts can be fixed in follow-up PRs

### Impact

- ✅ **PRs can merge** even if script tests fail
- ✅ **Status shows** test results (with warning if failed)
- ✅ **Results visible** in workflow summary
- ⚠️ **Warning displayed** but doesn't block merge

## Viewing Results

### GitHub UI

1. Go to PR or commit
2. Click "Checks" tab
3. Find "test-emulator-scripts" job
4. View logs and results

### GitHub CLI

```bash
# List recent runs
gh run list

# View specific run
gh run view <run-id>

# Watch running workflow
gh run watch

# View logs for emulator tests
gh run view <run-id> --log | grep -A 50 "test-emulator-scripts"
```

### Artifacts

If tests generate artifacts, they're uploaded with 7-day retention:
- `emulator-script-test-results` - Test logs and results

## Local Testing Before Push

**Always test locally before pushing:**

```bash
# Setup (one-time)
./scripts/tests/setup.sh

# Run tests
./scripts/tests/run_tests.sh

# Or with verbose output
./scripts/tests/run_tests.sh --verbose
```

This ensures:
- ✅ Tests pass before CI runs
- ✅ Faster feedback loop
- ✅ Less CI resource usage
- ✅ Cleaner git history

## Troubleshooting CI Failures

### Tests Fail in CI but Pass Locally

1. **Check dependency versions**:
   ```bash
   # CI uses system packages
   # Local might have different versions
   ```

2. **Check file permissions**:
   ```bash
   # CI makes scripts executable
   # Ensure local scripts are executable too
   chmod +x scripts/tests/*.sh
   chmod +x scripts/tests/*.bats
   ```

3. **Check bash version**:
   ```bash
   # CI uses system bash
   bash --version
   ```

### Tests Don't Run

**Check file paths:**
- Ensure changed files match the filter patterns
- Check workflow logs for change detection

**Force run:**
- Modify a file matching the filter patterns
- Or push an empty commit to trigger

### Dependencies Not Found

**Check installation step:**
- Verify `apt-get update` succeeded
- Check if packages are available in Ubuntu repos
- May need to use different installation method

## Best Practices

### For Developers

1. **Test locally first** - Don't rely on CI for feedback
2. **Fix test failures** - Even though non-blocking, fix them
3. **Update tests** - When modifying scripts, update tests too
4. **Check CI results** - Review even if non-blocking

### For Maintainers

1. **Monitor test results** - Track failure rates
2. **Update CI config** - As test infrastructure evolves
3. **Document changes** - Update this doc when workflow changes
4. **Consider making blocking** - Once tests are stable

## Future Improvements

- [ ] Add test result reporting (similar to app tests)
- [ ] Add test coverage reporting
- [ ] Consider making blocking once stable
- [ ] Add matrix testing (different bash versions)
- [ ] Add performance benchmarks

## Related Documentation

- [EMULATOR_MANAGEMENT_TESTING.md](EMULATOR_MANAGEMENT_TESTING.md) - Testing guide
- [EMULATOR_MANAGEMENT_IMPLEMENTATION.md](EMULATOR_MANAGEMENT_IMPLEMENTATION.md) - Implementation details
- `.github/workflows/build-and-test.yml` - Workflow configuration

