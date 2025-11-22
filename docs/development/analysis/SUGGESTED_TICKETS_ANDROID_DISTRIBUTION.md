# Suggested GitHub Issues for Android Distribution & Production Readiness

**Date**: 2025-01-20  
**Based on**: `ANDROID_APP_GAP_ANALYSIS.md`  
**Status**: Suggestions Only (Not Created)

## Overview

This document suggests GitHub Issues to address the gaps identified in the Android App Gap Analysis. Issues are organized by priority and aligned with your ticketing system:

- **Labels**: `priority-1/2/3`, `type:technical/infrastructure`, `area:distribution/monitoring/legal`, `effort:small/medium/large`, `status:not-started`
- **Milestones**: Group related issues (e.g., "Distribution Readiness", "Production Monitoring")
- **Templates**: Use `technical.md` template for infrastructure work

---

## 🔴 Priority 1: Critical (Must Have for Distribution)

### Issue #1: Configure Release Signing

**Title**: `[Priority 1] Configure Release Build Signing`

**Labels**: `priority-1`, `type:technical`, `area:distribution`, `effort:small`, `status:not-started`

**Milestone**: `Distribution Readiness`

**Description**:
```markdown
## The Problem

Release builds cannot be signed for distribution. The app currently has no keystore configuration, which means:
- Release AABs are unsigned (cannot be uploaded to Play Store)
- No secure keystore management strategy
- CI/CD builds unsigned artifacts

## The Solution

Configure release signing in `build.gradle.kts` using environment variables for security:
- Add `signingConfigs` block with release configuration
- Use environment variables for keystore path, password, alias
- Update CI/CD to provide signing credentials via GitHub Secrets
- Create keystore generation script/documentation

## Technical Details

**Files**:
- `app/build.gradle.kts` (update - add signingConfigs)
- `scripts/generate-keystore.sh` (new - keystore generation helper)
- `docs/development/setup/RELEASE_SIGNING_SETUP.md` (new - documentation)

**Dependencies**: None (uses existing Gradle signing support)

**Implementation Notes**:
- Keystore should be stored securely (GitHub Secrets, not in repo)
- Use separate keystores for debug and release
- Document keystore backup strategy
- Update `.gitignore` to ensure keystores are never committed

**Estimated Effort**: 1-2 days

## Acceptance Criteria

- [ ] `signingConfigs` block added to `build.gradle.kts`
- [ ] Release build type uses signing config
- [ ] Keystore generation script created
- [ ] Documentation created for keystore setup
- [ ] CI/CD updated to use signing (GitHub Secrets)
- [ ] Test release build signs successfully
- [ ] Keystore excluded from git (verified in `.gitignore`)
```

---

### Issue #2: Enable Release Build Optimization

**Title**: `[Priority 1] Enable Code Minification and Resource Shrinking`

**Labels**: `priority-1`, `type:technical`, `area:distribution`, `effort:small`, `status:not-started`

**Milestone**: `Distribution Readiness`

**Description**:
```markdown
## The Problem

Release builds have optimization disabled:
- `isMinifyEnabled = false` - Code not minified
- No resource shrinking - Unused resources included
- Larger APK/AAB size than necessary
- No code obfuscation for security

## The Solution

Enable release build optimization:
- Set `isMinifyEnabled = true` in release build type
- Enable `isShrinkResources = true` for resource optimization
- Expand ProGuard rules as needed for dependencies
- Test thoroughly (minification can break reflection)

## Technical Details

**Files**:
- `app/build.gradle.kts` (update - enable minification)
- `app/proguard-rules.pro` (update - expand rules as needed)

**Dependencies**: None (uses existing ProGuard)

**Implementation Notes**:
- Test all features after enabling minification
- Add ProGuard rules for any reflection-based code
- Test with release build (not just debug)
- Monitor app size reduction

**Estimated Effort**: 1-2 days (including testing)

## Acceptance Criteria

- [ ] `isMinifyEnabled = true` in release build type
- [ ] `isShrinkResources = true` enabled
- [ ] ProGuard rules expanded for dependencies
- [ ] All features tested with minified release build
- [ ] App size reduced (measure before/after)
- [ ] No runtime crashes from minification
```

