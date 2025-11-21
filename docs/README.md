# Documentation Index

**Last Updated**: 2025-11-21

This directory contains all project documentation organized by purpose. Documentation is now organized into subdirectories for easier navigation.

## 🚀 Quick Start

- **[README.md](../README.md)** - Project overview and quick start
- **[AI_AGENT_GUIDELINES.md](../AI_AGENT_GUIDELINES.md)** - Complete guidelines for AI agents and developers
- **[Multi-Agent Workflow](development/workflow/MULTI_AGENT_WORKFLOW.md)** - How to work with multiple agents

## 📚 Active Documentation

### Development Documentation

Documentation is organized into subdirectories:

- **[Setup Guides](development/setup/README.md)** - AWS, Supabase, Python, OAuth, Docker setup
- **[Workflow Guides](development/workflow/README.md)** - Multi-agent workflow, git worktrees, branch management
- **[CI/CD](development/ci-cd/README.md)** - Continuous integration and deployment
- **[Tools](development/tools/README.md)** - Emulator management and development tools
- **[Feature Flags](development/feature-flags/README.md)** - Feature flag system
- **[Environments](development/environments/README.md)** - Staging, cloud, runtime configuration
- **[Guides](development/guides/README.md)** - General development guides
- **[Reports](development/reports/README.md)** - Analysis reports and evaluations

### Testing Documentation

Testing documentation is organized into:

- **[Guides](testing/guides/README.md)** - Test coverage, database access, framework integration
- **[Architecture](testing/architecture/README.md)** - Testing framework architecture and visual evaluation
- **[Implementation](testing/implementation/README.md)** - Implementation status and progress
- **[Tools](testing/tools/README.md)** - Video, screenshot, and AI vision tools
- **[Reports](testing/reports/README.md)** - Evaluation reports and analysis

### Learning Documentation

- **[Blog Posts](learning/blog-posts/README.md)** - AI-driven development blog posts
- **[Workflow Tools](learning/workflow-tools/README.md)** - Documentation tools and automation

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

1. **Setup Guides**: Add to `docs/development/setup/`
2. **Workflow Guides**: Add to `docs/development/workflow/`
3. **CI/CD**: Add to `docs/development/ci-cd/`
4. **Tools**: Add to `docs/development/tools/`
5. **Feature Flags**: Add to `docs/development/feature-flags/`
6. **Environments**: Add to `docs/development/environments/`
7. **General Guides**: Add to `docs/development/guides/`
8. **Reports**: Add to `docs/development/reports/`
9. **Testing**: Add to appropriate `docs/testing/` subdirectory
10. **Architecture**: Add to `docs/architecture/`
11. **Decisions**: Add to `docs/architecture/decisions/`
12. **Evaluations/Completed Work**: Add to `docs/archive/` when completed

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
- `docs/development/workflow/` - See [Workflow README](development/workflow/README.md)

**Testing:**
- `docs/testing/guides/` - See [Testing Guides README](testing/guides/README.md)
- `docs/testing/architecture/` - See [Testing Architecture README](testing/architecture/README.md)
- `AI_AGENT_GUIDELINES.md` (Testing section)

**Setup & Configuration:**
- `docs/development/setup/` - See [Setup README](development/setup/README.md)

**CI/CD:**
- `docs/development/ci-cd/` - See [CI/CD README](development/ci-cd/README.md)
- `docs/CI_CD_OPTIMIZATION.md` (root level)
- `AI_AGENT_GUIDELINES.md` (CI/CD section)

**Architecture:**
- `docs/architecture/` - Core architecture documentation
- `docs/architecture/DATA_LAYER_ARCHITECTURE.md`
- `docs/architecture/AUTHENTICATION.md`
- `docs/architecture/ERROR_HANDLING.md`

**Accessibility:**
- `docs/architecture/UX_PRINCIPLES.md`
- `docs/architecture/TYPOGRAPHY_SYSTEM.md`
- `AI_AGENT_GUIDELINES.md` (Accessibility section)

## Related Files

- **[AI_AGENT_GUIDELINES.md](../AI_AGENT_GUIDELINES.md)** - Complete agent guidelines
- **[.cursor/rules/](../.cursor/rules/)** - Cursor rules for automated enforcement
