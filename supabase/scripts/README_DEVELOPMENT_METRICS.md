# Development Metrics Schema

**Date**: 2025-11-21  
**Status**: Ready for use  
**Migration**: `20251121000000_create_development_metrics_tables.sql`

## Overview

This schema tracks development productivity metrics as identified in research-backed improvements:
- **PR cycle time** - Time from PR creation to merge
- **Deployment frequency** - Number of successful deployments per period
- **Test pass rate** - Percentage of tests passing
- **Rule compliance rate** - Percentage of rule compliance events
- **Documentation-first usage** - Tracked via rule compliance events

**Research shows**: Metrics-driven improvement increases productivity by 15-25%

## Schema Structure

**Important**: All metrics tables live in the `metrics` schema (not `public`).  
See: `docs/architecture/DEVELOPMENT_METRICS_ARCHITECTURE.md` for architectural decisions.

### Tables

1. **`pr_events`** - Tracks PR lifecycle events
   - Events: `created`, `reviewed`, `merged`, `closed`, `reopened`
   - Calculated fields: `cycle_time_seconds`, `review_time_seconds`
   - Indexed for efficient queries by PR number, event type, timestamp

2. **`deployment_events`** - Tracks deployment events
   - Environments: `development`, `staging`, `production`
   - Status: `success`, `failure`, `rollback`
   - Links to PR number and commit SHA

3. **`test_runs`** - Tracks test execution results
   - Test types: `unit`, `integration`, `e2e`, `all`
   - Calculated field: `pass_rate` (generated column)
   - Links to PR number and commit SHA

4. **`rule_compliance_events`** - Tracks rule compliance
   - Rules: Any rule name (e.g., `branching.mdc`, `code-quality.mdc`)
   - Status: `compliant`, `violation`, `warning`
   - Context: `commit`, `pr`, `code_review`, `manual_check`

5. **`development_metrics`** - Aggregated metrics (optional)
   - Pre-calculated metrics for dashboard
   - One metric per type per day
   - Can be populated by scheduled jobs

### Helper Functions

- `calculate_avg_pr_cycle_time(start_date, end_date)` - Average PR cycle time
- `calculate_deployment_frequency(start_date, end_date, env)` - Deployment count
- `calculate_avg_test_pass_rate(start_date, end_date)` - Average test pass rate
- `calculate_rule_compliance_rate(start_date, end_date)` - Compliance percentage

## Usage Examples

### Recording PR Events

```sql
-- PR created
INSERT INTO metrics.pr_events (pr_number, pr_title, branch_name, event_type, author)
VALUES (42, 'Add feature X', 'feature/add-x', 'created', 'developer');

-- PR merged (with cycle time calculation)
INSERT INTO metrics.pr_events (pr_number, event_type, cycle_time_seconds)
VALUES (42, 'merged', EXTRACT(EPOCH FROM (NOW() - (
    SELECT event_timestamp FROM metrics.pr_events 
    WHERE pr_number = 42 AND event_type = 'created' 
    LIMIT 1
))));
```

### Recording Deployment Events

```sql
-- Successful deployment
INSERT INTO metrics.deployment_events (
    deployment_id, environment, status, commit_sha, branch_name, pr_number
)
VALUES (
    'deploy-123', 'production', 'success', 'abc123', 'main', 42
);
```

### Recording Test Runs

```sql
-- Test run results
INSERT INTO metrics.test_runs (
    run_id, total_tests, passed_tests, failed_tests, skipped_tests,
    execution_time_seconds, test_type, branch_name, commit_sha, pr_number
)
VALUES (
    'test-run-456', 150, 145, 3, 2, 120, 'all', 'feature/add-x', 'abc123', 42
);
-- pass_rate is automatically calculated
```

### Recording Rule Compliance

```sql
-- Rule compliance check
INSERT INTO metrics.rule_compliance_events (
    rule_name, rule_category, compliance_status, context_type, context_id, branch_name
)
VALUES (
    'branching.mdc', 'foundation', 'compliant', 'pr', '42', 'feature/add-x'
);

-- Rule violation
INSERT INTO public.rule_compliance_events (
    rule_name, rule_category, compliance_status, context_type, context_id,
    violation_details, branch_name
)
VALUES (
    'code-quality.mdc', 'foundation', 'violation', 'code_review', '42',
    'Force unwrap operator used', 'feature/add-x'
);
```

### Querying Metrics

```sql
-- Average PR cycle time (last 30 days)
SELECT calculate_avg_pr_cycle_time(
    CURRENT_DATE - INTERVAL '30 days',
    CURRENT_DATE
) AS avg_cycle_time_seconds;

-- Deployment frequency (last 7 days, production)
SELECT calculate_deployment_frequency(
    CURRENT_DATE - INTERVAL '7 days',
    CURRENT_DATE,
    'production'
) AS deployment_count;

-- Average test pass rate (last 30 days)
SELECT calculate_avg_test_pass_rate(
    CURRENT_DATE - INTERVAL '30 days',
    CURRENT_DATE
) AS avg_pass_rate_percent;

-- Rule compliance rate (last 30 days)
SELECT calculate_rule_compliance_rate(
    CURRENT_DATE - INTERVAL '30 days',
    CURRENT_DATE
) AS compliance_rate_percent;
```

## Integration with CI/CD

### GitHub Actions Example

