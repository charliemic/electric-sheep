# Accessing Room Database on Android Emulator

## Overview

To inspect and verify data storage in the Room database on an emulated Android device, you can use Android's Database Inspector or command-line tools.

## Method 1: Android Studio Database Inspector (Recommended)

### Steps:
1. **Open Android Studio**
2. **Run the app** on the emulator
3. **Open Database Inspector**:
   - Go to `View` → `Tool Windows` → `App Inspection`
   - Or click the "App Inspection" tab at the bottom
4. **Select your app** from the dropdown
5. **Select the database**: Look for `app_database` or the name specified in `AppDatabase.kt`
6. **Browse tables**: Click on the `moods` table to see all entries
7. **Query data**: Use the SQL query tab to run custom queries

### Example Queries:
```sql
-- Get all moods
SELECT * FROM moods;

-- Get moods for a specific user
SELECT * FROM moods WHERE userId = 'user-123';

-- Get moods by date range
SELECT * FROM moods WHERE timestamp >= 1609459200000 AND timestamp <= 1609545600000;

-- Count moods per user
SELECT userId, COUNT(*) as count FROM moods GROUP BY userId;
```

## Method 2: ADB Shell + SQLite3

### Steps:
1. **Find the database file**:
   ```bash
   adb shell "run-as com.electricsheep.app find /data/data/com.electricsheep.app/databases -name '*.db'"
   ```

2. **Access the database**:
   ```bash
   adb shell
   run-as com.electricsheep.app
   cd /data/data/com.electricsheep.app/databases
   sqlite3 app_database
   ```

3. **Run SQL queries**:
   ```sql
   .tables                    -- List all tables
   .schema moods              -- Show table schema
   SELECT * FROM moods;        -- View all moods
   .headers on                -- Show column headers
   .mode column               -- Column output mode
   ```

4. **Exit**:
   ```sql
   .exit
   ```

## Method 3: Pull Database File

### Steps:
1. **Pull the database file**:
   ```bash
   adb shell "run-as com.electricsheep.app cat /data/data/com.electricsheep.app/databases/app_database" > app_database.db
   ```

   Note: This may not work on newer Android versions due to security restrictions.

2. **Use SQLite browser**:
   - Open `app_database.db` in a SQLite browser tool (e.g., DB Browser for SQLite)
   - Browse and query the database

## Method 4: Stetho (Development Only)

For more advanced debugging, you can add Stetho to the app:

### Add Dependency:
```kotlin
debugImplementation("com.facebook.stetho:stetho:1.6.0")
```

### Initialise in Application:
```kotlin
if (BuildConfig.DEBUG) {
    Stetho.initializeWithDefaults(this)
}
```

### Access:
- Open Chrome and navigate to `chrome://inspect`
- Click "inspect" under your device
- Go to the "Resources" tab → "Web SQL" → "app_database"

## Database Location

The Room database is typically located at:
```
/data/data/com.electricsheep.app/databases/app_database
```

## Database Schema

### `moods` Table:
- `id` (TEXT, PRIMARY KEY)
- `userId` (TEXT, indexed)
- `score` (INTEGER)
- `timestamp` (INTEGER)
- `createdAt` (INTEGER, nullable)
- `updatedAt` (INTEGER, nullable)

## Tips

1. **Clear app data** to reset the database:
   ```bash
   adb shell pm clear com.electricsheep.app
   ```

2. **View logs** to see database operations:
   ```bash
   adb logcat | grep -i "database\|room\|mood"
   ```

3. **Check migration status**:
   ```sql
   SELECT * FROM room_master_table;
   ```

4. **Verify indexes**:
   ```sql
   .indices moods
   ```

## Troubleshooting

### "Permission denied" errors:
- Make sure you're using `run-as` with the correct package name
- The app must be debuggable (debug builds are by default)

### Database not found:
- Make sure the app has been run at least once
- Check that Room has initialised the database
- Verify the database name matches `AppDatabase.kt`

### Cannot see data:
- Ensure you've created some mood entries in the app
- Check that you're querying the correct user's data (if using user scoping)
- Verify the app is using the correct database instance

