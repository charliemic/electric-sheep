# Risk-Based Security Scanning

**Last Updated**: 2025-01-22  
**Purpose**: Explain risk-based scanning strategy for fast pipelines

## Overview

To keep pipelines under 5 minutes, we use **risk-based scanning** - the longer dependency scan only runs on high-risk changes.

## Pipeline Time Targets

**Target**: All pipelines complete in < 5 minutes

**Current Performance**:
- Secret Scan: ~30 seconds ✅
- Security Lint: ~1-2 minutes ✅
- Dependency Scan: ~5-8 minutes ⚠️ (only on high-risk changes)

**Strategy**: Run dependency scan only when changes carry higher security risk.

## Risk Categories

### High-Risk Changes (Trigger Dependency Scan)

**Why**: These changes affect security posture and attack surface

1. **Dependency Changes**
   - `**/*.gradle*` - Gradle build files
   - `**/gradle-wrapper.properties` - Gradle wrapper
   - `package.json`, `package-lock.json` - Node dependencies
   - **Risk**: New dependencies may introduce vulnerabilities

2. **Authentication/Authorization Code**
   - `app/**/auth/**` - Authentication code
   - `app/**/Auth*.kt` - Auth-related classes
   - **Risk**: Auth bugs = account takeover

3. **Network/API Code**
   - `app/**/remote/**` - Remote data sources
   - `app/**/network/**` - Network code
   - `app/**/api/**` - API clients
   - **Risk**: Network code = attack surface

4. **Data Handling Code**
   - `app/**/data/**` - Data layer
   - `app/**/repository/**` - Repositories
   - `app/**/database/**` - Database code
   - **Risk**: Data handling bugs = data exposure

5. **Security-Related Code**
   - `app/**/security/**` - Security utilities
   - `app/**/Security*.kt` - Security classes
   - **Risk**: Security code bugs = vulnerabilities

6. **Build Configuration**
   - `build.gradle*` - Build configuration
   - `gradle.properties` - Gradle properties
   - `settings.gradle*` - Settings
   - **Risk**: Build config affects dependencies

### Low-Risk Changes (Skip Dependency Scan)

**Why**: These changes don't significantly affect security posture

1. **UI Changes**
   - `app/**/ui/**` - UI components
   - `app/**/screen/**` - Screens
   - **Risk**: Low - UI changes don't affect dependencies

2. **Documentation**
   - `docs/**` - Documentation
   - `*.md` - Markdown files
   - **Risk**: None - Documentation only

3. **Scripts**
   - `scripts/**` - Automation scripts
   - **Risk**: Low - Scripts don't affect app dependencies

4. **Tests**
   - `app/**/test/**` - Test code
   - **Risk**: Low - Tests don't affect production dependencies

5. **Configuration (Non-Build)**
   - `.github/workflows/**` - CI/CD workflows
   - `.cursor/**` - Cursor rules
   - **Risk**: Low - CI/CD doesn't affect app dependencies

## Scanning Strategy

### Always Run (Fast Checks)

**Secret Scan**: Always runs (~30 seconds)
- Secrets can be in any file
- Very fast, no reason to skip
- Blocks PRs with secrets

**Security Lint**: Runs on app code changes (~1-2 minutes)
- Only when `app/**` or `build.gradle*` changes
- Fast enough to always run
- Catches security code issues

### Conditional Run (Longer Checks)

**Dependency Scan**: Only on high-risk changes (~5-8 minutes)
- Runs when dependencies or high-risk code changes
- Skips for UI, docs, scripts, tests
- Weekly scheduled scan catches everything

## Workflow Logic

```
┌─────────────────────────────────────────┐
│  Push/PR Created                        │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  Change Detection                       │
│  → Classify: high-risk vs low-risk      │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  Parallel Execution                     │
│  ┌─────────────┐  ┌─────────────┐      │
│  │ Secret Scan │  │ Security    │      │
│  │ (always)    │  │ Lint        │      │
│  │ ~30s        │  │ (if app)    │      │
│  └─────────────┘  │ ~1-2 min    │      │
│                   └─────────────┘      │
│  ┌─────────────┐                       │
│  │ Dep Scan    │                       │
│  │ (if high-   │                       │
│  │  risk)      │                       │
│  │ ~5-8 min    │                       │
│  └─────────────┘                       │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  Status Aggregation                     │
│  → Reports overall security status      │
└─────────────────────────────────────────┘
```

## Pipeline Time Examples

### Low-Risk Change (UI Update)

**Files Changed**: `app/ui/screens/HomeScreen.kt`

**Jobs Run**:
- Secret Scan: ~30s
- Security Lint: ~1-2 min
- Dependency Scan: **Skipped** (low risk)

**Total Time**: ~1-2 minutes ✅

### High-Risk Change (Auth Update)

**Files Changed**: `app/auth/UserManager.kt`

**Jobs Run**:
- Secret Scan: ~30s
- Security Lint: ~1-2 min
- Dependency Scan: ~5-8 min (high risk)

**Total Time**: ~5-8 minutes ⚠️ (acceptable for high-risk changes)

### Dependency Change

