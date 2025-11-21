# Development Metrics Architecture

**Date**: 2025-11-21  
**Status**: Architectural Evaluation  
**Priority**: High

## Executive Summary

This document evaluates the architecture for development metrics tracking, addressing:
1. **Schema separation** - Should metrics live in a separate schema?
2. **Environment strategy** - Which environments should track metrics?
3. **Historical data migration** - Can we infer metrics from existing history?
4. **AI agent awareness** - How do we ensure prompts consider these decisions?

## Current State

### Existing Infrastructure
- **Supabase**: Staging and production projects
- **Schemas**: Currently using `public` schema for all tables (`moods`, `feature_flags`)
- **Local metrics**: JSON files in `development-metrics/` directory
- **CI/CD**: GitHub Actions workflows

### Proposed Metrics Schema
- **Location**: `public` schema (in current migration)
- **Tables**: `pr_events`, `deployment_events`, `test_runs`, `rule_compliance_events`, `development_metrics`
- **Purpose**: Track PR cycle time, deployment frequency, test pass rate, rule compliance

## Architectural Decisions

### 1. Schema Separation: Should Metrics Live in a Separate Schema?

#### Option A: Separate `metrics` Schema (RECOMMENDED)

**Pros:**
- ✅ **Separation of concerns** - Production data (`public`) vs analytics data (`metrics`)
- ✅ **Security isolation** - Different RLS policies for metrics vs production data
- ✅ **Performance** - Can optimize metrics schema independently
- ✅ **Scalability** - Easier to archive/partition metrics data
- ✅ **Clear boundaries** - Makes it obvious what's production vs analytics

**Cons:**
- ⚠️ Requires schema configuration in `config.toml`
- ⚠️ Slightly more complex queries (schema prefix)

**Implementation:**
```sql
-- Create metrics schema
CREATE SCHEMA IF NOT EXISTS metrics;

-- Move all metrics tables to metrics schema
CREATE TABLE metrics.pr_events (...);
CREATE TABLE metrics.deployment_events (...);
CREATE TABLE metrics.test_runs (...);
CREATE TABLE metrics.rule_compliance_events (...);
CREATE TABLE metrics.development_metrics (...);
```

**Update `supabase/config.toml`:**
```toml
schemas = ["public", "graphql_public", "metrics"]
```

#### Option B: Keep in `public` Schema (Current)

**Pros:**
- ✅ Simpler - no schema management
- ✅ Consistent with existing tables (`moods`, `feature_flags`)

**Cons:**
- ❌ Mixes production data with analytics
- ❌ Harder to apply different retention policies
- ❌ Less clear separation of concerns

#### Recommendation: **Option A - Separate `metrics` Schema**

**Rationale**: Development metrics are analytics/observability data, not production application data. They should be separated for:
- Clear architectural boundaries
- Different retention/archival policies
- Independent optimization
- Better security isolation

### 2. Environment Strategy: Where Should Metrics Live?

#### Current Environment Setup
- **Local Supabase**: Development/testing
- **Staging Supabase**: `https://rmcnvcqnowgsvvbmfssi.supabase.co`
- **Production Supabase**: `https://mvuzvoyvijsdqsfqjgpd.supabase.co`

#### Option A: Metrics in All Environments (RECOMMENDED)

**Strategy:**
- **Local**: Track local development metrics (optional, for testing)
- **Staging**: Track staging environment metrics (CI/CD, staging deployments)
- **Production**: Track production metrics (production deployments, real PRs)

**Pros:**
- ✅ Complete picture across environments
- ✅ Can compare staging vs production metrics
- ✅ Local testing of metrics collection
- ✅ Environment-specific insights

**Cons:**
- ⚠️ More complex setup (multiple Supabase projects)
- ⚠️ Need to aggregate across environments for full picture

**Implementation:**
```kotlin
// In CI/CD or metrics collection script
val environment = when {
    isLocalDev() -> "local"
    isStaging() -> "staging"
    else -> "production"
}

val supabaseUrl = when (environment) {
    "local" -> "http://127.0.0.1:54321"
    "staging" -> BuildConfig.SUPABASE_STAGING_URL
    else -> BuildConfig.SUPABASE_URL
}
```

#### Option B: Metrics Only in Production

