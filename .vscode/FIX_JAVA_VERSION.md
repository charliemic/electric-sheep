# Fix Java Version Error

## Problem

You're seeing this error:
> "Could not use Gradle version 8.2 and Java version 24.0.2 to configure the build."

**Root Cause**: Cursor IDE is detecting Java 24, but this project requires Java 17.

## Quick Fix (Choose One)

### Option 1: Set JAVA_HOME in Terminal (Recommended)

1. **Open Terminal in Cursor**: `Ctrl+`` (backtick)

2. **Set Java 17**:
   ```bash
   export JAVA_HOME=$(/usr/libexec/java_home -v 17)
   ```

3. **Verify**:
   ```bash
   echo $JAVA_HOME
   java -version  # Should show version 17
   ```

4. **Reload Cursor Window**: `Cmd+Shift+P` → "Reload Window"

### Option 2: Use Setup Script

1. **Run setup script**:
   ```bash
   source scripts/setup-env.sh
   ```

2. **Reload Cursor Window**: `Cmd+Shift+P` → "Reload Window"

### Option 3: Configure in Gradle Settings (If Java 17 is installed)

1. **Find Java 17 path**:
   ```bash
   /usr/libexec/java_home -v 17
   # Or check:
   ls -la /Library/Java/JavaVirtualMachines/
   ```

2. **Edit `gradle.properties`**:
   ```properties
   org.gradle.java.home=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
   ```
   (Uncomment and set the path from step 1)

3. **Reload Cursor Window**: `Cmd+Shift+P` → "Reload Window"

### Option 4: Install Java 17 (If Not Installed)

1. **Install via Homebrew**:
   ```bash
   brew install openjdk@17
   ```

2. **Link it**:
   ```bash
   sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
   ```

3. **Set JAVA_HOME**:
   ```bash
   export JAVA_HOME=$(/usr/libexec/java_home -v 17)
   ```

4. **Reload Cursor Window**: `Cmd+Shift+P` → "Reload Window"

## Verify Fix

After applying the fix:

1. **Check Java version**:
   ```bash
   java -version  # Should show 17.x
   ```

2. **Check Gradle**:
   ```bash
   ./gradlew --version  # Should show Java 17
   ```

3. **Reload Cursor**: `Cmd+Shift+P` → "Reload Window"

4. **Check Problems panel**: `Cmd+Shift+M` - Error should be gone

## Permanent Fix (Add to Shell Config)

To make this permanent, add to your `~/.zshrc`:

```bash
# Java 17 for Electric Sheep
export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || echo "$JAVA_HOME")
```

Then:
```bash
source ~/.zshrc
```

## Still Having Issues?

1. **Check what Java versions are installed**:
   ```bash
   /usr/libexec/java_home -V
   ```

2. **Check current JAVA_HOME**:
   ```bash
   echo $JAVA_HOME
   ```

3. **Check Gradle Java**:
   ```bash
   ./gradlew --version
   ```

4. **Clean Gradle cache**:
   ```bash
   ./gradlew clean
   rm -rf .gradle
   ```

5. **Reload Cursor**: `Cmd+Shift+P` → "Reload Window"

## Why This Happens

- macOS may have multiple Java versions installed
- Cursor IDE may detect the "default" Java (which could be 24)
- Gradle 8.2 requires Java 17 (not 24)
- Setting JAVA_HOME explicitly forces the correct version

