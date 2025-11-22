# Template Extraction Strategy & Ticketing Approach

**Date**: 2025-01-20  
**Purpose**: Research template extraction patterns and define ticketing approach for tracking extraction work.

---

## Part 1: Template Extraction Patterns

### Research Findings

Based on industry best practices and common patterns, here are the main approaches for extracting reusable templates:

### Approach 1: GitHub Template Repository

**Pattern**: Create a dedicated GitHub repository that serves as a template/starter kit.

**How It Works**:
1. Create a new repository (e.g., `cursor-rules-template`, `android-dev-workflow-template`)
2. Copy generalizable components from source project
3. Remove app-specific references
4. Add parameterization/config files
5. Add README with usage instructions
6. Mark repository as "Template" in GitHub settings

**Examples in the Wild**:
- `cookiecutter` templates (Python project templates)
- `create-react-app` (React starter template)
- `vite` templates (various framework templates)
- GitHub's own template repositories

**Advantages**:
- ✅ Easy to use: "Use this template" button in GitHub
- ✅ Version control: Full git history
- ✅ Independent versioning: Can version template separately
- ✅ Community sharing: Can open source and share
- ✅ Clear separation: Template repo vs. app repo

**Disadvantages**:
- ❌ Maintenance overhead: Two repos to maintain
- ❌ Sync complexity: Updates need to be synced back
- ❌ Initial setup: Need to create new repo structure

**Best For**:
- Complete starter kits (e.g., "Android app with Cursor rules")
- Templates that are used to start new projects
- Templates that don't change frequently

**Our Use Case**:
- ✅ **Cursor Rules Template** - Complete set of rules for new projects
- ✅ **Workflow Scripts Template** - Git workflow scripts for new projects
- ⚠️ **Component Libraries** - Better as separate packages (see Approach 2)

---

### Approach 2: Separate Package/Library Repository

**Pattern**: Extract components into a separate repository that's used as a dependency.

**How It Works**:
1. Create a new repository for the library (e.g., `android-utilities`, `compose-accessibility`)
2. Extract generalizable code
3. Package as library (Maven, npm, etc.)
4. Publish to package registry (Maven Central, npm, etc.)
5. Use as dependency in app

**Examples in the Wild**:
- Android libraries (Material Components, Room, etc.)
- npm packages (lodash, axios, etc.)
- Python packages (requests, pandas, etc.)

**Advantages**:
- ✅ Version management: Semantic versioning
- ✅ Dependency management: Standard package managers
- ✅ Reusability: Can be used in multiple projects
- ✅ Independent development: Can evolve separately
- ✅ Community contribution: Open source potential

**Disadvantages**:
- ❌ Setup complexity: Need to set up build/publish pipeline
- ❌ Breaking changes: Need to manage versioning carefully
- ❌ Maintenance: Separate repo to maintain

**Best For**:
- Code libraries (utilities, UI components, architecture patterns)
- Components that evolve independently
- Components used across multiple projects

**Our Use Case**:
- ✅ **Android Utilities Library** - Logger, DateFormatter, error handling
- ✅ **Compose Accessibility Library** - AccessibleButton, AccessibleCard, etc.
- ✅ **Architecture Libraries** - Auth library, Feature flags library, Sync library
- ✅ **Supabase Tools Library** - PostgREST, auth admin, DB admin scripts

---

### Approach 3: Monorepo with Shared Packages

**Pattern**: Keep everything in one repository but organize into packages/modules.

**How It Works**:
1. Organize codebase into packages (e.g., `packages/cursor-rules`, `packages/workflow-scripts`)
2. Use workspace tools (Gradle, npm workspaces, etc.)
3. Share packages within monorepo
4. Optionally publish packages externally

**Examples in the Wild**:
- Google's monorepo (Bazel)
- Facebook's monorepo (Buck)
- Lerna monorepos (JavaScript)
- Gradle multi-project builds (Android)

