-- Create moods table for mood tracking
-- This migration creates the initial schema for mood entries

-- Drop existing table if it exists (from previous failed migration attempts)
DROP TABLE IF EXISTS public.moods CASCADE;

-- Create moods table
CREATE TABLE public.moods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    score INTEGER NOT NULL CHECK (score >= 1 AND score <= 10),
    timestamp BIGINT NOT NULL,
    created_at BIGINT,
    updated_at BIGINT
);

-- Create index on user_id for faster queries
CREATE INDEX IF NOT EXISTS idx_moods_user_id ON public.moods(user_id);

-- Create index on timestamp for date range queries
CREATE INDEX IF NOT EXISTS idx_moods_timestamp ON public.moods(timestamp DESC);

-- Enable Row-Level Security
ALTER TABLE public.moods ENABLE ROW LEVEL SECURITY;

-- Create RLS policy: Users can only see their own moods
CREATE POLICY "Users can view own moods"
    ON public.moods
    FOR SELECT
    USING (auth.uid() = user_id);

-- Create RLS policy: Users can insert their own moods
CREATE POLICY "Users can insert own moods"
    ON public.moods
    FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- Create RLS policy: Users can update their own moods
CREATE POLICY "Users can update own moods"
    ON public.moods
    FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- Create RLS policy: Users can delete their own moods
CREATE POLICY "Users can delete own moods"
    ON public.moods
    FOR DELETE
    USING (auth.uid() = user_id);

-- Add comment to table
COMMENT ON TABLE public.moods IS 'Mood entries scoped to individual users';

