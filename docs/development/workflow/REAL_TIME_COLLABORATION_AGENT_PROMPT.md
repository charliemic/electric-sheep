# Prompt for Other Agents: Incorporate Real-Time Collaboration

**Date**: 2025-01-20  
**Purpose**: Copy-paste prompt for other agents to incorporate real-time collaboration

## Quick Prompt (Copy-Paste This)

```
Please review the real-time collaboration model implementation in the feature/real-time-collaboration-model branch and incorporate it into your workflow.

After the PR is merged:
1. Pull latest main: git checkout main && git pull origin main
2. Read the onboarding guide: docs/development/workflow/REAL_TIME_COLLABORATION_ONBOARDING.md
3. If currently working, check for updates: ./scripts/check-rule-updates.sh
4. Update workflow to always check for updates before starting work (via pre-work-check.sh)

The pre-work-check.sh now automatically checks for rule/workflow updates, so just pull latest main and you'll have it.
```

## Detailed Prompt (If More Context Needed)

```
I need to incorporate the new real-time collaboration model that enables sharing agent improvements and workflow updates in real-time while maintaining isolation for code changes.

What was implemented:
1. Real-time sharing for rules/workflow docs (changes propagate immediately via git)
2. Update detection script: scripts/check-rule-updates.sh
3. Pre-work-check.sh enhanced with step 2.5: Check for rule/workflow updates
4. Updated branching rules with real-time collaboration sections

What I need you to do:
1. If I'm currently working on a task:
   - Run: ./scripts/check-rule-updates.sh
   - If updates detected, pull latest main and rebase my branch
   - Review updated rules/workflow files

2. If I'm starting new work:
   - Run: ./scripts/pre-work-check.sh (now includes update check)
   - Pull latest main if updates detected
   - Create worktree from updated main

3. Going forward:
   - Always run pre-work-check before starting work (includes update check)
   - Pull latest main before creating worktree
   - Review rule/workflow updates if detected

Key points:
- Rules/workflow are now shared in real-time (no conflicts possible)
- Code changes still isolated (worktrees still required)
- Check for updates before starting work
- Pull latest main to get updates

Documentation:
- docs/development/workflow/REAL_TIME_COLLABORATION_ONBOARDING.md - Quick onboarding
- docs/development/workflow/REAL_TIME_COLLABORATION.md - Complete guide
- docs/development/workflow/REAL_TIME_COLLABORATION_SUMMARY.md - Quick reference
```

## Even Simpler Prompt (Minimal)

```
Please incorporate the new real-time collaboration model:

1. Read: docs/development/workflow/REAL_TIME_COLLABORATION_ONBOARDING.md
2. Run: ./scripts/check-rule-updates.sh (if currently working)
3. Update workflow to always check for updates before starting work

Rules/workflow are now shared in real-time. Code changes remain isolated.
```

