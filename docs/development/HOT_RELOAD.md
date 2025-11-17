# Hot Reloading and Development Workflow

## Overview

This guide covers different approaches to hot-reloading and automated rebuilds for faster development iteration.

**Key Point**: Database migrations run automatically when the app starts and detects a version mismatch. No manual steps needed!

## Options

### 1. Android Studio Apply Changes (Recommended for UI)

**Best for**: UI changes, Compose updates, small code changes

**How to use**:
1. Run the app once from Android Studio
2. Make code changes
3. Click the "Apply Changes" button (⚡) in the toolbar
4. Changes are applied instantly without full rebuild

**Limitations**:
- Works best for UI/Compose changes
- Some changes require full restart (manifest, resources, etc.)
- Requires Android Studio

### 2. Gradle Continuous Build

**Best for**: Automatic rebuilds on any file change

**How to use**:
```bash
./scripts/continuous-build.sh
```

**What it does**:
- Watches for changes in source files
- Automatically rebuilds when changes detected
- Uses Gradle's built-in `--continuous` flag

**Pros**:
- No additional dependencies
- Works with any editor
- Handles all file types

**Cons**:
- Full rebuild on each change (slower than Apply Changes)
- Doesn't automatically install/launch

### 3. File Watcher Script

**Best for**: Automatic rebuild + install + launch

**How to use**:
```bash
./scripts/watch-and-reload.sh
```

**Requirements**:
- `fswatch` (install via `brew install fswatch`)

**What it does**:
- Watches `app/src/` for changes
- Automatically rebuilds and installs on change
- Restarts the app

**Pros**:
- Fully automated workflow
- Works with any editor

**Cons**:
- Requires fswatch installation
- Full rebuild cycle (slower)

### 4. Manual Reload Script

**Best for**: On-demand rebuilds with options

**How to use**:
```bash
# Standard rebuild and launch
./scripts/dev-reload.sh

# Clean build (removes build cache)
./scripts/dev-reload.sh --clean

# Fresh start (clears app data, triggers migrations)
./scripts/dev-reload.sh --fresh
```

**What it does**:
- Builds and installs the app
- Launches the app
- Shows recent logs
- Provides database access instructions

**Use cases**:
- After making significant changes
- When testing migrations
- When you want to see build output

## Database Migrations

### Automatic Migration

Database migrations run automatically when:
- The app starts
- The database version in `AppDatabase.kt` is higher than the current database version
- Room detects the version mismatch

### Testing Migrations

To test migrations with a fresh database:

```bash
# Clear app data and reinstall
./scripts/dev-reload.sh --fresh
```

Or manually:
```bash
adb shell pm clear com.electricsheep.app
./gradlew installDebug
adb shell am start -n com.electricsheep.app/.MainActivity
```

### Migration Workflow

1. **Update database version** in `AppDatabase.kt`:
   ```kotlin
   @Database(entities = [Mood::class], version = 3) // Increment version
   ```

2. **Create migration** in `DatabaseMigrations.kt`:
   ```kotlin
   val MIGRATION_2_3 = object : Migration(2, 3) {
       override fun migrate(database: SupportSQLiteDatabase) {
           // Migration SQL
       }
   }
   ```

3. **Add to migrations array**:
   ```kotlin
   fun getMigrations(): Array<Migration> {
       return arrayOf(MIGRATION_1_2, MIGRATION_2_3)
   }
   ```

4. **Test migration**:
   ```bash
   ./scripts/dev-reload.sh --fresh
   ```

## Recommended Workflow

### For UI Development
1. Use **Android Studio Apply Changes** for quick UI iterations
2. Use **dev-reload.sh** when Apply Changes doesn't work

### For Logic/Data Changes
1. Use **continuous-build.sh** for automatic rebuilds
2. Or use **dev-reload.sh** for on-demand rebuilds

### For Testing Migrations
1. Use **dev-reload.sh --fresh** to clear data and test migration

## Tips

1. **Keep emulator running**: Avoids device connection delays
2. **Use incremental builds**: Gradle caches build artifacts
3. **Watch logs**: Use `adb logcat | grep -i electric` to monitor app behavior
4. **Database inspection**: Use Android Studio Database Inspector for real-time database viewing

## Troubleshooting

### Build fails after changes
- Try clean build: `./scripts/dev-reload.sh --clean`
- Check for compilation errors in logs
- Verify Java version: `java -version` (should be 17)

### App doesn't update after rebuild
- Force stop app: `adb shell am force-stop com.electricsheep.app`
- Reinstall: `./gradlew installDebug`

### Migration not running
- Check database version in `AppDatabase.kt`
- Verify migration is in `getMigrations()` array
- Clear app data: `adb shell pm clear com.electricsheep.app`

### Continuous build not detecting changes
- Ensure you're editing files in `app/src/`
- Check file permissions
- Try manual build first: `./gradlew build`