```yaml
# Record test run
- name: Record test metrics
  run: |
    # After tests run, record results
    curl -X POST "${{ secrets.SUPABASE_URL }}/rest/v1/test_runs" \
      -H "apikey: ${{ secrets.SUPABASE_SERVICE_ROLE_KEY }}" \
      -H "Authorization: Bearer ${{ secrets.SUPABASE_SERVICE_ROLE_KEY }}" \
      -H "Content-Type: application/json" \
      -d '{
        "run_id": "${{ github.run_id }}",
        "total_tests": 150,
        "passed_tests": 145,
        "failed_tests": 3,
        "skipped_tests": 2,
        "execution_time_seconds": 120,
        "test_type": "all",
        "branch_name": "${{ github.head_ref }}",
        "commit_sha": "${{ github.sha }}",
        "pr_number": ${{ github.event.pull_request.number }}
      }'
```

### PR Event Tracking

```yaml
# On PR opened
- name: Record PR created
  if: github.event.action == 'opened'
  run: |
    curl -X POST "${{ secrets.SUPABASE_URL }}/rest/v1/pr_events" \
      -H "apikey: ${{ secrets.SUPABASE_SERVICE_ROLE_KEY }}" \
      -H "Authorization: Bearer ${{ secrets.SUPABASE_SERVICE_ROLE_KEY }}" \
      -H "Content-Type: application/json" \
      -d '{
        "pr_number": ${{ github.event.pull_request.number }},
        "pr_title": "${{ github.event.pull_request.title }}",
        "branch_name": "${{ github.head_ref }}",
        "event_type": "created",
        "author": "${{ github.event.pull_request.user.login }}"
      }'

# On PR merged
- name: Record PR merged
  if: github.event.action == 'closed' && github.event.pull_request.merged == true
  run: |
    # Calculate cycle time and record merge
    # (cycle time calculation can be done in SQL or application code)
```

## Dashboard Queries

### Weekly Metrics Summary

```sql
-- Get all key metrics for the last 7 days
SELECT 
    'PR Cycle Time (avg)' AS metric_name,
    ROUND(calculate_avg_pr_cycle_time(
        CURRENT_DATE - INTERVAL '7 days',
        CURRENT_DATE
    ) / 3600, 2) AS value,
    'hours' AS unit
UNION ALL
SELECT 
    'Deployment Frequency',
    calculate_deployment_frequency(
        CURRENT_DATE - INTERVAL '7 days',
        CURRENT_DATE,
        'production'
    )::TEXT,
    'deployments'
UNION ALL
SELECT 
    'Test Pass Rate (avg)',
    ROUND(calculate_avg_test_pass_rate(
        CURRENT_DATE - INTERVAL '7 days',
        CURRENT_DATE
    ), 2)::TEXT,
    'percent'
UNION ALL
SELECT 
    'Rule Compliance Rate',
    ROUND(calculate_rule_compliance_rate(
        CURRENT_DATE - INTERVAL '7 days',
        CURRENT_DATE
    ), 2)::TEXT,
    'percent';
```

### Trend Analysis

```sql
-- PR cycle time trend (last 30 days, daily averages)
SELECT 
    DATE(event_timestamp) AS date,
    AVG(cycle_time_seconds) / 3600 AS avg_cycle_time_hours
    FROM metrics.pr_events
WHERE event_type = 'merged'
    AND event_timestamp >= CURRENT_DATE - INTERVAL '30 days'
    AND cycle_time_seconds IS NOT NULL
GROUP BY DATE(event_timestamp)
ORDER BY date DESC;
```

## Populating Aggregated Metrics

You can pre-calculate metrics for faster dashboard queries:

```sql
-- Daily aggregation job (run via cron or scheduled function)
INSERT INTO metrics.development_metrics (metric_date, metric_type, metric_value, metric_unit, sample_size)
VALUES (
    CURRENT_DATE - 1, -- Yesterday
    'pr_cycle_time_avg',
    calculate_avg_pr_cycle_time(CURRENT_DATE - 1, CURRENT_DATE - 1),
    'seconds',
    (SELECT COUNT(*) FROM metrics.pr_events 
     WHERE event_type = 'merged' 
     AND event_timestamp::DATE = CURRENT_DATE - 1)
)
ON CONFLICT (metric_date, metric_type) 
DO UPDATE SET 
    metric_value = EXCLUDED.metric_value,
    sample_size = EXCLUDED.sample_size,
    updated_at = NOW();
```

## Security

- **RLS enabled** on all tables
- **Service role** can manage all data (for CI/CD)
- **Authenticated users** can read data
- Use service role key in CI/CD workflows
- Never expose service role key in client applications

## Next Steps

1. **Integrate with CI/CD** - Add metric recording to GitHub Actions workflows
2. **Create dashboard** - Build visualization using Supabase or external tool
3. **Set up aggregation** - Schedule daily metric aggregation
4. **Review metrics** - Weekly/monthly review of metrics for insights
5. **Iterate** - Adjust metrics based on what's most valuable

## Related Documentation

- `docs/development/lessons/RESEARCH_BASED_IMPROVEMENTS.md` - Research backing
- `docs/development/lessons/CURSOR_RULES_EVALUATION.md` - Rule evaluation
- Migration files:
  - `supabase/migrations/20251121000000_create_metrics_schema.sql` - Creates metrics schema
  - `supabase/migrations/20251121000001_create_development_metrics_tables.sql` - Creates metrics tables
- Architecture: `docs/architecture/DEVELOPMENT_METRICS_ARCHITECTURE.md` - Architectural decisions

