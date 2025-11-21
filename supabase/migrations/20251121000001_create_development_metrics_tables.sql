-- Create development metrics tables for tracking productivity metrics
-- This migration creates the schema for tracking PR cycle time, deployment frequency,
-- test pass rate, rule compliance, and documentation-first usage
-- 
-- ADR-001: Metrics live in metrics schema (not public) for separation of concerns
-- See: docs/architecture/DEVELOPMENT_METRICS_ARCHITECTURE.md
--
-- Prerequisites: Run 20251121000000_create_metrics_schema.sql first
--
-- Review: See docs/architecture/DEVELOPMENT_METRICS_MIGRATION_REVIEW.md for validation

-- Drop existing tables if they exist (for clean migration)
DROP TABLE IF EXISTS metrics.development_metrics CASCADE;
DROP TABLE IF EXISTS metrics.pr_events CASCADE;
DROP TABLE IF EXISTS metrics.deployment_events CASCADE;
DROP TABLE IF EXISTS metrics.test_runs CASCADE;
DROP TABLE IF EXISTS metrics.rule_compliance_events CASCADE;

-- ============================================================================
-- PR Events Table
-- Tracks PR lifecycle events (created, reviewed, merged, closed)
-- ============================================================================
CREATE TABLE metrics.pr_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pr_number INTEGER NOT NULL,
    pr_title TEXT,
    branch_name TEXT,
    event_type TEXT NOT NULL CHECK (event_type IN ('created', 'reviewed', 'merged', 'closed', 'reopened')),
    event_timestamp TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    
    -- PR metadata
    author TEXT,
    reviewer TEXT,
    labels TEXT[],
    
    -- Calculated fields (can be computed from events)
    cycle_time_seconds INTEGER CHECK (cycle_time_seconds IS NULL OR (cycle_time_seconds >= 0 AND cycle_time_seconds <= 31536000)), -- 0 to 1 year in seconds
    review_time_seconds INTEGER CHECK (review_time_seconds IS NULL OR (review_time_seconds >= 0 AND review_time_seconds <= 31536000)), -- 0 to 1 year in seconds
    
    -- Additional metadata
    metadata JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

-- Indexes for PR events
CREATE INDEX IF NOT EXISTS idx_pr_events_pr_number ON metrics.pr_events(pr_number);
CREATE INDEX IF NOT EXISTS idx_pr_events_event_type ON metrics.pr_events(event_type);
CREATE INDEX IF NOT EXISTS idx_pr_events_event_timestamp ON metrics.pr_events(event_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_pr_events_branch_name ON metrics.pr_events(branch_name);

-- Composite index for common queries
CREATE INDEX IF NOT EXISTS idx_pr_events_pr_type_timestamp 
    ON metrics.pr_events(pr_number, event_type, event_timestamp DESC);

-- Unique constraint to prevent duplicate events for same PR/type/timestamp
-- Allows same event type multiple times, but not at exact same timestamp
CREATE UNIQUE INDEX IF NOT EXISTS idx_pr_events_unique 
    ON metrics.pr_events(pr_number, event_type, event_timestamp);

-- ============================================================================
-- Deployment Events Table
-- Tracks deployment events (success, failure, rollback)
-- ============================================================================
CREATE TABLE metrics.deployment_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    deployment_id TEXT NOT NULL,
    environment TEXT NOT NULL CHECK (environment IN ('development', 'staging', 'production')),
    status TEXT NOT NULL CHECK (status IN ('success', 'failure', 'rollback')),
    deployed_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    
    -- Deployment metadata
    commit_sha TEXT NOT NULL, -- Required for traceability
    branch_name TEXT NOT NULL, -- Required for traceability
    pr_number INTEGER,
    deployed_by TEXT,
    
    -- Additional metadata
    metadata JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

-- Indexes for deployment events
CREATE INDEX IF NOT EXISTS idx_deployment_events_deployment_id ON metrics.deployment_events(deployment_id);
CREATE INDEX IF NOT EXISTS idx_deployment_events_environment ON metrics.deployment_events(environment);
CREATE INDEX IF NOT EXISTS idx_deployment_events_status ON metrics.deployment_events(status);
CREATE INDEX IF NOT EXISTS idx_deployment_events_deployed_at ON metrics.deployment_events(deployed_at DESC);
CREATE INDEX IF NOT EXISTS idx_deployment_events_pr_number ON metrics.deployment_events(pr_number);

-- ============================================================================
-- Test Runs Table
-- Tracks test execution results
-- ============================================================================
CREATE TABLE metrics.test_runs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    run_id TEXT NOT NULL,
    run_timestamp TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    
    -- Test results
    total_tests INTEGER NOT NULL DEFAULT 0,
    passed_tests INTEGER NOT NULL DEFAULT 0,
    failed_tests INTEGER NOT NULL DEFAULT 0,
    skipped_tests INTEGER NOT NULL DEFAULT 0,
    pass_rate DECIMAL(5, 2) GENERATED ALWAYS AS (
        CASE 
            WHEN total_tests > 0 THEN (passed_tests::DECIMAL / total_tests::DECIMAL * 100)
            ELSE 0
        END
    ) STORED,
    
    -- Execution metadata
    execution_time_seconds INTEGER CHECK (execution_time_seconds IS NULL OR execution_time_seconds >= 0), -- Must be non-negative
    test_type TEXT CHECK (test_type IN ('unit', 'integration', 'e2e', 'all')),
    branch_name TEXT,
    commit_sha TEXT,
    pr_number INTEGER,
    
    -- Additional metadata
    metadata JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    
    -- Constraints
    CONSTRAINT check_test_counts CHECK (
        total_tests >= 0 AND
        passed_tests >= 0 AND
        failed_tests >= 0 AND
        skipped_tests >= 0 AND
        (passed_tests + failed_tests + skipped_tests) <= total_tests
    )
);

