# Cursor IDE Optimization Guide

**Date**: 2025-01-20  
**Purpose**: Evaluate current Cursor IDE usage and provide actionable improvements

## Current State Assessment

### ✅ What's Working Well

1. **Excellent Script Automation**
   - Comprehensive shell scripts for common tasks (`dev-reload.sh`, `emulator-manager.sh`, etc.)
   - Well-organized workflow automation
   - Good use of Makefile for shortcuts

2. **Strong Project Structure**
   - Clear documentation organization
   - Comprehensive cursor rules (`.cursor/rules/`)
   - Good separation of concerns

3. **Gradle Configuration**
   - Proper Java 17 setup
   - Gradle wrapper configured correctly
   - Build system is functional

### ❌ Current Issues Identified

1. **Gradle Errors in Toolbar**
   - Gradle sync issues not being resolved
   - IDE not properly configured for Android development
   - Missing IDE-specific configuration files

2. **Underutilized Plugins/Extensions**
   - No VS Code/Cursor extensions configuration
   - Missing language server configurations
   - No workspace-specific settings

3. **Inefficient Workflow**
   - Not leveraging IDE features (IntelliSense, debugging, etc.)
   - Manual terminal commands instead of IDE integration
   - Missing build/debug configurations

## Recommended Improvements

### 1. Fix Gradle Sync Issues (CRITICAL)

#### Problem
Gradle errors in toolbar indicate the IDE isn't properly syncing with your Gradle project.

#### Solution: Create VS Code/Cursor Workspace Settings

Create `.vscode/settings.json`:

```json
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.import.gradle.enabled": true,
  "java.import.gradle.wrapper.enabled": true,
  "java.import.gradle.java.home": null,
  "java.import.gradle.version": null,
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-17",
      "path": "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home",
      "default": true
    }
  ],
  "java.compile.nullAnalysis.mode": "automatic",
  "kotlin.languageServer.enabled": true,
  "files.exclude": {
    "**/.gradle": false,
    "**/build": true,
    "**/.idea": false
  },
  "files.watcherExclude": {
    "**/.git/objects/**": true,
    "**/.git/subtree-cache/**": true,
    "**/build/**": true,
    "**/.gradle/**": true
  }
}
```

#### Additional Steps

1. **Install Required Extensions** (see section 2)
2. **Run Gradle Sync**:
   ```bash
   ./gradlew --refresh-dependencies
   ```
3. **Reload Cursor Window**: `Cmd+Shift+P` → "Reload Window"

### 2. Essential Extensions/Plugins

#### Required Extensions

Create `.vscode/extensions.json`:

```json
{
  "recommendations": [
    // Kotlin Support
    "fwcd.kotlin",
    "mathiasfrohlich.kotlin",
    
    // Java Support (for Android)
    "vscjava.vscode-java-pack",
    
    // Gradle Support
    "naco-siren.gradle-language-support",
    "vscjava.vscode-gradle",
    
    // Android Support
    "adelphes.android-dev-ext",
    
    // Git & Version Control
    "eamodio.gitlens",
    "mhutchie.git-graph",
    
    // Code Quality
    "sonarsource.sonarlint-vscode",
    "dbaeumer.vscode-eslint",
    
    // Markdown (for your extensive docs)
    "yzhang.markdown-all-in-one",
    "davidanson.vscode-markdownlint",
    
    // YAML (for feature flags, test scenarios)
    "redhat.vscode-yaml",
    
    // Shell Scripts
    "timonwong.shellcheck",
    
    // Python (for your scripts)
    "ms-python.python",
    "ms-python.vscode-pylance",
    
    // Docker (if using)
    "ms-azuretools.vscode-docker",
    
    // General Productivity
    "ms-vscode.vscode-json",
    "editorconfig.editorconfig",
    "streetsidesoftware.code-spell-checker"
  ]
}
```

#### Extension Installation

1. Open Command Palette: `Cmd+Shift+P`
2. Type: "Extensions: Show Recommended Extensions"
3. Click "Install All" or install individually

### 3. Workspace Configuration

