# Electric Sheep

Android app for personal utilities, starting with mood management.

## Development Setup

### Prerequisites
- **JDK 17** (recommended) or JDK 11 (minimum)
  - ⚠️ **Important**: Java 18+ may cause compatibility issues with Android build tools
  - See [KSP Migration Guide](docs/development/KSP_MIGRATION.md) for Java version details
- Android SDK (API level 34)
- Gradle (via wrapper)
- GitHub CLI (optional, for debugging CI failures)

### Installation

1. Clone the repository
2. Install Android SDK (see [Android SDK Setup](#android-sdk-setup))
3. Run `./gradlew build` to verify setup

### Android SDK Setup

**macOS (using Homebrew):**
```bash
brew install --cask android-commandlinetools
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

**Important**: Add Android SDK to your shell PATH permanently:
```bash
# For zsh (default on macOS)
echo 'export PATH="$PATH:$HOME/Library/Android/sdk/platform-tools"' >> ~/.zshrc
source ~/.zshrc

# Verify adb is accessible
adb version
```

Create `local.properties` in project root:
```properties
sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
```

## Development Workflow

For faster iteration during development:

**Option 1: Quick Reload Script**
```bash
# Rebuild, install, and launch app
./scripts/dev-reload.sh

# With clean build
./scripts/dev-reload.sh --clean

# With fresh database (clears app data)
./scripts/dev-reload.sh --fresh
```

**Option 2: Continuous Build (Gradle Watch Mode)**
```bash
# Automatically rebuilds on file changes
./scripts/continuous-build.sh

# Or run in background (survives shell disconnection)
./scripts/run-background-reload.sh
# Stop with: pkill -f continuous-build.sh
# View logs: tail -f .build-watch.log
```

**Option 3: File Watcher (requires fswatch)**
```bash
# Watch for changes and auto-reload
./scripts/watch-and-reload.sh
```

**Option 4: Android Studio Apply Changes**
- Use Android Studio's built-in "Apply Changes" feature
- Make code changes and click the lightning bolt icon (⚡)
- Changes are applied without full rebuild (faster for UI changes)

**Note**: Database migrations run automatically on app startup when the database version changes. To test migrations with fresh data, use `./scripts/dev-reload.sh --fresh` to clear app data.

## Running on Android Emulator

1. Start an emulator:
   ```bash
   emulator -avd YOUR_AVD_NAME
   ```
   Or use Android Studio's AVD Manager.

2. Build and install:
   ```bash
   ./gradlew installDebug
   ```

3. Or use the quick reload script:
   ```bash
   ./scripts/dev-reload.sh
   ```

## Development Metrics

We track development metrics over time to analyze trends and patterns. See [Development Metrics Guide](development-metrics/README.md) for details.

**Quick Start:**
- Capture all metrics: `./scripts/metrics/capture-all-metrics.sh`
- Capture after tests: `./scripts/gradle-wrapper-test.sh` (automatically captures test metrics)
- Capture after builds: `./scripts/gradle-wrapper-build.sh` (automatically captures build metrics)
- Store a prompt: `./scripts/metrics/capture-prompt.sh "your prompt text"`

Metrics are stored in `development-metrics/` with timestamps for historical analysis.

## Accessing the Database

To inspect the Room database on the emulator, see [Database Access Guide](docs/testing/DATABASE_ACCESS.md).

**Quick method using Android Studio:**
1. Run the app on the emulator
2. Open `View` → `Tool Windows` → `App Inspection`
3. Select your app and browse the `moods` table

**Quick method using ADB:**
```bash
adb shell
run-as com.electricsheep.app
cd /data/data/com.electricsheep.app/databases
sqlite3 app_database
SELECT * FROM moods;
```

## Building

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test

# Run lint
./gradlew lint
```

## CI/CD

The project uses GitHub Actions for CI/CD. See `.github/workflows/ci.yml` for configuration.

**Viewing CI Build Logs:**
```bash
# List recent runs
gh run list

# Watch a specific run
gh run watch <run-id>

# View logs
gh run view <run-id> --log
```

## Project Structure

```
app/
├── src/
│   ├── main/          # Production code
│   │   ├── java/      # Kotlin source files
│   │   └── res/       # Resources
│   └── test/          # Unit tests
docs/                   # Documentation
scripts/                # Development scripts
development-metrics/    # Development metrics tracking
```

## Contributing

See [AI Agent Guidelines](AI_AGENT_GUIDELINES.md) for development best practices and coding standards.