---

### Issue #3: Set Up Fastlane for Play Store Distribution

**Title**: `[Priority 1] Set Up Fastlane for Android Play Store Automation`

**Labels**: `priority-1`, `type:infrastructure`, `area:distribution`, `effort:medium`, `status:not-started`

**Milestone**: `Distribution Readiness`

**Description**:
```markdown
## The Problem

No automation for Play Store distribution:
- Manual uploads to Play Console
- No automated release notes
- No screenshot management
- No version management integration

## The Solution

Set up Fastlane for Android:
- Create Fastlane configuration (`fastlane/Appfile`, `fastlane/Fastfile`)
- Configure Play Store API credentials
- Create lanes for: build, upload, promote (internal → alpha → beta → production)
- Integrate with CI/CD for automated releases

## Technical Details

**Files**:
- `fastlane/Appfile` (new - app identifier, package name)
- `fastlane/Fastfile` (new - build and deployment lanes)
- `fastlane/metadata/` (new - store listing metadata structure)
- `.github/workflows/release.yml` (new - automated release workflow)

**Dependencies**: 
- Fastlane gem
- Google Play API credentials

**Implementation Notes**:
- Use service account for Play Store API
- Store credentials in GitHub Secrets
- Create metadata structure for store listing
- Set up internal testing track first

**Estimated Effort**: 3-5 days

## Acceptance Criteria

- [ ] Fastlane installed and configured
- [ ] `Appfile` created with app identifier
- [ ] `Fastfile` created with build/upload lanes
- [ ] Metadata structure created
- [ ] Play Store API credentials configured
- [ ] Test upload to internal testing track
- [ ] CI/CD workflow for automated releases
- [ ] Documentation for Fastlane usage
```

---

### Issue #4: Integrate Crash Reporting (Firebase Crashlytics)

**Title**: `[Priority 1] Integrate Firebase Crashlytics for Production Crash Reporting`

**Labels**: `priority-1`, `type:technical`, `area:monitoring`, `effort:medium`, `status:not-started`

**Milestone**: `Production Monitoring`

**Description**:
```markdown
## The Problem

No crash reporting in production:
- Basic exception handler only logs (no centralized reporting)
- Cannot track crashes in production
- No crash analytics or trends
- Comment in code: `// Log to crash reporting service in production` (not implemented)

## The Solution

Integrate Firebase Crashlytics:
- Add Firebase SDK to project
- Configure Firebase project
- Replace basic exception handler with Crashlytics reporting
- Add custom keys for context (user ID, environment, etc.)
- Set up crash alerts

## Technical Details

**Files**:
- `app/build.gradle.kts` (update - add Firebase dependencies)
- `app/google-services.json` (new - Firebase config, in `.gitignore`)
- `app/src/main/java/com/electricsheep/app/ElectricSheepApplication.kt` (update - integrate Crashlytics)
- `docs/development/setup/FIREBASE_CRASHLYTICS_SETUP.md` (new - setup guide)

**Dependencies**: 
- `com.google.firebase:firebase-crashlytics-ktx:18.6.1`
- `com.google.firebase:firebase-analytics-ktx:21.5.0`
- Firebase project setup

**Implementation Notes**:
- Set up Firebase project first
- Add `google-services.json` to `.gitignore`
- Configure Crashlytics in `ElectricSheepApplication.onCreate()`
- Add custom keys for debugging context
- Test crash reporting in debug build

**Estimated Effort**: 2-3 days

## Acceptance Criteria

