-- Add versioning support to feature_flags table
-- This migration adds a version field for tracking flag changes

-- Add version column (defaults to 1 for existing flags)
ALTER TABLE public.feature_flags 
ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 1 NOT NULL;

-- Create index on version for efficient version checks
CREATE INDEX IF NOT EXISTS idx_feature_flags_version ON public.feature_flags(version);

-- Update existing flags to version 1 if they don't have a version
UPDATE public.feature_flags 
SET version = 1 
WHERE version IS NULL;

-- Add comment
COMMENT ON COLUMN public.feature_flags.version IS 'Version number for this flag. Incremented when flag is updated. Used for cache invalidation.';

-- Function to increment version on update
CREATE OR REPLACE FUNCTION increment_feature_flag_version()
RETURNS TRIGGER AS $$
BEGIN
    -- Only increment if any actual flag value changed
    IF (OLD.boolean_value IS DISTINCT FROM NEW.boolean_value) OR
       (OLD.string_value IS DISTINCT FROM NEW.string_value) OR
       (OLD.int_value IS DISTINCT FROM NEW.int_value) OR
       (OLD.enabled IS DISTINCT FROM NEW.enabled) THEN
        NEW.version = OLD.version + 1;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to auto-increment version on update
CREATE TRIGGER increment_feature_flag_version_trigger
    BEFORE UPDATE ON public.feature_flags
    FOR EACH ROW
    EXECUTE FUNCTION increment_feature_flag_version();

