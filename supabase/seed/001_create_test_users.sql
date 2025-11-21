-- Seed script: Create test users in Supabase Auth
-- 
-- NOTE: This script documents the test users that should exist.
-- Actual user creation must be done via Supabase Admin API (not SQL).
-- See supabase/scripts/create-test-users.sh for the implementation.
--
-- This SQL script is for reference and can be used to verify users exist.

-- Test user definitions (version-controlled)
-- These match TestUserFixtures.UserIds in the app code

-- Verify test users exist (idempotent check)
-- This will fail if users don't exist, which is expected on first run
-- The create-test-users.sh script will create them via Admin API

DO $$
DECLARE
    test_users TEXT[] := ARRAY[
        'test-user-tech-novice-high-stable',
        'test-user-tech-novice-low-stable',
        'test-user-tech-novice-high-unstable',
        'test-user-tech-novice-low-unstable',
        'test-user-tech-savvy-high-stable',
        'test-user-tech-savvy-low-stable',
        'test-user-tech-savvy-high-unstable',
        'test-user-tech-savvy-low-unstable'
    ];
    user_id TEXT;
    user_exists BOOLEAN;
BEGIN
    RAISE NOTICE 'Checking for test users in auth.users...';
    
    FOREACH user_id IN ARRAY test_users
    LOOP
        -- Check if user exists in auth.users
        SELECT EXISTS(
            SELECT 1 FROM auth.users 
            WHERE id::TEXT = user_id
        ) INTO user_exists;
        
        IF user_exists THEN
            RAISE NOTICE 'Test user exists: %', user_id;
        ELSE
            RAISE WARNING 'Test user missing: % (run create-test-users.sh to create)', user_id;
        END IF;
    END LOOP;
    
    RAISE NOTICE 'Test user check complete. Use create-test-users.sh to create missing users.';
END $$;

COMMENT ON SCHEMA public IS 'Test users are created via Supabase Admin API, not SQL. See supabase/scripts/create-test-users.sh';



