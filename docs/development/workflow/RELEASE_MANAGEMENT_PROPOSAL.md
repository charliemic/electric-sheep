# Release Management Implementation Proposal

**Date**: 2025-01-22  
**Role**: Release Manager Agent  
**Status**: Proposal for Review  
**Priority**: 🔴 High Priority

## Executive Summary

As Release Manager, I propose implementing a **comprehensive multi-component release management system** to enable safe, repeatable, and automated releases across all project components. This addresses critical gaps identified in the repository review.

**Components Identified**:
- **A) Android App** (`app/`) - Gradle, version 1.0
- **B) Test Framework** (`test-automation/`) - Gradle, version 1.0.0
- **C) HTML Viewer/Dashboard** (`html-viewer/`) - Astro/Node.js, version 0.1.0
- **D) Metrics Dashboard** (`scripts/metrics/`) - Node.js/Fastify, no versioning
- **E) Scripts** (`scripts/`) - Shell/Python scripts, no versioning
- **F) Supabase** (`supabase/`) - Database migrations, versioned via timestamps
- **G) Feature Flags** (`feature-flags/`) - YAML config, deployed via CI/CD

**Current State**: Manual version management, no changelog, no release process, components versioned independently  
**Target State**: Unified release management, automated versioning, changelog, documented release workflow, git tagging

---

## 🎯 Goals

1. **Unified Version Management**: Single source of truth for all component versions
2. **Component-Specific Releases**: Support independent or coordinated releases
3. **Standard Changelog**: Track all changes across all components
4. **Repeatable Release Process**: Clear, documented workflow for each component
5. **Git Tagging**: Tag all releases with component-specific tags
6. **Release Notes**: Auto-generate from commits per component
7. **CI/CD Integration**: Automated release builds for each component type

---

## 📦 Component Release Strategy

### Component Release Types

**Independent Releases**: Components can be released independently
- Test Framework updates don't require app releases
- HTML Viewer updates are separate from app releases
- Scripts can be updated without affecting other components

**Coordinated Releases**: Major releases can coordinate multiple components
- App v2.0.0 might coordinate with Test Framework v2.0.0
- Breaking changes might require coordinated updates

**Versioning Strategy**:
- **Android App**: Semantic versioning (1.0.0) + versionCode (integer)
- **Test Framework**: Semantic versioning (1.0.0)
- **HTML Viewer**: Semantic versioning (0.1.0) - currently pre-1.0
- **Metrics Dashboard**: Semantic versioning (start at 0.1.0)
- **Scripts**: Optional versioning (version file or git tags)
- **Supabase**: Migration-based (timestamps, no semantic versioning)
- **Feature Flags**: Config-based (no versioning, deployment-based)

### Git Tagging Strategy

**Component-Specific Tags**:
- `app-v1.0.0` - Android app releases
- `test-framework-v1.0.0` - Test framework releases
- `html-viewer-v0.1.0` - HTML viewer releases
- `metrics-dashboard-v0.1.0` - Metrics dashboard releases
- `v1.0.0` - Coordinated releases (all components)

**Unified Tags** (for coordinated releases):
- `v1.0.0` - All components released together

---

## 📋 Proposed Implementation

### Phase 1: Foundation (Week 1) - **START HERE**

#### 1.1 Create CHANGELOG.md

**Priority**: 🔴 Critical  
**Effort**: 30 minutes  
**Impact**: High - Standard practice, tracks all changes

