# Project Goals and Principles

**Last Updated**: 2025-11-21  
**Status**: Active

This document outlines key goals and principles for the Electric Sheep project.

## Repository Content Principles

### ✅ What Should Be in the Repo

**Useful project resources should be committed:**
- ✅ Documentation (setup guides, architecture decisions, workflows)
- ✅ Scripts and automation tools
- ✅ Configuration templates and examples
- ✅ Test fixtures and data structures
- ✅ Design system components
- ✅ Shared utilities and helpers
- ✅ CI/CD workflows and configurations

**Always use proper workflow:**
- ✅ Use feature branches for all changes
- ✅ Use git worktree for shared files (see [MULTI_AGENT_WORKFLOW.md](./MULTI_AGENT_WORKFLOW.md))
- ✅ Follow branch naming conventions
- ✅ Document changes appropriately

### ❌ What Should NOT Be in the Repo

**Local/private content should never be committed:**
- ❌ Credentials (AWS keys, API keys, tokens)
- ❌ Personal Cursor settings (local workspace config)
- ❌ Environment-specific secrets
- ❌ Temporary files and caches
- ❌ Personal notes or scratch files

**Protected by `.gitignore`:**
- AWS credentials files (`*aws-credentials*.txt`, `*cursor-bedrock-credentials*.txt`)
- Cursor local config (`.cursor/local/`, `.cursor/settings.json`)
- Local properties (`local.properties`)
- Environment files (`.env`, `.env.local`)

## AWS Access Principles

### Security-First Approach

**AWS access should be:**
- ✅ **Read-only by default** - Only grant write permissions when absolutely necessary
- ✅ **Scoped to specific services** - Limit access to only what's needed
- ✅ **Time-limited** - Use temporary credentials (SSO) when possible
- ✅ **Auditable** - All access should be logged and traceable

### AWS Bedrock Specific

**For Cursor/Bedrock integration:**
- ✅ **Read-only access** - Only `bedrock:InvokeModel` and `bedrock:ListFoundationModels`
- ✅ **No write permissions** - Cannot create, modify, or delete Bedrock resources
- ✅ **Model access only** - Restricted to using models, not managing them
- ✅ **Temporary credentials** - Use SSO session tokens that expire
- ✅ **No cross-service access** - Bedrock permissions don't grant access to other AWS services

**IAM Policy Example (Minimal Permissions):**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "bedrock:InvokeModel",
        "bedrock:InvokeModelWithResponseStream"
      ],
      "Resource": "arn:aws:bedrock:*::foundation-model/*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "bedrock:ListFoundationModels"
      ],
      "Resource": "*"
    }
  ]
}
```

**What this policy does NOT allow:**
- ❌ Creating or modifying Bedrock models
- ❌ Managing Bedrock model access
- ❌ Accessing other AWS services (S3, EC2, etc.)
- ❌ Reading or writing to other AWS resources

### Credential Management

**Never commit credentials:**
- ❌ AWS Access Keys
- ❌ AWS Secret Keys
- ❌ AWS Session Tokens
- ❌ AWS SSO credentials

**Use secure methods:**
- ✅ AWS SSO for temporary credentials
- ✅ Environment variables for local development
- ✅ GitHub Secrets for CI/CD
- ✅ Credential extraction scripts (not committed with credentials)

**When credentials expire:**
- Re-authenticate via SSO
- Extract new credentials using scripts
- Update local configuration (not committed)
- Never store credentials in version control

## Workflow Principles

### Branching and Worktrees

**Always use feature branches:**
- ✅ Create feature branch before making changes
- ✅ Use descriptive branch names: `feature/<description>`
- ✅ Never commit directly to `main`

**Use worktrees for shared files:**
- ✅ Use git worktree when modifying shared files
- ✅ Prevents conflicts with other agents/developers
- ✅ Complete file system isolation

**See**: [MULTI_AGENT_WORKFLOW.md](./MULTI_AGENT_WORKFLOW.md) for complete guidelines

### Documentation

**Document useful things:**
- ✅ Setup procedures and workflows
- ✅ Architecture decisions and patterns
- ✅ Troubleshooting guides
- ✅ Configuration examples (with placeholders)

**Don't document private things:**
- ❌ Personal credentials or keys
- ❌ Local-only configuration
- ❌ Temporary workarounds

## Security Checklist

Before committing changes, verify:

- [ ] No credentials or secrets in code
- [ ] No hardcoded API keys or tokens
- [ ] Local config files are in `.gitignore`
- [ ] AWS access is minimal and read-only where possible
- [ ] Documentation uses placeholders, not real values
- [ ] Personal/local settings are not committed
- [ ] Temporary files are excluded

## Related Documentation

- [MULTI_AGENT_WORKFLOW.md](./MULTI_AGENT_WORKFLOW.md) - Branching and workflow guidelines
- [AWS_BEDROCK_CURSOR_SETUP.md](./AWS_BEDROCK_CURSOR_SETUP.md) - AWS Bedrock setup (security-focused)
- [PUBLIC_REPO_SECURITY_REVIEW.md](./PUBLIC_REPO_SECURITY_REVIEW.md) - Security review
- [AI_AGENT_GUIDELINES.md](../../AI_AGENT_GUIDELINES.md) - Complete agent guidelines

