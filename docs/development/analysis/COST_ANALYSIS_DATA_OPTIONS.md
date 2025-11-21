# Cost Analysis Data Options

**Date**: January 21, 2025  
**Purpose**: Explore data analysis options for cost tracking system

---

## Current State

### ✅ Working Components

1. **Data Collection**
   - ✅ `capture-agent-usage.sh` - Collects cost data
   - ✅ Local JSON storage (`development-metrics/agent-usage/`)
   - ✅ Supabase sync (when env vars configured)
   - ✅ Cost calculation (pricing, token estimation)

2. **Basic Analysis**
   - ✅ `analyze-costs.sh` - Aggregates costs by model, company, task type, provider
   - ✅ Supports JSON, CSV, text output
   - ✅ Can query Supabase or local JSON files

### ⚠️ Testing Status

**Supabase Sync Test:**
- ✅ Script correctly detects missing env vars
- ✅ Falls back to local JSON storage
- ✅ JSON structure matches Supabase schema
- ⚠️ **Needs actual Supabase connection to fully test**

**To Test Supabase Sync:**
```bash
# Set environment variables
export SUPABASE_URL="https://your-project.supabase.co"
export SUPABASE_SECRET_KEY="sb_secret_..."

# Test sync
./scripts/metrics/capture-agent-usage.sh \
  --model "anthropic.claude-sonnet-4-5-20250929-v1:0" \
  --input-tokens 1500 \
  --output-tokens 600 \
  --task-type "test_sync"
```

---

## Data Analysis Options

### Option 1: SQL Queries (Supabase)

**When to Use**: When data is in Supabase, need complex queries, or want to join with other tables.

**Advantages:**
- ✅ Powerful SQL queries
- ✅ Can join with prompts, sessions, other metrics
- ✅ Fast aggregation
- ✅ Complex filtering and grouping
- ✅ Built-in functions (SUM, AVG, COUNT, etc.)

**Example Queries:**

```sql
-- Total cost by model (last 30 days)
SELECT 
    agent_model,
    SUM(total_cost) as total_cost,
    COUNT(*) as interactions,
    AVG(total_cost) as avg_cost_per_interaction
FROM agent_usage
WHERE created_at >= NOW() - INTERVAL '30 days'
GROUP BY agent_model
ORDER BY total_cost DESC;

-- Cost trends over time (daily)
SELECT 
    DATE(created_at) as date,
    SUM(total_cost) as daily_cost,
    COUNT(*) as interactions
FROM agent_usage
WHERE created_at >= NOW() - INTERVAL '30 days'
GROUP BY DATE(created_at)
ORDER BY date DESC;

-- Task type efficiency (cost per interaction)
SELECT 
    task_type,
    COUNT(*) as interactions,
    SUM(total_cost) as total_cost,
    AVG(total_cost) as avg_cost,
    AVG(total_tokens) as avg_tokens
FROM agent_usage
GROUP BY task_type
ORDER BY avg_cost DESC;

-- Model comparison (cost efficiency)
SELECT 
    agent_model,
    SUM(input_tokens) as total_input_tokens,
    SUM(output_tokens) as total_output_tokens,
    SUM(total_cost) as total_cost,
    SUM(total_cost) / SUM(total_tokens) * 1000000 as cost_per_million_tokens
FROM agent_usage
GROUP BY agent_model
ORDER BY cost_per_million_tokens;
```

**Implementation:**
- Use `postgrest_query` from `supabase-postgrest.sh`
- Or direct SQL via Supabase CLI: `supabase db query "SELECT ..."`
- Or Supabase Studio SQL editor

---

### Option 2: Enhanced Analysis Scripts

**When to Use**: When you want automated reports, scheduled analysis, or scriptable workflows.

**Current Script**: `analyze-costs.sh`
- ✅ Basic aggregation
- ✅ Multiple output formats
- ✅ Time period filtering

**Potential Enhancements:**

