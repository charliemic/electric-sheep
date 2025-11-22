# GitHub Sub-Issues API Check & Issue Templates

**Date**: 2025-01-20  
**Purpose**: Document how to check for sub-issues via API and create comprehensive issue templates.

---

## Part 1: Checking Sub-Issues Feature Availability

### API Limitations

**Current Status**: GitHub's API does **not directly expose** whether sub-issues are enabled. The feature is in beta and availability varies.

### Methods to Check

#### Method 1: GraphQL API Query (Limited)

**Query Repository Features**:
```graphql
query {
  repository(owner: "OWNER", name: "REPO") {
    hasIssuesEnabled
    # Note: No direct field for sub-issues feature
  }
}
```

**Limitation**: This only checks if issues are enabled, not sub-issues specifically.

#### Method 2: REST API Check (Limited)

```bash
# Check if issues are enabled
gh api repos/OWNER/REPO | jq '.has_issues'

# Check repository features (doesn't include sub-issues)
gh api repos/OWNER/REPO | jq '.features'
```

**Limitation**: Sub-issues feature status is not exposed in REST API.

#### Method 3: Manual UI Check (Most Reliable)

**Steps**:
1. Navigate to repository on GitHub
2. Go to Issues tab
3. Click "New Issue"
4. Look for "Add sub-issue" button or option
5. If visible, sub-issues are enabled

**Alternative**: Check if you can create a sub-issue from an existing issue:
1. Open any issue
2. Look for "Add sub-issue" button
3. If present, feature is enabled

#### Method 4: GraphQL Mutation Test (Try to Create)

**Test by Attempting to Create Sub-Issue**:
```graphql
mutation {
  createIssue(input: {
    repositoryId: "REPO_ID"
    title: "Test Sub-Issue"
    body: "Testing sub-issue creation"
    # If sub-issues are supported, there may be a parentId field
  }) {
    issue {
      id
      number
    }
  }
}
```

**Note**: This may fail if sub-issues aren't enabled, but the error message might indicate availability.

### Recommended Approach

**Since Relationships are available in UI:**

1. **Use Relationships Feature**: This is the standard way to create parent-child hierarchies
2. **UI is Primary**: Relationships panel in Issues UI is the main interface
3. **API May Support**: Check GraphQL API for relationship fields (may be available)
4. **Fallback**: Use Milestones for additional grouping if needed

### Script to Check

```bash
#!/bin/bash
# check-sub-issues.sh

REPO_OWNER="OWNER"
REPO_NAME="REPO"

echo "Checking if sub-issues are available..."

# Method 1: Check if issues are enabled
ISSUES_ENABLED=$(gh api repos/$REPO_OWNER/$REPO_NAME | jq -r '.has_issues')

if [ "$ISSUES_ENABLED" != "true" ]; then
  echo "❌ Issues are not enabled for this repository"
  exit 1
fi

echo "✅ Issues are enabled"

# Method 2: Try to get repository URL for manual check
REPO_URL=$(gh api repos/$REPO_OWNER/$REPO_NAME | jq -r '.html_url')
echo ""
echo "⚠️  Sub-issues feature status is not available via API"
echo "📋 Manual check required:"
echo "   1. Visit: $REPO_URL/issues"
echo "   2. Click 'New Issue'"
echo "   3. Look for 'Add sub-issue' option"
echo ""
echo "💡 If sub-issues aren't available, use milestones for grouping"
```

---

## Part 2: Issue Templates

GitHub supports two types of issue templates:

1. **Markdown Templates** (`.md`) - Simple, text-based
2. **YAML Forms** (`.yml`) - Structured forms with dynamic fields

### Template Location

All templates go in: `.github/ISSUE_TEMPLATE/`

### Template Types to Create

1. **Epic Template** - For parent epic issues
2. **Extraction Template** - For extraction sub-issues (Markdown)
3. **Extraction Form** - For extraction sub-issues (YAML form with dropdowns)
4. **Feature Template** - For feature requests
5. **Technical Template** - For technical improvements

---

## Part 3: Template Files

### Template 1: Epic (Markdown)

**File**: `.github/ISSUE_TEMPLATE/epic.md`