**Pros:**
- ✅ Simpler - single source of truth
- ✅ Only tracks "real" metrics

**Cons:**
- ❌ No staging metrics for comparison
- ❌ Can't test metrics collection locally
- ❌ Missing context for staging-specific issues

#### Option C: Separate Analytics Supabase Project

**Pros:**
- ✅ Complete separation from production data
- ✅ Can have different retention/archival policies
- ✅ Doesn't impact production database performance

**Cons:**
- ❌ Additional Supabase project to manage
- ❌ More complex setup
- ❌ Additional cost

#### Recommendation: **Option A - Metrics in All Environments**

**Rationale**: 
- Need staging metrics to compare against production
- Local metrics useful for testing metrics collection
- Environment-specific insights valuable
- Can aggregate across environments when needed

### 3. Historical Data Migration: Can We Infer from History?

#### Existing Data Sources

1. **GitHub API** - PR history, commit history
2. **CI/CD Logs** - Test runs, deployment history
3. **JSON Metrics Files** - `development-metrics/` directory
4. **Git History** - Commit timestamps, branch information

#### Migration Strategy

**Phase 1: Backfill from GitHub API**

```sql
-- Create temporary staging table
CREATE TEMP TABLE temp_pr_events AS
SELECT 
    pr_number,
    pr_title,
    branch_name,
    'created' AS event_type,
    created_at AS event_timestamp,
    author,
    NULL::INTEGER AS cycle_time_seconds
FROM (
    -- Query GitHub API for PR history
    -- This would be done via script, not SQL
) AS github_data;
```

**Phase 2: Calculate Cycle Times**

```sql
-- Calculate cycle times from historical PR data
UPDATE temp_pr_events t1
SET cycle_time_seconds = (
    SELECT EXTRACT(EPOCH FROM (
        (SELECT event_timestamp FROM temp_pr_events 
         WHERE pr_number = t1.pr_number AND event_type = 'merged')
        - 
        (SELECT event_timestamp FROM temp_pr_events 
         WHERE pr_number = t1.pr_number AND event_type = 'created')
    ))
)
WHERE t1.event_type = 'merged';
```

**Phase 3: Migrate JSON Metrics**

```sql
-- Script to read JSON files and insert into test_runs
-- Example structure:
INSERT INTO metrics.test_runs (
    run_id, run_timestamp, total_tests, passed_tests, 
    failed_tests, skipped_tests, execution_time_seconds, test_type
)
SELECT 
    data->>'run_id',
    (data->>'timestamp')::TIMESTAMPTZ,
    (data->>'total_tests')::INTEGER,
    (data->>'passed_tests')::INTEGER,
    (data->>'failed_tests')::INTEGER,
    (data->>'skipped_tests')::INTEGER,
    (data->>'execution_time_seconds')::INTEGER,
    data->>'test_type'
FROM (
    -- Read from development-metrics/tests/*.json files
) AS json_data;
```

**Phase 4: Infer Rule Compliance from Git History**

```sql
-- Infer rule compliance from commit messages and branch names
-- Example: Check if commits follow branching rules
INSERT INTO metrics.rule_compliance_events (
    rule_name, rule_category, compliance_status, 
    context_type, context_id, event_timestamp
)
SELECT 
    'branching.mdc' AS rule_name,
    'foundation' AS rule_category,
    CASE 
        WHEN branch_name LIKE 'feature/%' OR branch_name LIKE 'fix/%' 
        THEN 'compliant'
        ELSE 'violation'
    END AS compliance_status,
    'commit' AS context_type,
    commit_sha AS context_id,
    commit_timestamp AS event_timestamp
FROM (
    -- Parse git log output
    -- git log --format="%H|%s|%an|%ad" --date=iso
) AS git_data;
```

#### Recommendation: **Incremental Migration**

1. **Start fresh** - Begin tracking new metrics going forward
2. **Backfill selectively** - Migrate high-value historical data (last 30-90 days)
3. **Use temp tables** - Create temp tables for migration, validate, then insert
4. **Script-based** - Use Python/Node.js scripts to query APIs and populate

**Why not full backfill?**
- Historical data may be incomplete
- Some metrics can't be accurately inferred (e.g., exact test pass rates from old runs)
- Focus on forward-looking metrics (research shows 15-25% improvement from tracking)