#### Create `.vscode/launch.json` for Debugging

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "kotlin",
      "request": "launch",
      "name": "Debug Kotlin",
      "projectRoot": "${workspaceFolder}",
      "mainClass": "com.electricsheep.app.MainActivity",
      "args": ""
    }
  ]
}
```

#### Create `.vscode/tasks.json` for Build Tasks

```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "Gradle: Build",
      "type": "shell",
      "command": "./gradlew",
      "args": ["build"],
      "group": {
        "kind": "build",
        "isDefault": true
      },
      "problemMatcher": ["$gradle"]
    },
    {
      "label": "Gradle: Test",
      "type": "shell",
      "command": "./gradlew",
      "args": ["test"],
      "group": "test",
      "problemMatcher": ["$gradle"]
    },
    {
      "label": "Gradle: Install Debug",
      "type": "shell",
      "command": "./gradlew",
      "args": ["installDebug"],
      "group": "build",
      "problemMatcher": ["$gradle"]
    },
    {
      "label": "Dev Reload",
      "type": "shell",
      "command": "./scripts/dev-reload.sh",
      "group": "build",
      "problemMatcher": []
    },
    {
      "label": "Emulator: Start",
      "type": "shell",
      "command": "./scripts/emulator-manager.sh",
      "args": ["start"],
      "group": "build",
      "problemMatcher": []
    },
    {
      "label": "Emulator: Status",
      "type": "shell",
      "command": "./scripts/emulator-manager.sh",
      "args": ["status"],
      "group": "build",
      "problemMatcher": []
    }
  ]
}
```

### 4. Optimize Cursor-Specific Features

#### AI Features

1. **Use Cursor Rules Effectively**
   - Your `.cursor/rules/` directory is excellent
   - Reference rules in chat: "According to the accessibility rule..."
   - Use `discover-rules.sh` before starting work

2. **Leverage Composer**
   - Use Composer for multi-file changes
   - Create feature branches before using Composer
   - Review changes before accepting

3. **Chat Context**
   - Use `@codebase` to reference entire codebase
   - Use `@docs` to reference documentation
   - Use `@rules` to reference cursor rules

#### Keyboard Shortcuts

Essential shortcuts to learn:

- `Cmd+K` - Quick AI edit
- `Cmd+L` - Open chat
- `Cmd+I` - Inline AI edit
- `Cmd+Shift+L` - Composer
- `Cmd+Shift+P` - Command Palette
- `Cmd+B` - Toggle sidebar
- `Cmd+J` - Toggle panel

### 5. Improve Development Workflow

#### Use IDE Instead of Terminal (When Appropriate)

**Instead of:**
```bash
./gradlew build
./scripts/dev-reload.sh
```

**Use IDE:**
- `Cmd+Shift+B` - Run build task
- `F5` - Debug
- `Cmd+Shift+P` → "Tasks: Run Task" → "Dev Reload"

#### Use Integrated Terminal

- Keep terminal in IDE (`Ctrl+`` or `View → Terminal`)
- Use split terminals for multiple commands
- Use terminal tabs for different tasks

#### Use Source Control Integration

- Use Git panel instead of command line
- Stage/unstage files visually
- Review diffs in IDE
- Commit from IDE with proper messages

### 6. Code Navigation & Search

#### Use These Features

1. **Go to Definition**: `F12` or `Cmd+Click`
2. **Find References**: `Shift+F12`
3. **Go to Symbol**: `Cmd+T` (files), `Cmd+Shift+O` (symbols in file)
4. **Search Everywhere**: `Cmd+P` (files), `Cmd+Shift+F` (text)
5. **Command Palette**: `Cmd+Shift+P` (everything)

#### Use Breadcrumbs

- Enable breadcrumbs: `View → Appearance → Show Breadcrumbs`
- Navigate file hierarchy easily
- Understand file structure at a glance

### 7. Linting & Error Checking

#### Enable Real-time Linting

Your project has linting configured, but IDE may not show it:

1. Install SonarLint extension (recommended above)
2. Enable Kotlin language server (in settings)
3. Check Problems panel (`Cmd+Shift+M`)

#### Fix Gradle Sync

1. Open Command Palette: `Cmd+Shift+P`
2. Type: "Java: Clean Java Language Server Workspace"
3. Reload window
4. Wait for Gradle sync to complete

### 8. Debugging Setup

#### Android Debugging

