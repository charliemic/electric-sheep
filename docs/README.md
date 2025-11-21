# Documentation Index

**Last Updated**: 2025-11-19

This directory contains all project documentation organized by purpose. For quick reference, see the sections below.

## 🚀 Quick Start

- **[README.md](../README.md)** - Project overview and quick start
- **[AI_AGENT_GUIDELINES.md](../AI_AGENT_GUIDELINES.md)** - Complete guidelines for AI agents and developers
- **[Multi-Agent Workflow](development/MULTI_AGENT_WORKFLOW.md)** - How to work with multiple agents

## 📚 Active Documentation

### Development Workflows

#### Multi-Agent Workflow
- **[MULTI_AGENT_WORKFLOW.md](development/MULTI_AGENT_WORKFLOW.md)** - Complete multi-agent workflow guidelines
- **[MULTI_AGENT_QUICK_REFERENCE.md](development/MULTI_AGENT_QUICK_REFERENCE.md)** - Quick reference guide
- **[AGENT_COORDINATION.md](development/AGENT_COORDINATION.md)** - Current work tracking and coordination
- **[GIT_WORKTREE_COMPATIBILITY.md](development/GIT_WORKTREE_COMPATIBILITY.md)** - Git worktree compatibility analysis

#### Development Tools
- **[HOT_RELOAD.md](development/HOT_RELOAD.md)** - Hot reloading and development workflow
- **[KSP_MIGRATION.md](development/KSP_MIGRATION.md)** - KSP migration guide (reference)
- **[AWS_BEDROCK_CURSOR_SETUP.md](development/AWS_BEDROCK_CURSOR_SETUP.md)** - AWS Bedrock setup for Cursor IDE
- **[AWS_BEDROCK_QUICK_START.md](development/AWS_BEDROCK_QUICK_START.md)** - Quick start guide for AWS Bedrock

#### Supabase Setup & Configuration
- **[SUPABASE_SETUP.md](development/SUPABASE_SETUP.md)** - Complete Supabase setup guide (local and cloud)
- **[CLOUD_SETUP_STEPS.md](development/CLOUD_SETUP_STEPS.md)** - Step-by-step cloud setup
- **[STAGING_ENVIRONMENT_SETUP.md](development/STAGING_ENVIRONMENT_SETUP.md)** - Staging environment configuration
- **[STAGING_APP_CONFIGURATION.md](development/STAGING_APP_CONFIGURATION.md)** - Staging app configuration
- **[RUNTIME_ENVIRONMENT_SWITCHING.md](development/RUNTIME_ENVIRONMENT_SWITCHING.md)** - Runtime environment switching feature

#### OAuth & Authentication
- **[GOOGLE_OAUTH_SETUP.md](development/GOOGLE_OAUTH_SETUP.md)** - Google OAuth configuration
- **[SUPABASE_GOOGLE_CONFIG.md](development/SUPABASE_GOOGLE_CONFIG.md)** - Supabase Google provider setup
- **[SUPABASE_OAUTH_CALLBACK_SETUP.md](development/SUPABASE_OAUTH_CALLBACK_SETUP.md)** - OAuth callback URL configuration
- **[OAUTH_IMPLEMENTATION.md](development/OAUTH_IMPLEMENTATION.md)** - OAuth implementation guide (Chrome Custom Tabs)
- **[SUPABASE_EMAIL_CONFIRMATION.md](development/SUPABASE_EMAIL_CONFIRMATION.md)** - Email confirmation configuration

#### CI/CD & Infrastructure
- **[CI_CD_MIGRATION_SETUP.md](development/CI_CD_MIGRATION_SETUP.md)** - CI/CD migration setup
- **[SERVICE_ROLE_SETUP.md](development/SERVICE_ROLE_SETUP.md)** - Service role key setup
- **[DOCKER_SETUP.md](development/DOCKER_SETUP.md)** - Docker Desktop setup (if needed)
- **[../CI_CD_OPTIMIZATION.md](../CI_CD_OPTIMIZATION.md)** - CI/CD optimization details

#### Feature Flags
- **[FEATURE_FLAGS_TESTING.md](development/FEATURE_FLAGS_TESTING.md)** - Feature flags testing guide
- **[FLIPT_SETUP.md](development/FLIPT_SETUP.md)** - Flipt setup (if using Flipt)

### Testing

