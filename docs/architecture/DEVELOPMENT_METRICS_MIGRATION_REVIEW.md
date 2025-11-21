# Development Metrics Migration Review

**Date**: 2025-11-21  
**Status**: Review Complete  
**Purpose**: Review metrics migration against existing patterns and common pitfalls

## Review Methodology

Reviewed:
1. Existing migrations (`moods`, `feature_flags`)
2. Insertion patterns (PostgREST, direct SQL)
3. Validation patterns (CHECK constraints, application validation)
4. Common pitfalls from past failures
5. RLS policy patterns

## Patterns Identified

### ✅ Good Patterns to Follow

1. **Migration Structure**
   - Use `DROP TABLE IF EXISTS ... CASCADE` for clean migrations
   - Use `CREATE INDEX IF NOT EXISTS` for indexes
   - Use `CREATE OR REPLACE FUNCTION` for functions
   - Use `ADD COLUMN IF NOT EXISTS` for adding columns

2. **Validation**
   - Use CHECK constraints for enum-like values
   - Use composite constraints for complex validation
   - Validate required fields with NOT NULL
   - Use DEFAULT values where appropriate

3. **RLS Policies**
   - Always enable RLS
   - Service role can manage all (for CI/CD)
   - Users can read their own data (for user-scoped data)
   - Use `auth.uid()` for user-scoped data
   - Use `auth.jwt() ->> 'role' = 'service_role'` for service role

4. **Triggers**
   - Use triggers for auto-updating timestamps
   - Use `BEFORE UPDATE` for timestamp updates
   - Handle existing triggers with `CREATE OR REPLACE`

5. **Functions**
   - Use `CREATE OR REPLACE FUNCTION` for idempotency
   - Use `COALESCE` for NULL handling
   - Return sensible defaults (0, empty, etc.)

## Common Pitfalls Found in Past Migrations

### 1. Null Value Handling ❌
**Issue**: YAML `null` becomes string `"null"`, not SQL `NULL`

**Example from feature flags**:
```sql
-- ❌ BAD: This would insert string "null"
INSERT INTO feature_flags (segment_id) VALUES ('null');

-- ✅ GOOD: Check for null explicitly
if [ "$SEGMENT_ID" = "null" ] || [ -z "$SEGMENT_ID" ]; then
    SEGMENT_ID_SQL="NULL"
fi
```

**Impact on metrics**: Not directly applicable (no YAML parsing), but good to know for future scripts.

### 2. Migration Order Dependencies ⚠️
**Issue**: Feature flags deployment ran before schema migration completed

**Fix**: Ensure migrations run in order, add dependency checks

**Impact on metrics**: ✅ Handled - schema creation migration runs first (20251121000000), tables migration runs second (20251121000001)

### 3. Missing Validation Constraints ⚠️
**Issue**: Some tables didn't have proper CHECK constraints

**Example from feature_flags**:
```sql
-- ✅ GOOD: Complex validation with composite constraint
CONSTRAINT check_single_value CHECK (
    (value_type = 'boolean' AND string_value IS NULL AND int_value IS NULL) OR
    (value_type = 'string' AND boolean_value IS NULL AND int_value IS NULL) OR
    (value_type = 'int' AND boolean_value IS NULL AND string_value IS NULL)
)
```

**Impact on metrics**: ✅ Good - metrics tables have CHECK constraints for enums

### 4. RLS Policy Completeness ✅
**Issue**: Missing policies for service role access

**Example from moods**:
```sql
-- ✅ GOOD: Service role can manage all
CREATE POLICY "Service role can manage moods"
    ON public.moods
    FOR ALL
    USING (auth.jwt() ->> 'role' = 'service_role')
    WITH CHECK (auth.jwt() ->> 'role' = 'service_role');
```

**Impact on metrics**: ✅ Good - all metrics tables have service role policies

### 5. Trigger Idempotency ✅
**Issue**: Triggers need to be idempotent (can run multiple times)

**Example from feature_flags**:
```sql
-- ✅ GOOD: CREATE OR REPLACE for function, separate CREATE TRIGGER
CREATE OR REPLACE FUNCTION update_feature_flags_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_feature_flags_updated_at
    BEFORE UPDATE ON public.feature_flags
    FOR EACH ROW
    EXECUTE FUNCTION update_feature_flags_updated_at();
```

**Impact on metrics**: ⚠️ Need to check - trigger might fail if exists

### 6. Function Schema Qualification ⚠️
**Issue**: Functions should be schema-qualified or in search_path

**Impact on metrics**: Functions are created without schema qualification - should work if `metrics` is in search_path, but better to be explicit

### 7. Index Naming Conflicts ⚠️
**Issue**: Index names might conflict if same name used in different schemas

**Impact on metrics**: ✅ Good - using `IF NOT EXISTS` prevents conflicts

## Issues Found in Metrics Migration

