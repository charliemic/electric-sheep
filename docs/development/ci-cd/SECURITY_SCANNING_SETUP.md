# Security Scanning Setup

**Date**: 2025-11-21  
**Status**: Setup Guide  
**Purpose**: Enable automated security vulnerability scanning

## Overview

Security scanning detects vulnerable dependencies and helps prevent security issues before they reach production.

## GitHub Dependabot

### Enable Dependabot Alerts

1. **Go to repository Settings**:
   - Navigate to: Settings → Security → Code security and analysis
   - Or: https://github.com/charliemic/electric-sheep/settings/security_analysis

2. **Enable Dependabot alerts**:
   - ✅ Enable "Dependabot alerts"
   - ✅ Enable "Dependabot security updates" (optional, auto-updates PRs)

### What Dependabot Does

- **Scans dependencies** for known vulnerabilities
- **Creates alerts** for vulnerable dependencies
- **Suggests updates** to fix vulnerabilities
- **Auto-creates PRs** (if security updates enabled)

### Dependabot Configuration

Create `.github/dependabot.yml`:

```yaml
version: 2
updates:
  # Enable version updates for Gradle dependencies
  - package-ecosystem: "gradle"
    directory: "/"
    schedule:
      interval: "weekly"
    open-pull-requests-limit: 10
    reviewers:
      - "charliemic"
    labels:
      - "dependencies"
      - "security"
```

## OWASP Dependency Check (Optional)

For more comprehensive scanning, add OWASP dependency check to CI/CD:

```yaml
security-scan:
  name: Security Scan (OWASP)
  runs-on: ubuntu-latest
  steps:
    - name: Checkout code
      uses: actions/checkout@v4
    
    - name: Run OWASP dependency check
      uses: dependency-check/Dependency-Check_Action@main
      with:
        project: 'electric-sheep'
        path: '.'
        format: 'HTML'
        args: >
          --enableRetired
          --enableExperimental
          --failOnCVSS 7
```

## Benefits

- ✅ **Automated detection** - No manual scanning needed
- ✅ **Early warning** - Catch vulnerabilities before production
- ✅ **Auto-updates** - Dependabot can create fix PRs
- ✅ **Compliance** - Helps meet security requirements

## Related Documentation

- `docs/development/ci-cd/AUTOMATED_CODE_REVIEW_IMPLEMENTATION.md` - Complete implementation guide
- GitHub Docs: https://docs.github.com/en/code-security/dependabot