**Advantages**:
- ✅ Single repository: Easier to manage
- ✅ Atomic changes: Can update app and library together
- ✅ Shared tooling: Same CI/CD, same standards
- ✅ Easy refactoring: Can refactor across packages easily

**Disadvantages**:
- ❌ Repository size: Can grow large
- ❌ Coupling risk: Packages might become too coupled
- ❌ Sharing complexity: Harder to share with other projects

**Best For**:
- Projects with multiple related packages
- When packages evolve together
- When you want atomic updates

**Our Use Case**:
- ⚠️ **Not recommended** - We want to share templates with other projects
- ⚠️ **Better for libraries** - If we extract libraries, monorepo could work
- ✅ **Consider for future** - If we build multiple related libraries

---

### Approach 4: Hybrid: Template Repo + Library Packages

**Pattern**: Use template repos for starter kits, library packages for reusable code.

**How It Works**:
1. **Template Repos**: For complete starter kits (Cursor rules, workflow scripts)
2. **Library Packages**: For reusable code (utilities, UI components)
3. **App Repo**: Uses libraries as dependencies, follows template patterns

**Advantages**:
- ✅ Best of both worlds: Templates for structure, libraries for code
- ✅ Flexibility: Can use templates independently or with libraries
- ✅ Clear separation: Different purposes, different approaches

**Our Recommended Approach**:
- ✅ **Template Repos**: Cursor rules, workflow scripts, documentation templates
- ✅ **Library Packages**: Android utilities, Compose components, architecture libraries
- ✅ **Hybrid**: Use templates to start projects, libraries as dependencies

---

## Part 2: Ticketing Approach for Template Extraction

### Overview

Based on the existing GitHub Issues system in this project, here's the ticketing structure I would create for tracking template extraction work.

### Epic Structure (Using GitHub Sub-Issues)

**✅ GitHub Supports Issue Hierarchy!**

GitHub now supports **sub-issues** (beta/opt-in feature) that allow you to create parent-child relationships between issues. This enables true epic/sub-issue hierarchy.

**Parent Epic**: "Extract Reusable Templates and Libraries"

**How to Create**:
1. Create the parent epic issue
2. Add sub-issues for each extraction component
3. GitHub will show the hierarchy in the issue view
4. Progress tracking automatically includes sub-issues

**Description**:
```markdown
Extract generalizable components from Electric Sheep into reusable templates and libraries.

**Goal**: Create reusable components that can benefit other projects while maintaining Electric Sheep's functionality.

**Scope**:
- Cursor rules template repository
- Workflow scripts template repository
- Android utilities library
- Compose accessibility library
- Architecture libraries (auth, feature flags, sync)
- Supabase tools library
- Metrics library
- Test automation framework
- Documentation templates
- CI/CD templates

**Success Criteria**:
- [ ] All high-value components extracted
- [ ] Templates are parameterized and documented
- [ ] Libraries are published and usable
- [ ] Electric Sheep uses extracted components
- [ ] Documentation complete

**Sub-Issues**:
- See sub-issues below for individual extraction components
```

**Sub-Issues Structure**:
- Each extraction component becomes a sub-issue of the parent epic
- Sub-issues can have their own phases tracked via labels
- Progress on sub-issues automatically reflects in parent epic
- Can view hierarchy in GitHub Issues UI

### Issue Labels

**Extend existing label structure with extraction-specific labels:**

#### New Labels

**Extraction Labels**:
- `extraction:cursor-rules` - Cursor rules template extraction
- `extraction:workflow-scripts` - Workflow scripts template extraction
- `extraction:android-utilities` - Android utilities library extraction
- `extraction:compose-accessibility` - Compose accessibility library extraction
- `extraction:architecture` - Architecture library extraction
- `extraction:supabase-tools` - Supabase tools library extraction
- `extraction:metrics` - Metrics library extraction
- `extraction:test-automation` - Test automation framework extraction
- `extraction:documentation` - Documentation template extraction
- `extraction:ci-cd` - CI/CD template extraction