```markdown
---
name: Epic
about: Create a parent epic issue
title: '[Epic] [Epic Name]'
labels: 'type:epic, priority-1'
assignees: ''
---

## Epic Overview

**Epic Name**: [Name of the epic]

**Goal**: [High-level goal of this epic]

**Scope**:
- [Component 1]
- [Component 2]
- [Component 3]

**Success Criteria**:
- [ ] [Criterion 1]
- [ ] [Criterion 2]
- [ ] [Criterion 3]

## Sub-Issues

**Sub-issues will be created as children of this epic:**
- [ ] [Sub-issue 1]
- [ ] [Sub-issue 2]
- [ ] [Sub-issue 3]

## Timeline

**Estimated Duration**: [X weeks/months]

**Priority**: [Priority level]

## Notes

[Additional notes, dependencies, risks]
```

---

### Template 2: Extraction (Markdown - Simple)

**File**: `.github/ISSUE_TEMPLATE/extraction.md`

```markdown
---
name: Template/Library Extraction
about: Extract a component into a reusable template or library
title: '[Extraction] Extract [Component Name]'
labels: 'type:technical, phase:analysis'
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

---

### Template 3: Extraction Form (YAML - Advanced)

**File**: `.github/ISSUE_TEMPLATE/extraction-form.yml`

```yaml
name: Template/Library Extraction (Form)
description: Extract a component into a reusable template or library
title: "[Extraction] Extract [Component Name]"
labels: ["type:technical", "phase:analysis"]
body:
  - type: markdown
    attributes:
      value: |
        ## Component to Extract
        
        Fill out the form below to create an extraction issue.

  - type: input
    id: component_name
    attributes:
      label: Component Name
      description: Name of the component to extract (e.g., "Cursor Rules Template")
      placeholder: "Cursor Rules Template"
    validations:
      required: true

  - type: dropdown
    id: component_type
    attributes:
      label: Component Type
      description: What type of component is this?
      options:
        - Template Repository
        - Code Library
        - Documentation Template
    validations:
      required: true

  - type: dropdown
    id: extraction_type
    attributes:
      label: Extraction Type
      description: Which extraction type label should be applied?
      options:
        - cursor-rules
        - workflow-scripts
        - android-utilities
        - compose-accessibility
        - architecture
        - supabase-tools
        - metrics
        - test-automation
        - documentation
        - ci-cd
    validations:
      required: true

  - type: input
    id: source_location
    attributes:
      label: Source Location
      description: Where is the component currently located? (e.g., `.cursor/rules/`)
      placeholder: ".cursor/rules/"
    validations:
      required: true

  - type: input
    id: target_location
    attributes:
      label: Target Location
      description: Where will the extracted component live? (e.g., new repository name)
      placeholder: "cursor-rules-template"
    validations:
      required: true

  - type: dropdown
    id: priority
    attributes:
      label: Priority
      description: What is the priority of this extraction?
      options:
        - priority-1
        - priority-2
        - priority-3
        - priority-4
        - priority-5
    validations:
      required: true

  - type: dropdown
    id: effort
    attributes:
      label: Estimated Effort
      description: How much effort is required?
      options:
        - effort:small
        - effort:medium
        - effort:large
        - effort:xlarge
    validations:
      required: true

  - type: textarea
    id: current_state
    attributes:
      label: Current State
      description: Describe the current implementation
      placeholder: |
        - Current implementation details
        - Files/components involved
        - App-specific references
    validations:
      required: true

  - type: textarea
    id: generalization_needed
    attributes:
      label: Generalization Needed
      description: What needs to be parameterized or generalized?
      placeholder: |
        - App-specific references to parameterize
        - Dependencies
        - Breaking changes
    validations:
      required: true

  - type: textarea
    id: reusability_assessment
    attributes:
      label: Reusability Assessment
      description: Who would use this and what's the value?
      placeholder: |
        - Who would use this?
        - What projects would benefit?
        - What's the value?
    validations:
      required: true

  - type: checkboxes
    id: acceptance_criteria
    attributes:
      label: Acceptance Criteria
      description: Check all that apply
      options:
        - label: Component extracted successfully
        - label: App-specific references parameterized
        - label: Documentation complete
        - label: Tests pass
        - label: Electric Sheep uses extracted component
        - label: Component is usable in other projects
        - label: Migration guide available

  - type: textarea
    id: dependencies
    attributes:
      label: Dependencies
      description: List any blocking or blocked issues
      placeholder: |
        Blocks: #123, #124
        Blocked By: #125
    validations:
      required: false

  - type: input
    id: estimated_effort_weeks
    attributes:
      label: Estimated Effort (Weeks)
      description: Total estimated effort in weeks
      type: number
      placeholder: "2"
    validations:
      required: true

  - type: textarea
    id: notes
    attributes:
      label: Additional Notes
      description: Any additional notes, considerations, or risks
      placeholder: "Additional notes..."
    validations:
      required: false
