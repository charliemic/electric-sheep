# Artifact Duplication Prevention

**Date**: 2025-01-20  
**Status**: Complete  
**Purpose**: Prevent agents from duplicating existing scripts, documents, and code artifacts

## Problem Statement

Agents were frequently creating duplicate or replicated artifacts:
- **Scripts**: Multiple Google Docs upload scripts (`google-docs-api-upload.py`, `google-docs-browser-upload.py`, `google-docs-workflow.py`, etc.)
- **Documentation**: Multiple AWS Bedrock setup guides (`AWS_BEDROCK_SETUP_WALKTHROUGH.md`, `AWS_BEDROCK_QUICK_START.md`)
- **Code**: Similar implementations that could be consolidated

This led to:
- Maintenance burden (multiple files to update)
- Confusion about which artifact to use
- Code duplication violating DRY principles
- Repository clutter

## Solution

Created comprehensive rules and tooling to prevent duplication:

### 1. Cursor Rule: `.cursor/rules/artifact-duplication.mdc`

**Purpose**: Enforce artifact duplication prevention for all agents

**Key Requirements**:
- ✅ **MANDATORY**: Search for existing artifacts before creating new ones
- ✅ Use multiple search methods (file name, content, semantic)
- ✅ Evaluate if existing artifact can be extended vs creating new
- ✅ Document why new artifact is needed if existing cannot be used
- ✅ Consolidate duplicates when found

**Decision Tree**:
- Same purpose? → **EXTEND** existing
- Different purpose? → **CREATE** new (document why)
- Duplicates found? → **CONSOLIDATE**

### 2. Helper Script: `scripts/check-existing-artifacts.sh`

**Purpose**: Automated search for existing artifacts

**Usage**:
```bash
./scripts/check-existing-artifacts.sh <keyword>
```

**Searches**:
- Scripts matching keyword (by name)
- Documentation matching keyword (by name)
- Scripts containing keyword (content search)
- Documentation containing keyword (content search)
- Code files matching keyword

**Output**: Lists all matching artifacts with context to help decide if extension is possible

### 3. Pre-Work Check Integration

**Updated**: `scripts/pre-work-check.sh`

**Added**: Reminder to check for existing artifacts before creating new ones

**Integration**: Runs automatically as part of pre-work checklist

### 4. AI Agent Guidelines Update

**Updated**: `AI_AGENT_GUIDELINES.md`

**Added**: "Check for Existing Artifacts" as step 2 in "Before Making Changes" section

## What Constitutes Duplication

### Scripts
**Duplication:**
- Multiple scripts with similar names (`script-v2.sh`, `script-new.sh`)
- Scripts with same purpose but different implementations
- Scripts that can be unified with flags/subcommands

**NOT duplication:**
- Scripts serving distinct purposes
- Scripts part of a toolchain
- Clearly versioned or deprecated scripts

### Documentation
**Duplication:**
- Multiple docs covering same topic
- Docs with overlapping content
- Status reports duplicating information

**NOT duplication:**
- Docs serving different audiences
- Docs in different contexts
- Archived versions of superseded content

### Code
**Duplication:**
- Multiple implementations of same functionality
- Copy-pasted code blocks
- Similar classes/components that could share base

**NOT duplication:**
- Platform-specific implementations
- Test doubles
- Intentional variations for different use cases

## Search Checklist (MANDATORY)

Before creating ANY artifact:

- [ ] **File name search**: `find . -name "*<keyword>*"`
- [ ] **Content search**: `grep -r "<keyword>" scripts/ docs/`
- [ ] **Semantic search**: Use codebase search for related implementations
- [ ] **Directory review**: Check relevant directories manually
- [ ] **Helper script**: Run `./scripts/check-existing-artifacts.sh <keyword>`
- [ ] **Decision made**: Document why extending vs creating new

## Examples

### Good: Extending Existing Script
```bash
# Existing: scripts/google-docs-workflow.py
# Need: Add new upload method

# ✅ GOOD: Extend with new subcommand
python scripts/google-docs-workflow.py upload --method new-method

# ❌ BAD: Create new script
python scripts/google-docs-new-method-upload.py
```

### Good: Creating New Script (Different Purpose)
```bash
# Existing: scripts/emulator-manager.sh (manages lifecycle)
# Need: Discover available emulators

# ✅ GOOD: New script for different purpose
scripts/emulator-discovery.sh

# ❌ BAD: Add discovery to manager (mixing concerns)
scripts/emulator-manager.sh discover
```

### Good: Consolidating Documentation
```bash
# Before: Multiple AWS setup docs
docs/development/setup/AWS_BEDROCK_SETUP_WALKTHROUGH.md
docs/development/setup/AWS_BEDROCK_QUICK_START.md

# After: Single comprehensive guide
docs/development/setup/AWS_BEDROCK_SETUP.md
# - Quick Start section
# - Detailed Walkthrough section
```

## Enforcement

### Pre-Creation Validation

**Before creating a new artifact, agents MUST:**

1. ✅ Run search commands (file name, content, semantic)
2. ✅ Review search results
3. ✅ Document decision (extend vs create new)
4. ✅ If creating new, explain why existing cannot be used

### Post-Creation Review

**After creating a new artifact, verify:**

1. ✅ No duplicates exist with same purpose
2. ✅ New artifact is properly documented
3. ✅ References to old artifacts are updated
4. ✅ Deprecation notices added if replacing old artifact

## Integration Points

### Pre-Work Check
- **Script**: `scripts/pre-work-check.sh`
- **Check**: Reminds agents to search before creating
- **Helper**: References `check-existing-artifacts.sh`

### Cursor Rules
- **Rule**: `.cursor/rules/artifact-duplication.mdc`
- **Enforcement**: Automatically applied to all agent interactions
- **Coverage**: Scripts, documentation, code

### AI Agent Guidelines
- **Section**: "Before Making Changes"
- **Step**: Step 2 (after branch check)
- **Reference**: Links to cursor rule

## Related Documentation

- `.cursor/rules/artifact-duplication.mdc` - Complete duplication prevention rules
- `scripts/check-existing-artifacts.sh` - Helper script for searching
- `scripts/pre-work-check.sh` - Pre-work validation (includes duplication check)
- `AI_AGENT_GUIDELINES.md` - General agent guidelines
- `.cursor/rules/code-quality.mdc` - DRY principles
- `.cursor/rules/repository-maintenance.mdc` - Repository cleanup rules

## Next Steps

### Immediate
- ✅ Rule created and integrated
- ✅ Helper script created
- ✅ Pre-work check updated
- ✅ AI Agent Guidelines updated

### Future Improvements
- [ ] Add automated duplicate detection in CI/CD
- [ ] Create consolidation scripts for common patterns
- [ ] Add metrics tracking for duplication prevention
- [ ] Regular repository audits for duplicates

## Success Metrics

**Indicators of success:**
- Fewer duplicate artifacts created
- More artifacts extended vs created
- Better consolidation of existing duplicates
- Clearer documentation about artifact purposes

**Monitoring:**
- Review PRs for new artifacts (check if existing could be used)
- Track consolidation efforts
- Monitor repository growth (should slow for similar artifacts)

