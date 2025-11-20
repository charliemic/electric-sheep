# CI/CD Caching Optimization Analysis

**Date**: 2025-11-20  
**Status**: Analysis and Recommendations

## Current Caching State

### ✅ Already Cached
- **Gradle dependencies**: `~/.gradle/caches`, `~/.gradle/wrapper`
- **Android build cache**: `~/.android/build-cache`, `.gradle`
- **Java setup**: Automatic via `setup-java@v4` with `cache: 'gradle'`

### ❌ Not Cached (Opportunities)

#### 1. Tool Installations
- **yq**: Downloaded every run in feature flags workflow (~2-3 seconds)
- **Supabase CLI**: Setup action downloads every run (~5-10 seconds)
- **PostgreSQL client**: Installed via apt every run (~5-10 seconds)
- **jq, bc, uuid-runtime**: Installed every run in test data workflow (~3-5 seconds)

#### 2. Cache Action Version
- Using `actions/cache@v3` (v4 available with better performance)

#### 3. Android SDK Components
- SDK components downloaded every run (could be cached)

#### 4. Build Artifacts Between Jobs
- No artifact sharing between jobs (could save rebuild time)

## Optimization Opportunities

### Priority 1: High Impact, Easy Implementation

#### 1.1 Cache Tool Installations

**Impact**: Save 10-20 seconds per workflow run

**Implementation**:
```yaml
- name: Cache yq
  uses: actions/cache@v4
  id: cache-yq
  with:
    path: /usr/local/bin/yq
    key: yq-${{ runner.os }}-latest
    restore-keys: |
      yq-${{ runner.os }}-

- name: Install yq
  if: steps.cache-yq.outputs.cache-hit != 'true'
  run: |
    sudo wget -qO /usr/local/bin/yq https://github.com/mikefarah/yq/releases/latest/download/yq_linux_amd64
    sudo chmod +x /usr/local/bin/yq
```

**Better approach**: Use GitHub Actions cache for tool binaries directory:
```yaml
- name: Cache tools
  uses: actions/cache@v4
  id: cache-tools
  with:
    path: |
      /usr/local/bin/yq
      /usr/local/bin/supabase
    key: tools-${{ runner.os }}-${{ hashFiles('.github/workflows/**/*.yml') }}
    restore-keys: |
      tools-${{ runner.os }}-
```

#### 1.2 Upgrade Cache Actions

**Impact**: Better cache performance, faster restores

**Change**: `actions/cache@v3` → `actions/cache@v4`

**Benefits**:
- Faster cache operations
- Better cache hit rates
- Improved error handling

#### 1.3 Cache Supabase CLI

**Impact**: Save 5-10 seconds per Supabase workflow

**Note**: `supabase/setup-cli@v1` may already cache internally, but we can verify and optimize.

### Priority 2: Medium Impact

#### 2.1 Cache PostgreSQL Client

**Impact**: Save 5-10 seconds per workflow that uses psql

**Implementation**:
```yaml
- name: Cache PostgreSQL client
  uses: actions/cache@v4
  id: cache-psql
  with:
    path: /usr/bin/psql
    key: psql-${{ runner.os }}-latest
    restore-keys: |
      psql-${{ runner.os }}-

- name: Install PostgreSQL client
  if: steps.cache-psql.outputs.cache-hit != 'true'
  run: |
    sudo apt-get update
    sudo apt-get install -y postgresql-client
```

**Note**: System packages are typically fast to install, but caching can help on slower runners.

#### 2.2 Cache Android SDK Components

**Impact**: Save 30-60 seconds on first run, subsequent runs faster

**Implementation**:
```yaml
- name: Cache Android SDK
  uses: actions/cache@v4
  with:
    path: |
      ${{ env.ANDROID_HOME }}/licenses
      ${{ env.ANDROID_HOME }}/platforms
      ${{ env.ANDROID_HOME }}/build-tools
    key: android-sdk-${{ runner.os }}-${{ hashFiles('**/*.gradle*') }}
    restore-keys: |
      android-sdk-${{ runner.os }}-
```

**Note**: `android-actions/setup-android@v3` may already handle some caching internally.

### Priority 3: Lower Impact, More Complex

#### 3.1 Share Build Artifacts Between Jobs

**Impact**: Could save rebuild time if jobs depend on each other

**Current**: Each job builds independently
**Potential**: Share `.gradle` build cache between jobs (already done via cache)

**Note**: Already optimized - build cache is shared via GitHub Actions cache.

#### 3.2 Parallel Job Optimization

**Current**: Jobs run in parallel where possible
**Opportunity**: Could combine lint + accessibility (already documented in CI_CD_OPTIMIZATION.md)

## Recommended Optimizations

### Immediate (High Impact, Low Effort)

1. ✅ **Upgrade cache actions**: `v3` → `v4`
2. ✅ **Cache yq binary**: Save 2-3 seconds per feature flags workflow
3. ✅ **Cache tool installations**: jq, bc, uuid-runtime for test data workflow

### Short Term (Medium Impact)

4. ✅ **Cache PostgreSQL client**: If used frequently
5. ✅ **Verify Supabase CLI caching**: Ensure setup action caches properly

### Long Term (Lower Priority)

6. ⚠️ **Cache Android SDK**: May already be handled by setup action
7. ⚠️ **Combine lint jobs**: Already documented, low priority

## Implementation Plan

### Step 1: Upgrade Cache Actions

Replace all `actions/cache@v3` with `actions/cache@v4` in:
- `.github/workflows/build-and-test.yml`

### Step 2: Add Tool Caching

Add caching for:
- yq (feature flags workflow)
- jq, bc, uuid-runtime (test data workflow)

### Step 3: Verify Existing Caching

Check if:
- Supabase CLI setup action caches internally
- Android SDK setup caches internally
- Any other tools have built-in caching

## Expected Improvements

### Time Savings Per Workflow Run

- **Feature flags workflow**: ~5-10 seconds (yq caching + apt cache)
- **Test data workflow**: ~3-5 seconds (apt cache)
- **Build workflow**: ~2-5 seconds (cache v4 performance + apt cache)
- **Total**: ~10-20 seconds per workflow run

### Actual Implementation

**Caching Strategy**:
- ✅ **yq binary**: Cached in `/usr/local/bin/yq` (downloaded tool)
- ✅ **apt package cache**: Cached in `/var/cache/apt` (speeds up `apt-get install`)
- ✅ **Cache v4**: Better performance than v3

**Why not cache system binaries**:
- System binaries in `/usr/bin` require sudo and are overwritten by apt-get
- Better to cache the apt package cache directory instead
- `apt-get install` is already fast, but caching package lists helps

### Cost Savings

- Reduced compute time = lower GitHub Actions minutes usage
- Better cache hit rates = fewer downloads

## Cache Key Strategy

### Current Strategy
- Uses file hashes for cache invalidation
- Uses restore-keys for partial matches

### Recommended Strategy
- Keep current approach (works well)
- Add tool-specific caches with version-based keys
- Use `latest` suffix for tools that auto-update

## Monitoring

After implementing optimizations:
1. Monitor cache hit rates in workflow logs
2. Track workflow execution times
3. Verify no regressions in build/test reliability

