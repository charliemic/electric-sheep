# App Specificity Analysis

**Date**: 2025-01-20  
**Purpose**: Identify which parts of the Electric Sheep codebase are specific to this app vs. generalizable, and which are team-oriented vs. individual-oriented.

## Overview

This analysis categorizes components along two axes:

1. **Team vs Individual**: Whether components are designed for team collaboration or individual use
2. **App-Specific vs Generalizable**: Whether components are specific to Electric Sheep or could be reused in other projects

## Analysis Framework

### Team vs Individual

**Team-oriented**: Components designed for collaboration, shared workflows, multi-agent coordination
- Cursor rules, workflow scripts, coordination docs
- Shared conventions, standards, best practices
- Multi-agent coordination tools

**Individual-oriented**: Components for single-developer use
- Personal development tools
- Individual learning/experimentation
- Single-agent workflows

### App-Specific vs Generalizable

**App-Specific**: Components tied to Electric Sheep's domain
- Mood tracking business logic
- App-specific UI components
- Electric Sheep branding/theming

**Generalizable**: Components that could be reused in other projects
- Generic utilities, frameworks, patterns
- Reusable infrastructure
- Platform-agnostic tools

---

## Categorization Matrix

### 1. Cursor Rules (`.cursor/rules/`)

**Team vs Individual**: ✅ **TEAM**
- Designed for multi-agent coordination
- Enforce shared standards and workflows
- Prevent conflicts between agents
- Examples: `branching.mdc`, `agent-coordination.mdc`, `smart-prompts.mdc`

**App-Specific vs Generalizable**: ✅ **GENERALIZABLE**
- Most rules are project-agnostic
- Can be adapted to any software project
- Examples:
  - `branching.mdc` - Git workflow (any project)
  - `code-quality.mdc` - Code standards (any project)
  - `testing.mdc` - Testing principles (any project)
  - `error-handling.mdc` - Error handling patterns (any project)
  - `accessibility.mdc` - Accessibility standards (any mobile app)
  - `security.mdc` - Security principles (any project)
  - `documentation-first.mdc` - Documentation approach (any project)
  - `working-patterns-first.mdc` - Development approach (any project)

**App-Specific Exceptions**:
- Some rules reference "Electric Sheep" specifically (but could be parameterized)
- Examples: `emulator-workflow.mdc` (Android-specific, but generalizable to any Android project)

**Recommendation**: Extract to a reusable Cursor rules template/library

---

### 2. Workflow Scripts (`scripts/`)

#### Team-Oriented Workflow Scripts

**Team vs Individual**: ✅ **TEAM**
- Designed for multi-agent coordination
- Enforce shared workflows
- Prevent conflicts

**App-Specific vs Generalizable**: ✅ **GENERALIZABLE**

**Examples**:
- `pre-work-check.sh` - Pre-work validation (any project)
- `create-worktree.sh` - Git worktree management (any project)
- `check-agent-coordination.sh` - Multi-agent coordination (any project)
- `post-merge-cleanup.sh` - Post-merge cleanup (any project)
- `session-end-check.sh` - Session validation (any project)
- `discover-rules.sh` - Rule discovery (any Cursor project)
- `wip-commit.sh` - WIP commit helper (any project)

**Recommendation**: Extract to a reusable workflow scripts library

#### App-Specific Development Scripts

**Team vs Individual**: ✅ **TEAM** (shared development tools)

**App-Specific vs Generalizable**: ⚠️ **MIXED**

**App-Specific**:
- `dev-reload.sh` - Electric Sheep app reload (app-specific)
- `load-test-data.sh` - Electric Sheep test data (app-specific)
- `test-signup-helpers.sh` - Electric Sheep signup tests (app-specific)
- `verify-supabase-data.sh` - Electric Sheep Supabase verification (app-specific)