**Extraction Phase Labels**:
- `phase:analysis` - Analysis and planning phase
- `phase:extraction` - Code extraction phase
- `phase:parameterization` - Parameterization and generalization phase
- `phase:documentation` - Documentation phase
- `phase:testing` - Testing phase
- `phase:integration` - Integration back into app phase
- `phase:publication` - Publication/release phase

**Extraction Type Labels**:
- `extraction-type:template` - Template repository
- `extraction-type:library` - Code library/package
- `extraction-type:documentation` - Documentation template

### Hierarchy Options

**Option 1: Relationships Feature (Recommended - Native GitHub Feature)**

GitHub supports **parent-child relationships** via the **"Relationships"** panel in Issues UI:

**Structure**:
```
📋 Epic: Extract Reusable Templates and Libraries (Parent)
  ├── 📝 Sub-issue: Extract Cursor Rules Template
  ├── 📝 Sub-issue: Extract Workflow Scripts Template
  ├── 📝 Sub-issue: Extract Android Utilities Library
  ├── 📝 Sub-issue: Extract Compose Accessibility Library
  ├── 📝 Sub-issue: Extract Architecture Libraries
  │     ├── 📝 Sub-issue: Extract Auth Library
  │     ├── 📝 Sub-issue: Extract Feature Flags Library
  │     └── 📝 Sub-issue: Extract Sync Library
  ├── 📝 Sub-issue: Extract Supabase Tools Library
  ├── 📝 Sub-issue: Extract Metrics Library
  ├── 📝 Sub-issue: Extract Test Automation Framework
  ├── 📝 Sub-issue: Extract Documentation Templates
  └── 📝 Sub-issue: Extract CI/CD Templates
```

**Benefits**:
- ✅ True hierarchy (parent-child relationships)
- ✅ Progress tracking (sub-issue progress reflects in parent)
- ✅ Visual hierarchy in GitHub UI
- ✅ Native GitHub feature (no third-party tools needed)
- ✅ Works with GitHub Projects

**How to Use**:
- Open any issue in GitHub
- Find **"Relationships"** panel in right sidebar
- Click **"Add parent"** to link as child of another issue
- Parent issue will show all children in Relationships panel

**Option 2: Milestones (Alternative - Grouping)**

If sub-issues aren't available, use milestones to group related issues:

1. **Cursor Rules Template** - Extract and parameterize Cursor rules
2. **Workflow Scripts Template** - Extract workflow scripts
3. **Android Utilities Library** - Extract Android utilities
4. **Compose Accessibility Library** - Extract Compose components
5. **Architecture Libraries** - Extract architecture patterns
6. **Supabase Tools Library** - Extract Supabase tools
7. **Metrics Library** - Extract metrics scripts
8. **Test Automation Framework** - Extract test framework
9. **Documentation Templates** - Extract documentation templates
10. **CI/CD Templates** - Extract CI/CD workflows

**Benefits**:
- ✅ Always available (not beta)
- ✅ Good for grouping related work
- ✅ Can filter by milestone

**Limitations**:
- ❌ Not true hierarchy (just grouping)
- ❌ No automatic progress tracking
- ❌ Can't nest milestones

**Recommendation**: Use **Relationships** feature (available in UI), use **Milestones** for additional grouping.

### Issue Templates

**Create issue template for extraction work:**

#### `.github/ISSUE_TEMPLATE/extraction.md`