-- Indexes for test runs
CREATE INDEX IF NOT EXISTS idx_test_runs_run_id ON metrics.test_runs(run_id);
CREATE INDEX IF NOT EXISTS idx_test_runs_run_timestamp ON metrics.test_runs(run_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_test_runs_test_type ON metrics.test_runs(test_type);
CREATE INDEX IF NOT EXISTS idx_test_runs_pass_rate ON metrics.test_runs(pass_rate);
CREATE INDEX IF NOT EXISTS idx_test_runs_pr_number ON metrics.test_runs(pr_number);

-- ============================================================================
-- Rule Compliance Events Table
-- Tracks rule compliance and usage
-- ============================================================================
CREATE TABLE metrics.rule_compliance_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_timestamp TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    
    -- Rule information
    rule_name TEXT NOT NULL,
    rule_category TEXT CHECK (rule_category IN ('foundation', 'implementation', 'specialized')),
    compliance_status TEXT NOT NULL CHECK (compliance_status IN ('compliant', 'violation', 'warning')),
    
    -- Context
    context_type TEXT CHECK (context_type IN ('commit', 'pr', 'code_review', 'manual_check')),
    context_id TEXT, -- PR number, commit SHA, etc. (optional - may not always have context)
    branch_name TEXT,
    
    -- Details
    violation_details TEXT,
    metadata JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

-- Indexes for rule compliance
CREATE INDEX IF NOT EXISTS idx_rule_compliance_rule_name ON metrics.rule_compliance_events(rule_name);
CREATE INDEX IF NOT EXISTS idx_rule_compliance_status ON metrics.rule_compliance_events(compliance_status);
CREATE INDEX IF NOT EXISTS idx_rule_compliance_event_timestamp ON metrics.rule_compliance_events(event_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_rule_compliance_category ON metrics.rule_compliance_events(rule_category);

-- Composite index for rule compliance queries
CREATE INDEX IF NOT EXISTS idx_rule_compliance_rule_status_timestamp 
    ON metrics.rule_compliance_events(rule_name, compliance_status, event_timestamp DESC);

-- ============================================================================
-- Development Metrics Summary Table (Optional - for aggregated metrics)
-- ============================================================================
CREATE TABLE metrics.development_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    metric_date DATE NOT NULL,
    metric_type TEXT NOT NULL CHECK (metric_type IN (
        'pr_cycle_time_avg',
        'pr_cycle_time_median',
        'deployment_frequency',
        'test_pass_rate_avg',
        'test_pass_rate_median',
        'rule_compliance_rate',
        'documentation_first_usage_rate'
    )),
    metric_value DECIMAL(10, 2) NOT NULL,
    metric_unit TEXT, -- 'seconds', 'count', 'percentage', etc.
    
    -- Aggregation metadata
    sample_size INTEGER,
    metadata JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    
    -- Unique constraint: one metric per type per day
    UNIQUE(metric_date, metric_type)
);

-- Indexes for development metrics
CREATE INDEX IF NOT EXISTS idx_development_metrics_date ON metrics.development_metrics(metric_date DESC);
CREATE INDEX IF NOT EXISTS idx_development_metrics_type ON metrics.development_metrics(metric_type);
CREATE INDEX IF NOT EXISTS idx_development_metrics_date_type 
    ON metrics.development_metrics(metric_date DESC, metric_type);

-- ============================================================================
-- Row-Level Security (RLS)
-- ============================================================================

-- PR Events RLS
ALTER TABLE metrics.pr_events ENABLE ROW LEVEL SECURITY;

-- Service role can manage all PR events (for CI/CD)
CREATE POLICY "Service role can manage pr_events"
    ON metrics.pr_events
    FOR ALL
    USING (auth.jwt() ->> 'role' = 'service_role')
    WITH CHECK (auth.jwt() ->> 'role' = 'service_role');

-- Authenticated users can read PR events
CREATE POLICY "Authenticated users can read pr_events"
    ON metrics.pr_events
    FOR SELECT
    USING (auth.role() = 'authenticated');

-- Deployment Events RLS
ALTER TABLE metrics.deployment_events ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Service role can manage deployment_events"
    ON metrics.deployment_events
    FOR ALL
    USING (auth.jwt() ->> 'role' = 'service_role')
    WITH CHECK (auth.jwt() ->> 'role' = 'service_role');

CREATE POLICY "Authenticated users can read deployment_events"
    ON metrics.deployment_events
    FOR SELECT
    USING (auth.role() = 'authenticated');

-- Test Runs RLS
ALTER TABLE metrics.test_runs ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Service role can manage test_runs"
    ON metrics.test_runs
    FOR ALL
    USING (auth.jwt() ->> 'role' = 'service_role')
    WITH CHECK (auth.jwt() ->> 'role' = 'service_role');

CREATE POLICY "Authenticated users can read test_runs"
    ON metrics.test_runs
    FOR SELECT
    USING (auth.role() = 'authenticated');

-- Rule Compliance Events RLS
ALTER TABLE metrics.rule_compliance_events ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Service role can manage rule_compliance_events"
    ON metrics.rule_compliance_events
    FOR ALL
    USING (auth.jwt() ->> 'role' = 'service_role')
    WITH CHECK (auth.jwt() ->> 'role' = 'service_role');

CREATE POLICY "Authenticated users can read rule_compliance_events"
    ON metrics.rule_compliance_events
    FOR SELECT
    USING (auth.role() = 'authenticated');

-- Development Metrics RLS
ALTER TABLE metrics.development_metrics ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Service role can manage development_metrics"
    ON metrics.development_metrics
    FOR ALL
    USING (auth.jwt() ->> 'role' = 'service_role')
    WITH CHECK (auth.jwt() ->> 'role' = 'service_role');

CREATE POLICY "Authenticated users can read development_metrics"
    ON metrics.development_metrics
    FOR SELECT
    USING (auth.role() = 'authenticated');

-- ============================================================================
-- Functions and Triggers
-- ============================================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_development_metrics_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for development_metrics (idempotent - can run multiple times)
DROP TRIGGER IF EXISTS update_development_metrics_updated_at ON metrics.development_metrics;
CREATE TRIGGER update_development_metrics_updated_at
    BEFORE UPDATE ON metrics.development_metrics
    FOR EACH ROW
    EXECUTE FUNCTION update_development_metrics_updated_at();

-- ============================================================================
-- Helper Functions for Metric Calculations
-- ============================================================================

-- Function to calculate average PR cycle time for a date range
CREATE OR REPLACE FUNCTION calculate_avg_pr_cycle_time(
    start_date DATE,
    end_date DATE
)
RETURNS DECIMAL(10, 2) AS $$
DECLARE
    avg_seconds DECIMAL(10, 2);
BEGIN
    SELECT AVG(cycle_time_seconds)
    INTO avg_seconds
    FROM metrics.pr_events
    WHERE event_type = 'merged'
        AND event_timestamp::DATE BETWEEN start_date AND end_date
        AND cycle_time_seconds IS NOT NULL;
    
    RETURN COALESCE(avg_seconds, 0);
END;
$$ LANGUAGE plpgsql;

-- Function to calculate deployment frequency for a date range
CREATE OR REPLACE FUNCTION calculate_deployment_frequency(
    start_date DATE,
    end_date DATE,
    env TEXT DEFAULT 'production'
)
RETURNS INTEGER AS $$
DECLARE
    deployment_count INTEGER;
BEGIN
    SELECT COUNT(*)
    INTO deployment_count
    FROM metrics.deployment_events
    WHERE environment = env
        AND status = 'success'
        AND deployed_at::DATE BETWEEN start_date AND end_date;
    
    RETURN COALESCE(deployment_count, 0);
END;
$$ LANGUAGE plpgsql;

-- Function to calculate average test pass rate for a date range
CREATE OR REPLACE FUNCTION calculate_avg_test_pass_rate(
    start_date DATE,
    end_date DATE
)
RETURNS DECIMAL(5, 2) AS $$
DECLARE
    avg_rate DECIMAL(5, 2);
BEGIN
    SELECT AVG(pass_rate)
    INTO avg_rate
    FROM metrics.test_runs
    WHERE run_timestamp::DATE BETWEEN start_date AND end_date;
    
    RETURN COALESCE(avg_rate, 0);
END;
$$ LANGUAGE plpgsql;

-- Function to calculate rule compliance rate for a date range
CREATE OR REPLACE FUNCTION calculate_rule_compliance_rate(
    start_date DATE,
    end_date DATE
)
RETURNS DECIMAL(5, 2) AS $$
DECLARE
    total_events INTEGER;
    compliant_events INTEGER;
    compliance_rate DECIMAL(5, 2);
BEGIN
    SELECT COUNT(*)
    INTO total_events
    FROM metrics.rule_compliance_events
    WHERE event_timestamp::DATE BETWEEN start_date AND end_date;
    
    SELECT COUNT(*)
    INTO compliant_events
    FROM metrics.rule_compliance_events
    WHERE event_timestamp::DATE BETWEEN start_date AND end_date
        AND compliance_status = 'compliant';
    
    IF total_events > 0 THEN
        compliance_rate := (compliant_events::DECIMAL / total_events::DECIMAL) * 100;
    ELSE
        compliance_rate := 0;
    END IF;
    
    RETURN compliance_rate;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- Comments
-- ============================================================================

COMMENT ON TABLE metrics.pr_events IS 'Tracks PR lifecycle events for calculating cycle time and review time';
COMMENT ON TABLE metrics.deployment_events IS 'Tracks deployment events for calculating deployment frequency';
COMMENT ON TABLE metrics.test_runs IS 'Tracks test execution results for calculating test pass rates';
COMMENT ON TABLE metrics.rule_compliance_events IS 'Tracks rule compliance events for calculating compliance rates';
COMMENT ON TABLE metrics.development_metrics IS 'Aggregated development metrics for dashboard and reporting';

COMMENT ON FUNCTION calculate_avg_pr_cycle_time IS 'Calculates average PR cycle time (created to merged) for a date range';
COMMENT ON FUNCTION calculate_deployment_frequency IS 'Calculates deployment frequency (count) for a date range and environment';
COMMENT ON FUNCTION calculate_avg_test_pass_rate IS 'Calculates average test pass rate (percentage) for a date range';
COMMENT ON FUNCTION calculate_rule_compliance_rate IS 'Calculates rule compliance rate (percentage) for a date range';
