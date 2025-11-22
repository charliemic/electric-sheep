# Release Process

**Last Updated**: 2025-01-22  
**Purpose**: Document the release process for all Electric Sheep components

## Overview

Electric Sheep consists of multiple components that can be released independently or together:

- **Android App** (`app/`) - Main application
- **Test Framework** (`test-automation/`) - Test automation framework
- **Metrics Dashboard** (`scripts/metrics/`) - Development metrics dashboard (includes content authoring)

## Release Types

### Independent Releases (Default)

Each component can be released independently:
- Test Framework updates don't require app releases
- Components use component-specific git tags: `app-v1.0.0`, `test-framework-v1.0.0`, etc.

### Coordinated Releases (Major Versions)

All components released together:
- Use unified git tag: `v1.0.0`
- Useful for major version bumps or breaking changes
- All component workflows trigger on unified tag

## Pre-Release Checklist

Before releasing any component, complete this checklist:

### Code Quality
- [ ] All tests passing (`./gradlew test` for Gradle projects, `npm test` for Node projects)
- [ ] Linting passes (`./gradlew lint` for Android app)
- [ ] No critical security vulnerabilities
- [ ] Code review completed (if applicable)

### Version Management
- [ ] Version bumped using `./scripts/bump-version.sh`
- [ ] `version.properties` updated
- [ ] Component-specific build files updated (if needed)
- [ ] `CHANGELOG.md` updated with release notes

### Documentation
- [ ] Release notes prepared
- [ ] Breaking changes documented (if any)
- [ ] Migration guide created (if breaking changes)

### Build & Signing
- [ ] Release build successful
- [ ] Signing verified (for Android app)
- [ ] Artifacts generated correctly

## Release Steps

### Step 1: Prepare Release

1. **Ensure you're on main branch and up to date**:
   ```bash
   git checkout main
   git pull origin main
   ```

2. **Run pre-release checks**:
   ```bash
   # Run tests
   ./gradlew test
   
   # Run linting (Android app)
   ./gradlew lint
   ```

3. **Update CHANGELOG.md**:
   - Move items from `[Unreleased]` to new version section
   - Add release date
   - Document breaking changes (if any)

### Step 2: Generate Changelog (Optional but Recommended)

**Before bumping version, generate changelog from commits**:
```bash
# Generate changelog for specific component
./scripts/generate-changelog.sh app 1.0.1

# Or generate for all components
./scripts/generate-changelog.sh all 1.0.1
```

This will:
- Extract commits since last release
- Categorize them (Added, Fixed, Changed, etc.)
- Update CHANGELOG.md automatically

**Note**: The version bump script also updates CHANGELOG.md, but generating it first gives you a preview.

### Step 3: Bump Version

**For a single component**:
```bash
# Bump patch version (1.0.0 → 1.0.1)
./scripts/bump-version.sh app patch

# Bump minor version (1.0.0 → 1.1.0)
./scripts/bump-version.sh test-framework minor

# Bump major version (1.0.0 → 2.0.0)
```

**For all components (coordinated release)**:
```bash
# Bump all components to major version
./scripts/bump-version.sh all major
```

**Dry run (preview changes)**:
```bash
./scripts/bump-version.sh app patch --dry-run
```

The script will:
- Update `version.properties`
- Update component-specific build files
- Update `CHANGELOG.md`
- Create git tag
- **Note**: You still need to commit and push

### Step 4: Review Changes

```bash
# Review version changes
git diff version.properties

# Review changelog updates
git diff CHANGELOG.md

# Review component build file changes
git diff app/build.gradle.kts
git diff test-automation/build.gradle.kts
```

### Step 5: Commit and Push

```bash
# Stage changes
git add version.properties CHANGELOG.md app/build.gradle.kts test-automation/build.gradle.kts

# Commit
git commit -m "chore: release <component> v<version>"

# Push commits
git push origin main

# Push tags
git push --tags
```

### Step 6: Verify Release

1. **Check GitHub Actions**:
   - Release workflow should trigger automatically on tag push
   - Workflow: `.github/workflows/release-<component>.yml`
   - Verify build succeeds
   - Verify artifacts are uploaded

2. **Verify GitHub Release**:
   - Check that release was created automatically
   - Verify release notes are extracted from CHANGELOG.md
   - Verify artifacts are attached (AAB for app, JAR for test-framework, etc.)

3. **Test Release Artifacts**:
   - Download and verify artifacts
   - Test installation (for Android app)
   - Verify version numbers match tag

## Component-Specific Release Workflows

### Android App Release

**Trigger**: Git tag `app-v*` (e.g., `app-v1.0.0`)

**Workflow**: `.github/workflows/release-app.yml`

**Trigger**: Git tag `app-v*` (e.g., `app-v1.0.0`)

**Steps**:
1. Checkout code
2. Extract version from tag
3. Set up JDK 17 and Android SDK
4. Build release AAB (`./gradlew bundleRelease`)
5. Sign AAB (using keystore from GitHub Secrets)
6. Extract release notes from CHANGELOG.md
7. Create GitHub release with notes
8. Upload signed AAB as release asset