```markdown
---
name: Template/Library Extraction
about: Extract a component into a reusable template or library
title: '[Extraction] Extract [Component Name]'
labels: 'type:technical, extraction:[component], phase:analysis'
assignees: ''
---

## Component to Extract

**Component Name**: [e.g., Cursor Rules Template]

**Component Type**: 
- [ ] Template Repository
- [ ] Code Library
- [ ] Documentation Template

**Source Location**: [e.g., `.cursor/rules/`]

**Target Location**: [e.g., New repository: `cursor-rules-template`]

## Analysis

**Current State**:
- [Describe current implementation]
- [List files/components involved]
- [Identify app-specific references]

**Generalization Needed**:
- [List app-specific references to parameterize]
- [Identify dependencies]
- [Identify breaking changes]

**Reusability Assessment**:
- [Who would use this?]
- [What projects would benefit?]
- [What's the value?]

## Extraction Plan

**Phase 1: Analysis** (Current)
- [ ] Analyze component structure
- [ ] Identify app-specific references
- [ ] Document dependencies
- [ ] Assess reusability

**Phase 2: Extraction**
- [ ] Create target repository/package
- [ ] Copy generalizable code
- [ ] Remove app-specific code
- [ ] Set up build/publish pipeline (if library)

**Phase 3: Parameterization**
- [ ] Replace app-specific references with variables
- [ ] Create configuration files
- [ ] Add customization points
- [ ] Test parameterization

**Phase 4: Documentation**
- [ ] Write README
- [ ] Document usage
- [ ] Add examples
- [ ] Create migration guide

**Phase 5: Testing**
- [ ] Test in isolation
- [ ] Test in new project
- [ ] Verify functionality
- [ ] Test edge cases

**Phase 6: Integration**
- [ ] Update Electric Sheep to use extracted component
- [ ] Verify app still works
- [ ] Update dependencies
- [ ] Test integration

**Phase 7: Publication** (If applicable)
- [ ] Publish to package registry (if library)
- [ ] Mark as template (if template repo)
- [ ] Create release notes
- [ ] Announce/share

## Acceptance Criteria

- [ ] Component extracted successfully
- [ ] App-specific references parameterized
- [ ] Documentation complete
- [ ] Tests pass
- [ ] Electric Sheep uses extracted component
- [ ] Component is usable in other projects
- [ ] Migration guide available

## Dependencies

**Blocks**: [List issues this blocks]

**Blocked By**: [List issues that block this]

## Estimated Effort

- **Analysis**: [X days]
- **Extraction**: [X days]
- **Parameterization**: [X days]
- **Documentation**: [X days]
- **Testing**: [X days]
- **Integration**: [X days]
- **Publication**: [X days]
- **Total**: [X weeks]

## Notes

[Additional notes, considerations, risks]
```

### Example Issues

#### Parent Epic: Extract Reusable Templates and Libraries

**Title**: `[Epic] Extract Reusable Templates and Libraries`

**Type**: Parent Epic (will have sub-issues)

**Labels**: 
- `type:technical`
- `priority-1`
- `effort:xlarge`

**Description**: [Epic description from above]

**Sub-Issues**: 10 sub-issues (one per extraction component)

---

#### Sub-Issue 1: Cursor Rules Template Extraction

**Title**: `[Extraction] Extract Cursor Rules Template`

**Type**: Sub-issue (child of Epic)

**Labels**: 
- `type:technical`
- `extraction:cursor-rules`
- `extraction-type:template`
- `phase:analysis`
- `priority-1`
- `effort:medium`

**Parent**: Epic: Extract Reusable Templates and Libraries

**Description**: [Use extraction template above]

**Subtasks** (Checklist in issue):
- [ ] Analyze `.cursor/rules/` structure
- [ ] Identify "Electric Sheep" references
- [ ] Create `cursor-rules-template` repository
- [ ] Copy rules and parameterize
- [ ] Create README with usage instructions
- [ ] Test in new project
- [ ] Update Electric Sheep to reference template
- [ ] Publish template repository

---

#### Issue 2: Android Utilities Library Extraction

**Title**: `[Extraction] Extract Android Utilities Library`

**Labels**:
- `type:technical`
- `extraction:android-utilities`
- `extraction-type:library`
- `phase:analysis`
- `priority-2`
- `effort:large`