```

**Benefits of YAML Form**:
- ✅ **Structured Input**: Dropdowns, checkboxes, validation
- ✅ **Auto-Labeling**: Can set labels based on form values
- ✅ **Better UX**: Guided form instead of free-form markdown
- ✅ **Validation**: Required fields, type checking
- ✅ **Dynamic**: Can use form values in issue body

---

### Template 4: Feature Request (Markdown)

**File**: `.github/ISSUE_TEMPLATE/feature.md`

```markdown
---
name: Feature Request
about: Request a new feature
title: '[Priority X] Feature Name'
labels: 'type:feature, status:not-started'
assignees: ''
---

## The Story

**The Problem**: [Describe the problem users face]

**The Vision**: [Describe the desired outcome]

**The Solution**: [Describe the proposed solution]

## Business Value

- **User Value**: [What value does this provide to users?]
- **Engagement**: [Expected impact on engagement]
- **Retention**: [Expected impact on retention]

## Success Metrics

- [Metric 1] (target: [value])
- [Metric 2] (target: [value])
- [Metric 3] (target: [value])

## Technical Details

**Files**:
- `path/to/file.kt` (new/update)

**Dependencies**: [List dependencies]

**Implementation Notes**:
- [Note 1]
- [Note 2]

**Estimated Effort**: [X weeks]

## Acceptance Criteria

- [ ] [Criterion 1]
- [ ] [Criterion 2]
- [ ] [Criterion 3]
```

---

### Template 5: Technical Improvement (Markdown)

**File**: `.github/ISSUE_TEMPLATE/technical.md`

```markdown
---
name: Technical Improvement
about: Technical improvement or refactoring
title: '[Technical] Improvement Name'
labels: 'type:technical, status:not-started'
assignees: ''
---

## The Problem

[Describe the technical problem]

## The Solution

[Describe the technical solution]

## Technical Details

**Files**:
- `path/to/file.kt` (new/update)

**Dependencies**: [List dependencies]

**Implementation Notes**:
- [Note 1]
- [Note 2]

**Estimated Effort**: [X weeks]

## Acceptance Criteria

