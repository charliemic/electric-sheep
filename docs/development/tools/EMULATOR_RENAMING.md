# Emulator Renaming Guide

This guide explains how to rename Android emulators to make them clearer for feature-specific work.

## Quick Start

```bash
# Rename an emulator
./scripts/emulator-manager.sh rename <old-name> <new-name>

# Example: Rename to mood chart emulator
./scripts/emulator-manager.sh rename electric_sheep_test_device electric_sheep_mood_chart
```

## Automatic Feature Detection

The emulator manager now automatically detects your feature from:
1. **Git branch name**: If on `feature/mood-chart-visualization`, it will look for `electric_sheep_mood_chart_visualization`
2. **Worktree name**: If in `electric-sheep-mood-chart`, it will look for `electric_sheep_mood_chart`

## Renaming Process

When you rename an emulator:
1. The `.avd` directory is renamed
2. The `.ini` file is renamed and updated
3. The `config.ini` inside the `.avd` directory is updated
4. The old `.ini` file is removed

**Important**: The emulator must be stopped before renaming.

## Examples

### Rename for Mood Chart Feature
```bash
# Stop emulator if running
./scripts/emulator-manager.sh stop

# Rename
./scripts/emulator-manager.sh rename electric_sheep_test_device electric_sheep_mood_chart

# Start with new name
./scripts/emulator-manager.sh start electric_sheep_mood_chart
```

### Rename for Trivia Feature
```bash
./scripts/emulator-manager.sh rename electric_sheep_env_test electric_sheep_trivia
```

### Rename for Design Work
```bash
./scripts/emulator-manager.sh rename Medium_Phone_API_36.0 electric_sheep_design
```

## Naming Conventions

Recommended naming pattern:
- `electric_sheep_<feature_name>`
- Use underscores, not hyphens
- Keep names descriptive but concise

Examples:
- `electric_sheep_mood_chart`
- `electric_sheep_trivia`
- `electric_sheep_design`
- `electric_sheep_testing`

## Automatic Selection

When you run `./scripts/emulator-manager.sh start` without specifying a name:
1. It checks for a feature-specific emulator based on your branch/worktree
2. Falls back to `electric_sheep_dev` if available
3. Uses the first available emulator as last resort

## Verification

After renaming, verify the emulator exists:
```bash
./scripts/emulator-manager.sh list
```

You should see your renamed emulator in the list.

## Troubleshooting

### "Cannot rename running emulator"
Stop the emulator first:
```bash
./scripts/emulator-manager.sh stop
./scripts/emulator-manager.sh rename <old> <new>
```

### "Emulator 'new-name' already exists"
Choose a different name or remove the existing emulator first.

### "AVD directory not found"
The emulator may have been created in a different location. Check:
```bash
ls -la ~/.android/avd/
```

## Related Commands

- `./scripts/emulator-manager.sh list` - List all available emulators
- `./scripts/emulator-manager.sh status` - Show current emulator status
- `./scripts/emulator-manager.sh running` - List running emulators



