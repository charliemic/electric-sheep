-- Create feature_flags table for remote feature flag management
-- This migration creates the schema for feature flags with support for future segmentation

-- Drop existing table if it exists
DROP TABLE IF EXISTS public.feature_flags CASCADE;

-- Create feature_flags table
CREATE TABLE public.feature_flags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key TEXT UNIQUE NOT NULL,
    value_type TEXT NOT NULL CHECK (value_type IN ('boolean', 'string', 'int')),
    boolean_value BOOLEAN,
    string_value TEXT,
    int_value INTEGER,
    enabled BOOLEAN DEFAULT true NOT NULL,
    
    -- Segmentation support: segment_id can link to a segments table in the future
    -- NULL means the flag applies to all users (global flag)
    segment_id UUID,
    
    -- User-specific flags: user_id can target specific users
    -- NULL means the flag applies to all users in the segment (or globally if segment_id is also NULL)
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    
    -- Metadata
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    
    -- Ensure at least one value is set based on value_type
    CONSTRAINT check_boolean_value CHECK (
        value_type != 'boolean' OR boolean_value IS NOT NULL
    ),
    CONSTRAINT check_string_value CHECK (
        value_type != 'string' OR string_value IS NOT NULL
    ),
    CONSTRAINT check_int_value CHECK (
        value_type != 'int' OR int_value IS NOT NULL
    ),
    
    -- Ensure only one value type is set
    CONSTRAINT check_single_value CHECK (
        (value_type = 'boolean' AND string_value IS NULL AND int_value IS NULL) OR
        (value_type = 'string' AND boolean_value IS NULL AND int_value IS NULL) OR
        (value_type = 'int' AND boolean_value IS NULL AND string_value IS NULL)
    )
);

-- Create indexes for fast lookups
CREATE INDEX IF NOT EXISTS idx_feature_flags_key ON public.feature_flags(key);
CREATE INDEX IF NOT EXISTS idx_feature_flags_segment_id ON public.feature_flags(segment_id);
CREATE INDEX IF NOT EXISTS idx_feature_flags_user_id ON public.feature_flags(user_id);
CREATE INDEX IF NOT EXISTS idx_feature_flags_enabled ON public.feature_flags(enabled);

-- Composite index for common query pattern: enabled flags by key and user
CREATE INDEX IF NOT EXISTS idx_feature_flags_key_user_enabled 
    ON public.feature_flags(key, user_id, enabled) 
    WHERE enabled = true;

-- Enable Row-Level Security
ALTER TABLE public.feature_flags ENABLE ROW LEVEL SECURITY;

-- RLS Policy: Users can read flags that apply to them
-- This includes:
-- 1. Global flags (user_id IS NULL AND segment_id IS NULL)
-- 2. Flags for their user_id
-- 3. Flags for their segment (when segments are implemented)
CREATE POLICY "Users can read applicable feature flags"
    ON public.feature_flags
    FOR SELECT
    USING (
        -- Global flags (no user, no segment)
        (user_id IS NULL AND segment_id IS NULL) OR
        -- User-specific flags
        (user_id = auth.uid()) OR
        -- Segment-based flags (when segments are implemented, this will need to check segment membership)
        -- For now, segment_id IS NOT NULL means it's not a global flag, so we exclude it unless user matches
        (segment_id IS NULL AND user_id IS NULL)
    );

-- RLS Policy: Service role can manage all flags (for CI/CD deployments)
-- This allows the service role (used by GitHub Actions) to insert/update/delete flags
CREATE POLICY "Service role can manage feature flags"
    ON public.feature_flags
    FOR ALL
    USING (auth.jwt() ->> 'role' = 'service_role')
    WITH CHECK (auth.jwt() ->> 'role' = 'service_role');

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_feature_flags_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to automatically update updated_at
CREATE TRIGGER update_feature_flags_updated_at
    BEFORE UPDATE ON public.feature_flags
    FOR EACH ROW
    EXECUTE FUNCTION update_feature_flags_updated_at();

-- Add comment to table
COMMENT ON TABLE public.feature_flags IS 'Feature flags for remote configuration. Supports global, user-specific, and future segment-based targeting.';
COMMENT ON COLUMN public.feature_flags.segment_id IS 'Future: Link to segments table for user segmentation. NULL = global flag.';
COMMENT ON COLUMN public.feature_flags.user_id IS 'User-specific flag. NULL = applies to all users (or all users in segment).';

