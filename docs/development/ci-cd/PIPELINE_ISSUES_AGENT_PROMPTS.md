# Pipeline Issues Resolution - Agent Prompts

**Date**: 2025-01-22  
**Purpose**: Prompts for 3 agents to resolve pipeline issues

---

## Agent 1: Secret Scanning Failures

**Copy this prompt and give it to Agent 1:**

---

You are assigned to investigate and fix **Secret Scanning Failures** in the CI/CD pipeline.

### Your Task

1. **Investigate secret scanning failures** on these branches:
   - `feature/release-management-system-implementation`
   - `feature/driver-worker-architecture`

2. **Identify root causes**:
   - Review `.gitleaks.toml` configuration
   - Check `.github/workflows/secret-scan.yml` workflow
   - Identify false positives vs. actual secrets
   - Review recent changes that may have introduced issues

3. **Fix the issues**:
   - Update Gitleaks configuration if needed
   - Remove actual secrets if found
   - Fix false positives in allowlist
   - Test fixes

4. **Document findings**:
   - Root cause analysis
   - Solutions implemented
   - Prevention measures

### Setup Instructions

1. **Run pre-work check**:
   ```bash
   ./scripts/pre-work-check.sh
   ```

2. **Create isolated worktree** (MANDATORY):
   ```bash
   ./scripts/create-worktree.sh pipeline-secret-scanning
   cd ../electric-sheep-pipeline-secret-scanning
   ```

3. **Create feature branch**:
   ```bash
   git checkout -b fix/pipeline-secret-scanning-issues
   ```

4. **Update coordination doc**:
   - Mark task as "In Progress" in `docs/development/workflow/AGENT_COORDINATION.md`
   - Document files you'll modify

### Key Files to Investigate

- `.gitleaks.toml` - Gitleaks configuration
- `.github/workflows/secret-scan.yml` - Secret scanning workflow
- Files flagged by secret scanning (check CI logs)

### Expected Deliverables

1. **Investigation Report** (in your branch):
   - What was investigated
   - What was found (false positives, actual secrets, config issues)
   - Root cause analysis

2. **Fix Implementation**:
   - Updated `.gitleaks.toml` (if needed)
   - Removed actual secrets (if found)
   - Fixed workflow files (if needed)
   - Tested fixes

3. **Documentation**:
   - Findings documented in `docs/development/ci-cd/PIPELINE_ISSUES_ROOT_CAUSE_ANALYSIS.md`
   - Solutions documented
   - Prevention measures documented in `docs/development/ci-cd/PIPELINE_ISSUES_PREVENTION_STRATEGY.md`

### Resources

- Previous incident analysis: `docs/development/reports/PR_MERGE_INCIDENT_ANALYSIS.md`
- Initiative plan: `docs/development/ci-cd/PIPELINE_ISSUES_INITIATIVE.md`
- Coordination doc: `docs/development/workflow/AGENT_COORDINATION.md`

### Success Criteria

- ✅ Secret scanning passes on affected branches
- ✅ Root causes identified and documented
- ✅ Prevention measures implemented
- ✅ Documentation complete

---

## Agent 2: Security/Dependency Scans

**Copy this prompt and give it to Agent 2:**

---

You are assigned to investigate and fix **Security/Dependency Scan Failures** in the CI/CD pipeline.

### Your Task

1. **Investigate security/dependency scan failures** on:
   - `feature/release-management-system-implementation`

2. **Identify root causes**:
   - Review `.github/workflows/security-scan.yml` workflow
   - Review `.github/workflows/dependency-scan.yml` workflow
   - Check for vulnerabilities or dependency issues
   - Review recent dependency updates

3. **Fix the issues**:
   - Update dependencies if vulnerable
   - Fix workflow configuration if needed
   - Address security vulnerabilities
   - Test fixes

4. **Document findings**:
   - Root cause analysis
   - Solutions implemented
   - Prevention measures

### Setup Instructions

1. **Run pre-work check**:
   ```bash
   ./scripts/pre-work-check.sh
   ```

2. **Create isolated worktree** (MANDATORY):
   ```bash
   ./scripts/create-worktree.sh pipeline-security-scans
   cd ../electric-sheep-pipeline-security-scans
   ```

3. **Create feature branch**:
   ```bash
   git checkout -b fix/pipeline-security-dependency-scans
   ```

4. **Update coordination doc**:
   - Mark task as "In Progress" in `docs/development/workflow/AGENT_COORDINATION.md`
   - Document files you'll modify

### Key Files to Investigate

- `.github/workflows/security-scan.yml` - Security scanning workflow
- `.github/workflows/dependency-scan.yml` - Dependency scanning workflow
- `build.gradle.kts` - Build configuration (dependencies)
- `app/build.gradle.kts` - App build configuration
- CI logs for specific failures

### Expected Deliverables

1. **Investigation Report** (in your branch):
   - What was investigated
   - What was found (vulnerabilities, dependency issues, config problems)
   - Root cause analysis

2. **Fix Implementation**:
   - Updated dependencies (if vulnerable)
   - Fixed workflow files (if needed)
   - Addressed security vulnerabilities
   - Tested fixes

3. **Documentation**:
   - Findings documented in `docs/development/ci-cd/PIPELINE_ISSUES_ROOT_CAUSE_ANALYSIS.md`
   - Solutions documented
   - Prevention measures documented in `docs/development/ci-cd/PIPELINE_ISSUES_PREVENTION_STRATEGY.md`

### Resources

- Previous incident analysis: `docs/development/reports/PR_MERGE_INCIDENT_ANALYSIS.md`
- Initiative plan: `docs/development/ci-cd/PIPELINE_ISSUES_INITIATIVE.md`
- Coordination doc: `docs/development/workflow/AGENT_COORDINATION.md`

