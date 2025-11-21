# VS Code / Cursor IDE Configuration

This directory contains workspace-specific settings for Cursor IDE (based on VS Code).

## Files

- **`settings.json`** - Workspace settings (Java, Kotlin, file exclusions, etc.)
- **`extensions.json`** - Recommended extensions for this project
- **`tasks.json`** - Build and development tasks (Gradle, scripts, etc.)
- **`launch.json`** - Debug configurations

## Quick Start

1. **Install Recommended Extensions**:
   - Open Command Palette: `Cmd+Shift+P`
   - Type: "Extensions: Show Recommended Extensions"
   - Click "Install All"

2. **Reload Window**:
   - `Cmd+Shift+P` → "Reload Window"
   - Wait for Gradle sync to complete

3. **Use Tasks**:
   - `Cmd+Shift+B` - Run default build task
   - `Cmd+Shift+P` → "Tasks: Run Task" - See all available tasks

## Available Tasks

- **Gradle: Build** - `./gradlew build`
- **Gradle: Test** - `./gradlew test`
- **Gradle: Install Debug** - `./gradlew installDebug`
- **Gradle: Clean** - `./gradlew clean`
- **Gradle: Refresh Dependencies** - `./gradlew --refresh-dependencies`
- **Dev Reload** - `./scripts/dev-reload.sh`
- **Dev Reload (Clean)** - `./scripts/dev-reload.sh --clean`
- **Dev Reload (Fresh)** - `./scripts/dev-reload.sh --fresh`
- **Emulator: Start** - `./scripts/emulator-manager.sh start`
- **Emulator: Status** - `./scripts/emulator-manager.sh status`
- **Emulator: Stop** - `./scripts/emulator-manager.sh stop`
- **Pre-Work Check** - `./scripts/pre-work-check.sh`
- **Check Agent Coordination** - `./scripts/check-agent-coordination.sh`

## Troubleshooting

### Java Version Issues

See: `docs/development/guides/FIX_JAVA_VERSION.md` for complete guide.

### Gradle Sync Issues

1. **Check Java Version**:
   ```bash
   java -version  # Should be 17
   echo $JAVA_HOME
   ```

2. **Clean and Refresh**:
   - Run task: "Gradle: Refresh Dependencies"
   - Or: `./gradlew --refresh-dependencies`

3. **Reload Window**:
   - `Cmd+Shift+P` → "Reload Window"

### Extensions Not Working

1. Reload window: `Cmd+Shift+P` → "Reload Window"
2. Check extension logs: `Help → Toggle Developer Tools → Console`
3. Reinstall extension if needed

## Customization

These settings are committed to the repository for team consistency. If you need personal overrides:

1. Use User Settings (not workspace settings)
2. Or create `.vscode/settings.local.json` (add to `.gitignore`)

## Related Documentation

- [Cursor IDE Optimization Guide](../../docs/development/guides/CURSOR_IDE_OPTIMIZATION.md) - Complete optimization guide
- [Development Workflow](../../docs/development/workflow/) - Workflow documentation

