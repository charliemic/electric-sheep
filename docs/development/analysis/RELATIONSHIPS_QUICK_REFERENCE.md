# GitHub Relationships Feature - Quick Reference

**Date**: 2025-01-20  
**Purpose**: Quick guide for using GitHub's Relationships feature to create parent-child issue hierarchies.

---

## ✅ Feature Available: Relationships Panel

GitHub supports parent-child relationships through the **"Relationships"** panel in Issues UI.

**Location**: Right sidebar of any issue page

---

## How to Create Parent-Child Relationships

### Step 1: Create Parent Epic

1. Go to Issues → New Issue
2. Select **"Epic"** template (or use `epic.md`)
3. Fill out epic details
4. Create issue (this becomes the parent)

**Example**: `[Epic] Extract Reusable Templates and Libraries` (#100)

### Step 2: Create Child Issues

1. Go to Issues → New Issue
2. Select **"Template/Library Extraction"** template (or use `extraction.md` or `extraction-form.yml`)
3. Fill out extraction details
4. Create issue (this becomes a child)

**Example**: `[Extraction] Extract Cursor Rules Template` (#101)

### Step 3: Link Child to Parent

1. Open the **child issue** (#101)
2. Find **"Relationships"** panel in right sidebar
3. Click **"Add parent"**
4. Search for parent issue (e.g., #100)
5. Select parent issue
6. ✅ Child is now linked to parent!

### Step 4: Verify Hierarchy

1. Open the **parent issue** (#100)
2. Check **"Relationships"** panel
3. You should see all child issues listed
4. ✅ Hierarchy is visible!

---

## Relationships Panel Options

The Relationships panel provides three types of relationships:

### 1. Add Parent
- **Use**: Link issue as child of another issue
- **When**: Creating sub-issues for an epic
- **Result**: Parent shows all children

### 2. Mark as Blocked By
- **Use**: Indicate this issue is blocked by another
- **When**: Issue cannot proceed until another is complete
- **Result**: Shows blocking dependency

### 3. Mark as Blocking
- **Use**: Indicate this issue blocks another
- **When**: This issue must complete before another can start
- **Result**: Shows what this issue blocks

---

## Example Workflow: Template Extraction Epic

### 1. Create Parent Epic

**Issue #100**: `[Epic] Extract Reusable Templates and Libraries`
- Use `epic.md` template
- Set priority, scope, success criteria

### 2. Create Child Issues

**Issue #101**: `[Extraction] Extract Cursor Rules Template`
- Use `extraction.md` or `extraction-form.yml` template
- Fill out component details

**Issue #102**: `[Extraction] Extract Workflow Scripts Template`
- Use extraction template
- Fill out component details

**Issue #103**: `[Extraction] Extract Android Utilities Library`
- Use extraction template
- Fill out component details

### 3. Link Children to Parent

For each child issue (#101, #102, #103):
1. Open child issue
2. Click **"Add parent"** in Relationships panel
3. Search for #100 (parent epic)
4. Select parent
5. ✅ Linked!

### 4. View Hierarchy

Open parent issue #100:
- **Relationships** panel shows:
  - 📝 Extract Cursor Rules Template (#101)
  - 📝 Extract Workflow Scripts Template (#102)
  - 📝 Extract Android Utilities Library (#103)

---

## Visual Hierarchy

```
📋 Epic: Extract Reusable Templates and Libraries (#100)
  ├── 📝 Extract Cursor Rules Template (#101) [linked via Relationships]
  ├── 📝 Extract Workflow Scripts Template (#102) [linked via Relationships]
  ├── 📝 Extract Android Utilities Library (#103) [linked via Relationships]
  └── ... (more children)
```

---

## GitHub Projects Integration

Relationships work with GitHub Projects:

1. **Enable "Parent issue" Field**:
   - In project table view
   - Click `+` in field header
   - Select "Parent issue" from hidden fields

2. **Enable "Sub-issue progress" Field**:
   - In project table view
   - Click `+` in field header
   - Select "Sub-issue progress" from hidden fields

3. **View Hierarchy**:
   - Parent issue shows in "Parent issue" column
   - Progress tracked in "Sub-issue progress" column

---

## Tips

### ✅ Best Practices

1. **Create parent first** - Establish epic before creating children
2. **Use templates** - Consistent structure for all issues
3. **Link immediately** - Link children to parent right after creation
4. **Verify hierarchy** - Check parent issue to see all children
5. **Use labels** - Add extraction-specific labels for filtering

### ⚠️ Common Issues

1. **Relationships panel not visible?**
   - Check if you're on the issue page (not list view)
   - Look in right sidebar
   - May need to scroll down

2. **Can't find parent issue?**
   - Make sure parent issue exists
   - Check issue number is correct
   - Try searching by title

3. **Child not showing in parent?**
   - Verify link was created successfully
   - Refresh parent issue page
   - Check Relationships panel in parent

---

## Quick Commands

### Create Parent Epic
```bash
gh issue create \
  --title "[Epic] Extract Reusable Templates and Libraries" \
  --body-file .github/ISSUE_TEMPLATE/epic.md \
  --label "type:epic,priority-1"
```

### Create Child Issue
```bash
gh issue create \
  --title "[Extraction] Extract Cursor Rules Template" \
  --body-file .github/ISSUE_TEMPLATE/extraction.md \
  --label "type:technical,extraction:cursor-rules,phase:analysis"
```

**Note**: Linking via Relationships must be done in UI (not via CLI).

---

## Related Documents

- `docs/development/analysis/TEMPLATE_EXTRACTION_AND_TICKETING.md` - Full extraction strategy
- `docs/development/analysis/GITHUB_SUB_ISSUES_AND_TEMPLATES.md` - Templates and API info
- `.github/ISSUE_TEMPLATE/` - Issue templates