- [ ] Firebase project created
- [ ] Firebase SDK added to dependencies
- [ ] `google-services.json` configured (not committed)
- [ ] Exception handler updated to use Crashlytics
- [ ] Custom keys added for context
- [ ] Test crash reported successfully
- [ ] Crash alerts configured in Firebase Console
- [ ] Documentation created
```

---

### Issue #5: Create Privacy Policy and Terms of Service

**Title**: `[Priority 1] Create Privacy Policy and Terms of Service`

**Labels**: `priority-1`, `type:feature`, `area:legal`, `effort:medium`, `status:not-started`

**Milestone**: `Legal Compliance`

**Description**:
```markdown
## The Problem

No privacy policy or terms of service:
- Required for Play Store submission
- No GDPR compliance features
- No user data disclosure
- No way for users to access privacy policy

## The Solution

Create legal documents and integrate into app:
- Write privacy policy (data collection, usage, storage)
- Write terms of service
- Host privacy policy (GitHub Pages or dedicated URL)
- Add privacy policy link in app (settings/about screen)
- Implement GDPR features (data export, deletion)

## Technical Details

**Files**:
- `docs/legal/PRIVACY_POLICY.md` (new - privacy policy document)
- `docs/legal/TERMS_OF_SERVICE.md` (new - terms of service)
- `app/src/main/java/com/electricsheep/app/ui/screens/AboutScreen.kt` (new - about screen with privacy link)
- `app/src/main/java/com/electricsheep/app/data/export/DataExportService.kt` (new - GDPR data export)
- `app/src/main/java/com/electricsheep/app/data/deletion/DataDeletionService.kt` (new - GDPR data deletion)

**Dependencies**: None

**Implementation Notes**:
- Privacy policy should cover: data collection, storage, usage, third-party services (Supabase)
- Terms of service should cover: usage, liability, user responsibilities
- Host privacy policy publicly (GitHub Pages recommended)
- Add privacy policy link to app settings/about screen
- Implement data export (JSON format)
- Implement data deletion (user request)

**Estimated Effort**: 3-5 days (including legal review if needed)

## Acceptance Criteria

- [ ] Privacy policy document created
- [ ] Terms of service document created
- [ ] Privacy policy hosted publicly (URL)
- [ ] Privacy policy link added to app
- [ ] Data export feature implemented
- [ ] Data deletion feature implemented
- [ ] GDPR compliance verified
- [ ] Legal documents reviewed (if applicable)
```

---

## 🟡 Priority 2: Important (Should Have Soon)

### Issue #6: Automate Version Management

**Title**: `[Priority 2] Automate Version Code and Version Name Management`

**Labels**: `priority-2`, `type:infrastructure`, `area:distribution`, `effort:small`, `status:not-started`

**Milestone**: `Distribution Readiness`

**Description**:
```markdown
## The Problem

Version management is manual:
- `versionCode = 1` and `versionName = "1.0"` hardcoded
- Manual version bumps required
- No git tagging for releases
- No automated changelog generation

## The Solution

Automate version management:
- Create version bump script
- Integrate version bump into CI/CD
- Set up git tagging for releases
- Generate changelog from git commits
- Update Fastlane to use version from git

## Technical Details

**Files**:
- `scripts/bump-version.sh` (new - version bump script)
- `app/build.gradle.kts` (update - read version from git or file)
- `.github/workflows/release.yml` (update - auto-increment version)
- `scripts/generate-changelog.sh` (new - changelog generation)

**Dependencies**: None

**Implementation Notes**:
- Use semantic versioning (MAJOR.MINOR.PATCH)
- Auto-increment versionCode on each release
- Tag releases in git
- Generate changelog from commit messages

**Estimated Effort**: 2-3 days

## Acceptance Criteria