While Cursor doesn't have full Android Studio debugging, you can:

1. **Use ADB from Terminal**:
   ```bash
   adb logcat | grep -i electric
   ```

2. **Use Logcat Extension** (if available):
   - Search for "Android Logcat" in extensions

3. **Use Remote Debugging**:
   - Set up remote debugging in Android Studio
   - Use Cursor for code editing
   - Use Android Studio for debugging

### 9. File Organization

#### Use Workspace Folders

If working with multiple related projects:

1. `File → Add Folder to Workspace`
2. Add `test-automation/` as separate folder
3. Organize by project structure

#### Use File Nesting

Configure file nesting in settings:
```json
{
  "explorer.fileNesting.enabled": true,
  "explorer.fileNesting.patterns": {
    "*.kt": "${capture}.kt.test, ${capture}Test.kt",
    "*.gradle.kts": "${capture}.gradle.kts, ${capture}.properties",
    "*.md": "${capture}.md, ${capture}*.md"
  }
}
```

### 10. Performance Optimization

#### Exclude Build Directories

Already configured in recommended settings, but verify:

```json
{
  "files.exclude": {
    "**/build": true,
    "**/.gradle": false,
    "**/node_modules": true
  },
  "search.exclude": {
    "**/build": true,
    "**/.gradle": true
  }
}
```

#### Limit File Watchers

```json
{
  "files.watcherExclude": {
    "**/.git/objects/**": true,
    "**/build/**": true,
    "**/.gradle/**": true
  }
}
```

## Implementation Checklist

### Immediate Actions (Do Now)

- [ ] Create `.vscode/settings.json` with recommended settings
- [ ] Create `.vscode/extensions.json` with recommended extensions
- [ ] Install recommended extensions
- [ ] Create `.vscode/tasks.json` for build tasks
- [ ] Run `./gradlew --refresh-dependencies`
- [ ] Reload Cursor window
- [ ] Verify Gradle sync completes without errors

### Short-term Improvements (This Week)

- [ ] Learn essential keyboard shortcuts
- [ ] Set up integrated terminal workflow
- [ ] Configure file nesting
- [ ] Enable breadcrumbs
- [ ] Set up source control integration
- [ ] Create launch.json for debugging

### Long-term Optimizations (This Month)

- [ ] Master Cursor AI features (Composer, Chat, Inline edits)
- [ ] Set up remote debugging workflow
- [ ] Create custom snippets for common patterns
- [ ] Configure workspace folders if needed
- [ ] Set up code snippets for your patterns

## Troubleshooting

### Gradle Sync Still Failing?

1. **Check Java Version**:
   ```bash
   java -version  # Should be 17
   echo $JAVA_HOME
   ```

2. **Clean Gradle Cache**:
   ```bash
   ./gradlew clean
   rm -rf .gradle
   ./gradlew --refresh-dependencies
   ```

3. **Check Android SDK**:
   ```bash
   echo $ANDROID_HOME
   ls $ANDROID_HOME/platforms
   ```

4. **Verify local.properties**:
   ```bash
   cat local.properties
   ```

### Extensions Not Working?

1. Reload window: `Cmd+Shift+P` → "Reload Window"
2. Check extension logs: `Help → Toggle Developer Tools → Console`
3. Reinstall extension if needed

### Performance Issues?

1. Disable unused extensions
2. Exclude more directories from file watching
3. Increase memory if needed (check Cursor settings)
4. Close unused files/tabs

## Additional Resources

- [Cursor Documentation](https://cursor.sh/docs)
- [VS Code Java Extension Guide](https://code.visualstudio.com/docs/java/java-tutorial)
- [Kotlin Language Server](https://github.com/fwcd/kotlin-language-server)
- [Gradle VS Code Extension](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-gradle)

## Next Steps

1. **Start with Immediate Actions** - Fix Gradle sync first
2. **Install Extensions** - Get language support working
3. **Learn Shortcuts** - Improve efficiency
4. **Use IDE Features** - Replace terminal commands where appropriate
5. **Master AI Features** - Leverage Cursor's AI capabilities

Remember: The goal is to make the IDE work for you, not against you. Start with fixing the Gradle errors, then gradually adopt more IDE features as you become comfortable.

