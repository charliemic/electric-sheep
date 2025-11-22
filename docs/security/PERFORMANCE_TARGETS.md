# Security Pipeline Performance Targets

**Last Updated**: 2025-01-22  
**Target**: All pipelines complete in < 5 minutes

## Performance Targets

### Target Times

| Change Type | Secret Scan | Security Lint | Dependency Scan | Total Time |
|-------------|-------------|--------------|----------------|------------|
| **Low-Risk** (UI, docs, scripts) | ~30s | ~1-2 min | **Skipped** | **< 2 min** ✅ |
| **High-Risk** (auth, network, data) | ~30s | ~1-2 min | ~5-8 min | **< 8 min** ⚠️ |
| **Dependencies** (gradle files) | ~30s | ~1-2 min | ~5-8 min | **< 8 min** ⚠️ |
| **Scheduled** (weekly) | ~30s | ~1-2 min | ~5-8 min | **< 10 min** ✅ |

### Risk-Based Scanning

**High-Risk Changes** (trigger dependency scan):
- ✅ Dependency files (`*.gradle*`, `package.json`)
- ✅ Authentication code (`app/**/auth/**`)
- ✅ Network/API code (`app/**/remote/**`)
- ✅ Data handling (`app/**/data/**`, `app/**/repository/**`)
- ✅ Security code (`app/**/security/**`)
- ✅ Build configuration (`build.gradle*`)

**Low-Risk Changes** (skip dependency scan):
- ❌ UI changes (`app/**/ui/**`)
- ❌ Documentation (`docs/**`)
- ❌ Scripts (`scripts/**`)
- ❌ Tests (`app/**/test/**`)
- ❌ CI/CD workflows (`.github/workflows/**`)

## Optimization Summary

### Parallelization
- ✅ All checks run in parallel
- ✅ Total time = longest job (not sum)

### Caching
- ✅ OWASP CVE database cached
- ✅ Gradle dependencies cached
- ✅ Android build artifacts cached

### Change Detection
- ✅ Only run relevant checks
- ✅ Skip dependency scan for low-risk changes

### Performance Tweaks
- ✅ Shallow checkout for PRs
- ✅ Parallel Gradle execution
- ✅ Async artifact uploads
- ✅ Reduced timeouts

## Expected Results

**Most PRs** (low-risk changes):
- Pipeline time: **< 2 minutes** ✅
- Dependency scan: Skipped
- Fast feedback for developers

**Security PRs** (high-risk changes):
- Pipeline time: **< 8 minutes** ⚠️
- Dependency scan: Runs
- Comprehensive security check

**Dependency PRs**:
- Pipeline time: **< 8 minutes** ⚠️
- Dependency scan: Runs (necessary)
- Catches new vulnerabilities

## Monitoring

Track pipeline times in GitHub Actions to ensure targets are met.