#### A. Trend Analysis
```bash
# Daily/weekly/monthly trends
./scripts/analysis/analyze-costs.sh --trends daily
./scripts/analysis/analyze-costs.sh --trends weekly
./scripts/analysis/analyze-costs.sh --trends monthly
```

**Output:**
```
Daily Trends (Last 30 Days):
  Day 1: $1.50 (10 interactions)
  Day 2: $2.10 (15 interactions)
  Day 3: $1.80 (12 interactions)
  ...
```

#### B. Efficiency Metrics
```bash
# Cost per token, cost per interaction, etc.
./scripts/analysis/analyze-costs.sh --efficiency
```

**Output:**
```
Efficiency Metrics:
  Average cost per token: $0.000008
  Average cost per interaction: $0.024
  Average tokens per interaction: 2,100
  Most efficient model: claude-3-5-haiku ($0.000004/token)
```

#### C. Comparison Reports
```bash
# Compare models, time periods, task types
./scripts/analysis/analyze-costs.sh --compare models
./scripts/analysis/analyze-costs.sh --compare "last-7-days" "last-30-days"
```

#### D. Anomaly Detection
```bash
# Detect unusual cost spikes or patterns
./scripts/analysis/analyze-costs.sh --detect-anomalies
```

**Output:**
```
Anomalies Detected:
  ⚠️  High cost spike on 2025-01-15: $5.20 (3x average)
  ⚠️  Unusual model usage: Opus used for simple formatting task
```

---

### Option 3: Visualization

**When to Use**: When you want visual charts, dashboards, or interactive exploration.

#### A. Supabase Dashboard (Built-in)
- ✅ Supabase Studio has built-in dashboard
- ✅ Can create charts from SQL queries
- ✅ No additional setup needed
- ✅ Accessible via web interface

**Setup:**
1. Go to Supabase Studio → SQL Editor
2. Create saved queries
3. Add to dashboard

#### B. Metabase (If Installed)
- ✅ More powerful visualization
- ✅ Interactive dashboards
- ✅ Scheduled reports
- ⚠️ Requires separate setup

**See**: `docs/development/setup/METABASE_DASHBOARD_SETUP.md`

#### C. Simple HTML Dashboard
- ✅ No dependencies
- ✅ Can use Chart.js or similar
- ✅ Generate from JSON data
- ✅ Shareable HTML file

**Example:**
```bash
# Generate HTML dashboard
./scripts/metrics/generate-dashboard.sh --output dashboard.html
```

#### D. CSV Export + Excel/Sheets
- ✅ Export to CSV
- ✅ Open in Excel/Google Sheets
- ✅ Create charts manually
- ✅ Easy to share

```bash
./scripts/analysis/analyze-costs.sh --output csv > costs.csv
```

---

### Option 4: Integration with Existing Metrics

**When to Use**: When you want to correlate costs with other metrics (prompts, sessions, builds, tests).

**Current Metrics System:**
- `development-metrics/prompts/` - Prompt tracking
- `development-metrics/sessions/` - Session tracking
- `development-metrics/agent-usage/` - Cost tracking

**Potential Integrations:**

#### A. Cost per Prompt
```sql
-- Join agent_usage with prompts (if prompt_id linked)
SELECT 
    p.prompt,
    au.total_cost,
    au.agent_model,
    au.task_type
FROM agent_usage au
JOIN prompts p ON au.prompt_id = p.id
ORDER BY au.total_cost DESC
LIMIT 10;
```

#### B. Cost per Session
```sql
-- Aggregate costs by session
SELECT 
    session_id,
    COUNT(*) as interactions,
    SUM(total_cost) as session_cost,
    AVG(response_time_seconds) as avg_response_time
FROM agent_usage
GROUP BY session_id
ORDER BY session_cost DESC;
```

#### C. Cost per Task Type
```sql
-- Cost breakdown by task type
SELECT 
    task_type,
    COUNT(*) as count,
    SUM(total_cost) as total_cost,
    AVG(total_cost) as avg_cost,
    MIN(total_cost) as min_cost,
    MAX(total_cost) as max_cost
FROM agent_usage
GROUP BY task_type
ORDER BY total_cost DESC;
```