### 4. AI Agent Awareness: How Do Prompts Consider These Decisions?

#### Current Gap

AI agents need to know:
- ✅ Metrics schema location (`metrics` vs `public`)
- ✅ Environment strategy (which Supabase project to use)
- ✅ Historical migration approach (when to backfill vs start fresh)
- ✅ Schema structure and patterns

#### Solution: Architecture Decision Records (ADRs)

**Create ADR document** that AI agents can reference:

```markdown
# ADR-001: Development Metrics Architecture

## Decision
- Metrics live in `metrics` schema (not `public`)
- Metrics tracked in all environments (local, staging, production)
- Historical migration is selective (last 30-90 days, not full backfill)

## Context
[Why these decisions were made]

## Consequences
[What this means for implementation]
```

#### Solution: Cursor Rules Integration

**Add to `.cursor/rules/` or architecture docs:**

```markdown
## Development Metrics

When working with development metrics:
- ✅ Use `metrics` schema (not `public`)
- ✅ Consider environment (local/staging/production)
- ✅ Use service role for CI/CD writes
- ✅ Historical data: backfill selectively, not full history
```

#### Solution: Code Comments and Documentation

**In migration files and scripts:**

```sql
-- ADR-001: Metrics in metrics schema, not public
-- See: docs/architecture/DEVELOPMENT_METRICS_ARCHITECTURE.md
CREATE SCHEMA IF NOT EXISTS metrics;
```

## Recommended Architecture

### Schema Structure

```
supabase/
├── migrations/
│   └── 20251121000000_create_metrics_schema.sql  (NEW - creates schema)
│   └── 20251121000001_create_development_metrics_tables.sql  (UPDATED - uses metrics schema)
└── scripts/
    └── migrate_historical_metrics.sh  (NEW - backfill script)
```

### Environment Configuration

```kotlin
// Metrics collection configuration
data class MetricsConfig(
    val environment: String, // "local", "staging", "production"
    val supabaseUrl: String,
    val serviceRoleKey: String,
    val schema: String = "metrics"  // Always metrics schema
)
```

### Migration Script Structure

```bash
#!/bin/bash
# migrate_historical_metrics.sh

# 1. Query GitHub API for PR history (last 90 days)
# 2. Parse JSON metrics files
# 3. Query git history for rule compliance
# 4. Insert into temp tables
# 5. Validate data
# 6. Insert into metrics schema tables
```

## Implementation Plan

### Phase 1: Schema Separation (1-2 days)
1. ✅ Create `metrics` schema migration
2. ✅ Update metrics tables migration to use `metrics` schema
3. ✅ Update `config.toml` to include `metrics` schema
4. ✅ Update RLS policies for `metrics` schema

### Phase 2: Environment Configuration (1 day)
1. ✅ Document environment strategy
2. ✅ Create metrics collection configuration
3. ✅ Update CI/CD to use correct environment

### Phase 3: Historical Migration (2-3 days)
1. ✅ Create migration script for GitHub API data
2. ✅ Create migration script for JSON metrics files
3. ✅ Create migration script for git history (rule compliance)
4. ✅ Test migration on staging
5. ✅ Run migration on production

### Phase 4: Documentation and Rules (1 day)
1. ✅ Create ADR document
2. ✅ Update cursor rules with metrics guidance
3. ✅ Add architecture documentation
4. ✅ Update AI agent guidelines

## Questions for Consideration

1. **Retention Policy**: How long should we keep metrics? (Recommendation: 1-2 years, then archive)
2. **Aggregation Frequency**: Daily? Weekly? (Recommendation: Daily for dashboard, weekly for trends)
3. **Alerting**: Should we alert on metric thresholds? (Recommendation: Yes, for critical metrics like test pass rate < 80%)
4. **Dashboard**: Build custom dashboard or use Supabase dashboard? (Recommendation: Start with Supabase, evolve to custom)

## Related Documentation

- `supabase/migrations/20251121000000_create_development_metrics_tables.sql` - Current migration
- `supabase/scripts/README_DEVELOPMENT_METRICS.md` - Usage guide
- `docs/development/lessons/RESEARCH_BASED_IMPROVEMENTS.md` - Research backing
- `docs/architecture/DATA_LAYER_ARCHITECTURE.md` - Data layer patterns
- `docs/development/environments/` - Environment configuration