- [ ] Version bump script created
- [ ] Version read from git or version file
- [ ] CI/CD auto-increments versionCode
- [ ] Git tagging for releases
- [ ] Changelog generation script
- [ ] Fastlane uses version from git
- [ ] Documentation for version management
```

---

### Issue #7: Implement Google Play In-App Updates

**Title**: `[Priority 2] Implement Google Play In-App Updates`

**Labels**: `priority-2`, `type:feature`, `area:distribution`, `effort:medium`, `status:not-started`

**Milestone**: `Distribution Readiness`

**Description**:
```markdown
## The Problem

No in-app update mechanism:
- Users must manually update from Play Store
- No way to notify users of updates
- No force update for critical fixes

## The Solution

Integrate Google Play In-App Updates:
- Add Play Core library
- Check for updates on app launch
- Implement flexible updates (background)
- Implement immediate updates (for critical fixes)
- Add update notification UI

## Technical Details

**Files**:
- `app/build.gradle.kts` (update - add Play Core dependencies)
- `app/src/main/java/com/electricsheep/app/update/UpdateManager.kt` (new - update management)
- `app/src/main/java/com/electricsheep/app/ui/screens/UpdateScreen.kt` (new - update UI)

**Dependencies**: 
- `com.google.android.play:app-update:2.1.0`
- `com.google.android.play:app-update-ktx:2.1.0`

**Implementation Notes**:
- Check for updates in `ElectricSheepApplication.onCreate()`
- Use flexible updates for non-critical updates
- Use immediate updates for critical security fixes
- Add update UI with progress indicator

**Estimated Effort**: 2-3 days

## Acceptance Criteria

- [ ] Play Core library added
- [ ] Update check on app launch
- [ ] Flexible update flow implemented
- [ ] Immediate update flow implemented
- [ ] Update UI with progress indicator
- [ ] Test with staged rollout
- [ ] Documentation for update mechanism
```

---

### Issue #8: Create Play Store Assets (Icons, Screenshots, Feature Graphic)

**Title**: `[Priority 2] Create Play Store Assets (Icons, Screenshots, Feature Graphic)`

**Labels**: `priority-2`, `type:feature`, `area:distribution`, `effort:medium`, `status:not-started`

**Milestone**: `Distribution Readiness`

**Description**:
```markdown
## The Problem

Missing Play Store assets:
- No Play Store app icon (512x512)
- No feature graphic (1024x500)
- No screenshots for different device sizes
- No promotional images

## The Solution

Create all required Play Store assets:
- Generate Play Store app icon (512x512)
- Create feature graphic (1024x500)
- Take screenshots for different device sizes (phone, tablet)
- Create promotional images (optional)
- Organize assets in Fastlane metadata structure

## Technical Details

**Files**:
- `fastlane/metadata/en-US/images/icon.png` (new - 512x512)
- `fastlane/metadata/en-US/images/featureGraphic.png` (new - 1024x500)
- `fastlane/metadata/en-US/images/phoneScreenshots/` (new - phone screenshots)
- `fastlane/metadata/en-US/images/tabletScreenshots/` (new - tablet screenshots)
- `scripts/generate-screenshots.sh` (new - automated screenshot generation, optional)

**Dependencies**: None (design tools, screenshot tools)

**Implementation Notes**:
- Use existing app icon as base for Play Store icon
- Feature graphic should be visually appealing and brand-consistent
- Screenshots should showcase key features
- Consider automated screenshot generation for consistency

**Estimated Effort**: 2-3 days (design + implementation)

## Acceptance Criteria

- [ ] Play Store app icon created (512x512)
- [ ] Feature graphic created (1024x500)
- [ ] Phone screenshots taken (multiple sizes)
- [ ] Tablet screenshots taken (if applicable)
- [ ] Assets organized in Fastlane metadata structure
- [ ] Assets uploaded to Play Console
- [ ] Documentation for asset requirements
```

---

## 🟢 Priority 3: Nice to Have (Can Add Later)

### Issue #9: Implement SSL Certificate Pinning

**Title**: `[Priority 3] Implement SSL Certificate Pinning for Supabase API`

**Labels**: `priority-3`, `type:technical`, `area:security`, `effort:small`, `status:not-started`

**Milestone**: `Security Hardening`

**Description**:
```markdown
## The Problem

