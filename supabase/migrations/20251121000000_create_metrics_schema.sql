-- Create metrics schema for development metrics tracking
-- ADR-001: Metrics live in separate schema for separation of concerns
-- See: docs/architecture/DEVELOPMENT_METRICS_ARCHITECTURE.md

-- Create metrics schema
CREATE SCHEMA IF NOT EXISTS metrics;

-- Grant necessary permissions
GRANT USAGE ON SCHEMA metrics TO authenticated;
GRANT USAGE ON SCHEMA metrics TO service_role;
GRANT ALL ON SCHEMA metrics TO service_role;

-- Add comment
COMMENT ON SCHEMA metrics IS 'Schema for development metrics and analytics. Separated from public schema for clear architectural boundaries.';

