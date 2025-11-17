# Data Layer Architecture

This document describes the offline-first data layer architecture for the Electric Sheep Android app.

## Overview

The data layer follows an **offline-first** pattern where:
- **Local storage (Room)** is the single source of truth
- **Remote storage (Supabase)** is used for sync and backup
- Data is always available offline
- Sync happens in the background when online

## Architecture Layers

```
┌─────────────────────────────────────┐
│         UI Layer (Compose)          │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      MoodRepository                 │
│  (Business Logic & Sync)            │
└──────┬──────────────────┬───────────┘
       │                  │
┌──────▼──────┐   ┌──────▼───────────┐
│   Local     │   │     Remote       │
│  DataSource │   │   DataSource     │
└──────┬──────┘   └──────┬───────────┘
       │                  │
┌──────▼──────┐   ┌──────▼───────────┐
│    Room     │   │    Supabase      │
│  Database   │   │     Client       │
└─────────────┘   └──────────────────┘
```

## Components

### 1. Data Models (`data/model/`)

**Mood.kt**: Core data model representing a mood entry
- `id`: Unique identifier (UUID string)
- `score`: Numerical mood score (1-10)
- `timestamp`: When mood was recorded (Unix timestamp in milliseconds)
- `createdAt`: Record creation time
- `updatedAt`: Last update time

**Validation**: The model includes `isValid()` method to ensure data integrity.

### 2. Local Data Source (`data/local/`)

**MoodDao.kt**: Room DAO interface for local database operations
- Direct interface to Room database
- Provides Flow-based reactive queries
- Handles CRUD operations
- Supports date range queries
- All queries scoped to userId for security

**AppDatabase.kt**: Room database definition
- Defines database schema
- Manages migrations
- Provides DAO access

### 3. Remote Data Source (`data/remote/`)

**SupabaseDataSource.kt**: Supabase client wrapper
- Handles all remote operations
- Provides error handling and logging
- Supports CRUD and bulk operations (upsert)

### 4. Repository (`data/repository/`)

**MoodRepository.kt**: Main data access point
- Implements offline-first pattern
- Handles sync between local and remote
- Provides Result-based error handling
- Always saves locally first, syncs remotely in background

**Key Methods**:
- `getAllMoods()`: Get all moods (from local)
- `saveMood()`: Save mood (local first, then remote)
- `updateMood()`: Update mood (local first, then remote)
- `deleteMood()`: Delete mood (local first, then remote)
- `syncWithRemote()`: Pull latest from remote and merge

### 5. Migration System (`data/migration/`)

**DatabaseMigration.kt**: Forward-only migration management
- All migrations are forward-only (never rollback)
- Migrations are applied sequentially
- Includes validation to ensure migration sequence is complete

**Adding a Migration**:
1. Create migration object: `MIGRATION_X_Y`
2. Add to `getMigrations()` array
3. Update database version in `AppDatabase.kt`
4. Test migration thoroughly

**Migration Best Practices**:
- **Trust Room's Migration System**: Room guarantees migrations run exactly once in order
- **No Column Existence Checks**: Don't check if columns exist - Room handles this automatically
- **No Raw Cursor Usage**: Use `SupportSQLiteDatabase.execSQL()` only, avoid raw Cursor queries
- **Idempotent SQL**: Use `IF NOT EXISTS` for indexes/constraints where possible
- **Forward-Only**: Never create rollback migrations
- **Test Thoroughly**: Test on both fresh databases and existing databases

### 6. Data Module (`data/DataModule.kt`)

**DataModule.kt**: Dependency injection and setup
- Creates Supabase client
- Creates Room database
- Wires up data sources and repository
- TODO: Move Supabase credentials to BuildConfig

## Data Flow

### Saving a Mood Entry

1. User creates mood entry in UI
2. UI calls `repository.saveMood(mood)`
3. Repository validates mood data
4. Repository generates UUID if new entry
5. **Repository saves to local storage** (Room)
6. Repository attempts to sync to remote (non-blocking)
7. If remote sync fails, mood is still saved locally
8. Sync will retry later (to be implemented)

### Reading Mood Entries

