# Java Setup Evaluation & Optimization

**Date**: 2025-01-20  
**Purpose**: Evaluate current Java setup, recommend optimal version, and provide cleanup steps

## Current Project Requirements

### Android Gradle Plugin Compatibility

- **AGP Version**: 8.2.0
- **Gradle Version**: 8.2
- **Target SDK**: 34
- **Compile SDK**: 34

### Java Version Requirements

**Project Configuration:**
- `sourceCompatibility`: Java 17
- `targetCompatibility`: Java 17
- `jvmTarget`: 17

**AGP 8.2.0 Compatibility:**
- ✅ **Java 11**: Minimum supported
- ✅ **Java 17**: Recommended (LTS, best compatibility)
- ⚠️ **Java 19-20**: Supported but may have issues
- ❌ **Java 21+**: Not officially supported (may cause build failures)
- ❌ **Java 24+**: Known incompatibilities with Android build tools

## Evaluation: Is Java 17 the Right Choice?

### ✅ **YES - Java 17 is Optimal**

**Reasons:**

1. **LTS (Long Term Support)**: Java 17 is an LTS release (supported until 2029)
2. **AGP Compatibility**: Fully supported by Android Gradle Plugin 8.2.0
3. **CI/CD Alignment**: Your CI uses Java 17 (GitHub Actions)
4. **Stability**: Most stable for Android development
5. **Industry Standard**: Most Android projects use Java 17

### ❌ **Should NOT Use Java 21+**

**Why not:**
- Android Gradle Plugin 8.2.0 doesn't officially support Java 21+
- Known issues with `jlink` and other build tools
- Your error message confirms: "Could not use Gradle version 8.2 and Java version 24.0.2"
- Build tools may fail with newer Java versions

### 🔄 **Could Upgrade to Java 19-20 (Not Recommended)**

**If you wanted to:**
- AGP 8.2.0 technically supports Java 19-20
- But Java 17 is more stable and widely tested
- No significant benefits for Android development
- Risk of compatibility issues

## Current Setup Assessment

### What You Have

Based on your error message:
- **Java 24.0.2** detected (incompatible)
- **Java 17** required (not currently active)

### What You Need

1. **Java 17 installed** (check if installed)
2. **JAVA_HOME set to Java 17** (currently pointing to Java 24)
3. **Gradle configured to use Java 17** (via JAVA_HOME or gradle.properties)

## Java Setup Cleanup & Optimization

### Step 1: Audit Current Java Installations

```bash
# List all installed Java versions
/usr/libexec/java_home -V

# Check current JAVA_HOME
echo $JAVA_HOME

# Check which Java is active
java -version
which java
```

### Step 2: Install Java 17 (If Not Installed)

**Option A: Homebrew (Recommended)**
```bash
brew install openjdk@17

# Link it to system location
sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
```

**Option B: Adoptium (Temurin)**
```bash
# Download from: https://adoptium.net/temurin/releases/?version=17
# Install the .pkg file
```

**Option C: SDKMAN (For Multiple Projects)**
```bash
# Install SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install Java 17
sdk install java 17.0.9-tem
sdk use java 17.0.9-tem
```

### Step 3: Clean Up Multiple Java Versions

**If you have multiple Java versions:**

