# Zsh Configuration for Electric Sheep Development

This document provides recommended configuration for your `~/.zshrc` file to optimize your Mac development environment for Electric Sheep.

## Quick Setup

Add the following configuration block to your `~/.zshrc` file. This configuration:
- Automatically sets up Java 17 for Android development
- Configures Android SDK paths
- Only sets variables if the required tools are installed (no errors if missing)
- Provides helpful warnings if tools are missing

## Recommended Configuration

Add this block to your `~/.zshrc`:

```bash
# =============================================================================
# Electric Sheep Development Environment
# =============================================================================

# Java 17 for Android development (required for Electric Sheep)
# Automatically detects Java 17 using Mac-native tool
if command -v /usr/libexec/java_home >/dev/null 2>&1; then
    if JAVA_HOME_17=$(/usr/libexec/java_home -v 17 2>/dev/null); then
        export JAVA_HOME="$JAVA_HOME_17"
        # Add Java bin to PATH if not already there
        [[ ":$PATH:" != *":$JAVA_HOME/bin:"* ]] && export PATH="$JAVA_HOME/bin:$PATH"
    elif [ -d "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home" ]; then
        export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"
        [[ ":$PATH:" != *":$JAVA_HOME/bin:"* ]] && export PATH="$JAVA_HOME/bin:$PATH"
    fi
fi

# Android SDK configuration
# Automatically detects Android SDK in common locations
if [ -z "$ANDROID_HOME" ]; then
    if [ -d "$HOME/Library/Android/sdk" ]; then
        export ANDROID_HOME="$HOME/Library/Android/sdk"
    elif [ -d "$HOME/Android/Sdk" ]; then
        export ANDROID_HOME="$HOME/Android/Sdk"
    fi
fi

# Add Android SDK tools to PATH if ANDROID_HOME is set
if [ -n "$ANDROID_HOME" ]; then
    # Platform tools (adb, fastboot, etc.)
    if [ -d "$ANDROID_HOME/platform-tools" ]; then
        [[ ":$PATH:" != *":$ANDROID_HOME/platform-tools:"* ]] && \
            export PATH="$PATH:$ANDROID_HOME/platform-tools"
    fi
    
    # Command line tools (sdkmanager, avdmanager, etc.)
    if [ -d "$ANDROID_HOME/cmdline-tools/latest/bin" ]; then
        [[ ":$PATH:" != *":$ANDROID_HOME/cmdline-tools/latest/bin:"* ]] && \
            export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin"
    elif [ -d "$ANDROID_HOME/tools/bin" ]; then
        # Fallback for older SDK installations
        [[ ":$PATH:" != *":$ANDROID_HOME/tools/bin:"* ]] && \
            export PATH="$PATH:$ANDROID_HOME/tools/bin"
    fi
    
    # Build tools (optional, but useful)
    if [ -d "$ANDROID_HOME/build-tools" ]; then
        # Add latest build tools version to PATH
        LATEST_BUILD_TOOLS=$(ls -1 "$ANDROID_HOME/build-tools" 2>/dev/null | sort -V | tail -1)
        if [ -n "$LATEST_BUILD_TOOLS" ] && [ -d "$ANDROID_HOME/build-tools/$LATEST_BUILD_TOOLS" ]; then
            [[ ":$PATH:" != *":$ANDROID_HOME/build-tools/$LATEST_BUILD_TOOLS:"* ]] && \
                export PATH="$PATH:$ANDROID_HOME/build-tools/$LATEST_BUILD_TOOLS"
        fi
    fi
fi

# Optional: GitHub CLI (useful for debugging CI failures)
if command -v gh >/dev/null 2>&1; then
    # GitHub CLI is available
    :
fi

# Optional: Verify Java version on shell startup (only if JAVA_HOME is set)
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA_VERSION=$("$JAVA_HOME/bin/java" -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 18 ] 2>/dev/null; then
        echo "⚠️  Warning: Java $JAVA_VERSION detected. Electric Sheep requires Java 17."
        echo "   Install Java 17: brew install openjdk@17"
        echo "   Then set: export JAVA_HOME=\$(/usr/libexec/java_home -v 17)"
    fi
fi
```

## Installation

### Option 1: Manual Installation

1. Open your `~/.zshrc` file:
   ```bash
   nano ~/.zshrc
   # or
   code ~/.zshrc
   ```

2. Add the configuration block above to the end of the file

3. Reload your shell configuration:
   ```bash
   source ~/.zshrc
   ```

### Option 2: Automated Installation

```bash
# Append the configuration to your .zshrc
cat >> ~/.zshrc << 'EOF'

# =============================================================================
# Electric Sheep Development Environment
# =============================================================================
# ... (paste the configuration block above)
EOF

# Reload configuration
source ~/.zshrc
```

## Verification

After adding the configuration, verify it's working:

```bash
# Check Java version
java -version
echo $JAVA_HOME

# Check Android SDK
echo $ANDROID_HOME
adb version

# Check PATH includes Android tools
which adb
which sdkmanager
```

## Features

### Automatic Detection
- **Java 17**: Automatically finds Java 17 using `/usr/libexec/java_home -v 17`
- **Android SDK**: Detects SDK in common Mac locations (`~/Library/Android/sdk` or `~/Android/Sdk`)
- **No Errors**: Only sets variables if tools are installed (won't break your shell if missing)

### PATH Management
- **Smart PATH Updates**: Only adds paths if they're not already in PATH
- **No Duplicates**: Prevents duplicate PATH entries
- **Build Tools**: Automatically adds latest build tools version to PATH

### Safety Features
- **Conditional Checks**: All paths are checked before being added
- **Version Warnings**: Warns if Java 18+ is detected (incompatible with Android builds)
- **Silent Operation**: Doesn't print anything if everything is configured correctly

## Troubleshooting

### Java 17 Not Found

If Java 17 is not detected:

```bash
# Install Java 17 via Homebrew
brew install openjdk@17

# Verify installation
/usr/libexec/java_home -V

# Reload shell
source ~/.zshrc
```

### Android SDK Not Found

If Android SDK is not detected:

```bash
# Install Android command line tools
brew install --cask android-commandlinetools

# Set up SDK
export ANDROID_HOME=$HOME/Library/Android/sdk
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# Reload shell
source ~/.zshrc
```

### PATH Issues

If tools aren't found in PATH:

```bash
# Check what's in PATH
echo $PATH | tr ':' '\n' | grep -E "(java|android)"

# Manually verify paths exist
ls -la "$ANDROID_HOME/platform-tools"
ls -la "$JAVA_HOME/bin"
```

## Alternative: Using the Setup Script

If you prefer not to modify your `.zshrc`, you can use the project's setup script:

```bash
# In your .zshrc, add:
alias es-setup='source /Users/CharlieCalver/git/electric-sheep/scripts/setup-env.sh'

# Then run when needed:
es-setup
```

However, the `.zshrc` configuration is recommended as it sets up your environment automatically on every shell session.

## Best Practices

1. **Keep it in `.zshrc`**: This ensures the environment is set up for every interactive shell
2. **Don't duplicate**: If you already have Java/Android configuration, merge it with this instead of duplicating
3. **Test after changes**: Always run `source ~/.zshrc` and verify after making changes
4. **Backup first**: Consider backing up your `.zshrc` before making changes:
   ```bash
   cp ~/.zshrc ~/.zshrc.backup
   ```