**Generalizable** (with minor customization):
- `emulator-manager.sh` - Android emulator management (any Android project)
- `emulator-discovery.sh` - Emulator discovery service (any Android project)
- `emulator-lock-manager.sh` - Emulator locking (any multi-agent Android project)
- `get-device-id.sh` - Device ID helper (any Android project)
- `setup-test-emulator.sh` - Test emulator setup (any Android project)

**Recommendation**: 
- Extract generalizable scripts to a reusable library
- Keep app-specific scripts in project

#### Supabase/Backend Scripts

**Team vs Individual**: ✅ **TEAM** (shared backend tools)

**App-Specific vs Generalizable**: ⚠️ **MIXED**

**Generalizable** (Supabase-specific, but reusable):
- `scripts/lib/supabase-postgrest.sh` - PostgREST API operations (any Supabase project)
- `scripts/lib/supabase-auth-admin.sh` - Auth admin operations (any Supabase project)
- `scripts/lib/supabase-db-admin.sh` - DB admin operations (any Supabase project)
- `scripts/lib/supabase-migration-admin.sh` - Migration admin (any Supabase project)
- `supabase-setup.sh` - Supabase setup (any Supabase project)
- `supabase-link-cloud.sh` - Supabase cloud linking (any Supabase project)
- `sync-feature-flags.sh` - Feature flag sync (any Supabase project with feature flags)

**App-Specific**:
- `verify-supabase-data.sh` - Electric Sheep data verification (app-specific)
- `load-test-data.sh` - Electric Sheep test data (app-specific)

**Recommendation**: Extract Supabase library scripts to a reusable Supabase tools library

#### Metrics and Analysis Scripts

**Team vs Individual**: ✅ **TEAM** (shared metrics)

**App-Specific vs Generalizable**: ✅ **GENERALIZABLE**

**Examples**:
- `scripts/metrics/capture-prompt.sh` - Prompt tracking (any AI-assisted project)
- `scripts/metrics/capture-agent-usage.sh` - Agent usage tracking (any AI-assisted project)
- `scripts/metrics/capture-build-metrics.sh` - Build metrics (any project)
- `scripts/metrics/capture-test-metrics.sh` - Test metrics (any project)
- `scripts/metrics/capture-complexity-metrics.sh` - Complexity metrics (any project)
- `scripts/collect-metrics.sh` - Metrics collection (any project)
- `scripts/analysis/` - Analysis scripts (any project)

**Recommendation**: Extract to a reusable metrics library

#### Documentation and Content Scripts

**Team vs Individual**: ✅ **TEAM** (shared documentation tools)

**App-Specific vs Generalizable**: ✅ **GENERALIZABLE**

**Examples**:
- `md-to-html.sh` - Markdown to HTML (any project)
- `md-to-google-doc.sh` - Markdown to Google Docs (any project)
- `md-to-both.sh` - Markdown to both (any project)
- `google-docs-workflow.py` - Google Docs workflow (any project)
- `generate-pdf-from-ai-report.sh` - PDF generation (any project)

**Recommendation**: Extract to a reusable documentation tools library

---

### 3. App Code (`app/src/main/`)

**Team vs Individual**: ⚠️ **MIXED**
- Code is team-accessible (version control)
- But primarily individual development workflow
- Team collaboration happens via PRs/reviews

**App-Specific vs Generalizable**: ⚠️ **MIXED**

#### App-Specific Business Logic

**App-Specific**: ✅ **APP-SPECIFIC**

**Examples**:
- `data/model/Mood.kt` - Mood tracking domain model
- `data/repository/MoodRepository.kt` - Mood repository
- `ui/screens/mood/MoodManagementScreen.kt` - Mood management UI
- `ui/screens/mood/MoodManagementViewModel.kt` - Mood management logic
- `ui/components/MoodChart.kt` - Mood visualization
- `config/MoodConfig.kt` - Mood configuration
- `data/model/Question.kt`, `QuizAnswer.kt`, `QuizSession.kt` - Trivia domain models
- `ui/screens/trivia/TriviaScreen.kt` - Trivia UI