- [ ] [Criterion 1]
- [ ] [Criterion 2]
- [ ] [Criterion 3]
```

---

## Part 4: Template Configuration

### Template Selection Screen

GitHub will show a template selection screen when creating a new issue. The order is determined by:

1. **Alphabetical order** of template files
2. **`name` field** in frontmatter (shown in selection)
3. **`about` field** in frontmatter (shown as description)

### Default Labels

Templates can set default labels in the frontmatter:
```yaml
labels: 'type:technical, extraction:cursor-rules, phase:analysis'
```

**Note**: Labels must exist in the repository before they can be used.

### Default Assignees

Templates can set default assignees:
```yaml
assignees: 'username1, username2'
```

### Default Title

Templates can set default title format:
```yaml
title: '[Extraction] Extract [Component Name]'
```

---

## Part 5: Creating Templates

### Step 1: Create Directory

```bash
mkdir -p .github/ISSUE_TEMPLATE
```

### Step 2: Create Template Files

Create the template files listed above:
- `epic.md`
- `extraction.md`
- `extraction-form.yml`
- `feature.md`
- `technical.md`

### Step 3: Commit Templates

```bash
git add .github/ISSUE_TEMPLATE/
git commit -m "docs: Add GitHub issue templates for epic and extraction work"
git push
```

### Step 4: Test Templates

1. Go to repository on GitHub
2. Click "New Issue"
3. Verify templates appear in selection
4. Test creating an issue from each template

---

## Part 6: Using Templates with Relationships (Parent-Child)

### ✅ Using Relationships Feature

**Workflow**:
1. **Create Parent Epic**: Use `epic.md` template to create the parent epic issue
2. **Create Child Issues**: Create separate issues using `extraction.md` or `extraction-form.yml` template
3. **Link as Child**: 
   - Open the child issue
   - Find **"Relationships"** panel in right sidebar
   - Click **"Add parent"**
   - Search for and select the parent epic issue
   - Child issue is now linked to parent
4. **View Hierarchy**: Parent issue will show all child issues in the Relationships panel

**Example**:
```
📋 Epic: Extract Reusable Templates and Libraries (#100)
  ├── 📝 Extract Cursor Rules Template (#101) [linked via Relationships]
  ├── 📝 Extract Workflow Scripts Template (#102) [linked via Relationships]
  └── 📝 Extract Android Utilities Library (#103) [linked via Relationships]
```

### Alternative: Manual Linking

**If Relationships panel isn't available:**

1. Create parent epic using `epic.md` template
2. Create separate issues using `extraction.md` template
3. Link issues manually in description: `Related to #123` or `Parent: #123`
4. Use milestones to group related issues
5. Use labels to track relationships

---

## Part 7: Template Best Practices

### Markdown Templates

**Use When**:
- ✅ Simple, free-form input needed
- ✅ Narrative/story-driven issues
- ✅ Flexible structure

**Best For**:
- Epic issues
- Feature requests
- Technical improvements

### YAML Forms

**Use When**:
- ✅ Structured data needed
- ✅ Validation required
- ✅ Dropdowns/checkboxes helpful
- ✅ Auto-labeling based on values

**Best For**:
- Extraction issues (with component type dropdown)
- Bug reports (with severity/environment dropdowns)
- Feature requests (with priority/area dropdowns)

### Hybrid Approach

**Recommended**:
- **Epic**: Markdown (narrative, flexible)
- **Extraction**: YAML Form (structured, validated)
- **Feature**: Markdown (narrative, story-driven)
- **Technical**: Markdown (flexible, technical details)

---

## Part 8: Automation Script

### Script to Check Sub-Issues and Create Templates

```bash
#!/bin/bash
# setup-issue-templates.sh

REPO_OWNER="OWNER"
REPO_NAME="REPO"

echo "🔍 Checking sub-issues availability..."

# Check if issues are enabled
ISSUES_ENABLED=$(gh api repos/$REPO_OWNER/$REPO_NAME | jq -r '.has_issues')

if [ "$ISSUES_ENABLED" != "true" ]; then
  echo "❌ Issues are not enabled"
  exit 1
fi

echo "✅ Issues are enabled"

# Check for sub-issues (manual check required)
echo ""
echo "⚠️  Sub-issues status:"
echo "   Visit: https://github.com/$REPO_OWNER/$REPO_NAME/issues/new"
echo "   Look for 'Add sub-issue' option"
echo ""

# Create template directory
echo "📁 Creating template directory..."
mkdir -p .github/ISSUE_TEMPLATE

echo "✅ Template directory created"
echo ""
echo "📝 Next steps:"
echo "   1. Create template files in .github/ISSUE_TEMPLATE/"
echo "   2. Commit and push templates"
echo "   3. Test templates in GitHub UI"
```

---

## Conclusion

### Relationships Feature Status

**✅ Available**: GitHub supports parent-child relationships via the **"Relationships"** panel in Issues UI.

**How to Use**:
1. **Create parent epic** - Use `epic.md` template
2. **Create child issues** - Use `extraction.md` or `extraction-form.yml` template
3. **Link via Relationships** - Use "Add parent" in Relationships panel
4. **View hierarchy** - Parent issue shows all children

**API Support**: May be available via GraphQL API, but UI is primary interface.

**Fallback**: If Relationships aren't available, use milestones + manual linking.

### Template Strategy

**Recommended Templates**:
1. **Epic** (Markdown) - Parent epic issues
2. **Extraction Form** (YAML) - Structured extraction issues
3. **Extraction** (Markdown) - Alternative simple extraction template
4. **Feature** (Markdown) - Feature requests
5. **Technical** (Markdown) - Technical improvements

**Benefits**:
- ✅ Consistent issue structure
- ✅ Guided input (YAML forms)
- ✅ Auto-labeling
- ✅ Validation
- ✅ Better UX

---

**Related Documents**:
- `docs/development/analysis/TEMPLATE_EXTRACTION_AND_TICKETING.md` - Extraction strategy
- `docs/development/GITHUB_ISSUES_SETUP.md` - GitHub Issues setup guide

