-- Function to generate a mood score based on pattern
-- This replicates the logic from TestUserFixtures.kt in SQL
-- 
-- Parameters:
--   pattern: mood pattern ('high_stable', 'low_stable', 'high_unstable', 'low_unstable')
--   day_offset: days ago from today (0 = today, 1 = yesterday, etc.)
--   seed: random seed for reproducibility (optional)
--
-- Returns: INTEGER score between 1 and 10

CREATE OR REPLACE FUNCTION generate_mood_score(
    pattern TEXT,
    day_offset INTEGER,
    seed INTEGER DEFAULT NULL
) RETURNS INTEGER AS $$
DECLARE
    base_score NUMERIC;
    variance NUMERIC;
    final_score INTEGER;
    random_seed INTEGER;
BEGIN
    -- Use seed if provided, otherwise use day_offset for deterministic generation
    random_seed := COALESCE(seed, day_offset);
    
    -- Set random seed for reproducibility
    PERFORM setseed(random_seed::NUMERIC / 1000000.0);
    
    CASE pattern
        WHEN 'high_stable' THEN
            -- High stable: 7-9 range, low variance
            base_score := 8.0;
            variance := (sin(day_offset * 0.1) * 0.5) + (random() * 0.5 - 0.25);
            
        WHEN 'low_stable' THEN
            -- Low stable: 2-4 range, low variance
            base_score := 3.0;
            variance := (sin(day_offset * 0.1) * 0.5) + (random() * 0.5 - 0.25);
            
        WHEN 'high_unstable' THEN
            -- High unstable: 4-10 range, high variance
            base_score := 7.0;
            variance := (sin(day_offset * 0.5) * 2.5) + (random() * 2.0 - 1.0);
            
        WHEN 'low_unstable' THEN
            -- Low unstable: 1-6 range, high variance
            base_score := 3.5;
            variance := (sin(day_offset * 0.5) * 2.5) + (random() * 2.0 - 1.0);
            
        ELSE
            -- Default: medium stable
            base_score := 5.0;
            variance := (random() * 1.0 - 0.5);
    END CASE;
    
    -- Calculate final score and clamp to valid range (1-10)
    final_score := GREATEST(1, LEAST(10, ROUND(base_score + variance)::INTEGER));
    
    RETURN final_score;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

COMMENT ON FUNCTION generate_mood_score IS 'Generates a mood score (1-10) based on pattern and day offset, replicating TestUserFixtures.kt logic';



