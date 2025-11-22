#!/bin/bash

# Query and manage task board
# Usage: ./scripts/query-task-board.sh <command> [args]

set -e

TASK_BOARD="docs/development/workflow/TASK_BOARD.md"
COORDINATION_DOC="docs/development/workflow/AGENT_COORDINATION.md"

COMMAND=$1
TASK_ID=$2
SKILLS=${3:-""}

case $COMMAND in
  available)
    # List available tasks from GitHub Issues
    echo "📋 Available Tasks:"
    echo ""
    
    if ! command -v gh &> /dev/null; then
        echo "⚠️  GitHub CLI not found. Install: https://cli.github.com/"
        echo ""
        echo "Available tasks from task board:"
        grep -A 10 "Status: Available" "$TASK_BOARD" 2>/dev/null || echo "No available tasks in task board"
        exit 0
    fi
    
    # Query GitHub Issues with status:available label
    AVAILABLE_ISSUES=$(gh issue list --label "status:available" --limit 20 --json number,title,labels,body,state 2>/dev/null || echo "[]")
    
    if [ "$AVAILABLE_ISSUES" = "[]" ] || [ -z "$AVAILABLE_ISSUES" ]; then
        echo "No available tasks found."
        echo ""
        echo "💡 To see all issues: gh issue list"
        echo "💡 To create tasks: Use driver agent to decompose project"
        exit 0
    fi
    
    # Filter by skills if provided
    if [ -n "$SKILLS" ]; then
        echo "Filtering by skills: $SKILLS"
        echo ""
    fi
    
    # Display issues
    echo "$AVAILABLE_ISSUES" | jq -r '.[] | 
        "Issue #\(.number): \(.title)
        Skills: \(.labels[] | select(.name | startswith("skill:")) | .name | sub("skill:"; ""))
        Status: \(.labels[] | select(.name | startswith("status:")) | .name | sub("status:"; ""))
        \(.body | split("\n")[0:2] | join(" "))
        → Claim: gh issue edit \(.number) --add-label '\''status:in-progress'\'' --assign '\''@me'\''
        "'
    
    echo ""
    echo "💡 For task details: docs/development/workflow/TASK_BOARD.md"
    ;;
    
  claim)
    if [ -z "$TASK_ID" ]; then
        echo "❌ Error: Task ID required"
        echo "Usage: ./scripts/query-task-board.sh claim <issue-number>"
        exit 1
    fi
    
    echo "📋 Claiming task #$TASK_ID..."
    
    if ! command -v gh &> /dev/null; then
        echo "❌ Error: GitHub CLI not found. Install: https://cli.github.com/"
        exit 1
    fi
    
    # Claim the issue
    gh issue edit "$TASK_ID" \
        --add-label "status:in-progress" \
        --remove-label "status:available" \
        --assign "@me" 2>/dev/null || {
        echo "❌ Error: Failed to claim issue #$TASK_ID"
        echo "   Check if issue exists and you have permissions"
        exit 1
    }
    
    echo "✅ Claimed issue #$TASK_ID"
    echo ""
    echo "📖 Next steps:"
    echo "   1. Read task details: docs/development/workflow/TASK_BOARD.md"
    echo "   2. Create branch: git checkout -b feature/task-$TASK_ID"
    echo "   3. Start work!"
    ;;
    
  status)
    if [ -z "$TASK_ID" ]; then
        echo "❌ Error: Task ID required"
        echo "Usage: ./scripts/query-task-board.sh status <issue-number>"
        exit 1
    fi
    
    if ! command -v gh &> /dev/null; then
        echo "⚠️  GitHub CLI not found. Checking task board..."
        grep -A 20 "Task:.*$TASK_ID" "$TASK_BOARD" 2>/dev/null || echo "Task not found in task board"
        exit 0
    fi
    
    # Get issue status
    ISSUE=$(gh issue view "$TASK_ID" --json number,title,labels,state,assignees,body 2>/dev/null || echo "{}")
    
    if [ "$ISSUE" = "{}" ]; then
        echo "❌ Issue #$TASK_ID not found"
        exit 1
    fi
    
    echo "$ISSUE" | jq -r '
        "Task #\(.number): \(.title)
        Status: \(.labels[] | select(.name | startswith("status:")) | .name | sub("status:"; ""))
        Assigned: \(.assignees[].login // "Unassigned")
        State: \(.state)
        
        Description:
        \(.body | split("\n")[0:5] | join("\n"))
        "'
    ;;
    
  update)
    if [ -z "$TASK_ID" ]; then
        echo "❌ Error: Task ID required"
        echo "Usage: ./scripts/query-task-board.sh update <issue-number> <status>"
        echo "Status: available, in-progress, blocked, complete"
        exit 1
    fi
    
    NEW_STATUS=$2
    if [ -z "$NEW_STATUS" ]; then
        echo "❌ Error: Status required"
        echo "Usage: ./scripts/query-task-board.sh update <issue-number> <status>"
        exit 1
    fi
    
    if ! command -v gh &> /dev/null; then
        echo "❌ Error: GitHub CLI not found"
        exit 1
    fi
    
    # Update issue status
    gh issue edit "$TASK_ID" \
        --remove-label "status:available,status:in-progress,status:blocked,status:complete" \
        --add-label "status:$NEW_STATUS" 2>/dev/null || {
        echo "❌ Error: Failed to update issue #$TASK_ID"
        exit 1
    }
    
    echo "✅ Updated issue #$TASK_ID to status: $NEW_STATUS"
    ;;
    
  list)
    # List all tasks for a project
    PROJECT=${TASK_ID:-""}
    
    if [ -z "$PROJECT" ]; then
        echo "📋 All Tasks:"
        echo ""
        
        if ! command -v gh &> /dev/null; then
            echo "⚠️  GitHub CLI not found. Checking task board..."
            grep -E "^### Task:" "$TASK_BOARD" 2>/dev/null || echo "No tasks in task board"
            exit 0
        fi
        
        gh issue list --limit 50 --json number,title,labels,state --jq '.[] | 
            "\(.number): \(.title) [\(.labels[] | select(.name | startswith("status:")) | .name)]"'
    else
        echo "📋 Tasks for project: $PROJECT"
        grep -A 10 "Project: $PROJECT" "$TASK_BOARD" 2>/dev/null || echo "Project not found"
    fi
    ;;
    
  *)
    echo "Usage: ./scripts/query-task-board.sh <command> [args]"
    echo ""
    echo "Commands:"
    echo "  available [skills]     - List available tasks (optionally filter by skills)"
    echo "  claim <issue-number>   - Claim a task"
    echo "  status <issue-number>  - Get task status"
    echo "  update <issue> <status> - Update task status"
    echo "  list [project]         - List all tasks (optionally for a project)"
    echo ""
    echo "Examples:"
    echo "  ./scripts/query-task-board.sh available"
    echo "  ./scripts/query-task-board.sh available android,ui"
    echo "  ./scripts/query-task-board.sh claim 100"
    echo "  ./scripts/query-task-board.sh status 100"
    echo "  ./scripts/query-task-board.sh update 100 complete"
    exit 1
    ;;
esac