**Recommendation**: Keep in app (domain-specific)

#### Generalizable Infrastructure

**Generalizable**: ✅ **GENERALIZABLE**

**Examples**:
- `util/Logger.kt` - Centralized logging (any Android project)
- `util/DateFormatter.kt` - Date formatting (any project)
- `error/AppError.kt` - Error handling (any project)
- `data/local/AppDatabase.kt` - Room database setup (any Android project)
- `data/migration/DatabaseMigration.kt` - Migration utilities (any Room project)
- `data/local/RoomErrorHandler.kt` - Room error handling (any Room project)

**Recommendation**: Extract to a reusable Android utilities library

#### Generalizable UI Components

**Generalizable**: ✅ **GENERALIZABLE**

**Examples**:
- `ui/components/AccessibleButton.kt` - Accessible button (any Compose project)
- `ui/components/AccessibleCard.kt` - Accessible card (any Compose project)
- `ui/components/AccessibleTextField.kt` - Accessible text field (any Compose project)
- `ui/components/AccessibleErrorMessage.kt` - Accessible error message (any Compose project)
- `ui/components/AccessibleScreen.kt` - Accessible screen wrapper (any Compose project)
- `ui/components/LoadingIndicator.kt` - Loading indicator (any Compose project)
- `ui/components/FocusManagement.kt` - Focus management (any Compose project)

**Recommendation**: Extract to a reusable Compose accessibility library

#### Generalizable Architecture Components

**Generalizable**: ✅ **GENERALIZABLE**

**Examples**:
- `auth/AuthProvider.kt` - Auth provider interface (any app with auth)
- `auth/UserManager.kt` - User state management (any app with auth)
- `auth/AuthModule.kt` - Auth dependency injection (any app with auth)
- `config/FeatureFlagManager.kt` - Feature flag management (any app with feature flags)
- `config/FeatureFlagProvider.kt` - Feature flag provider interface (any app)
- `data/sync/SyncManager.kt` - Sync management (any offline-first app)
- `data/sync/MoodSyncWorker.kt` - Background sync worker (any offline-first app)

**Recommendation**: Extract to reusable architecture libraries:
- Auth library (provider abstraction)
- Feature flags library
- Sync library (offline-first pattern)

#### App-Specific Theming

**App-Specific**: ✅ **APP-SPECIFIC**

**Examples**:
- `ui/theme/Color.kt` - Electric Sheep color scheme
- `ui/theme/Theme.kt` - Electric Sheep theme
- `ui/theme/Type.kt` - Electric Sheep typography
- `ui/theme/Spacing.kt` - Electric Sheep spacing (could be generalizable)

**Recommendation**: Keep in app (branding-specific)

---

### 4. Test Automation (`test-automation/`)

**Team vs Individual**: ✅ **TEAM** (shared test infrastructure)

**App-Specific vs Generalizable**: ⚠️ **MIXED**

**Generalizable Framework**:
- Hybrid AI + Appium architecture (any mobile app)
- Human-like action abstraction (any mobile app)
- Accessibility-first testing (any mobile app)
- Kotlin + Appium framework (any Android app)

**App-Specific Tests**:
- Electric Sheep-specific test scenarios
- App-specific personas
- App-specific test data

**Recommendation**: 
- Extract framework to a reusable test automation library
- Keep app-specific tests in project

---

### 5. Documentation (`docs/`)

**Team vs Individual**: ✅ **TEAM** (shared knowledge)

**App-Specific vs Generalizable**: ⚠️ **MIXED**

#### Generalizable Documentation

**Examples**:
- `docs/architecture/ERROR_HANDLING.md` - Error handling patterns (any project)
- `docs/architecture/AUTHENTICATION.md` - Auth architecture (any app with auth)
- `docs/architecture/DATA_LAYER_ARCHITECTURE.md` - Data layer patterns (any offline-first app)
- `docs/architecture/COMPREHENSIVE_ACCESSIBILITY_GUIDE.md` - Accessibility guide (any mobile app)
- `docs/architecture/UX_PRINCIPLES.md` - UX principles (any app)
- `docs/testing/` - Testing guides (any project)
- `docs/development/workflow/` - Development workflow (any project)
- `docs/development/setup/` - Setup guides (platform-specific, but generalizable)