**Artifacts**:
- Signed AAB file (`app-release.aab`)
- Release notes (from CHANGELOG.md)
- GitHub release with download link

### Test Framework Release

**Trigger**: Git tag `test-framework-v*` (e.g., `test-framework-v1.0.0`)

**Workflow**: `.github/workflows/release-test-framework.yml`

**Trigger**: Git tag `test-framework-v*` (e.g., `test-framework-v1.0.0`)

**Steps**:
1. Checkout code
2. Extract version from tag
3. Set up JDK 17
4. Build JAR artifact (`./gradlew build`)
5. Extract release notes from CHANGELOG.md
6. Create GitHub release with notes
7. Upload JAR as release asset

**Artifacts**:
- JAR file (`test-automation/build/libs/*.jar`)
- Release notes (from CHANGELOG.md)
- GitHub release with download link

### Metrics Dashboard Release

**Trigger**: Git tag `metrics-dashboard-v*` (e.g., `metrics-dashboard-v1.0.0`)

**Workflow**: `.github/workflows/release-metrics-dashboard.yml`

**Trigger**: Git tag `metrics-dashboard-v*` (e.g., `metrics-dashboard-v1.0.0`)

**Steps**:
1. Checkout code
2. Extract version from tag
3. Set up Node.js 18
4. Install dependencies (`npm ci`)
5. Verify package.json version matches tag
6. Extract release notes from CHANGELOG.md
7. Create GitHub release with notes
8. Upload package files as release assets

**Artifacts**:
- Package files (`package.json`, `*.js`)
- Release notes (from CHANGELOG.md)
- GitHub release with download link

## Post-Release Tasks

### Immediate (Within 1 Hour)

- [ ] Verify release artifacts are accessible
- [ ] Test installation/usage (if applicable)
- [ ] Announce release (if applicable)
- [ ] Update documentation links (if needed)

### Short-term (Within 1 Day)

- [ ] Monitor for issues
- [ ] Collect user feedback
- [ ] Update project status/docs
- [ ] Archive old releases (if needed)

## Emergency Releases

For critical bug fixes or security patches:

1. **Create hotfix branch**:
   ```bash
   git checkout -b hotfix/<component>-<version>
   ```

2. **Fix the issue**:
   - Make minimal changes
   - Add tests
   - Update CHANGELOG.md

3. **Bump patch version**:
   ```bash
   ./scripts/bump-version.sh <component> patch
   ```

4. **Release immediately**:
   - Follow normal release process
   - Mark as emergency release in notes
   - Communicate urgency to users

## Rollback Procedures

If a release has critical issues:

### Android App Rollback

1. **Identify last known good version**
2. **Create rollback release**:
   ```bash
   git checkout <last-good-tag>
   ./scripts/bump-version.sh app patch
   # Follow normal release process
   ```
3. **Communicate rollback to users**

### Other Components

1. **Revert to previous version tag**
2. **Create new patch release** (if needed)
3. **Update documentation**

## Version Numbering

### Semantic Versioning

All components use [Semantic Versioning](https://semver.org/):

- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes (backward compatible)

### Version Code (Android App Only)

- Auto-incremented on each release
- Must always increase
- Used by Play Store for updates

### Examples

- `1.0.0 → 1.0.1`: Bug fix (patch)
- `1.0.0 → 1.1.0`: New feature (minor)
- `1.0.0 → 2.0.0`: Breaking change (major)

## Release Frequency Recommendations

### Android App
- **Patch**: Weekly (bug fixes)
- **Minor**: Monthly (new features)
- **Major**: Quarterly (breaking changes)

### Test Framework
- **Patch**: As needed (bug fixes)
- **Minor**: When new test capabilities added
- **Major**: When framework architecture changes

### Metrics Dashboard
- **Patch**: As needed (bug fixes)
- **Minor**: When new metrics added
- **Major**: When architecture changes

## Troubleshooting

### Version Bump Fails

**Issue**: Script fails to update version

**Solution**:
1. Check `version.properties` exists
2. Verify component name is correct
3. Check file permissions
4. Review script output for errors

### Git Tag Already Exists

**Issue**: Tag already exists when creating release

**Solution**:
1. Check if tag exists: `git tag -l "app-v1.0.0"`
2. Delete tag if needed: `git tag -d "app-v1.0.0"` (local) or `git push origin :refs/tags/app-v1.0.0` (remote)
3. Re-run version bump script

### Build Fails After Version Update

**Issue**: Build fails after updating version

**Solution**:
1. Verify `version.properties` format is correct
2. Check build files read version correctly
3. Verify version format matches semantic versioning
4. Check for syntax errors in build files

## Related Documentation

- `docs/development/workflow/RELEASE_MANAGEMENT_PROPOSAL.md` - Complete proposal
- `CHANGELOG.md` - Change log
- `version.properties` - Version file
- `scripts/bump-version.sh` - Version bump script

---

**Last Updated**: 2025-01-22

