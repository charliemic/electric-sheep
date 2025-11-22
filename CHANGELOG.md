# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [app 1.0.1] - 2025-11-22

### Added
- Uadd initial Android app with Jetpack Compose and CI/CD
- Uadd logging infrastructure and mood management app skeleton
- Uoptimize CI to skip app builds when only non-app files change
- UAdd enable_trivia_app feature flag
- UAdd ENABLE_TRIVIA_APP_MODE BuildConfig field
- UEnable enable_trivia_app flag in staging
- UEnable trivia app in debug builds (#19)
### Changed
- UMerge pull request #1 from charliemic/feat/initial-android-app-setup
- UMerge pull request #7 from charliemic/feature/optimize-ci-skip-app-builds
- UUse HTTP-based approach for SQL execution (matching user setup pattern)
- UMerge pull request #66 from charliemic/feature/app-specificity-analysis
### Fixed
- URestore HTTP API approach for mood data loading
### Documentation
- UAdd debug summary of fixes applied
- UAdd app specificity analysis and template extraction strategy
### Added
- Release management system
- Multi-component version management
- Unified changelog tracking

## [1.0.0] - 2025-01-22

### Added
- Mood tracking functionality
- Supabase authentication (email/password and Google OAuth)
- Feature flags system
- Accessibility support (WCAG AA compliance)
- Test automation framework
- HTML viewer/converter tool
- Metrics dashboard
- Comprehensive documentation
- Security scanning workflows
- Legal documentation (privacy policy, terms templates)
- CI/CD pipelines
- Multi-agent workflow coordination

### Changed
- Initial release

---

**Note**: This changelog tracks changes across all components (Android App, Test Framework, Metrics Dashboard). Component-specific changes are noted where relevant.

**Note**: HTML Viewer functionality has been merged into Metrics Dashboard (content authoring feature).