**Recommendation**: Extract to a reusable documentation template

#### App-Specific Documentation

**Examples**:
- `docs/development/TODO.md` - Electric Sheep backlog
- `docs/development/analysis/HOLISTIC_CODEBASE_ANALYSIS.md` - Electric Sheep analysis
- `docs/architecture/` - Some Electric Sheep-specific architecture decisions
- App-specific feature documentation

**Recommendation**: Keep in app (project-specific)

---

### 6. CI/CD (`.github/workflows/`)

**Team vs Individual**: ✅ **TEAM** (shared CI/CD)

**App-Specific vs Generalizable**: ⚠️ **MIXED**

**Generalizable**:
- Build and test workflow patterns (any Android project)
- Lint checks (any Android project)
- Accessibility checks (any Android project)
- Build APK/AAB (any Android project)

**App-Specific**:
- Electric Sheep-specific test scenarios
- App-specific deployment targets
- App-specific environment variables

**Recommendation**: Extract workflow templates to reusable GitHub Actions templates

---

### 7. Supabase Configuration (`supabase/`)

**Team vs Individual**: ✅ **TEAM** (shared backend)

**App-Specific vs Generalizable**: ✅ **APP-SPECIFIC**

**Examples**:
- `supabase/migrations/` - Electric Sheep database schema
- `supabase/seed/` - Electric Sheep seed data
- `supabase/config.toml` - Electric Sheep Supabase config

**Recommendation**: Keep in app (domain-specific)

---

### 8. Feature Flags (`feature-flags/`)

**Team vs Individual**: ✅ **TEAM** (shared feature management)

**App-Specific vs Generalizable**: ✅ **APP-SPECIFIC**

**Examples**:
- `feature-flags/flags.yaml` - Electric Sheep feature flags

**Recommendation**: Keep in app (domain-specific)

---

## Summary Matrix

| Component | Team vs Individual | App-Specific vs Generalizable | Recommendation |
|-----------|-------------------|------------------------------|----------------|
| **Cursor Rules** | ✅ TEAM | ✅ GENERALIZABLE | Extract to reusable template |
| **Workflow Scripts** | ✅ TEAM | ✅ GENERALIZABLE | Extract to reusable library |
| **App Dev Scripts** | ✅ TEAM | ⚠️ MIXED | Extract generalizable, keep app-specific |
| **Supabase Scripts** | ✅ TEAM | ✅ GENERALIZABLE | Extract to Supabase tools library |
| **Metrics Scripts** | ✅ TEAM | ✅ GENERALIZABLE | Extract to metrics library |
| **App Business Logic** | ⚠️ MIXED | ✅ APP-SPECIFIC | Keep in app |
| **App Infrastructure** | ⚠️ MIXED | ✅ GENERALIZABLE | Extract to utilities library |
| **UI Components** | ⚠️ MIXED | ✅ GENERALIZABLE | Extract to Compose library |
| **Architecture** | ⚠️ MIXED | ✅ GENERALIZABLE | Extract to architecture libraries |
| **Theming** | ⚠️ MIXED | ✅ APP-SPECIFIC | Keep in app |
| **Test Automation** | ✅ TEAM | ⚠️ MIXED | Extract framework, keep tests |
| **Documentation** | ✅ TEAM | ⚠️ MIXED | Extract templates, keep app-specific |
| **CI/CD** | ✅ TEAM | ⚠️ MIXED | Extract templates, keep app-specific |
| **Supabase Config** | ✅ TEAM | ✅ APP-SPECIFIC | Keep in app |
| **Feature Flags** | ✅ TEAM | ✅ APP-SPECIFIC | Keep in app |