### Success Criteria

- ✅ Security scans pass on affected branch
- ✅ Dependency scans pass on affected branch
- ✅ Root causes identified and documented
- ✅ Prevention measures implemented
- ✅ Documentation complete

---

## Agent 3: Supabase Migration Deployment

**Copy this prompt and give it to Agent 3:**

---

You are assigned to investigate and fix **Supabase Migration Deployment Failures** in the CI/CD pipeline.

### Your Task

1. **Investigate Supabase migration deployment failures** on:
   - `main` branch

2. **Identify root causes**:
   - Review `.github/workflows/supabase-schema-deploy.yml` workflow
   - Check migration files for syntax errors
   - Verify Supabase connection and authentication
   - Review recent migration changes

3. **Fix the issues**:
   - Fix migration syntax errors (if any)
   - Fix connection/authentication issues
   - Update workflow configuration if needed
   - Test fixes

4. **Document findings**:
   - Root cause analysis
   - Solutions implemented
   - Prevention measures

### Setup Instructions

1. **Run pre-work check**:
   ```bash
   ./scripts/pre-work-check.sh
   ```

2. **Create isolated worktree** (MANDATORY):
   ```bash
   ./scripts/create-worktree.sh pipeline-supabase-migrations
   cd ../electric-sheep-pipeline-supabase-migrations
   ```

3. **Create feature branch**:
   ```bash
   git checkout -b fix/pipeline-supabase-migration-deployment
   ```

4. **Update coordination doc**:
   - Mark task as "In Progress" in `docs/development/workflow/AGENT_COORDINATION.md`
   - Document files you'll modify

### Key Files to Investigate

- `.github/workflows/supabase-schema-deploy.yml` - Migration deployment workflow
- `supabase/migrations/` - Migration files (check for syntax errors)
- `supabase/config.toml` - Supabase configuration
- CI logs for specific failures

### Important Notes

- **CRITICAL**: Never run migrations locally - they are applied via CI/CD only
- See: `.cursor/rules/supabase-migrations.mdc` for migration workflow
- Migrations are applied automatically when pushed to `main` or `develop`

### Expected Deliverables

1. **Investigation Report** (in your branch):
   - What was investigated
   - What was found (syntax errors, connection issues, auth problems, config issues)
   - Root cause analysis

2. **Fix Implementation**:
   - Fixed migration files (if syntax errors)
   - Fixed workflow configuration (if needed)
   - Fixed connection/authentication (if needed)
   - Tested fixes (verify workflow runs successfully)

3. **Documentation**:
   - Findings documented in `docs/development/ci-cd/PIPELINE_ISSUES_ROOT_CAUSE_ANALYSIS.md`
   - Solutions documented
   - Prevention measures documented in `docs/development/ci-cd/PIPELINE_ISSUES_PREVENTION_STRATEGY.md`

### Resources

- Supabase migrations guide: `docs/development/setup/SUPABASE_MIGRATIONS_GUIDE.md`
- Previous incident analysis: `docs/development/reports/PR_MERGE_INCIDENT_ANALYSIS.md`
- Initiative plan: `docs/development/ci-cd/PIPELINE_ISSUES_INITIATIVE.md`
- Coordination doc: `docs/development/workflow/AGENT_COORDINATION.md`
- Migration workflow rule: `.cursor/rules/supabase-migrations.mdc`

### Success Criteria

- ✅ Supabase migration deployment succeeds on main branch
- ✅ Root causes identified and documented
- ✅ Prevention measures implemented
- ✅ Documentation complete

---

## Common Instructions for All Agents

### Workflow

1. **Start**: Run pre-work check, create worktree, create branch
2. **Investigate**: Review files, check CI logs, identify issues
3. **Analyze**: Document root causes
4. **Fix**: Implement solutions
5. **Test**: Verify fixes work
6. **Document**: Update shared documents
7. **Commit**: Commit frequently (WIP commits are fine)
8. **Update**: Update coordination doc with progress

### Documentation Locations

- **Root Cause Analysis**: `docs/development/ci-cd/PIPELINE_ISSUES_ROOT_CAUSE_ANALYSIS.md`
  - Each agent adds their section
  - Document findings, root causes, impact

- **Prevention Strategy**: `docs/development/ci-cd/PIPELINE_ISSUES_PREVENTION_STRATEGY.md`
  - Each agent adds prevention measures
  - Document how to prevent recurrence

- **Coordination**: `docs/development/workflow/AGENT_COORDINATION.md`
  - Update status as you progress
  - Document files modified

### Communication

- **Check coordination doc** before modifying shared files
- **Update coordination doc** when status changes
- **Document conflicts** if any (unlikely - isolated worktrees)
- **Ask lead agent** if you need clarification

### Success Criteria (All Agents)

- ✅ Your assigned issue is resolved
- ✅ Root causes identified and documented
- ✅ Prevention measures implemented
- ✅ Documentation complete
- ✅ CI/CD pipeline healthy

---

## Lead Agent Coordination

**As the lead agent, you should:**

1. **Monitor progress** - Check coordination doc for agent updates
2. **Resolve conflicts** - If agents need to coordinate (unlikely)
3. **Consolidate findings** - Review all agent findings
4. **Create summary** - Overall initiative summary
5. **Verify completion** - Ensure all issues resolved

**After all agents complete:**

1. Review all findings
2. Create consolidated root cause analysis
3. Create consolidated prevention strategy
4. Verify all pipeline issues resolved
5. Update initiative status to "Complete"