**Implementation**:
- Create `CHANGELOG.md` following [Keep a Changelog](https://keepachangelog.com/) format
- Start with current version (1.0.0)
- Document recent changes retroactively
- Add to repository root

**Format**:
```markdown
# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial release

## [1.0.0] - 2025-01-22

### Added
- Mood tracking functionality
- Supabase authentication
- Feature flags system
- Accessibility support
```

**Files**:
- `CHANGELOG.md` (new)

---

#### 1.2 Unified Version Management Script

**Priority**: 🔴 Critical  
**Effort**: 4-5 hours  
**Impact**: High - Automates version bumps for all components

**Implementation**:
- Create `scripts/bump-version.sh` script
- Supports component-specific or unified versioning
- Supports semantic versioning (MAJOR.MINOR.PATCH)
- Auto-increments versionCode for Android app
- Updates component-specific version files
- Updates `CHANGELOG.md` with new version entry
- Creates component-specific git tags
- Commits changes

**Script Features**:
```bash
# Bump Android app (patch: 1.0.0 → 1.0.1)
./scripts/bump-version.sh app patch

# Bump test framework (minor: 1.0.0 → 1.1.0)
./scripts/bump-version.sh test-framework minor

# Bump HTML viewer (patch: 0.1.0 → 0.1.1)
./scripts/bump-version.sh html-viewer patch

# Bump metrics dashboard (patch: 0.1.0 → 0.1.1)
./scripts/bump-version.sh metrics-dashboard patch

# Coordinated release (all components to 2.0.0)
./scripts/bump-version.sh all major

# Dry run (preview changes)
./scripts/bump-version.sh app patch --dry-run
```

**Component Support**:
- `app` - Android app (updates `app/build.gradle.kts`, increments versionCode)
- `test-framework` - Test framework (updates `test-automation/build.gradle.kts`)
- `html-viewer` - HTML viewer (updates `html-viewer/package.json`)
- `metrics-dashboard` - Metrics dashboard (updates `scripts/metrics/package.json`)
- `all` - All components (coordinated release)

**Files**:
- `scripts/bump-version.sh` (new)
- `app/build.gradle.kts` (update - read version from file)
- `test-automation/build.gradle.kts` (update - read version from file)
- `html-viewer/package.json` (update - read version from file)
- `scripts/metrics/package.json` (create if doesn't exist)
- `version.properties` (new - unified version file)

**Dependencies**:
- `jq` (for JSON parsing if using version file)
- `git` (for tagging)

---

#### 1.3 Update Component Build Files for Dynamic Versioning

**Priority**: 🔴 Critical  
**Effort**: 2-3 hours  
**Impact**: High - Enables automated version management for all components

**Implementation**:
- Read versions from `version.properties` file (unified version file)
- Component-specific fallbacks if version file doesn't exist
- Auto-increment versionCode for Android app

**Version File Format** (`version.properties`):
```properties
# Unified version file
app.versionName=1.0.0
app.versionCode=1
test-framework.version=1.0.0
html-viewer.version=0.1.0
metrics-dashboard.version=0.1.0
```

**Android App** (`app/build.gradle.kts`):
```kotlin
// Read version from version.properties
val versionPropertiesFile = rootProject.file("version.properties")
val versionProperties = java.util.Properties()
if (versionPropertiesFile.exists()) {
    versionProperties.load(versionPropertiesFile.inputStream())
}

val versionName = versionProperties.getProperty("app.versionName", "1.0.0")
val versionCode = versionProperties.getProperty("app.versionCode", "1").toInt()

defaultConfig {
    versionCode = versionCode
    versionName = versionName
    // ...
}
```

**Test Framework** (`test-automation/build.gradle.kts`):
```kotlin
val versionPropertiesFile = rootProject.file("version.properties")
val versionProperties = java.util.Properties()
if (versionPropertiesFile.exists()) {
    versionProperties.load(versionPropertiesFile.inputStream())
}

version = versionProperties.getProperty("test-framework.version", "1.0.0")
```

**HTML Viewer** (`html-viewer/package.json`):
- Script updates `package.json` directly (npm versioning)

**Metrics Dashboard** (`scripts/metrics/package.json`):
- Script updates `package.json` directly (npm versioning)

**Files**:
- `app/build.gradle.kts` (update)
- `test-automation/build.gradle.kts` (update)
- `version.properties` (new - created by bump-version script)

---

### Phase 2: Release Process Documentation (Week 1)

#### 2.1 Release Process Documentation

**Priority**: 🟡 Important  
**Effort**: 2 hours  
**Impact**: High - Enables consistent releases

**Implementation**:
- Create `docs/development/workflow/RELEASE_PROCESS.md`
- Document complete release workflow
- Include pre-release checklist
- Include post-release tasks
- Include rollback procedures

**Contents**:
1. Pre-Release Checklist
2. Release Steps
3. Post-Release Tasks
4. Rollback Procedures
5. Emergency Releases
6. Release Communication

**Files**:
- `docs/development/workflow/RELEASE_PROCESS.md` (new)

---

#### 2.2 Release Checklist

**Priority**: 🟡 Important  
**Effort**: 1 hour  
**Impact**: Medium - Prevents release mistakes

**Implementation**:
- Create release checklist template
- Include in release process documentation
- Cover: testing, versioning, changelog, signing, deployment

**Checklist Items**:
- [ ] All tests passing
- [ ] CHANGELOG.md updated
- [ ] Version bumped
- [ ] Release notes prepared
- [ ] Release build tested
- [ ] Signing verified
- [ ] Git tag created
- [ ] Release notes published

---

### Phase 3: Automation (Week 2)

#### 3.1 Changelog Generation Script

**Priority**: 🟡 Important  
**Effort**: 3-4 hours  
**Impact**: Medium - Automates changelog updates

**Implementation**:
- Create `scripts/generate-changelog.sh`
- Parse git commits since last release
- Categorize by type (feat, fix, docs, etc.)
- Generate changelog entries
- Update CHANGELOG.md

**Script Features**:
```bash
# Generate changelog for next release
./scripts/generate-changelog.sh

# Generate changelog for specific version
./scripts/generate-changelog.sh 1.1.0
```

**Commit Message Format** (Conventional Commits):
- `feat:` → Added
- `fix:` → Fixed
- `docs:` → Documentation
- `refactor:` → Changed
- `perf:` → Performance
- `test:` → Testing
- `chore:` → Miscellaneous

**Files**:
- `scripts/generate-changelog.sh` (new)

---

#### 3.2 Component-Specific Release Workflows (GitHub Actions)

**Priority**: 🟡 Important  
**Effort**: 6-8 hours  
**Impact**: High - Automates release process for all components

**Implementation**:
- Create component-specific release workflows
- Triggered by component-specific git tags
- Each workflow handles component-specific build/deploy steps

**Android App Release** (`.github/workflows/release-app.yml`):
- Triggered by: `app-v*` tags (e.g., `app-v1.0.0`)
- Steps:
  1. Checkout code
  2. Set up Android SDK
  3. Build release AAB
  4. Sign AAB (using GitHub Secrets)
  5. Create GitHub release
  6. Upload AAB
  7. Publish release notes from CHANGELOG.md

**Test Framework Release** (`.github/workflows/release-test-framework.yml`):
- Triggered by: `test-framework-v*` tags (e.g., `test-framework-v1.0.0`)
- Steps:
  1. Checkout code
  2. Set up JDK 17
  3. Build JAR artifact
  4. Create GitHub release
  5. Upload JAR
  6. Publish release notes

**HTML Viewer Release** (`.github/workflows/release-html-viewer.yml`):
- Triggered by: `html-viewer-v*` tags (e.g., `html-viewer-v0.1.0`)
- Steps:
  1. Checkout code
  2. Set up Node.js
  3. Install dependencies
  4. Build static site
  5. Deploy to GitHub Pages (or other hosting)
  6. Create GitHub release
  7. Upload build artifacts
  8. Publish release notes

**Metrics Dashboard Release** (`.github/workflows/release-metrics-dashboard.yml`):
- Triggered by: `metrics-dashboard-v*` tags (e.g., `metrics-dashboard-v0.1.0`)
- Steps:
  1. Checkout code
  2. Set up Node.js
  3. Install dependencies
  4. Create GitHub release
  5. Upload package
  6. Publish release notes

**Files**:
- `.github/workflows/release-app.yml` (new)
- `.github/workflows/release-test-framework.yml` (new)
- `.github/workflows/release-html-viewer.yml` (new)
- `.github/workflows/release-metrics-dashboard.yml` (new)

---

### Phase 4: Release Notes & Communication (Week 2)

#### 4.1 Release Notes Template

**Priority**: 🟢 Nice to Have  
**Effort**: 1 hour  
**Impact**: Low - Improves release communication

**Implementation**:
- Create release notes template
- Auto-populate from CHANGELOG.md
- Include in GitHub release

**Template**:
```markdown
## Release v1.0.0

### What's New
- [Features from CHANGELOG.md]

### Improvements
- [Improvements from CHANGELOG.md]

### Bug Fixes
- [Fixes from CHANGELOG.md]

### Full Changelog
See [CHANGELOG.md](../../CHANGELOG.md) for complete list of changes.
```

---

## 🚀 Implementation Plan

### Week 1: Foundation
**Day 1-2**: Create CHANGELOG.md, version management script  
**Day 3-4**: Update build.gradle.kts, test version bumping  
**Day 5**: Document release process, create checklist

### Week 2: Automation
**Day 1-2**: Create changelog generation script  
**Day 3-4**: Create release workflow (GitHub Actions)  
**Day 5**: Test complete release process

---

## 📊 Success Metrics

### Immediate (Week 1)
- ✅ CHANGELOG.md exists and follows standard format
- ✅ Version bump script works correctly
- ✅ build.gradle.kts reads version dynamically
- ✅ Release process documented

### Short-term (Week 2)
- ✅ Changelog generation script works
- ✅ Release workflow automates builds
- ✅ First automated release completed

### Long-term (Month 1)
- ✅ All releases use automated process
- ✅ Changelog stays up to date
- ✅ Release process is repeatable and reliable

---

## 🔧 Technical Details

### Version File Format

**`version.properties`** (Unified Version File):
```properties
# Android App
app.versionName=1.0.0
app.versionCode=1

# Test Framework
test-framework.version=1.0.0

# HTML Viewer
html-viewer.version=0.1.0

# Metrics Dashboard
metrics-dashboard.version=0.1.0
```

**Updated by**: `bump-version.sh`  
**Read by**: 
- `app/build.gradle.kts`
- `test-automation/build.gradle.kts`
- `html-viewer/package.json` (updated directly)
- `scripts/metrics/package.json` (updated directly)

### Git Tagging Strategy

**Component-Specific Tags**:
- Format: `{component}-v{MAJOR}.{MINOR}.{PATCH}`
- Examples: 
  - `app-v1.0.0` - Android app release
  - `test-framework-v1.0.0` - Test framework release
  - `html-viewer-v0.1.0` - HTML viewer release
  - `metrics-dashboard-v0.1.0` - Metrics dashboard release

**Unified Tags** (Coordinated Releases):
- Format: `v{MAJOR}.{MINOR}.{PATCH}`
- Examples: `v1.0.0`, `v1.1.0`, `v2.0.0`
- Used when all components are released together

**Created by**: `bump-version.sh`  
**Used by**: Release workflows to trigger component-specific builds

### Semantic Versioning

**MAJOR**: Breaking changes  
**MINOR**: New features (backward compatible)  
**PATCH**: Bug fixes (backward compatible)

**Examples**:
- `1.0.0 → 1.0.1`: Bug fix
- `1.0.0 → 1.1.0`: New feature
- `1.0.0 → 2.0.0`: Breaking change

---

## 🎯 Recommended Implementation Order

### Start Here (Critical Path)
1. ✅ **Create CHANGELOG.md** (30 min) - Quick win, high value
2. ✅ **Create version bump script** (2-3 hours) - Core functionality
3. ✅ **Update build.gradle.kts** (1 hour) - Enables automation
4. ✅ **Document release process** (2 hours) - Enables consistency

### Then (Important)
5. ✅ **Create changelog generation script** (3-4 hours) - Automation
6. ✅ **Create release workflow** (4-5 hours) - Full automation

### Finally (Nice to Have)
7. ✅ **Release notes template** (1 hour) - Polish

---

## 📝 Files to Create/Modify

### New Files
- `CHANGELOG.md` - Unified changelog (or component-specific)
- `version.properties` - Unified version file (generated)
- `scripts/bump-version.sh` - Multi-component version bump script
- `scripts/generate-changelog.sh` - Changelog generation (component-aware)
- `.github/workflows/release-app.yml` - Android app release workflow
- `.github/workflows/release-test-framework.yml` - Test framework release workflow
- `.github/workflows/release-html-viewer.yml` - HTML viewer release workflow
- `docs/development/workflow/RELEASE_PROCESS.md` - Release process docs (all components)

### Modified Files
- `app/build.gradle.kts` - Read version from version.properties
- `test-automation/build.gradle.kts` - Read version from version.properties
- `html-viewer/package.json` - Version updated by script
- `scripts/metrics/package.json` - Create if doesn't exist, version updated by script
- `README.md` - Add release process link (optional)

---

## ✅ Acceptance Criteria

### Phase 1 (Foundation)
- [ ] CHANGELOG.md exists and follows Keep a Changelog format
- [ ] Version bump script works for patch/minor/major
- [ ] build.gradle.kts reads version from file
- [ ] Version bump creates git tag
- [ ] Release process documented

### Phase 2 (Automation)
- [ ] Changelog generation script works
- [ ] Release workflow builds and signs AAB
- [ ] Release workflow creates GitHub release
- [ ] Release workflow uploads artifacts
- [ ] Complete release process tested end-to-end

---

## 🚨 Risks & Mitigations

### Risk 1: Version File Conflicts
**Mitigation**: Version file should be committed, script handles conflicts

### Risk 2: Git Tag Conflicts
**Mitigation**: Script checks for existing tags before creating

### Risk 3: Release Workflow Failures
**Mitigation**: Manual release process documented as fallback

### Risk 4: Changelog Generation Errors
**Mitigation**: Manual changelog updates always possible

---

## 📚 Related Documentation

- `docs/development/analysis/ANDROID_APP_GAP_ANALYSIS.md` - Gap analysis
- `docs/development/analysis/COMPREHENSIVE_REPOSITORY_REVIEW.md` - Repository review
- `docs/development/setup/RELEASE_SIGNING_ROLES.md` - Release signing setup
- [Keep a Changelog](https://keepachangelog.com/) - Changelog format
- [Semantic Versioning](https://semver.org/) - Versioning strategy

---

## 🎬 Next Steps

1. **Review this proposal** - Confirm approach and priorities
2. **Approve Phase 1** - Start with foundation (CHANGELOG, version script)
3. **Implement Phase 1** - Create CHANGELOG.md and version bump script
4. **Test Phase 1** - Verify version bumping works
5. **Proceed to Phase 2** - Document release process
6. **Proceed to Phase 3** - Automate with scripts and workflows

---

**Status**: Ready for Review  
**Estimated Total Effort**: 20-25 hours (increased for multi-component support)  
**Estimated Timeline**: 2-3 weeks  
**Priority**: 🔴 High (Critical for production releases)

---

## 🔄 Component Release Coordination

### Independent Releases (Default)
- Each component can be released independently
- No coordination required
- Use component-specific tags: `app-v1.0.0`, `test-framework-v1.0.0`, etc.

### Coordinated Releases (Major Versions)
- All components released together
- Use unified tag: `v1.0.0`
- All workflows trigger on unified tag
- Useful for major version bumps or breaking changes

### Release Frequency Recommendations

**Android App**: 
- Patch: Weekly (bug fixes)
- Minor: Monthly (new features)
- Major: Quarterly (breaking changes)

**Test Framework**:
- Patch: As needed (bug fixes)
- Minor: When new test capabilities added
- Major: When framework architecture changes

**HTML Viewer**:
- Patch: As needed (bug fixes)
- Minor: When new features added
- Major: When architecture changes

**Metrics Dashboard**:
- Patch: As needed (bug fixes)
- Minor: When new metrics added
- Major: When architecture changes

---

## 📊 Component Status Tracking

### Version Matrix

Track component versions in a single place:

| Component | Current Version | Last Release | Next Release |
|-----------|----------------|--------------|--------------|
| Android App | 1.0.0 | 2025-01-22 | TBD |
| Test Framework | 1.0.0 | 2025-01-22 | TBD |
| HTML Viewer | 0.1.0 | 2025-01-22 | TBD |
| Metrics Dashboard | 0.1.0 | TBD | TBD |

**Location**: `docs/development/workflow/COMPONENT_VERSIONS.md` (new)

---

**Release Manager Agent**  
*Proposing comprehensive release management system for Electric Sheep*

