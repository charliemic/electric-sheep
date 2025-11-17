# Database Migration Checklist

This checklist helps ensure migrations are properly implemented and tested before pushing.

## Pre-Push Checklist

### Migration Code
- [x] Migration is forward-only (never rollback)
- [x] Migration handles existing data gracefully
- [x] Migration creates necessary indexes
- [x] Migration updates database version in `AppDatabase.kt`
- [x] Migration is added to `DatabaseMigrations.getMigrations()`
- [x] **No manual column existence checks** - Room guarantees migrations run exactly once
- [x] **No raw Cursor usage** - Use Room's migration APIs only

### Testing
- [ ] Test migration on fresh database (version 2)
- [ ] Test migration on existing database (version 1 → 2)
- [ ] Verify no data loss during migration
- [ ] Verify migration handles edge cases (empty tables, null values)

### CI/CD
- [x] BuildConfig fields are configured (if needed)
- [x] CI pipeline will build successfully
- [x] No special CI configuration needed (Room handles migrations automatically)

### Code Integration
- [x] All queries updated to use new schema
- [x] Data models match new schema
- [x] Repository methods handle new fields
- [x] UI components handle authentication state

## Current Migration: v1 → v2

**Changes:**
- Added `userId` column to `moods` table
- Created index on `userId` for query performance
- Set placeholder value for existing data

**Status:** ✅ Ready to push

**Notes:**
- Migration is idempotent (safe if column already exists)
- Existing data gets `userId = 'placeholder_user'`
- New databases created at version 2 will have non-nullable `userId`
- Application code validates `userId` is not blank

## Migration Execution

Room automatically runs migrations when:
1. App starts and database version is lower than current
2. Database is opened for the first time after version change
3. No manual intervention needed

**Failure Handling:**
- If migration fails, app will crash on startup (by design)
- `fallbackToDestructiveMigrationOnUpgrade(false)` prevents data loss
- Migration errors are logged with full stack trace

## CI/CD Considerations

**No special configuration needed:**
- Room migrations run automatically during app startup
- CI builds don't need database setup
- Instrumented tests will run migrations automatically
- Unit tests use in-memory databases (no migration needed)

**BuildConfig:**
- `OFFLINE_ONLY_MODE` is configured in `build.gradle.kts`
- Available in both debug and release builds
- Defaults to `false` (can be overridden per build type)

