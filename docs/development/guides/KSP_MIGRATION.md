# KAPT to KSP Migration

## Summary

Successfully migrated from **KAPT** (Kotlin Annotation Processing Tool) to **KSP** (Kotlin Symbol Processing) to resolve Java 17 compatibility issues.

## Changes Made

### 1. Build Configuration (`app/build.gradle.kts`)

**Before:**
```kotlin
plugins {
    id("kotlin-kapt")
}
dependencies {
    kapt("androidx.room:room-compiler:2.6.1")
}
```

**After:**
```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
}
dependencies {
    ksp("androidx.room:room-compiler:2.6.1")
}

// KSP configuration for Room schema export
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
```

### 2. Data Model Changes (`Mood.kt`)

**Before:**
```kotlin
@Entity(tableName = "moods")
data class Mood(
    @PrimaryKey
    val id: String? = null,  // Nullable primary key
    // ...
)
```

**After:**
```kotlin
@Entity(tableName = "moods")
data class Mood(
    @PrimaryKey
    val id: String,  // Non-nullable primary key (required by Room/KSP)
    // ...
)
```

**Rationale**: Room with KSP is stricter and doesn't allow nullable primary keys. Since the repository always generates IDs before saving, making it non-nullable is safe.

### 3. Repository Updates

Updated `MoodRepository.saveMood()` to handle blank IDs instead of null:
```kotlin
// Generate ID if not provided
val moodWithId = if (moodWithUser.id.isBlank()) {
    moodWithUser.copy(id = UUID.randomUUID().toString())
} else {
    moodWithUser
}
```

### 4. Test Updates

Updated all test files to provide non-null IDs when creating `Mood` objects:
- Changed `Mood(id = null, ...)` to `Mood(id = "", ...)` for new entries
- Updated assertions from `assertNull(mood.id)` to `assertTrue(mood.id.isNotBlank())`

## Benefits of KSP

1. **No Java 17 Compatibility Issues**: KSP doesn't have the same Java 17 compatibility problems as KAPT
2. **Faster Builds**: KSP is typically 2x faster than KAPT
3. **Better Error Messages**: KSP provides clearer error messages
4. **Future-Proof**: KSP is the recommended tool going forward (KAPT is in maintenance mode)

## Java Version Implications

### Current Setup
- **Project Configuration**: Java 17 (sourceCompatibility, targetCompatibility, jvmTarget)
- **CI/CD**: Java 17 (GitHub Actions)
- **Local Development**: May vary

### Using Different Java Versions Locally

**Implications:**

1. **Build Compatibility**: 
   - ✅ **Java 11-17**: Fully compatible
   - ⚠️ **Java 18+**: May have compatibility issues with Android build tools
   - ❌ **Java 24+**: Known incompatibilities with Android Gradle Plugin

2. **Recommendations**:
   - **Best**: Use Java 17 locally to match CI/CD
   - **Acceptable**: Use Java 11 locally (backward compatible)
   - **Avoid**: Java 18+ for local development (may cause build tool issues)

3. **Managing Multiple Java Versions**:
   ```bash
   # Using SDKMAN (recommended)
   sdk install java 17.0.9-tem
   sdk use java 17.0.9-tem
   
   # Using Homebrew
   brew install openjdk@17
   export JAVA_HOME=$(/usr/libexec/java_home -v 17)
   ```

4. **Gradle JDK Configuration**:
   - Android Studio: `File` → `Settings` → `Build, Execution, Deployment` → `Build Tools` → `Gradle` → Set Gradle JDK to Java 17
   - Command line: Set `JAVA_HOME` environment variable

## Known Issues

### Java 24+ Incompatibility
If using Java 24+, you may encounter:
```
Error while executing process .../jlink with arguments ...
```

**Solution**: Use Java 17 or Java 11 for local development.

### Schema Export
KSP requires explicit schema export configuration:
```kotlin
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
```

The `schemas/` directory will be created automatically and should be committed to version control.

## Verification

To verify KSP is working:
```bash
./gradlew clean assembleDebug
```

Check for:
- ✅ No KAPT-related errors
- ✅ Room code generation succeeds
- ✅ Schema files generated in `app/schemas/`
- ✅ Build completes successfully

## Migration Checklist

- [x] Replace `kotlin-kapt` plugin with `com.google.devtools.ksp`
- [x] Replace `kapt()` dependencies with `ksp()`
- [x] Configure KSP schema export
- [x] Update data models (non-nullable primary keys)
- [x] Update repository code (handle blank IDs)
- [x] Update all tests
- [x] Verify build succeeds
- [x] Update documentation

## References

- [KSP Documentation](https://kotlinlang.org/docs/ksp-overview.html)
- [Room with KSP](https://developer.android.com/training/data-storage/room/migrating-db#ksp)
- [KSP vs KAPT](https://kotlinlang.org/docs/ksp-overview.html#ksp-vs-kapt)

