# Git Worktree Visualization in Cursor IDE

**Date**: 2025-01-20  
**Purpose**: Guide to visualizing and managing Git worktrees in Cursor IDE

## Answer: Is There a Worktree Plugin?

### ❌ **No Dedicated Worktree Extension**

Unfortunately, there is **no dedicated VS Code/Cursor extension** specifically for visualizing Git worktrees. However, you can use several approaches:

## Available Solutions

### Option 1: GitLens Extension (Recommended)

**Extension**: `eamodio.gitlens`

**Features:**
- ✅ Visual branch graph
- ✅ Branch comparison
- ✅ Commit history visualization
- ✅ Repository insights
- ⚠️ Limited worktree-specific features

**Installation:**
- Already in your `.vscode/extensions.json` recommendations
- Install via: `Cmd+Shift+P` → "Extensions: Show Recommended Extensions"

**Usage:**
1. Open Source Control panel (`Cmd+Shift+G`)
2. GitLens adds additional views:
   - **Repositories** - See all repositories
   - **Commits** - Visual commit history
   - **File History** - Track file changes
   - **Branches** - Visualize branch structure

**Limitations:**
- Doesn't show worktrees as separate entities
- Shows branches, but not worktree relationships
- Can't create/manage worktrees from UI

### Option 2: Git Graph Extension

**Extension**: `mhutchie.git-graph`

**Features:**
- ✅ Visual commit graph
- ✅ Branch visualization
- ✅ Interactive graph view
- ⚠️ No worktree-specific features

**Installation:**
- Already in your `.vscode/extensions.json` recommendations

**Usage:**
1. Open Command Palette: `Cmd+Shift+P`
2. Type: "Git Graph: View Git Graph"
3. See visual representation of branches and commits

**Limitations:**
- Shows branches, not worktrees
- Can't manage worktrees from UI

### Option 3: Terminal + Custom Scripts

**Best Option for Worktree Management**

Since there's no dedicated extension, use your existing scripts:

**Your Existing Scripts:**
- `scripts/create-worktree.sh` - Create worktrees
- `scripts/check-agent-coordination.sh` - Check worktree status
- `scripts/post-merge-cleanup.sh` - Cleanup worktrees

**Terminal Commands:**
```bash
# List all worktrees
git worktree list

# Show worktree details
git worktree list --porcelain

# Create new worktree
./scripts/create-worktree.sh <task-name>

# Check status
./scripts/check-agent-coordination.sh
```

### Option 4: Custom Task in Cursor

**Create a Task to List Worktrees**

Add to `.vscode/tasks.json`:

```json
{
  "label": "Git: List Worktrees",
  "type": "shell",
  "command": "git",
  "args": ["worktree", "list"],
  "group": "build",
  "presentation": {
    "reveal": "always",
    "panel": "dedicated"
  }
}
```

**Usage:**
- `Cmd+Shift+P` → "Tasks: Run Task" → "Git: List Worktrees"

## Recommended Workflow

### For Worktree Management

**Use Terminal + Scripts:**
1. **Create worktree**: `./scripts/create-worktree.sh <name>`
2. **List worktrees**: `git worktree list`
3. **Check status**: `./scripts/check-agent-coordination.sh`
4. **Cleanup**: `./scripts/post-merge-cleanup.sh <pr-number>`

### For Branch Visualization

**Use GitLens:**
1. Open Source Control panel (`Cmd+Shift+G`)
2. Use GitLens views to see branch structure
3. Use Git Graph extension for visual commit history

### For Worktree Status

**Create Custom Tasks:**

Add these to `.vscode/tasks.json`:

```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "Git: List Worktrees",
      "type": "shell",
      "command": "git",
      "args": ["worktree", "list"],
      "group": "build",
      "presentation": {
        "reveal": "always",
        "panel": "dedicated"
      }
    },
    {
      "label": "Git: Worktree Status",
      "type": "shell",
      "command": "git",
      "args": ["worktree", "list", "--porcelain"],
      "group": "build",
      "presentation": {
        "reveal": "always",
        "panel": "dedicated"
      }
    },
    {
      "label": "Check Agent Coordination",
      "type": "shell",
      "command": "./scripts/check-agent-coordination.sh",
      "group": "build",
      "presentation": {
        "reveal": "always",
        "panel": "dedicated"
      }
    }
  ]
}
```

## Alternative: External Tools

### GitKraken (GUI)

**Features:**
- ✅ Visual worktree representation
- ✅ Worktree management UI
- ✅ Branch visualization
- ✅ Commit graph

**Limitations:**
- ❌ Not integrated with Cursor
- ❌ Requires separate application
- ❌ Paid version for advanced features

**Installation:**
```bash
brew install --cask gitkraken
```

### SourceTree (GUI)

**Features:**
- ✅ Visual Git interface
- ✅ Worktree support
- ✅ Branch management

**Limitations:**
- ❌ Not integrated with Cursor
- ❌ Requires separate application

**Installation:**
```bash
brew install --cask sourcetree
```

## Best Practice Recommendation

### For Your Workflow

**Recommended Approach:**

1. **Use Terminal for Worktree Management**:
   - Your scripts are excellent (`create-worktree.sh`, etc.)
   - Terminal is most reliable for worktree operations
   - Scripts handle coordination and cleanup

2. **Use GitLens for Branch Visualization**:
   - Install GitLens extension (already recommended)
   - Use for seeing branch relationships
   - Use for commit history

3. **Use Tasks for Quick Status**:
   - Add worktree tasks to `.vscode/tasks.json`
   - Quick access via `Cmd+Shift+P` → "Tasks: Run Task"

4. **Use External Tool (Optional)**:
   - GitKraken or SourceTree for complex worktree visualization
   - Only if you need advanced GUI features

## Summary

### Question A: Worktree Visualization Plugin?

**Answer**: ❌ **No dedicated extension exists**

**Alternatives:**
- ✅ GitLens (branch visualization)
- ✅ Git Graph (commit graph)
- ✅ Terminal + scripts (best for worktree management)
- ✅ Custom tasks (quick status checks)
- ✅ External tools (GitKraken, SourceTree)

### Recommendation

**For worktree management**: Use your existing scripts + terminal  
**For branch visualization**: Use GitLens extension  
**For quick status**: Add custom tasks to `.vscode/tasks.json`

Your current workflow with scripts is actually the **best approach** for worktree management, as there's no better IDE integration available.

