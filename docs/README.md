# Documentation Index

This directory contains all project documentation organized by purpose.

## 📚 Developer Guides

Active documentation for developers working on the project.

### Getting Started
- **[README.md](../README.md)** - Project overview and quick start
- **[AI_AGENT_GUIDELINES.md](../AI_AGENT_GUIDELINES.md)** - Guidelines for AI agents and developers

### Development Workflows
- **[development/HOT_RELOAD.md](development/HOT_RELOAD.md)** - Hot reloading and development workflow
- **[development/KSP_MIGRATION.md](development/KSP_MIGRATION.md)** - KSP migration guide (reference)

### Supabase Setup & Configuration
- **[development/SUPABASE_SETUP.md](development/SUPABASE_SETUP.md)** - Complete Supabase setup guide (local and cloud)
- **[development/CLOUD_SETUP_STEPS.md](development/CLOUD_SETUP_STEPS.md)** - Step-by-step cloud setup
- **[development/GOOGLE_OAUTH_SETUP.md](development/GOOGLE_OAUTH_SETUP.md)** - Google OAuth configuration
- **[development/SUPABASE_GOOGLE_CONFIG.md](development/SUPABASE_GOOGLE_CONFIG.md)** - Supabase Google provider setup
- **[development/SUPABASE_OAUTH_CALLBACK_SETUP.md](development/SUPABASE_OAUTH_CALLBACK_SETUP.md)** - OAuth callback URL configuration (fixes localhost redirect issue)
- **[development/OAUTH_IMPLEMENTATION.md](development/OAUTH_IMPLEMENTATION.md)** - OAuth implementation guide (Chrome Custom Tabs)
- **[development/SUPABASE_EMAIL_CONFIRMATION.md](development/SUPABASE_EMAIL_CONFIRMATION.md)** - Email confirmation configuration
- **[development/CI_CD_MIGRATION_SETUP.md](development/CI_CD_MIGRATION_SETUP.md)** - CI/CD migration setup
- **[development/SERVICE_ROLE_SETUP.md](development/SERVICE_ROLE_SETUP.md)** - Service role key setup
- **[development/DOCKER_SETUP.md](development/DOCKER_SETUP.md)** - Docker Desktop setup (if needed)

### Testing
- **[testing/DATABASE_ACCESS.md](testing/DATABASE_ACCESS.md)** - Accessing Room database on emulator
- **[testing/TEST_COVERAGE.md](testing/TEST_COVERAGE.md)** - Test coverage status
- **[testing/SUPABASE_AUTH_PROVIDER_TESTING.md](testing/SUPABASE_AUTH_PROVIDER_TESTING.md)** - Testing Supabase auth provider

## 🏗️ Architecture

Architecture documentation and design decisions.

### Core Architecture
- **[architecture/DATA_LAYER_ARCHITECTURE.md](architecture/DATA_LAYER_ARCHITECTURE.md)** - Data layer architecture (Room, Supabase, offline-first)
- **[architecture/AUTHENTICATION.md](architecture/AUTHENTICATION.md)** - Authentication system architecture
- **[architecture/FEATURE_FLAGS.md](architecture/FEATURE_FLAGS.md)** - Feature flag system
- **[architecture/ERROR_HANDLING.md](architecture/ERROR_HANDLING.md)** - Error handling system guide
- **[architecture/ERROR_CONVERSION_STRATEGIES.md](architecture/ERROR_CONVERSION_STRATEGIES.md)** - Error conversion patterns

### Architecture Decisions
- **[architecture/decisions/DATA_STORAGE_OPTIONS.md](architecture/decisions/DATA_STORAGE_OPTIONS.md)** - Data storage technology decision
- **[architecture/MIGRATION_CHECKLIST.md](architecture/MIGRATION_CHECKLIST.md)** - Database migration checklist

## 📦 Archived Evaluations

Historical evaluations and analysis documents (kept for reference).

- **[archive/ARCHITECTURE_EVALUATION.md](archive/ARCHITECTURE_EVALUATION.md)** - Architecture evaluation and recommendations
- **[archive/ERROR_HANDLING_EVALUATION.md](archive/ERROR_HANDLING_EVALUATION.md)** - Error handling system evaluation
- **[archive/GOOGLE_SSO_IMPLEMENTATION.md](archive/GOOGLE_SSO_IMPLEMENTATION.md)** - Google SSO implementation status (historical)

## 📝 Documentation Guidelines

### Adding New Documentation

1. **Developer Guides**: Add to `docs/development/` or `docs/testing/`
2. **Architecture**: Add to `docs/architecture/`
3. **Decisions**: Add to `docs/architecture/decisions/`
4. **Evaluations**: Add to `docs/archive/` when completed (historical reference)

### Updating Documentation

- Keep developer guides current and accurate
- Update architecture docs when making significant changes
- Archive evaluations but don't delete them
- Update this index when adding new docs

