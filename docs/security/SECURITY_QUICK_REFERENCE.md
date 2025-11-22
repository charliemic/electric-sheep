# Security Quick Reference

**Last Updated**: 2025-01-22  
**Purpose**: Quick reference for security workflows and practices

## Security Workflows

### 1. Dependency Security Scan
**Workflow**: `.github/workflows/dependency-scan.yml`  
**Triggers**: Push to main/develop, PRs, weekly schedule  
**What it does**:
- Scans Gradle dependencies for known vulnerabilities (CVE)
- Uses OWASP Dependency-Check
- Fails on CVSS >= 7.0
- Uploads reports to GitHub Security
- Comments on PRs with vulnerability summary

**Manual trigger**: `workflow_dispatch` in Actions tab

### 2. Secret Scanning
**Workflow**: `.github/workflows/secret-scan.yml`  
**Triggers**: All pushes, PRs, weekly schedule  
**What it does**:
- Scans code for secrets, API keys, passwords
- Uses Gitleaks
- Blocks PRs with detected secrets
- Weekly full-history scan

**Configuration**: `.gitleaks.toml`

### 3. Security Linting
**Workflow**: `.github/workflows/security-lint.yml`  
**Triggers**: Push/PR when app code changes  
**What it does**:
- Security-focused Android lint checks
- Checks for hardcoded values, insecure random, SSL issues
- Comments on PRs with security issues
- Uploads lint reports

### 4. Dependabot
**Configuration**: `.github/dependabot.yml`  
**What it does**:
- Weekly dependency updates
- Security updates (automatic)
- Creates PRs for updates
- Groups related dependencies

## Security Best Practices

### Before Committing
- [ ] No secrets in code
- [ ] No hardcoded credentials
- [ ] Input validation implemented
- [ ] Authentication verified
- [ ] Authorization checked

### Before Creating PR
- [ ] Security scans passed
- [ ] No secrets detected
- [ ] Dependencies reviewed
- [ ] Security linting passed

### Before Merging
- [ ] All security checks passed
- [ ] No critical vulnerabilities
- [ ] Code review completed
- [ ] Security issues resolved

## Common Security Issues

### Secrets in Code
**Problem**: API keys, passwords, tokens committed  
**Solution**: 
- Remove from code
- Rotate the secret
- Use GitHub Secrets for CI/CD
- Use environment variables locally

### Vulnerable Dependencies
**Problem**: Dependencies with known CVEs  
**Solution**:
- Update to patched version
- Review CVE details
- Consider alternatives if no patch

### Hardcoded Values
**Problem**: Secrets or sensitive data hardcoded  
**Solution**:
- Move to configuration
- Use BuildConfig for non-sensitive
- Use environment variables

## Security Tools

### Dependency Scanning
- **Dependabot**: Automatic updates, GitHub-native
- **OWASP Dependency-Check**: Comprehensive CVE scanning
- **Snyk**: Commercial alternative

### Secret Scanning
- **Gitleaks**: Fast, open-source
- **GitHub Secret Scanning**: Native, automatic
- **TruffleHog**: Deep scanning

### Security Testing
- **OWASP ZAP**: Penetration testing
- **MobSF**: Mobile security framework
- **Custom tests**: App-specific security tests

## Security Contacts

- **Security Issues**: Create issue with `security` label
- **Critical Vulnerabilities**: Use GitHub Security Advisories
- **Questions**: See [Security Review](./SECURITY_REVIEW_AND_INITIATIVE.md)

## Related Documentation

- [Security Review and Initiative](./SECURITY_REVIEW_AND_INITIATIVE.md) - Comprehensive security plan
- [Security Rules](../../.cursor/rules/security.mdc) - Security coding guidelines
- [Project Goals](../development/guides/PROJECT_GOALS.md) - Security principles