1. UI observes `repository.getAllMoods()` (Flow)
2. Repository returns Flow from local data source
3. Local data source queries Room database
4. UI receives updates reactively
5. Background sync can update local data, triggering Flow updates

### Syncing with Remote

1. App calls `repository.syncWithRemote()`
2. Repository fetches all moods from Supabase
3. Repository merges remote data into local storage
4. Local storage updates trigger Flow emissions
5. UI automatically receives updated data

## Offline-First Guarantees

1. **Local is always available**: All reads come from local storage
2. **Writes are immediate**: Saves happen locally first
3. **Remote is optional**: App works fully offline
4. **Sync is background**: Remote operations don't block UI
5. **Failures are graceful**: Remote failures don't affect local saves

## Error Handling

- **Validation errors**: Return `Result.failure` with descriptive message
- **Local errors**: Propagate immediately (database issues are critical)
- **Remote errors**: Log warning, continue with local data
- **Network errors**: Silent failure, will retry on next sync

## Testing Strategy

Following the hourglass pattern:

### Unit Tests (Many)
- `MoodTest`: Model validation logic
- `MoodRepositoryTest`: Repository logic, offline-first behaviour
- Mock data sources to test repository in isolation

### Integration Tests (Thin)
- Test Room database operations
- Test Supabase client operations
- Test data source implementations

### Stack Tests (Wide)
- End-to-end tests with real database
- Test sync behaviour
- Test offline/online scenarios

## Background Sync

### Sync Configuration

Background sync is configured via `SyncConfig`:
- **Default interval**: 15 minutes
- **Minimum interval**: 5 minutes (prevents excessive syncing)
- **Maximum interval**: 60 minutes (ensures data freshness)
- **Configurable**: Can be customised per installation

### Sync Strategy

The sync process follows a bidirectional approach:

1. **Push Phase**: All local moods are upserted to remote
2. **Pull Phase**: All remote moods are fetched
3. **Merge Phase**: Local and remote moods are merged with conflict resolution

### Conflict Resolution

Since only this app can create/edit moods, conflicts only occur when:
- Multiple edits are made offline to the same mood
- The same mood exists in both local and remote with different data

**Resolution Strategy**: Latest edit wins (based on `updatedAt` timestamp)
- If `updatedAt` is null, falls back to `createdAt`
- The mood with the newer timestamp is kept
- Conflicts are logged for debugging

### WorkManager Integration

- **MoodSyncWorker**: Background worker that performs sync
- **SyncManager**: Manages periodic sync scheduling
- **WorkerFactory**: Provides dependency injection for workers
- **Automatic Start**: Sync starts automatically when app launches

### Sync Lifecycle

1. App starts → `ElectricSheepApplication.onCreate()`
2. Data layer initialised
3. WorkManager configured with custom factory
4. Periodic sync scheduled (15-minute interval by default)
5. Sync runs when:
   - Scheduled interval elapses
   - Network is available
   - App is running or in background

## Future Enhancements

1. **Real-time Updates**: Use Supabase real-time subscriptions for instant sync
2. **Offline Queue**: Queue failed remote operations for retry
3. **Data Encryption**: Encrypt sensitive local data
4. **Backup/Restore**: Export/import local data
5. **Sync Status UI**: Show sync status and last sync time to user

## Configuration

### Supabase Setup (TODO)

Currently, Supabase credentials are hardcoded in `DataModule.kt`. To configure:

1. Create Supabase project
2. Get project URL and anon key
3. Add to `local.properties` or `BuildConfig`:
   ```
   SUPABASE_URL=https://your-project.supabase.co
   SUPABASE_ANON_KEY=your-anon-key
   ```
4. Update `DataModule.createSupabaseClient()` to read from config

### Database Schema

The `moods` table in Supabase should match the `Mood` model:
- `id` (UUID, primary key)
- `score` (integer)
- `timestamp` (bigint)
- `created_at` (bigint, nullable)
- `updated_at` (bigint, nullable)

## Logging

All data layer components use the centralised `Logger` utility:
- **DEBUG**: Detailed operation logs (queries, saves, etc.)
- **INFO**: Important operations (successful saves, syncs)
- **WARN**: Recoverable errors (remote sync failures)
- **ERROR**: Critical errors (validation failures, database errors)

