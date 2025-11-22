# Legal Documentation Summary

**Quick Reference for Electric Sheep Legal Requirements**

## Current Status

- ❌ **LICENSE file**: Missing
- ❌ **Privacy Policy**: Missing (required if collecting user data)
- ❌ **Third-Party Licenses**: Not documented
- ✅ **Security**: Good (no secrets in repo)

## Immediate Actions Required

### 1. Add LICENSE File (5 minutes)

**Location**: Root of repository (`LICENSE`)

**Recommended**: MIT License (simple, permissive, good for personal projects)

**Action**: Copy template from `docs/legal/templates/LICENSE_MIT.txt` and customize

### 2. Create Privacy Policy (30 minutes)

**Location**: `docs/legal/PRIVACY_POLICY.md`

**Required if**:
- App collects user data (mood entries, email, etc.)
- Publishing to Play Store
- Users from EU/California

**Action**: Use template from `docs/legal/templates/PRIVACY_POLICY_TEMPLATE.md`

### 3. Document Third-Party Licenses (15 minutes)

**Location**: `NOTICES.md` (root) or `docs/legal/THIRD_PARTY_LICENSES.md`

**Action**: 
- List all dependencies from `app/build.gradle.kts`
- Document license for each (most are Apache 2.0)
- Or use Gradle plugin to auto-generate

## Internal Tracking (Optional but Recommended)

### 4. Compliance Tracking

**Location**: `docs/legal/COMPLIANCE_TRACKING.md` (internal)

**Purpose**: Track legal compliance status and decisions

### 5. Data Collection Inventory

**Location**: `docs/legal/DATA_COLLECTION_INVENTORY.md` (internal)

**Purpose**: Document what data is collected, where stored, how used

## For Personal/Non-Profit Projects

**Good News**: Legal requirements are minimal for personal projects.

**Still Recommended**:
- License file (clarifies how others can use your code)
- Privacy Policy (if collecting data, required for Play Store)
- Third-party licenses (good practice, may be required)

**Not Required**:
- Terms of Service (unless commercial)
- Contributor License Agreement (unless accepting contributions)
- Complex compliance (unless significant user base)

## Quick Decision Guide

**Q: Do I need a Privacy Policy?**
- ✅ **Yes** if: App collects user data (mood entries, email) OR publishing to Play Store
- ❌ **No** if: Truly offline-only, no data collection, not publishing

**Q: Which license should I choose?**
- **MIT**: Simple, permissive, good for learning projects (recommended)
- **Apache 2.0**: More explicit, includes patent protection
- **Unlicense**: Public domain, maximum freedom

**Q: Do I need Terms of Service?**
- ❌ **No** for personal, non-profit projects
- ✅ **Yes** if: Commercial, user-generated content, payments

## File Structure

```
electric-sheep/
├── LICENSE                    # Public (required)
├── NOTICES.md                 # Public (recommended)
├── docs/
│   └── legal/
│       ├── PRIVACY_POLICY.md           # Public (if collecting data)
│       ├── COMPLIANCE_TRACKING.md      # Internal (optional)
│       └── DATA_COLLECTION_INVENTORY.md # Internal (optional)
```

## See Also

- **Full Evaluation**: `docs/legal/LEGAL_EVALUATION.md`
- **Templates**: `docs/legal/templates/`