**Milestone**: `Android Utilities Library`

**Description**: [Use extraction template]

**Subtasks**:
- [ ] Analyze `app/src/main/java/com/electricsheep/app/util/`
- [ ] Create `android-utilities` library repository
- [ ] Extract Logger, DateFormatter, error handling
- [ ] Set up Gradle library build
- [ ] Publish to Maven (local or public)
- [ ] Update Electric Sheep to use library
- [ ] Write documentation
- [ ] Create examples

---

### GitHub Project (Kanban Board)

**Create a GitHub Project for extraction work:**

**Columns**:
1. **Backlog** - Not started extractions
2. **Analysis** - Currently analyzing
3. **Extraction** - Currently extracting
4. **Parameterization** - Currently parameterizing
5. **Documentation** - Currently documenting
6. **Testing** - Currently testing
7. **Integration** - Currently integrating
8. **Published** - Completed and published

**Filters**:
- By component type (template vs library)
- By priority
- By phase
- By assignee

**Views**:
- All Extractions
- Templates Only
- Libraries Only
- High Priority
- In Progress
- Blocked

---

### Workflow Integration

#### For Agents

**Before Starting Extraction**:
1. Check GitHub Issues: `is:open label:extraction:* label:phase:analysis`
2. Find extraction to work on
3. Assign yourself
4. Update labels: Add `phase:extraction`, remove `phase:analysis`
5. Create branch: `feature/extract-[component-name]`

**During Extraction**:
1. Update issue with progress (comments)
2. Update phase labels as you progress
3. Link PR when created
4. Update subtasks checklist

**When Completing**:
1. PR merged → Issue automatically links
2. Update phase to `phase:published`
3. Close issue
4. Update extraction status in master index

#### For Tracking

**Master Index** (Markdown file):
```markdown
# Template Extraction Status

## Overview
[Status summary, progress, next steps]

## Extractions

### Cursor Rules Template
- **Status**: 🟡 In Progress (Phase: Parameterization)
- **Issue**: [#123](link)
- **Repository**: [cursor-rules-template](link)
- **Progress**: 60% complete

### Android Utilities Library
- **Status**: 🔴 Not Started
- **Issue**: [#124](link)
- **Repository**: TBD
- **Progress**: 0% complete

[Continue for each extraction...]
```

---

## Part 3: Recommended Extraction Plan

### Phase 1: High-Value Templates (Quick Wins)

**Priority**: Start with templates that are easiest to extract and highest value.

1. **Cursor Rules Template** (2-3 weeks)
   - ✅ High value: Reusable across all Cursor projects
   - ✅ Low complexity: Mostly documentation
   - ✅ Quick win: Can extract quickly

2. **Workflow Scripts Template** (2-3 weeks)
   - ✅ High value: Reusable across all Git projects
   - ✅ Medium complexity: Need to parameterize
   - ✅ Quick win: Scripts are self-contained

### Phase 2: Documentation Templates (Medium Effort)

3. **Documentation Templates** (3-4 weeks)
   - ✅ High value: Reusable across all projects
   - ✅ Low complexity: Documentation only
   - ✅ Medium effort: Need to organize and template

### Phase 3: Code Libraries (Higher Effort)

4. **Android Utilities Library** (4-6 weeks)
   - ✅ High value: Reusable across Android projects
   - ⚠️ Medium complexity: Need build setup
   - ⚠️ Medium effort: Need to set up publishing

5. **Compose Accessibility Library** (4-6 weeks)
   - ✅ High value: Reusable across Compose projects
   - ⚠️ Medium complexity: UI components
   - ⚠️ Medium effort: Need examples and docs

### Phase 4: Architecture Libraries (Complex)

6. **Architecture Libraries** (6-8 weeks each)
   - Auth library
   - Feature flags library
   - Sync library
   - ⚠️ High complexity: Architecture patterns
   - ⚠️ High effort: Need comprehensive docs

