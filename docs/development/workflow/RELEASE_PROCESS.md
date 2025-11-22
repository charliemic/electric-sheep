# Release Process

**Last Updated**: 2025-01-22  
**Purpose**: Document the release process for all Electric Sheep components

## Overview

Electric Sheep consists of multiple components that can be released independently or together:

- **Android App** (`app/`) - Main application
- **Test Framework** (`test-automation/`) - Test automation framework
- **HTML Viewer** (`html-viewer/`) - Markdown/HTML converter tool
- **Metrics Dashboard** (`scripts/metrics/`) - Development metrics dashboard

## Release Types

### Independent Releases (Default)

Each component can be released independently:
- Test Framework updates don't require app releases
- HTML Viewer updates are separate from app releases
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

### Step 2: Bump Version

**For a single component**:
```bash
# Bump patch version (1.0.0 → 1.0.1)
./scripts/bump-version.sh app patch

# Bump minor version (1.0.0 → 1.1.0)
./scripts/bump-version.sh test-framework minor

# Bump major version (1.0.0 → 2.0.0)
./scripts/bump-version.sh html-viewer major
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

### Step 3: Review Changes

```bash
# Review version changes
git diff version.properties

# Review changelog updates
git diff CHANGELOG.md

# Review component build file changes
git diff app/build.gradle.kts
git diff test-automation/build.gradle.kts
```

### Step 4: Commit and Push

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

### Step 5: Verify Release

1. **Check GitHub Actions**:
   - Release workflow should trigger automatically
   - Verify build succeeds
   - Verify artifacts are uploaded

2. **Verify GitHub Release**:
   - Check that release was created
   - Verify release notes are correct
   - Verify artifacts are attached

3. **Test Release Artifacts**:
   - Download and verify artifacts
   - Test installation (for Android app)
   - Verify version numbers

## Component-Specific Release Workflows

### Android App Release

**Trigger**: Git tag `app-v*` (e.g., `app-v1.0.0`)

**Workflow**: `.github/workflows/release-app.yml`

**Steps**:
1. Checkout code
2. Set up Android SDK
3. Build release AAB
4. Sign AAB (using GitHub Secrets)
5. Create GitHub release
6. Upload signed AAB
7. Publish release notes from CHANGELOG.md

**Artifacts**:
- Signed AAB file
- Release notes

### Test Framework Release

**Trigger**: Git tag `test-framework-v*` (e.g., `test-framework-v1.0.0`)

**Workflow**: `.github/workflows/release-test-framework.yml`

**Steps**:
1. Checkout code
2. Set up JDK 17
3. Build JAR artifact
4. Create GitHub release
5. Upload JAR
6. Publish release notes

**Artifacts**:
- JAR file
- Release notes

### HTML Viewer Release

**Trigger**: Git tag `html-viewer-v*` (e.g., `html-viewer-v0.1.0`)

**Workflow**: `.github/workflows/release-html-viewer.yml`

**Steps**:
1. Checkout code
2. Set up Node.js
3. Install dependencies
4. Build static site
5. Deploy to GitHub Pages (or other hosting)
6. Create GitHub release
7. Upload build artifacts
8. Publish release notes

**Artifacts**:
- Built static site
- Release notes

### Metrics Dashboard Release

**Trigger**: Git tag `metrics-dashboard-v*` (e.g., `metrics-dashboard-v1.0.0`)

**Workflow**: `.github/workflows/release-metrics-dashboard.yml`

**Steps**:
1. Checkout code
2. Set up Node.js
3. Install dependencies
4. Create GitHub release
5. Upload package
6. Publish release notes

**Artifacts**:
- Package files
- Release notes

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

### HTML Viewer
- **Patch**: As needed (bug fixes)
- **Minor**: When new features added
- **Major**: When architecture changes

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

