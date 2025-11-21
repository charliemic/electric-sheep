-- Create agent_usage table for tracking AI agent interactions and costs
-- This table tracks agent usage alongside prompt metrics for cost analysis

-- Drop existing table if it exists
DROP TABLE IF EXISTS public.agent_usage CASCADE;

-- Create agent_usage table
CREATE TABLE public.agent_usage (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Link to prompt/metrics (optional - can track standalone)
    prompt_id TEXT,
    session_id TEXT,
    
    -- Agent information
    agent_company TEXT NOT NULL,  -- e.g., "Anthropic"
    agent_model TEXT NOT NULL,    -- e.g., "claude-3-5-sonnet"
    agent_model_id TEXT NOT NULL, -- e.g., "anthropic.claude-sonnet-4-5-20250929-v1:0"
    agent_provider TEXT NOT NULL, -- e.g., "AWS Bedrock"
    agent_region TEXT,            -- e.g., "eu-west-1"
    
    -- Usage metrics
    input_tokens INTEGER NOT NULL,
    output_tokens INTEGER NOT NULL,
    total_tokens INTEGER NOT NULL GENERATED ALWAYS AS (input_tokens + output_tokens) STORED,
    
    -- Cost information
    input_cost_per_million NUMERIC(10, 4) NOT NULL,  -- Price per million input tokens
    output_cost_per_million NUMERIC(10, 4) NOT NULL, -- Price per million output tokens
    input_cost NUMERIC(10, 6) NOT NULL,              -- Calculated input cost
    output_cost NUMERIC(10, 6) NOT NULL,             -- Calculated output cost
    total_cost NUMERIC(10, 6) NOT NULL GENERATED ALWAYS AS (input_cost + output_cost) STORED,
    currency TEXT DEFAULT 'USD' NOT NULL,
    
    -- Task context
    task_type TEXT,              -- e.g., "feature_implementation", "bug_fix", "documentation"
    task_complexity TEXT,        -- e.g., "simple", "standard", "complex"
    task_description TEXT,       -- Brief description of the task
    
    -- Metadata
    prompt_length INTEGER,      -- Prompt length in characters
    response_time_seconds NUMERIC(6, 2), -- Response time in seconds
    success BOOLEAN DEFAULT true,
    
    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

-- Create indexes for common queries
CREATE INDEX IF NOT EXISTS idx_agent_usage_created_at ON public.agent_usage(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_agent_usage_session_id ON public.agent_usage(session_id);
CREATE INDEX IF NOT EXISTS idx_agent_usage_model ON public.agent_usage(agent_model);
CREATE INDEX IF NOT EXISTS idx_agent_usage_company ON public.agent_usage(agent_company);
CREATE INDEX IF NOT EXISTS idx_agent_usage_provider ON public.agent_usage(agent_provider);
CREATE INDEX IF NOT EXISTS idx_agent_usage_task_type ON public.agent_usage(task_type);

-- Composite index for cost analysis queries
CREATE INDEX IF NOT EXISTS idx_agent_usage_model_date 
    ON public.agent_usage(agent_model, created_at DESC);

-- Composite index for company/provider analysis
CREATE INDEX IF NOT EXISTS idx_agent_usage_company_provider_date
    ON public.agent_usage(agent_company, agent_provider, created_at DESC);

-- Enable Row-Level Security
ALTER TABLE public.agent_usage ENABLE ROW LEVEL SECURITY;

-- RLS Policy: Service role can manage all agent usage (for CI/CD and admin operations)
-- This allows the service role (used by scripts) to insert/select/update/delete agent usage
CREATE POLICY "Service role can manage agent usage"
    ON public.agent_usage
    FOR ALL
    USING (auth.jwt() ->> 'role' = 'service_role')
    WITH CHECK (auth.jwt() ->> 'role' = 'service_role');

-- RLS Policy: Authenticated users can read agent usage (for analysis)
-- In the future, we might scope this to user_id if we add user tracking
CREATE POLICY "Authenticated users can read agent usage"
    ON public.agent_usage
    FOR SELECT
    USING (auth.role() = 'authenticated');

-- Add comments
COMMENT ON TABLE public.agent_usage IS 'Tracks AI agent interactions, usage, and costs for cost analysis and optimization';
COMMENT ON COLUMN public.agent_usage.prompt_id IS 'Optional link to prompt metrics (if tracked separately)';
COMMENT ON COLUMN public.agent_usage.session_id IS 'Session identifier for grouping related interactions';
COMMENT ON COLUMN public.agent_usage.total_tokens IS 'Computed column: input_tokens + output_tokens';
COMMENT ON COLUMN public.agent_usage.total_cost IS 'Computed column: input_cost + output_cost';

