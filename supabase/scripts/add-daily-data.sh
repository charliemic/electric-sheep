#!/bin/bash
# Add one day's worth of mood data for test users (yesterday's data)
# This script is idempotent - safe to run multiple times
#
# Prerequisites:
#   - Supabase CLI installed and configured
#   - Database connection (via supabase link)
#
# Usage:
#   ./add-daily-data.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Adding daily mood data for test users...${NC}"

# Check if supabase is linked
if ! supabase status >/dev/null 2>&1; then
    echo -e "${RED}Error: Supabase is not linked. Run 'supabase link' first.${NC}"
    exit 1
fi

# SQL script to add yesterday's data
SQL_SCRIPT=$(cat <<'EOF'
-- Add yesterday's mood data for all test users
-- This script is idempotent - checks if data exists before inserting

DO $$
DECLARE
    test_user RECORD;
    yesterday_date DATE;
    yesterday_timestamp BIGINT;
    mood_entry_id UUID;
    mood_score INTEGER;
    pattern_name TEXT;
    user_exists BOOLEAN;
    data_exists BOOLEAN;
    entries_created INTEGER := 0;
    baseline_start_date DATE;
    calculated_day_offset INTEGER;
    
    -- Test users with their patterns
    test_users CURSOR FOR
        SELECT 
            'test-user-tech-novice-high-stable'::TEXT AS user_id,
            'test-novice-high-stable@electric-sheep.test'::TEXT AS email,
            'high_stable'::TEXT AS pattern
        UNION ALL
        SELECT 'test-user-tech-novice-low-stable', 'test-novice-low-stable@electric-sheep.test', 'low_stable'
        UNION ALL
        SELECT 'test-user-tech-novice-high-unstable', 'test-novice-high-unstable@electric-sheep.test', 'high_unstable'
        UNION ALL
        SELECT 'test-user-tech-novice-low-unstable', 'test-novice-low-unstable@electric-sheep.test', 'low_unstable'
        UNION ALL
        SELECT 'test-user-tech-savvy-high-stable', 'test-savvy-high-stable@electric-sheep.test', 'high_stable'
        UNION ALL
        SELECT 'test-user-tech-savvy-low-stable', 'test-savvy-low-stable@electric-sheep.test', 'low_stable'
        UNION ALL
        SELECT 'test-user-tech-savvy-high-unstable', 'test-savvy-high-unstable@electric-sheep.test', 'high_unstable'
        UNION ALL
        SELECT 'test-user-tech-savvy-low-unstable', 'test-savvy-low-unstable@electric-sheep.test', 'low_unstable';
BEGIN
    -- Calculate yesterday's date
    yesterday_date := CURRENT_DATE - INTERVAL '1 day';
    yesterday_timestamp := EXTRACT(EPOCH FROM yesterday_date::TIMESTAMP AT TIME ZONE 'UTC')::BIGINT * 1000;
    
    -- Calculate baseline start date (30 days ago)
    baseline_start_date := CURRENT_DATE - INTERVAL '30 days';
    
    RAISE NOTICE 'Adding mood data for date: % (timestamp: %)', yesterday_date, yesterday_timestamp;
    
    -- Process each test user
    FOR test_user IN test_users
    LOOP
        -- Check if user exists
        SELECT EXISTS(
            SELECT 1 FROM auth.users 
            WHERE id::TEXT = test_user.user_id
        ) INTO user_exists;
        
        IF NOT user_exists THEN
            RAISE WARNING 'User % does not exist. Skipping.', test_user.user_id;
            CONTINUE;
        END IF;
        
        -- Check if data already exists for yesterday
        SELECT EXISTS(
            SELECT 1 FROM public.moods
            WHERE user_id::TEXT = test_user.user_id
            AND DATE(to_timestamp(timestamp / 1000)) = yesterday_date
            LIMIT 1
        ) INTO data_exists;
        
        IF data_exists THEN
            RAISE NOTICE 'Data already exists for user % on %. Skipping.', test_user.email, yesterday_date;
            CONTINUE;
        END IF;
        
        -- Calculate day offset for pattern generation
        -- Use days since a fixed reference point (30 days ago) to ensure consistent patterns
        -- This ensures the pattern continues smoothly from baseline data
        calculated_day_offset := EXTRACT(DAY FROM (yesterday_date - baseline_start_date))::INTEGER;
        
        -- Generate mood score based on pattern
        -- Use deterministic seed based on user_id and date for reproducibility
        mood_score := generate_mood_score(
            test_user.pattern,
            calculated_day_offset,
            (EXTRACT(EPOCH FROM yesterday_date)::INTEGER + LENGTH(test_user.user_id)) % 1000000
        );
        
        -- Generate deterministic UUID based on user_id and timestamp
        mood_entry_id := gen_random_uuid();
        
        -- Insert mood entry (idempotent: ON CONFLICT DO NOTHING)
        INSERT INTO public.moods (
            id,
            user_id,
            score,
            timestamp,
            created_at,
            updated_at
        ) VALUES (
            mood_entry_id,
            test_user.user_id::UUID,
            mood_score,
            yesterday_timestamp,
            yesterday_timestamp,
            yesterday_timestamp
        )
        ON CONFLICT (id) DO NOTHING;
        
        entries_created := entries_created + 1;
        RAISE NOTICE 'Created entry for user %: score=%', test_user.email, mood_score;
    END LOOP;
    
    RAISE NOTICE 'Daily data update complete. Created % entries.', entries_created;
END $$;
EOF
)

# Execute SQL script
if command -v supabase >/dev/null 2>&1; then
    echo "$SQL_SCRIPT" | supabase db execute || {
        echo -e "${RED}Error: Failed to execute SQL script${NC}"
        exit 1
    }
else
    echo -e "${RED}Error: Supabase CLI not found${NC}"
    exit 1
fi

echo -e "${GREEN}Daily data update complete!${NC}"