- **[TEST_COVERAGE.md](testing/TEST_COVERAGE.md)** - Test coverage status and strategy
- **[DATABASE_ACCESS.md](testing/DATABASE_ACCESS.md)** - Accessing Room database on emulator
- **[SUPABASE_AUTH_PROVIDER_TESTING.md](testing/SUPABASE_AUTH_PROVIDER_TESTING.md)** - Testing Supabase auth provider
- **[TEST_IMPROVEMENTS_ANALYSIS.md](testing/TEST_IMPROVEMENTS_ANALYSIS.md)** - Test improvements analysis
- **[TEST_VS_APP_IMPROVEMENTS.md](testing/TEST_VS_APP_IMPROVEMENTS.md)** - Test vs app improvements decision
- **[FRAMEWORK_ARCHITECTURE_RECOMMENDATION.md](testing/FRAMEWORK_ARCHITECTURE_RECOMMENDATION.md)** - Test framework architecture
- **[APPIUM_INTEGRATION_PLAN.md](testing/APPIUM_INTEGRATION_PLAN.md)** - Appium integration plan
- **[OPINIONATED_RECOMMENDATION.md](testing/OPINIONATED_RECOMMENDATION.md)** - Testing framework recommendations

## 🏗️ Architecture

### Core Architecture
- **[DATA_LAYER_ARCHITECTURE.md](architecture/DATA_LAYER_ARCHITECTURE.md)** - Data layer architecture (Room, Supabase, offline-first)
- **[AUTHENTICATION.md](architecture/AUTHENTICATION.md)** - Authentication system architecture
- **[ERROR_HANDLING.md](architecture/ERROR_HANDLING.md)** - Error handling system guide
- **[ERROR_CONVERSION_STRATEGIES.md](architecture/ERROR_CONVERSION_STRATEGIES.md)** - Error conversion patterns

### Feature Flags
- **[FEATURE_FLAGS.md](architecture/FEATURE_FLAGS.md)** - Feature flag system overview
- **[FEATURE_FLAGS_IMPLEMENTATION.md](architecture/FEATURE_FLAGS_IMPLEMENTATION.md)** - Feature flags implementation details
- **[FEATURE_FLAGS_CACHING.md](architecture/FEATURE_FLAGS_CACHING.md)** - Feature flags caching strategy
- **[FEATURE_FLAGS_FALLBACK.md](architecture/FEATURE_FLAGS_FALLBACK.md)** - Feature flags fallback behavior
- **[SUPABASE_FEATURE_FLAGS_ARCHITECTURE.md](architecture/SUPABASE_FEATURE_FLAGS_ARCHITECTURE.md)** - Supabase feature flags architecture

### Design System
- **[UX_PRINCIPLES.md](architecture/UX_PRINCIPLES.md)** - UX design principles and standards
- **[TYPOGRAPHY_SYSTEM.md](architecture/TYPOGRAPHY_SYSTEM.md)** - Typography system specification
- **[ICON_LOGO_DESIGN.md](architecture/ICON_LOGO_DESIGN.md)** - Icon and logo design principles
- **[UX_EVALUATION_SUMMARY.md](architecture/UX_EVALUATION_SUMMARY.md)** - UX principles evaluation summary
- **[UX_PRINCIPLES_EVALUATION.md](architecture/UX_PRINCIPLES_EVALUATION.md)** - Detailed UX principles evaluation

### Architecture Decisions
- **[decisions/DATA_STORAGE_OPTIONS.md](architecture/decisions/DATA_STORAGE_OPTIONS.md)** - Data storage technology decision
- **[decisions/FEATURE_FLAG_TOOLS.md](architecture/decisions/FEATURE_FLAG_TOOLS.md)** - Feature flag tool selection
- **[decisions/RAILWAY_FREE_TIER_ANALYSIS.md](architecture/decisions/RAILWAY_FREE_TIER_ANALYSIS.md)** - Railway free tier analysis
- **[decisions/SUPABASE_EDGE_FUNCTIONS_FEATURE_FLAGS.md](architecture/decisions/SUPABASE_EDGE_FUNCTIONS_FEATURE_FLAGS.md)** - Edge functions for feature flags decision

### Migration & OAuth
- **[MIGRATION_CHECKLIST.md](architecture/MIGRATION_CHECKLIST.md)** - Database migration checklist
- **[OAUTH_EVALUATION.md](architecture/OAUTH_EVALUATION.md)** - OAuth implementation evaluation
- **[OAUTH_SDK_DISCOVERY_ANALYSIS.md](architecture/OAUTH_SDK_DISCOVERY_ANALYSIS.md)** - OAuth SDK discovery and analysis

