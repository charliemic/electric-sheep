# AI-Driven Coding: Lessons Learned

A concise guide to effective AI-assisted development, focusing on workflow, collaboration, rules/guidelines, and outcomes.

## A) Workflow

### Branch Isolation is Essential
- **Always work on feature branches** - Never commit directly to main
- **Use git worktrees for complete isolation** when multiple agents work simultaneously
- **Enforce branch naming conventions** - Format: `<type>/<task-description>` (e.g., `feature/user-auth`, `docs/api-docs`)
- **Clean up merged branches immediately** - Prevents confusion and branch clutter

### Pre-Work Checklist
Before starting any task:
1. Verify you're on a feature branch (not main)
2. Pull latest main to ensure you're working from current state
3. Check coordination documents for conflicts with other work
4. Document your planned changes

### Development Cycle
1. **Create branch** → **Implement** → **Test** → **Commit** → **Push** → **PR** → **Merge** → **Cleanup**
2. Run tests locally before pushing
3. Never push failing tests or broken builds
4. Use atomic commits (one logical change per commit)

### Key Principle
**Isolation prevents conflicts, enables parallel work, and maintains code quality.**

---

## B) Collaboration

### Multi-Agent Coordination
- **Central coordination document** - Single source of truth for active work
- **File-level coordination** - Document which files you're modifying
- **Status tracking** - Mark work as "In Progress" or "Complete"
- **Conflict prevention** - Check coordination doc before modifying shared files

### Communication Patterns
- **Explicit over implicit** - Document assumptions and decisions
- **Context in commits** - Write descriptive commit messages explaining "why"
- **Status updates** - Update coordination doc when work status changes
- **Conflict resolution** - Coordinate when files overlap with other agents

### Shared Resources
Files that require coordination:
- Application entry points
- Shared configuration files
- Core data/architecture modules
- Build configuration

### Key Principle
**Transparency and coordination enable multiple agents to work effectively without conflicts.**

---

## C) Rules, Guidelines, and Steering

### Rule Structure
Rules should be:
- **Actionable** - Clear directives, not suggestions
- **Enforceable** - Can be checked/validated
- **Contextual** - Applied at the right level (workspace, file, function)
- **Prioritized** - Critical rules marked clearly

### Rule Categories

#### Critical Rules (Must Follow)
- Never work on main branch
- Always use Result<T> for operations that can fail
- Always verify authentication before user-scoped operations
- All tests must pass before committing

#### Quality Rules (Should Follow)
- Use centralized logging utilities
- Follow design system standards
- Implement accessibility features
- Write tests for new functionality

#### Style Rules (Consistency)
- Follow existing code patterns
- Use UK English spellings
- Never use force unwrap operators
- Use meaningful variable names

### Rule Enforcement
- **Pre-commit checks** - Automated validation where possible
- **CI/CD gates** - Block merges if critical rules violated
- **Code review** - Human verification of guideline adherence
- **Documentation** - Rules documented in accessible location

### Rule Evolution
- Rules should evolve based on lessons learned
- Document why rules exist (not just what they are)
- Review and update rules regularly
- Remove obsolete rules

### Key Principle
**Clear, enforceable rules provide consistent guidance and prevent common mistakes.**

---

## D) Outcomes

### Success Metrics
- **Code quality** - Tests passing, no lint errors, builds successful
- **Velocity** - Features delivered on time, parallel work enabled
- **Stability** - Fewer conflicts, cleaner repository state
- **Knowledge transfer** - New team members can onboard quickly

### Measurable Improvements
- **Reduced conflicts** - Branch isolation prevents merge conflicts
- **Faster iteration** - Clear workflow reduces decision overhead
- **Better quality** - Rules catch issues before they reach main
- **Improved collaboration** - Coordination prevents duplicate work

### Common Pitfalls Avoided
- ❌ Working on main branch → ✅ Always use feature branches
- ❌ Uncoordinated changes → ✅ Check coordination doc first
- ❌ Vague rules → ✅ Actionable, enforceable guidelines
- ❌ Accumulated technical debt → ✅ Regular cleanup and maintenance

### Key Principle
**Good workflow, coordination, and rules lead to predictable, high-quality outcomes.**

---

## Implementation Checklist

For a new team adopting AI-driven coding:

### Setup
- [ ] Establish branch naming convention
- [ ] Create coordination document template
- [ ] Set up CI/CD with appropriate gates
- [ ] Document critical rules in accessible location

### Workflow
- [ ] Create pre-work checklist
- [ ] Establish development cycle
- [ ] Set up branch cleanup process
- [ ] Document merge and cleanup procedures

### Collaboration
- [ ] Create coordination document
- [ ] Define shared resource list
- [ ] Establish communication patterns
- [ ] Set up status tracking

### Rules
- [ ] Identify critical rules (must follow)
- [ ] Document quality rules (should follow)
- [ ] Establish style guidelines
- [ ] Set up enforcement mechanisms

### Outcomes
- [ ] Define success metrics
- [ ] Set up monitoring/measurement
- [ ] Establish review cycles
- [ ] Create feedback loops

---

## Quick Reference

**Before starting work:**
1. Check branch (create feature branch if on main)
2. Pull latest main
3. Check coordination doc
4. Document planned changes

**During work:**
1. Follow established rules and guidelines
2. Update coordination doc with status
3. Run tests before committing
4. Write descriptive commit messages

**After work:**
1. Verify all tests pass
2. Update coordination doc (mark complete)
3. Create PR
4. Clean up branch after merge

---

## Key Takeaways

1. **Isolation enables parallel work** - Feature branches and worktrees prevent conflicts
2. **Coordination prevents duplication** - Central doc tracks active work
3. **Rules provide guidance** - Clear, actionable rules prevent mistakes
4. **Outcomes validate approach** - Measure success and iterate

The goal is to create a **predictable, scalable workflow** that enables multiple agents (human or AI) to work effectively together while maintaining code quality and avoiding conflicts.

