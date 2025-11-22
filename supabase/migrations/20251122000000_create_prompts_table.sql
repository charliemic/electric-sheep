-- Create prompts table for tracking user prompts and AI interactions
-- This table stores all prompts automatically captured during development

-- Drop existing table if it exists
DROP TABLE IF EXISTS public.prompts CASCADE;

-- Create prompts table
CREATE TABLE public.prompts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Prompt content
    prompt TEXT NOT NULL,
    prompt_length INTEGER NOT NULL,
    word_count INTEGER NOT NULL,
    
    -- Link to agent usage (optional - can track standalone)
    agent_usage_id UUID REFERENCES public.agent_usage(id) ON DELETE SET NULL,
    session_id TEXT,
    
    -- Task context
    task_type TEXT,              -- e.g., "feature_implementation", "bug_fix", "documentation"
    task_complexity TEXT,        -- e.g., "simple", "standard", "complex"
    
    -- Metadata
    source TEXT DEFAULT 'auto', -- e.g., "auto", "manual", "git", "cursor"
    estimated BOOLEAN DEFAULT false,
    
    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

-- Create indexes for common queries
CREATE INDEX IF NOT EXISTS idx_prompts_created_at ON public.prompts(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_prompts_session_id ON public.prompts(session_id);
CREATE INDEX IF NOT EXISTS idx_prompts_task_type ON public.prompts(task_type);
CREATE INDEX IF NOT EXISTS idx_prompts_source ON public.prompts(source);

-- Composite index for session analysis
CREATE INDEX IF NOT EXISTS idx_prompts_session_date 
    ON public.prompts(session_id, created_at DESC);

-- Composite index for task analysis
CREATE INDEX IF NOT EXISTS idx_prompts_task_date
    ON public.prompts(task_type, created_at DESC);

-- Enable Row-Level Security
ALTER TABLE public.prompts ENABLE ROW LEVEL SECURITY;

-- RLS Policy: Service role can manage all prompts (for CI/CD and admin operations)
CREATE POLICY "Service role can manage prompts"
    ON public.prompts
    FOR ALL
    USING (auth.jwt() ->> 'role' = 'service_role')
    WITH CHECK (auth.jwt() ->> 'role' = 'service_role');

-- RLS Policy: Authenticated users can read prompts (for analysis)
CREATE POLICY "Authenticated users can read prompts"
    ON public.prompts
    FOR SELECT
    USING (auth.role() = 'authenticated');

-- Add comments
COMMENT ON TABLE public.prompts IS 'Tracks all user prompts and AI interactions for analysis';
COMMENT ON COLUMN public.prompts.agent_usage_id IS 'Optional link to agent_usage table for cost analysis';
COMMENT ON COLUMN public.prompts.session_id IS 'Session identifier for grouping related prompts';
COMMENT ON COLUMN public.prompts.source IS 'Source of prompt: auto (automatic capture), manual (explicit capture), git (from commits), cursor (from Cursor database)';
