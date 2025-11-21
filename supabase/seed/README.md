# Test Data Seed Scripts

This directory contains seed scripts for loading test fixture data into Supabase.

## Structure

```
seed/
├── functions/              # Reusable SQL functions
│   └── generate_mood_score.sql
├── 001_create_test_users.sql      # Verify test users exist
├── 002_load_baseline_mood_data.sql # Load initial 30 days of data
└── README.md              # This file
```

## Scripts

### 001_create_test_users.sql

**Purpose**: Verify test users exist in Supabase Auth

**Note**: This script only checks for users. Actual user creation must be done via Supabase Admin API using `supabase/scripts/create-test-users.sh`.

**Why**: Supabase Auth users cannot be created via SQL - they must be created through the Auth Admin API.

### 002_load_baseline_mood_data.sql

**Purpose**: Load initial 30 days of mood data for all test users

**Features**:
- Idempotent (safe to run multiple times)
- Only inserts if data doesn't exist
- Generates data up until yesterday (avoids partial day data)
- Uses `generate_mood_score()` function for realistic patterns

**Usage**:
```bash
# Via Supabase CLI
supabase db execute < seed/002_load_baseline_mood_data.sql

# Or via psql
psql $DATABASE_URL < seed/002_load_baseline_mood_data.sql
```

## Functions

### generate_mood_score(pattern, day_offset, seed)

Generates a mood score (1-10) based on pattern and day offset.

**Parameters**:
- `pattern`: Mood pattern (`'high_stable'`, `'low_stable'`, `'high_unstable'`, `'low_unstable'`)
- `day_offset`: Days ago from today (0 = today, 1 = yesterday, etc.)
- `seed`: Random seed for reproducibility (optional)

**Returns**: INTEGER score between 1 and 10

**Patterns**:
- `high_stable`: 7-9 range, low variance
- `low_stable`: 2-4 range, low variance
- `high_unstable`: 4-10 range, high variance
- `low_unstable`: 1-6 range, high variance

## Test Users

All test users follow the pattern: `test-{tech-level}-{mood-pattern}@electric-sheep.test`

**Tech Levels**: `novice`, `savvy`  
**Mood Patterns**: `high-stable`, `low-stable`, `high-unstable`, `low-unstable`

**Total**: 8 test users (2 tech levels × 4 mood patterns)

## Running Seed Scripts

### Option 1: Use seed-test-data.sh (Recommended)

```bash
# Set environment variables
export SUPABASE_URL=https://xxx.supabase.co
export SUPABASE_SERVICE_ROLE_KEY=sb_secret_xxx

# Run all seed scripts
./supabase/scripts/seed-test-data.sh
```

### Option 2: Manual Execution

```bash
# 1. Create test users (via Admin API)
./supabase/scripts/create-test-users.sh

# 2. Load baseline data (via SQL)
supabase db execute < supabase/seed/002_load_baseline_mood_data.sql
```

## Idempotency

All seed scripts are **idempotent** - safe to run multiple times:

- **User creation**: Checks if user exists before creating
- **Data loading**: Checks if data exists before inserting
- **Daily updates**: Checks if yesterday's data exists before adding

## Data Freshness

- **Baseline data**: 30 days of historical data (up until yesterday)
- **Daily updates**: Adds yesterday's data each day (via nightly CI/CD job)
- **Always current**: Data is always up until yesterday (avoids partial day data)

## Related Documentation

- `supabase/scripts/` - Executable scripts for seeding
- `.github/workflows/test-data-nightly-update.yml` - Nightly data update job
- `docs/architecture/TEST_FIXTURES_ARCHITECTURE.md` - Architecture documentation
- `app/src/main/java/com/electricsheep/app/data/fixtures/` - Client-side test fixtures