### Phase 5: Infrastructure (Complex)

7. **Supabase Tools Library** (4-6 weeks)
   - ✅ High value: Reusable across Supabase projects
   - ⚠️ Medium complexity: Scripts + documentation
   - ⚠️ Medium effort: Need to organize

8. **Metrics Library** (3-4 weeks)
   - ✅ Medium value: Reusable across AI-assisted projects
   - ⚠️ Low complexity: Scripts
   - ⚠️ Medium effort: Need documentation

9. **Test Automation Framework** (6-8 weeks)
   - ✅ High value: Reusable across mobile apps
   - ⚠️ High complexity: Framework architecture
   - ⚠️ High effort: Need comprehensive docs

10. **CI/CD Templates** (2-3 weeks)
    - ✅ Medium value: Reusable across Android projects
    - ⚠️ Low complexity: Workflow files
    - ⚠️ Low effort: Mostly copy-paste

---

## Part 4: Ticketing Structure Summary

### What I Would Create

1. **Parent Epic Issue**: "Extract Reusable Templates and Libraries"
   - Overview, scope, success criteria
   - **Type**: Parent issue (will have sub-issues)
   - **Progress**: Automatically tracked from sub-issues

2. **10 Sub-Issues**: One per extraction component (children of epic)
   - Cursor Rules Template (sub-issue)
   - Workflow Scripts Template (sub-issue)
   - Android Utilities Library (sub-issue)
   - Compose Accessibility Library (sub-issue)
   - Architecture Libraries (sub-issue, with 3 nested sub-issues for auth/feature-flags/sync)
   - Supabase Tools Library (sub-issue)
   - Metrics Library (sub-issue)
   - Test Automation Framework (sub-issue)
   - Documentation Templates (sub-issue)
   - CI/CD Templates (sub-issue)
   - **Type**: Sub-issues (children of parent epic)
   - **Progress**: Tracked individually, contributes to parent

3. **Optional: Milestones** (if not using sub-issues, or for additional grouping)
   - Can still use milestones for grouping by phase or priority
   - Or use labels instead (simpler)

4. **Issue Structure**:
   - Use extraction issue template
   - Phased approach (analysis → extraction → parameterization → documentation → testing → integration → publication)
   - Clear acceptance criteria
   - Dependencies tracked
   - **Hierarchy**: Parent epic → Sub-issues → Subtasks (checklist)

4. **GitHub Project (Kanban)**: Visual tracking
   - 8 columns (Backlog → Analysis → Extraction → Parameterization → Documentation → Testing → Integration → Published)
   - Filterable by component type, priority, phase

5. **Master Index (Markdown)**: Status overview
   - High-level progress
   - Links to issues
   - Quick reference

6. **Issue Template**: Standardized format
   - Extraction issue template
   - Consistent structure
   - Clear phases and acceptance criteria

### Label Structure

**Existing Labels** (reuse):
- `type:technical`
- `priority-1` through `priority-5`
- `effort:small`, `effort:medium`, `effort:large`, `effort:xlarge`
- `status:not-started`, `status:in-progress`, `status:blocked`, `status:review`, `status:completed`

**New Labels** (add):
- `extraction:cursor-rules`, `extraction:workflow-scripts`, etc. (10 labels)
- `phase:analysis`, `phase:extraction`, etc. (7 labels)
- `extraction-type:template`, `extraction-type:library`, `extraction-type:documentation` (3 labels)

**Total**: ~20 new labels for extraction work

---

## Conclusion

### Template Extraction Strategy

**Recommended Approach**: **Hybrid (Template Repos + Library Packages)**

- **Template Repos**: Cursor rules, workflow scripts, documentation templates, CI/CD templates
- **Library Packages**: Android utilities, Compose components, architecture libraries, Supabase tools, metrics
- **Benefits**: Best of both worlds, clear separation, flexible usage

### Ticketing Approach with Hierarchy