### Issue 1: Trigger Not Idempotent ⚠️
**Problem**: `CREATE TRIGGER` will fail if trigger already exists

**Current Code**:
```sql
CREATE TRIGGER update_development_metrics_updated_at
    BEFORE UPDATE ON metrics.development_metrics
    FOR EACH ROW
    EXECUTE FUNCTION update_development_metrics_updated_at();
```

**Fix**: Add `DROP TRIGGER IF EXISTS` before creating

### Issue 2: Function Schema Qualification ⚠️
**Problem**: Functions reference tables without schema qualification in some places

**Current Code**:
```sql
CREATE OR REPLACE FUNCTION calculate_avg_pr_cycle_time(...)
RETURNS DECIMAL(10, 2) AS $$
BEGIN
    SELECT AVG(cycle_time_seconds)
    INTO avg_seconds
    FROM metrics.pr_events  -- ✅ Good - schema qualified
    ...
END;
$$ LANGUAGE plpgsql;
```

**Status**: ✅ Actually good - functions do use schema qualification

### Issue 3: Missing Validation for Calculated Fields ⚠️
**Problem**: `cycle_time_seconds` and `review_time_seconds` can be negative or unreasonably large

**Current Code**:
```sql
cycle_time_seconds INTEGER, -- Time from created to merged
review_time_seconds INTEGER, -- Time from created to first review
```

**Fix**: Add CHECK constraints for reasonable ranges

### Issue 4: Missing Unique Constraints ⚠️
**Problem**: Could have duplicate PR events for same PR number and event type

**Current Code**:
```sql
pr_number INTEGER NOT NULL,
event_type TEXT NOT NULL CHECK (event_type IN (...)),
```

**Fix**: Consider unique constraint on (pr_number, event_type, event_timestamp) or add logic to prevent duplicates

### Issue 5: Missing NOT NULL on Required Fields ⚠️
**Problem**: Some fields that should be required are nullable

**Current Code**:
```sql
deployment_id TEXT NOT NULL,  -- ✅ Good
commit_sha TEXT,  -- ⚠️ Should this be required?
branch_name TEXT,  -- ⚠️ Should this be required?
```

**Fix**: Review which fields should be NOT NULL

### Issue 6: Missing Defaults for Optional Fields ✅
**Status**: ✅ Good - optional fields have appropriate defaults or are nullable

## Recommended Fixes

### Fix 1: Make Triggers Idempotent
```sql
DROP TRIGGER IF EXISTS update_development_metrics_updated_at ON metrics.development_metrics;
CREATE TRIGGER update_development_metrics_updated_at
    BEFORE UPDATE ON metrics.development_metrics
    FOR EACH ROW
    EXECUTE FUNCTION update_development_metrics_updated_at();
```

### Fix 2: Add Validation for Calculated Fields
```sql
cycle_time_seconds INTEGER CHECK (cycle_time_seconds IS NULL OR (cycle_time_seconds >= 0 AND cycle_time_seconds <= 31536000)), -- 0 to 1 year in seconds
review_time_seconds INTEGER CHECK (review_time_seconds IS NULL OR (review_time_seconds >= 0 AND review_time_seconds <= 31536000)),
```

### Fix 3: Add Unique Constraint for PR Events (Optional)
```sql
-- Prevent duplicate events for same PR/type/timestamp
CREATE UNIQUE INDEX IF NOT EXISTS idx_pr_events_unique 
    ON metrics.pr_events(pr_number, event_type, event_timestamp);
```

### Fix 4: Review Required Fields
Decide which fields should be NOT NULL:
- `commit_sha` in deployment_events? (probably yes for production)
- `branch_name` in deployment_events? (probably yes)
- `context_id` in rule_compliance_events? (probably optional)

## Validation Checklist

- [x] Uses `DROP TABLE IF EXISTS ... CASCADE`
- [x] Uses `CREATE INDEX IF NOT EXISTS`
- [x] Uses `CREATE OR REPLACE FUNCTION`
- [x] Has CHECK constraints for enums
- [x] Has RLS policies for all tables
- [x] Has service role policies for CI/CD
- [x] Has triggers for auto-updates
- [ ] **Trigger is idempotent** (needs fix)
- [x] Functions use schema qualification
- [ ] **Has validation for calculated fields** (needs fix)
- [ ] **Has unique constraints where needed** (optional)
- [ ] **Required fields are NOT NULL** (needs review)

## Related Documentation

- `docs/archive/development/SUPABASE_MIGRATION_FAILURE_ANALYSIS.md` - Past migration failures
- `docs/architecture/FEATURE_FLAG_MANAGEMENT_EVALUATION.md` - Upsert patterns
- `supabase/migrations/20241117000000_create_moods_table.sql` - Reference migration
- `supabase/migrations/20251118130744_create_feature_flags_table.sql` - Reference migration