1. **Keep Java 17** (required for this project)
2. **Keep Java 24** (if needed for other projects, but don't use for Android)
3. **Remove old/unused versions** (optional cleanup)

**To remove old Java versions:**
```bash
# List installed versions
/usr/libexec/java_home -V

# Remove specific version (example)
sudo rm -rf /Library/Java/JavaVirtualMachines/jdk-11.jdk
```

**⚠️ Warning**: Only remove versions you're sure you don't need for other projects.

### Step 4: Configure Java 17 as Default for This Project

**Option A: Use Project Script (Recommended)**
```bash
# In terminal, before working:
source scripts/setup-env.sh
```

**Option B: Set in Shell Config (Permanent)**
Add to `~/.zshrc`:
```bash
# Java 17 for Electric Sheep
export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || echo "$JAVA_HOME")
```

**Option C: Set in gradle.properties (Project-Specific)**
```properties
# Uncomment and set path:
org.gradle.java.home=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
```

**Option D: Use Java Toolchains (Gradle Feature)**
Add to `app/build.gradle.kts`:
```kotlin
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
```

### Step 5: Verify Setup

```bash
# Check Java version
java -version  # Should show 17.x

# Check JAVA_HOME
echo $JAVA_HOME  # Should point to Java 17

# Check Gradle uses correct Java
./gradlew --version  # Should show Java 17

# Test build
./gradlew clean build  # Should succeed
```

## Recommended Setup

### For This Project (Electric Sheep)

**Best Practice:**
1. ✅ **Use Java 17** (LTS, stable, compatible)
2. ✅ **Set JAVA_HOME in `.zshrc`** (automatic on shell startup)
3. ✅ **Use `scripts/setup-env.sh`** (fallback if .zshrc not configured)
4. ✅ **Configure in `gradle.properties`** (project-specific override)

### Multi-Project Setup

**If you work on multiple projects with different Java versions:**

1. **Use SDKMAN** (best for managing multiple versions):
   ```bash
   sdk install java 17.0.9-tem
   sdk install java 21.0.1-tem
   
   # Switch per project
   cd electric-sheep
   sdk use java 17.0.9-tem
   ```

2. **Use Project-Specific Scripts**:
   ```bash
   # Each project has setup-env.sh
   source scripts/setup-env.sh
   ```

3. **Use `.tool-versions` (if using asdf)**:
   ```bash
   # In project root
   echo "java 17.0.9" > .tool-versions
   ```

## Cleanup Checklist

- [ ] Audit installed Java versions: `/usr/libexec/java_home -V`
- [ ] Install Java 17 if missing: `brew install openjdk@17`
- [ ] Set JAVA_HOME to Java 17 in `~/.zshrc`
- [ ] Verify Java 17 is active: `java -version`
- [ ] Test Gradle build: `./gradlew --version`
- [ ] Remove unused Java versions (optional)
- [ ] Configure IDE to use Java 17 (Cursor settings)
- [ ] Update `gradle.properties` if needed

## Troubleshooting

### Java 24 Still Detected

**Problem**: IDE or Gradle still using Java 24

**Solutions:**
1. **Set JAVA_HOME explicitly**:
   ```bash
   export JAVA_HOME=$(/usr/libexec/java_home -v 17)
   ```

2. **Reload IDE**: `Cmd+Shift+P` → "Reload Window"

3. **Set in gradle.properties**:
   ```properties
   org.gradle.java.home=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
   ```

4. **Check IDE settings**: `.vscode/settings.json` should have Java 17 configured

### Multiple Java Versions Conflicting

**Problem**: System has multiple Java versions, wrong one being used

**Solution**: Use explicit configuration:
1. Set JAVA_HOME in `.zshrc` (project-specific or global)
2. Use `gradle.properties` for project-specific override
3. Use `scripts/setup-env.sh` for manual override

### Build Still Fails

**Problem**: Build fails even with Java 17

**Solutions:**
1. **Clean Gradle cache**:
   ```bash
   ./gradlew clean
   rm -rf .gradle
   ```

2. **Refresh dependencies**:
   ```bash
   ./gradlew --refresh-dependencies
   ```

3. **Check AGP version**: Ensure Android Gradle Plugin 8.2.0 is compatible

## Future Considerations

### When to Upgrade Java Version

**Consider upgrading when:**
- ✅ Android Gradle Plugin supports newer Java (check AGP release notes)
- ✅ You upgrade to AGP 8.3+ (may support Java 21)
- ✅ CI/CD infrastructure supports newer Java
- ✅ All team members can use the same version

**Current recommendation**: **Stay on Java 17** until:
- AGP 8.3+ is released with Java 21 support
- You upgrade AGP to a version that supports Java 21+
- You have a specific need for Java 21+ features

### AGP Upgrade Path

If you want to use Java 21+ in the future:
1. Upgrade Android Gradle Plugin to 8.3+ (when available)
2. Update `sourceCompatibility` and `targetCompatibility` to Java 21
3. Update CI/CD to use Java 21
4. Test thoroughly before committing

## Summary

### Current Status
- ❌ **Java 24 detected** (incompatible)
- ✅ **Java 17 required** (optimal choice)
- ✅ **Project configured for Java 17** (correct)

### Action Items
1. ✅ **Install Java 17** (if not installed)
2. ✅ **Set JAVA_HOME to Java 17** (in `.zshrc` or project script)
3. ✅ **Configure IDE** (Cursor settings)
4. ✅ **Verify setup** (test build)
5. ⚠️ **Optional cleanup** (remove unused Java versions)

### Recommendation
**Java 17 is the correct and optimal choice** for this project. No need to upgrade to Java 21+ until AGP supports it.