---

## Extraction Opportunities

### High-Value Generalizable Components

1. **Cursor Rules Template** (`.cursor/rules/`)
   - Complete set of development rules
   - Multi-agent coordination patterns
   - Smart prompts architecture
   - **Value**: Reusable across all Cursor projects

2. **Workflow Scripts Library** (`scripts/`)
   - Pre-work checks
   - Worktree management
   - Agent coordination
   - Post-merge cleanup
   - **Value**: Reusable across all Git projects

3. **Android Utilities Library** (`app/src/main/java/com/electricsheep/app/util/`)
   - Logger
   - DateFormatter
   - Error handling
   - **Value**: Reusable across all Android projects

4. **Compose Accessibility Library** (`app/src/main/java/com/electricsheep/app/ui/components/`)
   - AccessibleButton, AccessibleCard, AccessibleTextField
   - AccessibleScreen, AccessibleErrorMessage
   - FocusManagement
   - **Value**: Reusable across all Compose projects

5. **Architecture Libraries**
   - Auth library (provider abstraction)
   - Feature flags library
   - Sync library (offline-first pattern)
   - **Value**: Reusable across similar apps

6. **Supabase Tools Library** (`scripts/lib/supabase-*.sh`)
   - PostgREST operations
   - Auth admin operations
   - DB admin operations
   - Migration admin
   - **Value**: Reusable across all Supabase projects

7. **Metrics Library** (`scripts/metrics/`)
   - Prompt tracking
   - Agent usage tracking
   - Build/test/complexity metrics
   - **Value**: Reusable across all AI-assisted projects

8. **Test Automation Framework** (`test-automation/`)
   - Hybrid AI + Appium architecture
   - Human-like action abstraction
   - Accessibility-first testing
   - **Value**: Reusable across all mobile apps

9. **Documentation Templates** (`docs/`)
   - Architecture patterns
   - Development workflow guides
   - Testing guides
   - Setup guides
   - **Value**: Reusable across all projects

10. **CI/CD Templates** (`.github/workflows/`)
    - Android build/test workflows
    - Accessibility checks
    - **Value**: Reusable across all Android projects

---

## Recommendations

### Immediate Actions

1. **Document Generalizable Components**
   - Create this analysis document (✅ Done)
   - Identify extraction candidates
   - Plan extraction strategy

2. **Create Reusable Libraries**
   - Start with highest-value components (Cursor rules, workflow scripts)
   - Extract incrementally
   - Maintain backward compatibility

3. **Parameterize App-Specific References**
   - Replace "Electric Sheep" with variables/config
   - Make scripts/configs more generic
   - Enable easy adaptation

### Long-Term Strategy

1. **Build Reusable Component Library**
   - Extract generalizable components
   - Create separate repositories/packages
   - Version and maintain independently

2. **Maintain App-Specific Components**
   - Keep domain-specific code in app
   - Use extracted libraries as dependencies
   - Focus app code on business logic

3. **Share with Community**
   - Open source generalizable components
   - Contribute to ecosystem
   - Learn from community feedback

---

## Conclusion

**Key Findings**:

1. **Most workflow and infrastructure components are generalizable** - Cursor rules, scripts, utilities, architecture patterns
2. **Business logic is app-specific** - Mood tracking, trivia, domain models
3. **Team-oriented components are mostly generalizable** - Workflow scripts, coordination tools, metrics
4. **High extraction value** - Many components could benefit other projects

**Next Steps**:

1. Prioritize extraction candidates
2. Create extraction plan
3. Begin with highest-value components (Cursor rules, workflow scripts)
4. Maintain app while extracting

---

**Related Documents**:
- `docs/development/analysis/HOLISTIC_CODEBASE_ANALYSIS.md` - Complete codebase analysis
- `docs/development/workflow/AGENT_COORDINATION.md` - Multi-agent coordination
- `.cursor/rules/` - Development rules and patterns

