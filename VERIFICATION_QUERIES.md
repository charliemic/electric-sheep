# Supabase Dashboard Verification Queries

**Purpose**: SQL queries to verify test data exists in Supabase

---

## Quick Verification Query

Run this in the Supabase Dashboard SQL Editor to see all test users and their mood entry counts:

```sql
SELECT 
    u.email,
    COUNT(m.id) as entry_count,
    MIN(DATE(to_timestamp(m.timestamp / 1000))) as earliest_date,
    MAX(DATE(to_timestamp(m.timestamp / 1000))) as latest_date,
    AVG(m.score) as avg_score,
    MIN(m.score) as min_score,
    MAX(m.score) as max_score
FROM auth.users u
LEFT JOIN public.moods m ON m.user_id = u.id
WHERE u.email LIKE 'test-%@electric-sheep.test'
GROUP BY u.email
ORDER BY u.email;
```

**Expected Results:**
- 8 rows (one per test user)
- Each row should have `entry_count = 30` (30 days of data)
- `earliest_date` should be ~30 days ago
- `latest_date` should be yesterday (no partial day data)

---

## Detailed Verification Queries

### 1. Count Test Users

```sql
SELECT COUNT(*) as user_count
FROM auth.users
WHERE email LIKE 'test-%@electric-sheep.test';
```

**Expected**: `8`

---

### 2. Count Total Mood Entries

```sql
SELECT COUNT(*) as total_entries
FROM public.moods m
INNER JOIN auth.users u ON m.user_id = u.id
WHERE u.email LIKE 'test-%@electric-sheep.test';
```

**Expected**: `240` (8 users × 30 days)

---

### 3. List All Test Users

```sql
SELECT 
    id,
    email,
    created_at,
    email_confirmed_at
FROM auth.users
WHERE email LIKE 'test-%@electric-sheep.test'
ORDER BY email;
```

**Expected**: 8 users with emails:
- `test-novice-high-stable@electric-sheep.test`
- `test-novice-low-stable@electric-sheep.test`
- `test-novice-high-unstable@electric-sheep.test`
- `test-novice-low-unstable@electric-sheep.test`
- `test-savvy-high-stable@electric-sheep.test`
- `test-savvy-low-stable@electric-sheep.test`
- `test-savvy-high-unstable@electric-sheep.test`
- `test-savvy-low-unstable@electric-sheep.test`

---

### 4. Check Date Range of Data

```sql
SELECT 
    MIN(DATE(to_timestamp(timestamp / 1000))) as earliest_date,
    MAX(DATE(to_timestamp(timestamp / 1000))) as latest_date,
    COUNT(DISTINCT DATE(to_timestamp(timestamp / 1000))) as unique_days
FROM public.moods m
INNER JOIN auth.users u ON m.user_id = u.id
WHERE u.email LIKE 'test-%@electric-sheep.test';
```

**Expected**:
- `earliest_date`: ~30 days ago
- `latest_date`: Yesterday (not today)
- `unique_days`: 30

---

### 5. Verify Mood Score Patterns

```sql
SELECT 
    u.email,
    CASE 
        WHEN u.email LIKE '%-high-stable@%' THEN 'high_stable'
        WHEN u.email LIKE '%-low-stable@%' THEN 'low_stable'
        WHEN u.email LIKE '%-high-unstable@%' THEN 'high_unstable'
        WHEN u.email LIKE '%-low-unstable@%' THEN 'low_unstable'
    END as expected_pattern,
    AVG(m.score) as avg_score,
    MIN(m.score) as min_score,
    MAX(m.score) as max_score,
    STDDEV(m.score) as stddev_score
FROM auth.users u
INNER JOIN public.moods m ON m.user_id = u.id
WHERE u.email LIKE 'test-%@electric-sheep.test'
GROUP BY u.email
ORDER BY u.email;
```

**Expected Patterns**:
- `high_stable`: avg ~7-9, low stddev
- `low_stable`: avg ~2-4, low stddev
- `high_unstable`: avg ~4-10, high stddev
- `low_unstable`: avg ~1-6, high stddev

---

### 6. Check generate_mood_score Function

```sql
-- Check if function exists
SELECT 
    p.proname as function_name,
    pg_get_function_arguments(p.oid) as arguments,
    pg_get_function_result(p.oid) as return_type
FROM pg_proc p
JOIN pg_namespace n ON p.pronamespace = n.oid
WHERE n.nspname = 'public' 
AND p.proname = 'generate_mood_score';
```

**Expected**: 1 row with function definition

---

### 7. Test the Function

```sql
-- Test the function with different patterns
SELECT 
    'high_stable' as pattern,
    generate_mood_score('high_stable', 0, 0) as score
UNION ALL
SELECT 
    'low_stable',
    generate_mood_score('low_stable', 0, 0)
UNION ALL
SELECT 
    'high_unstable',
    generate_mood_score('high_unstable', 0, 0)
UNION ALL
SELECT 
    'low_unstable',
    generate_mood_score('low_unstable', 0, 0);
```

**Expected**: 4 rows with scores between 1-10

---

## How to Run in Supabase Dashboard

1. **Go to Supabase Dashboard**: https://supabase.com/dashboard
2. **Select your project**: Staging project (`rmcnvcqnowgsvvbmfssi`)
3. **Navigate to SQL Editor**: Left sidebar → SQL Editor
4. **Paste query**: Copy one of the queries above
5. **Run query**: Click "Run" or press Cmd/Ctrl + Enter
6. **Review results**: Check the output matches expected values

---

## Quick Verification Checklist

After running the queries, verify:

- [ ] 8 test users exist
- [ ] 240 total mood entries (8 × 30)
- [ ] Each user has exactly 30 entries
- [ ] Data dates range from ~30 days ago to yesterday
- [ ] `generate_mood_score()` function exists
- [ ] Mood scores match expected patterns

---

**Status**: Ready to verify in Supabase Dashboard

