# electric-sheep
An android assistant app to hold other apps

## Development Setup

### Prerequisites
- JDK 17 or higher
- Android SDK (API level 34)
- Gradle (via wrapper)
- GitHub CLI (optional, for debugging CI failures)

### Installing GitHub CLI

GitHub CLI (`gh`) is useful for debugging CI/CD pipeline failures and accessing build logs from the command line.

**macOS:**
```bash
brew install gh
gh auth login
```

**Other platforms:**
See [GitHub CLI installation guide](https://cli.github.com/manual/installation)

### Viewing CI Build Logs

If a build fails, you can view the logs using:

**Using GitHub CLI:**
```bash
# List recent workflow runs
gh run list --branch <your-branch-name>

# View logs for a specific run
gh run view <run-id> --log

# View logs for the latest failed run
gh run list --status failure --limit 1 | head -1 | awk '{print $7}' | xargs gh run view --log
```

**Using GitHub Web Interface:**
1. Navigate to the [Actions tab](https://github.com/charliemic/electric-sheep/actions)
2. Click on the failed workflow run
3. Click on the `build` job
4. Expand each step to see detailed logs
5. Check the "Debug environment" step for environment variable information

### Building Locally

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run lint checks
./gradlew lint

# Build debug APK
./gradlew assembleDebug

# Build release AAB
./gradlew bundleRelease
```

### Running on Android Emulator

To test the app locally on an Android emulator:

**1. List available emulators:**
```bash
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools
emulator -list-avds
```

**2. Start an emulator:**
```bash
emulator -avd <AVD_NAME> &
# Or use the full path:
$ANDROID_HOME/emulator/emulator -avd <AVD_NAME> &
```

**3. Wait for emulator to boot:**
```bash
adb wait-for-device
adb devices  # Should show device as "device" not "offline"
```

**4. Install and run the app:**
```bash
# Install the debug APK
./gradlew installDebug

# Launch the app
adb shell am start -n com.electricsheep.app/.MainActivity
```

**5. Rebuild and reinstall after changes:**
```bash
./gradlew installDebug
```

**6. Stop the emulator:**
- Close the emulator window, or
- Run: `adb emu kill`

**Note:** If you don't have an emulator set up, you can create one using Android Studio's AVD Manager, or install one via command line using `sdkmanager` and `avdmanager`.

## Contributing

See [AI_AGENT_GUIDELINES.md](./AI_AGENT_GUIDELINES.md) for detailed development guidelines and best practices.