**Recommended Structure** (Using GitHub Sub-Issues):

1. **Parent Epic Issue** - "Extract Reusable Templates and Libraries"
   - Overview, scope, success criteria
   - **Progress**: Automatically tracked from sub-issues

2. **10 Sub-Issues** - One per extraction component (children of epic)
   - Each sub-issue tracks one extraction component
   - **Progress**: Tracked individually, contributes to parent
   - **Hierarchy**: Parent → Sub-issues → Subtasks (checklist)

3. **GitHub Project (Kanban)** - Visual tracking with hierarchy support
   - Can view parent-child relationships
   - Progress tracking includes sub-issues

4. **Master Index (Markdown)** - Status overview
   - High-level progress
   - Links to epic and sub-issues

5. **Issue Template** - Standardized format
   - Consistent structure
   - Clear phases and acceptance criteria

**Hierarchy Options Summary**:

| Option | Type | Hierarchy | Progress Tracking | Availability |
|--------|------|-----------|-------------------|--------------|
| **Sub-Issues** | Native GitHub | ✅ True parent-child | ✅ Automatic | Beta/opt-in |
| **Milestones** | Native GitHub | ⚠️ Grouping only | ❌ Manual | Always available |
| **Labels** | Native GitHub | ❌ No hierarchy | ❌ Manual | Always available |
| **Projects** | Native GitHub | ⚠️ Visual grouping | ✅ Automatic | Always available |

**Recommendation**: 
- **Primary**: Use **Sub-Issues** for true hierarchy (if available)
- **Fallback**: Use **Milestones** + **Labels** for grouping (if sub-issues not available)
- **Visual**: Use **GitHub Projects** for Kanban board with hierarchy view

**Integration**:
- Use existing GitHub Issues system
- Extend with extraction-specific labels
- Follow existing workflow patterns
- Maintain master index for overview
- **Enable sub-issues** if not already enabled

**Next Steps**:
1. **Verify Relationships Feature**:
   - Open any issue in GitHub
   - Check for "Relationships" panel in right sidebar
   - Confirm "Add parent" option is available ✅
2. **Create Parent Epic Issue**: "Extract Reusable Templates and Libraries"
   - Use `epic.md` template
3. **Create Child Issues**: 10 issues (one per extraction component)
   - Use `extraction.md` or `extraction-form.yml` template
4. **Link via Relationships**:
   - Open each child issue
   - Use "Add parent" in Relationships panel
   - Select parent epic
5. **Set up GitHub Project**: Kanban board with hierarchy support
6. **Create Master Index**: Markdown file for status overview
7. **Begin Phase 1 Extractions**: Start with Cursor Rules Template

**How to Create Parent-Child Relationships**:

**Via GitHub UI (Recommended)**:
1. Create parent epic issue using `epic.md` template
2. Create child issues using `extraction.md` or `extraction-form.yml` template
3. Open each child issue
4. Find **"Relationships"** panel in right sidebar
5. Click **"Add parent"**
6. Search for and select the parent epic issue
7. Child issue is now linked to parent ✅

**Via GitHub CLI** (create issues, link via UI):
```bash
# Create parent epic
gh issue create --title "[Epic] Extract Reusable Templates and Libraries" \
  --body-file .github/ISSUE_TEMPLATE/epic.md

# Create child issues
gh issue create --title "[Extraction] Extract Cursor Rules Template" \
  --body-file .github/ISSUE_TEMPLATE/extraction.md

# Note: Linking via Relationships must be done in UI
```

**Note**: Relationships feature is available in GitHub Issues UI. API support may vary - UI is the primary interface.

---

**Related Documents**:
- `docs/development/analysis/APP_SPECIFICITY_ANALYSIS.md` - Component categorization
- `docs/development/GITHUB_ISSUES_SETUP.md` - GitHub Issues setup guide
- `docs/development/TODO_SYSTEM_SUMMARY.md` - TODO + GitHub Issues system

