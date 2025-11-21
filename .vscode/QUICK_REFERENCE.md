# Cursor IDE Quick Reference

## Essential Keyboard Shortcuts

### AI Features
- `Cmd+K` - Quick AI edit (select code, then press)
- `Cmd+L` - Open AI chat
- `Cmd+I` - Inline AI edit
- `Cmd+Shift+L` - Composer (multi-file AI edits)

### Navigation
- `Cmd+P` - Quick file open
- `Cmd+Shift+O` - Go to symbol in file
- `Cmd+T` - Go to symbol in workspace
- `F12` - Go to definition
- `Shift+F12` - Find all references
- `Cmd+Shift+P` - Command Palette (everything)

### Build & Tasks
- `Cmd+Shift+B` - Run build task (default: Gradle Build)
- `Cmd+Shift+P` → "Tasks: Run Task" - All tasks

### UI
- `Cmd+B` - Toggle sidebar
- `Cmd+J` - Toggle panel (terminal, problems, etc.)
- `Cmd+Shift+M` - Show Problems panel
- `Ctrl+`` - Toggle terminal

## Common Workflows

### Fix Gradle Sync Errors

1. **Refresh Dependencies**:
   - `Cmd+Shift+P` → "Tasks: Run Task" → "Gradle: Refresh Dependencies"
   - Or: `./gradlew --refresh-dependencies`

2. **Reload Window**:
   - `Cmd+Shift+P` → "Reload Window"

3. **Check Java**:
   - Terminal: `java -version` (should be 17)
   - Terminal: `echo $JAVA_HOME`

### Development Loop

1. **Make code changes**
2. **Build**: `Cmd+Shift+B`
3. **Install & Run**: `Cmd+Shift+P` → "Tasks: Run Task" → "Dev Reload"
4. **Check logs**: Terminal → `adb logcat | grep -i electric`

### Using AI Features

1. **Quick Edit**:
   - Select code
   - Press `Cmd+K`
   - Describe what you want

2. **Chat**:
   - Press `Cmd+L`
   - Ask questions or request changes
   - Use `@codebase`, `@docs`, `@rules` for context

3. **Composer**:
   - Press `Cmd+Shift+L`
   - Describe multi-file changes
   - Review before accepting

## Troubleshooting

### Gradle Errors in Toolbar

1. Check Problems panel: `Cmd+Shift+M`
2. Run "Gradle: Refresh Dependencies" task
3. Reload window
4. Check Java version matches project (17)

### Extensions Not Working

1. Reload window
2. Check extension is installed
3. Check extension logs: `Help → Toggle Developer Tools`

### Performance Issues

1. Close unused files
2. Exclude build directories (already configured)
3. Disable unused extensions
4. Check file watcher exclusions

## Next Steps

1. ✅ Install recommended extensions
2. ✅ Learn keyboard shortcuts
3. ✅ Use tasks instead of terminal commands
4. ✅ Master AI features (Cmd+K, Cmd+L, Cmd+Shift+L)
5. ✅ Use Problems panel for errors

See [CURSOR_IDE_OPTIMIZATION.md](../../docs/development/guides/CURSOR_IDE_OPTIMIZATION.md) for complete guide.