## 📦 Archived Documentation

Historical evaluations, completed analyses, and resolved issues (kept for reference).

### Archived Evaluations
- **[archive/ARCHITECTURE_EVALUATION.md](archive/ARCHITECTURE_EVALUATION.md)** - Architecture evaluation (completed)
- **[archive/ERROR_HANDLING_EVALUATION.md](archive/ERROR_HANDLING_EVALUATION.md)** - Error handling evaluation (completed)
- **[archive/GOOGLE_SSO_IMPLEMENTATION.md](archive/GOOGLE_SSO_IMPLEMENTATION.md)** - Google SSO implementation (historical)

### Archived Development Docs
- **[archive/development/WORKFLOW_MIGRATION_STATUS.md](archive/development/WORKFLOW_MIGRATION_STATUS.md)** - Workflow migration status (completed)
- **[archive/development/SUPABASE_WORKFLOW_REVIEW.md](archive/development/SUPABASE_WORKFLOW_REVIEW.md)** - Supabase workflow review (completed)
- **[archive/development/SUPABASE_WORKFLOW_ALIGNMENT.md](archive/development/SUPABASE_WORKFLOW_ALIGNMENT.md)** - Workflow alignment (completed)
- **[archive/development/GITHUB_ACTIONS_FAILURE_ANALYSIS.md](archive/development/GITHUB_ACTIONS_FAILURE_ANALYSIS.md)** - CI failure analysis (resolved)
- **[archive/development/SUPABASE_MIGRATION_FAILURE_ANALYSIS.md](archive/development/SUPABASE_MIGRATION_FAILURE_ANALYSIS.md)** - Migration failure analysis (resolved)
- **[archive/development/MULTI_AGENT_WORKFLOW_EVALUATION.md](archive/development/MULTI_AGENT_WORKFLOW_EVALUATION.md)** - Multi-agent workflow evaluation (superseded by MULTI_AGENT_WORKFLOW.md)
- **[archive/REVIEW_2024-11-18.md](archive/REVIEW_2024-11-18.md)** - Codebase review (historical)

## 📝 Documentation Guidelines

### Adding New Documentation

1. **Developer Guides**: Add to `docs/development/` or `docs/testing/`
2. **Architecture**: Add to `docs/architecture/`
3. **Decisions**: Add to `docs/architecture/decisions/`
4. **Evaluations/Completed Work**: Add to `docs/archive/` when completed

### Updating Documentation

- Keep developer guides current and accurate
- Update architecture docs when making significant changes
- Archive evaluations and completed analyses (don't delete)
- Update this index when adding new docs

### Documentation Principles

- **Keep it current**: Update docs when code changes
- **Be specific**: Include examples and code snippets
- **Link related docs**: Cross-reference related documentation
- **Archive, don't delete**: Move completed analyses to archive

## 🔍 Finding Documentation

### By Topic

**Multi-Agent Workflow:**
- `docs/development/MULTI_AGENT_WORKFLOW.md`
- `docs/development/MULTI_AGENT_QUICK_REFERENCE.md`
- `docs/development/AGENT_COORDINATION.md`

**Testing:**
- `docs/testing/TEST_COVERAGE.md`
- `docs/testing/FRAMEWORK_ARCHITECTURE_RECOMMENDATION.md`
- `AI_AGENT_GUIDELINES.md` (Testing section)

**Accessibility:**
- `docs/architecture/UX_PRINCIPLES.md`
- `docs/architecture/TYPOGRAPHY_SYSTEM.md`
- `AI_AGENT_GUIDELINES.md` (Accessibility section)

**CI/CD:**
- `docs/development/CI_CD_MIGRATION_SETUP.md`
- `docs/CI_CD_OPTIMIZATION.md`
- `AI_AGENT_GUIDELINES.md` (CI/CD section)

**Architecture:**
- `docs/architecture/DATA_LAYER_ARCHITECTURE.md`
- `docs/architecture/AUTHENTICATION.md`
- `docs/architecture/ERROR_HANDLING.md`

## Related Files

- **[AI_AGENT_GUIDELINES.md](../AI_AGENT_GUIDELINES.md)** - Complete agent guidelines
- **[.cursor/rules/](../.cursor/rules/)** - Cursor rules for automated enforcement