No SSL certificate pinning:
- API calls vulnerable to man-in-the-middle attacks
- No protection against compromised certificates

## The Solution

Implement certificate pinning for Supabase API:
- Add certificate pinning library
- Configure pinning for Supabase domain
- Test with network security config
- Handle pinning failures gracefully

## Technical Details

**Files**:
- `app/build.gradle.kts` (update - add certificate pinning library)
- `app/src/main/res/xml/network_security_config.xml` (new - network security config)
- `app/src/main/AndroidManifest.xml` (update - reference network security config)

**Dependencies**: 
- Certificate pinning library (e.g., `com.squareup.okhttp3:okhttp:4.x` with pinning)

**Implementation Notes**:
- Pin Supabase certificate
- Handle certificate rotation (update pins when Supabase rotates certs)
- Test with network security config
- Monitor for pinning failures

**Estimated Effort**: 1-2 days

## Acceptance Criteria

- [ ] Certificate pinning library added
- [ ] Network security config created
- [ ] Supabase certificate pinned
- [ ] Pinning tested successfully
- [ ] Pinning failure handling implemented
- [ ] Documentation for certificate rotation
```

---

### Issue #10: Add Root Detection

**Title**: `[Priority 3] Add Root/Jailbreak Detection`

**Labels**: `priority-3`, `type:technical`, `area:security`, `effort:small`, `status:not-started`

**Milestone**: `Security Hardening`

**Description**:
```markdown
## The Problem

No root detection:
- App vulnerable on rooted devices
- No protection against tampering

## The Solution

Add root detection:
- Integrate root detection library
- Check for root on app launch
- Warn user or block app (configurable)
- Log root detection events

## Technical Details

**Files**:
- `app/build.gradle.kts` (update - add root detection library)
- `app/src/main/java/com/electricsheep/app/security/RootDetector.kt` (new - root detection)

**Dependencies**: 
- Root detection library (e.g., `com.scottyab:rootbeer-lib:0.1.0`)

**Implementation Notes**:
- Check for root on app launch
- Configurable behavior (warn vs block)
- Log root detection for analytics
- Test on rooted device (if available)

**Estimated Effort**: 1 day

## Acceptance Criteria

- [ ] Root detection library added
- [ ] Root check on app launch
- [ ] Warning or blocking UI (configurable)
- [ ] Root detection logged
- [ ] Tested on rooted device (if available)
- [ ] Documentation for root detection behavior
```

---

### Issue #11: Integrate Firebase Performance Monitoring

**Title**: `[Priority 3] Integrate Firebase Performance Monitoring`

**Labels**: `priority-3`, `type:technical`, `area:monitoring`, `effort:medium`, `status:not-started`

**Milestone**: `Production Monitoring`

**Description**:
```markdown
## The Problem

No performance monitoring:
- No app performance metrics
- No startup time tracking
- No memory leak detection
- No API call performance tracking

## The Solution

Integrate Firebase Performance Monitoring:
- Add Firebase Performance SDK
- Track app startup time
- Monitor memory usage
- Track API call performance
- Set up performance alerts

## Technical Details

**Files**:
- `app/build.gradle.kts` (update - add Performance Monitoring)
- `app/src/main/java/com/electricsheep/app/performance/PerformanceTracker.kt` (new - performance tracking)

**Dependencies**: 
- `com.google.firebase:firebase-perf-ktx:20.5.1`

**Implementation Notes**:
- Track startup time in `ElectricSheepApplication.onCreate()`
- Add custom traces for key operations
- Monitor API call performance
- Set up alerts for performance degradation

**Estimated Effort**: 2-3 days

## Acceptance Criteria

