-- Seed script: Load baseline mood data (30 days) for test users
-- This script is idempotent - safe to run multiple times
--
-- It checks if data exists and only inserts missing data.
-- Data is generated up until yesterday (to avoid partial day data).

-- Ensure the generate_mood_score function exists
-- Note: Function should be created separately before running this script
-- Or include the function SQL inline here if needed

DO $$
DECLARE
    -- Test user configurations
    test_user RECORD;
    days_to_generate INTEGER := 30;
    target_date DATE;
    day_offset INTEGER;
    mood_entry_id UUID;
    mood_timestamp BIGINT;
    mood_score INTEGER;
    pattern_name TEXT;
    user_exists BOOLEAN;
    data_exists BOOLEAN;
    entries_created INTEGER := 0;
    
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
    RAISE NOTICE 'Loading baseline mood data for test users...';
    RAISE NOTICE 'Generating % days of data (up until yesterday)', days_to_generate;
    
    -- Process each test user
    FOR test_user IN test_users
    LOOP
        -- Check if user exists
        SELECT EXISTS(
            SELECT 1 FROM auth.users 
            WHERE id::TEXT = test_user.user_id
        ) INTO user_exists;
        
        IF NOT user_exists THEN
            RAISE WARNING 'User % does not exist. Skipping. Run create-test-users.sh first.', test_user.user_id;
            CONTINUE;
        END IF;
        
        -- Check if data already exists for this user
        SELECT EXISTS(
            SELECT 1 FROM public.moods
            WHERE user_id::TEXT = test_user.user_id
            LIMIT 1
        ) INTO data_exists;
        
        IF data_exists THEN
            RAISE NOTICE 'Data already exists for user %. Skipping.', test_user.user_id;
            CONTINUE;
        END IF;
        
        RAISE NOTICE 'Generating data for user: % (pattern: %)', test_user.email, test_user.pattern;
        
        -- Generate data for each day (starting from yesterday, going back)
        FOR day_offset IN 0..(days_to_generate - 1)
        LOOP
            -- Calculate target date (yesterday - day_offset)
            target_date := CURRENT_DATE - INTERVAL '1 day' - (day_offset || ' days')::INTERVAL;
            
            -- Generate timestamp (start of day in UTC, converted to milliseconds)
            mood_timestamp := EXTRACT(EPOCH FROM target_date::TIMESTAMP AT TIME ZONE 'UTC')::BIGINT * 1000;
            
            -- Generate mood score based on pattern
            mood_score := generate_mood_score(test_user.pattern, day_offset, day_offset);
            
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
                mood_timestamp,
                mood_timestamp,
                mood_timestamp
            )
            ON CONFLICT (id) DO NOTHING;
            
            entries_created := entries_created + 1;
        END LOOP;
        
        RAISE NOTICE 'Created % entries for user %', days_to_generate, test_user.email;
    END LOOP;
    
    RAISE NOTICE 'Baseline data loading complete. Created % total entries.', entries_created;
END $$;

COMMENT ON SCHEMA public IS 'Baseline mood data seed script. Idempotent - safe to run multiple times.';