**Files Changed**: `app/build.gradle.kts`

**Jobs Run**:
- Secret Scan: ~30s
- Security Lint: ~1-2 min
- Dependency Scan: ~5-8 min (dependencies changed)

**Total Time**: ~5-8 minutes ⚠️ (necessary for dependency changes)

## Weekly Comprehensive Scan

**Schedule**: Sunday 3 AM UTC

**What Runs**:
- Full dependency scan (all dependencies)
- Full secret scan (full git history)
- Full security lint (all app code)

**Purpose**: Catch issues that might be missed by change-based scanning

**Time**: ~10-15 minutes (acceptable for scheduled scan)

## Manual Trigger

**When**: `workflow_dispatch`

**What Runs**: All checks (same as scheduled)

**Use Cases**:
- Before releases
- After major refactoring
- Security audits
- When you want comprehensive scan

## Benefits

### Speed
- ✅ Low-risk changes: ~1-2 minutes
- ✅ High-risk changes: ~5-8 minutes
- ✅ Scheduled scans: ~10-15 minutes (acceptable)

### Coverage
- ✅ All changes get secret scan
- ✅ App code changes get security lint
- ✅ High-risk changes get dependency scan
- ✅ Weekly scan catches everything

### Developer Experience
- ✅ Fast feedback for most changes
- ✅ Comprehensive checks when needed
- ✅ No unnecessary waiting
- ✅ Clear risk-based logic

## Risk Assessment Examples

### Example 1: UI Change (Low Risk)

**Change**: Update button color in `app/ui/components/Button.kt`

**Risk Assessment**:
- No dependency changes
- No auth/network/data code
- UI-only change

**Scanning**:
- ✅ Secret Scan: Run
- ✅ Security Lint: Run
- ❌ Dependency Scan: Skip

**Pipeline Time**: ~1-2 minutes ✅

### Example 2: Auth Change (High Risk)

**Change**: Update `app/auth/UserManager.kt`

**Risk Assessment**:
- Authentication code change
- High security impact
- May affect dependencies

**Scanning**:
- ✅ Secret Scan: Run
- ✅ Security Lint: Run
- ✅ Dependency Scan: Run (high risk)

**Pipeline Time**: ~5-8 minutes ⚠️ (acceptable for high-risk)

### Example 3: Dependency Update (High Risk)

**Change**: Update `app/build.gradle.kts` (add new library)

**Risk Assessment**:
- Dependency change
- New code in production
- Potential vulnerabilities

**Scanning**:
- ✅ Secret Scan: Run
- ✅ Security Lint: Run
- ✅ Dependency Scan: Run (dependencies changed)

**Pipeline Time**: ~5-8 minutes ⚠️ (necessary)

### Example 4: Documentation (Low Risk)

**Change**: Update `docs/README.md`

**Risk Assessment**:
- Documentation only
- No code changes
- No security impact

**Scanning**:
- ✅ Secret Scan: Run (secrets can be in docs)
- ❌ Security Lint: Skip (no app code)
- ❌ Dependency Scan: Skip (low risk)

**Pipeline Time**: ~30 seconds ✅

## Configuration

### High-Risk Paths

Defined in `.github/workflows/security-scan.yml`:

```yaml
high-risk:
  # Dependencies
  - '**/*.gradle*'
  - '**/gradle-wrapper.properties'
  - 'package.json'
  - 'package-lock.json'
  # Auth
  - 'app/**/auth/**'
  - 'app/**/Auth*.kt'
  # Network
  - 'app/**/remote/**'
  - 'app/**/network/**'
  - 'app/**/api/**'
  # Data
  - 'app/**/data/**'
  - 'app/**/repository/**'
  - 'app/**/database/**'
  # Security
  - 'app/**/security/**'
  - 'app/**/Security*.kt'
  # Build
  - 'build.gradle*'
  - 'gradle.properties'
  - 'settings.gradle*'
```

### Adjusting Risk Thresholds

**To add more high-risk paths**:
1. Edit `.github/workflows/security-scan.yml`
2. Add paths to `high-risk` filter
3. Test with a PR

**To reduce high-risk paths**:
1. Edit `.github/workflows/security-scan.yml`
2. Remove paths from `high-risk` filter
3. Test with a PR

## Monitoring

### Track Pipeline Times

**Check**:
- GitHub Actions → Workflow runs
- Execution time per job
- Total pipeline time

**Targets**:
- Low-risk changes: < 2 minutes
- High-risk changes: < 8 minutes
- Scheduled scans: < 15 minutes

### Track Dependency Scan Frequency

**Check**:
- How often dependency scan runs
- Whether it's skipping appropriately
- Whether high-risk changes are detected

**Expected**:
- Most PRs: Dependency scan skipped
- Auth/network/data PRs: Dependency scan runs
- Dependency PRs: Dependency scan runs

## Related Documentation

- [Security Principles](./SECURITY_PRINCIPLES.md) - Core security principles
- [Optimization Summary](./OPTIMIZATION_SUMMARY.md) - Performance optimizations
- [Security Quick Reference](./SECURITY_QUICK_REFERENCE.md) - Quick reference

