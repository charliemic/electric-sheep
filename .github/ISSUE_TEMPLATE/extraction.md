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