- [ ] Firebase Performance SDK added
- [ ] Startup time tracked
- [ ] Custom traces for key operations
- [ ] API call performance tracked
- [ ] Performance alerts configured
- [ ] Performance dashboard reviewed
- [ ] Documentation for performance monitoring
```

---

### Issue #12: Set Up Firebase App Distribution for Beta Testing

**Title**: `[Priority 3] Set Up Firebase App Distribution for Beta Testing`

**Labels**: `priority-3`, `type:infrastructure`, `area:distribution`, `effort:medium`, `status:not-started`

**Milestone**: `Beta Testing Infrastructure`

**Description**:
```markdown
## The Problem

No beta testing distribution:
- No way to distribute beta builds to testers
- No beta tester management
- No structured feedback collection

## The Solution

Set up Firebase App Distribution:
- Configure Firebase App Distribution
- Create beta testing groups
- Automate beta build distribution
- Set up feedback collection mechanism

## Technical Details

**Files**:
- `app/build.gradle.kts` (update - add App Distribution plugin)
- `.github/workflows/beta-distribution.yml` (new - automated beta distribution)
- `docs/development/testing/BETA_TESTING.md` (new - beta testing guide)

**Dependencies**: 
- Firebase App Distribution plugin
- Firebase project setup

**Implementation Notes**:
- Set up Firebase App Distribution
- Create tester groups (internal, external)
- Automate distribution via CI/CD
- Collect feedback via Firebase or external tool

**Estimated Effort**: 2-3 days

## Acceptance Criteria

- [ ] Firebase App Distribution configured
- [ ] Beta testing groups created
- [ ] Automated distribution workflow
- [ ] Feedback collection mechanism
- [ ] Beta tester onboarding process
- [ ] Documentation for beta testing
```

---

## Summary

### Total Issues Suggested: 12

**By Priority**:
- Priority 1 (Critical): 5 issues
- Priority 2 (Important): 3 issues
- Priority 3 (Nice to Have): 4 issues

**By Type**:
- Technical: 7 issues
- Infrastructure: 3 issues
- Feature: 2 issues

**By Effort**:
- Small (1-2 days): 5 issues
- Medium (2-5 days): 7 issues
- Large (5+ days): 0 issues

**By Milestone**:
- Distribution Readiness: 6 issues
- Production Monitoring: 2 issues
- Legal Compliance: 1 issue
- Security Hardening: 2 issues
- Beta Testing Infrastructure: 1 issue

---

## Recommended Implementation Order

### Phase 1: Distribution Readiness (Week 1-2)
1. Configure Release Signing (#1)
2. Enable Release Build Optimization (#2)
3. Set Up Fastlane (#3)
4. Automate Version Management (#6)

### Phase 2: Production Monitoring (Week 3)
5. Integrate Crash Reporting (#4)
6. Integrate Performance Monitoring (#11) - Optional

### Phase 3: Legal & Compliance (Week 4)
7. Create Privacy Policy and Terms (#5)

### Phase 4: User Experience (Week 5)
8. Implement In-App Updates (#7)
9. Create Play Store Assets (#8)

### Phase 5: Security & Testing (Ongoing)
10. Implement SSL Certificate Pinning (#9)
11. Add Root Detection (#10)
12. Set Up Beta Testing (#12)

---

## Next Steps

1. **Review suggested issues** - Adjust priorities, effort estimates, or scope
2. **Create issues in GitHub** - Use suggested titles, labels, and descriptions
3. **Set up milestones** - Create milestones for grouping related issues
4. **Prioritize** - Decide which issues to tackle first
5. **Start with Phase 1** - Begin with distribution readiness issues

---

## Related Documentation

- `docs/development/analysis/ANDROID_APP_GAP_ANALYSIS.md` - Complete gap analysis
- `docs/development/BACKLOG_GITHUB_ISSUES_APPROACH.md` - Ticketing system approach
- `docs/development/GITHUB_ISSUES_SETUP.md` - GitHub Issues setup guide