---

### Option 5: Automated Reporting

**When to Use**: When you want regular reports, alerts, or scheduled analysis.

#### A. Daily/Weekly Reports
```bash
# Generate daily report
./scripts/analysis/generate-daily-report.sh

# Generate weekly report
./scripts/analysis/generate-weekly-report.sh
```

**Output:**
- Summary email or file
- Cost trends
- Top models/tasks
- Anomalies

#### B. Budget Alerts
```bash
# Alert when costs exceed threshold
./scripts/analysis/check-budget.sh --threshold 50.00
```

**Output:**
```
⚠️  Budget Alert: Monthly cost ($52.30) exceeds threshold ($50.00)
   Current projection: $65.00/month
   Recommendation: Review Opus usage (86% of costs)
```

#### C. Optimization Suggestions
```bash
# Suggest cost optimizations
./scripts/analysis/suggest-optimizations.sh
```

**Output:**
```
Optimization Suggestions:
  💡 Use Haiku for simple tasks (save ~73% on formatting tasks)
  💡 Current: 1 Opus task ($0.10) could use Sonnet ($0.02) - save $0.08
  💡 Projected monthly savings: $12.50
```

---

## Recommended Approach

### Phase 1: Current (Basic Analysis)
- ✅ Use `analyze-costs.sh` for basic aggregation
- ✅ Export to CSV for manual analysis
- ✅ Use Supabase SQL queries for complex analysis

### Phase 2: Enhanced Analysis (After Data Collection)
- Add trend analysis to `analyze-costs.sh`
- Add efficiency metrics
- Add comparison reports
- Add anomaly detection

### Phase 3: Visualization (After Phase 2)
- Set up Supabase dashboard (easiest)
- Or create simple HTML dashboard
- Or use Metabase if already set up

### Phase 4: Automation (After Phase 3)
- Daily/weekly reports
- Budget alerts
- Optimization suggestions

---

## Quick Start: SQL Analysis

**If you have Supabase configured:**

```bash
# Query via Supabase CLI
supabase db query "
SELECT 
    agent_model,
    SUM(total_cost) as total_cost,
    COUNT(*) as interactions
FROM agent_usage
GROUP BY agent_model
ORDER BY total_cost DESC;
"

# Or use PostgREST API
source scripts/lib/supabase-postgrest.sh
postgrest_query "agent_usage" "created_at=gte.2025-01-01" "agent_model,total_cost,created_at"
```

**If you only have local JSON:**

```bash
# Use analyze-costs.sh
./scripts/analysis/analyze-costs.sh --period all --source json

# Export to CSV for further analysis
./scripts/analysis/analyze-costs.sh --period all --source json --output csv > costs.csv
```

---

## Next Steps

1. **Test Supabase Sync** (when env vars available)
   - Verify data inserts correctly
   - Test SQL queries
   - Verify joins with other tables

2. **Collect Real Data** (1-2 weeks)
   - Start tracking costs in actual sessions
   - Build up historical data
   - Identify patterns

3. **Enhance Analysis** (after data collection)
   - Add trend analysis
   - Add efficiency metrics
   - Add comparison reports

4. **Add Visualization** (after enhanced analysis)
   - Set up Supabase dashboard
   - Or create HTML dashboard
   - Or use Metabase

5. **Automate** (after visualization)
   - Daily/weekly reports
   - Budget alerts
   - Optimization suggestions

---

## Related Documentation

- `docs/development/analysis/COST_ANALYSIS_SYSTEM.md` - Complete system documentation
- `docs/development/analysis/README.md` - Quick reference
- `scripts/analysis/analyze-costs.sh` - Analysis script
- `scripts/metrics/capture-agent-usage.sh` - Data collection script
- `supabase/migrations/20251121210227_create_agent_usage_table.sql` - Database schema

