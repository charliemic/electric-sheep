# Documentation-First Principle

**Date**: 2025-11-21  
**Status**: Active Principle  
**Priority**: CRITICAL

## The Problem

When debugging or implementing features, agents often:
1. ❌ Try multiple fixes without checking documentation
2. ❌ Make assumptions about how tools/APIs work
3. ❌ Don't step back to understand the problem
4. ❌ Don't consult official sources first

**Example**: GitHub Pages deployment failures
- Multiple attempts to fix workflow without checking GitHub's official documentation
- Tried v3 actions when v4 was the current version
- Made assumptions about artifact handling instead of reading docs
- Only checked documentation after user explicitly asked to "step back"

## The Principle

**ALWAYS check official documentation FIRST before implementing fixes or features.**

### When to Check Documentation

1. **Before implementing a new feature**
   - Read official documentation for the tool/framework/API
   - Understand the recommended patterns
   - Check for examples or tutorials

2. **When debugging failures**
   - Check official troubleshooting guides
   - Verify you're using the latest/correct versions
   - Look for known issues or migration guides

3. **When encountering errors**
   - Search official documentation for the error message
   - Check changelogs or release notes
   - Verify version compatibility

4. **When unsure about best practices**
   - Consult official best practices guides
   - Check community examples
   - Review official patterns

## Implementation Checklist

### Before Starting Work

- [ ] **Search official documentation** for the tool/framework/API
- [ ] **Check version compatibility** - are you using the latest/correct version?
- [ ] **Review examples** - look for official examples or tutorials
- [ ] **Understand the pattern** - what's the recommended approach?

### When Debugging

- [ ] **Read error messages carefully** - what do they actually say?
- [ ] **Search documentation** for the error or symptom
- [ ] **Check version notes** - has something changed in recent versions?
- [ ] **Step back** - understand the problem before trying fixes

### When Implementing

- [ ] **Follow official patterns** - use recommended approaches
- [ ] **Use official examples** as starting points
- [ ] **Verify compatibility** - ensure versions match documentation
- [ ] **Test with official examples** first, then adapt

## Examples

### Good: Documentation-First Approach

```
1. User reports GitHub Pages deployment failing
2. Agent: "Let me check GitHub's official documentation for GitHub Pages deployment"
3. Agent searches: "GitHub Actions deploy-pages official documentation"
4. Agent finds: Official pattern uses upload-pages-artifact@v4 and deploy-pages@v4
5. Agent updates workflow to match official pattern
6. ✅ Success on first try
```

### Bad: Trial-and-Error Approach

```
1. User reports GitHub Pages deployment failing
2. Agent: "Let me try removing artifact_name parameter"
3. Fails: "Let me try using download-artifact"
4. Fails: "Let me try different action versions"
5. Fails: "Let me try..."
6. User: "Take a step back, check documentation"
7. Agent finally checks documentation
8. ✅ Success after multiple failed attempts
```

## Tools and Resources

### Documentation Sources

1. **Official Documentation**
   - GitHub: https://docs.github.com
   - Python: https://docs.python.org
   - Kotlin: https://kotlinlang.org/docs
   - Android: https://developer.android.com/docs

2. **Search Strategies**
   - Use official documentation search
   - Include version numbers in searches
   - Search for error messages
   - Look for "getting started" or "quick start" guides

3. **Version Checking**
   - Check package.json, build.gradle, requirements.txt
   - Verify against latest stable versions
   - Check changelogs for breaking changes

## Integration with Workflow

### Pre-Work Check Enhancement

Add to `scripts/pre-work-check.sh`:
```bash
# Check documentation before starting
echo "📚 Documentation check:"
echo "   → Have you reviewed official docs for this task?"
echo "   → Are you using the latest/correct versions?"
echo "   → Do you understand the recommended patterns?"
```

### Cursor Rules

Add to `.cursor/rules/documentation-first.mdc`:
- Always check official documentation before implementing
- Verify version compatibility
- Follow official patterns and examples
- Step back and understand before fixing

## Related Documentation

- `AI_AGENT_GUIDELINES.md` - General agent guidelines
- `docs/development/workflow/WEEKLY_WORKFLOW_ANALYSIS.md` - Workflow analysis
- `.cursor/rules/` - Cursor rules directory

