# Security Setup Guide

**Last Updated**: 2025-01-22  
**Purpose**: Step-by-step guide to enable security features

## Quick Setup (5 minutes)

### Step 1: Enable GitHub Secret Scanning

1. Go to your repository on GitHub
2. Click **Settings** → **Security** → **Secret scanning**
3. Click **Enable** for "Push protection"
4. (Optional) Configure alert recipients

**What this does:**
- Automatically scans all pushes for secrets
- Blocks pushes with detected secrets
- Sends alerts for any secrets found

### Step 2: Enable Dependabot

1. Go to **Settings** → **Security** → **Dependabot**
2. Click **Enable** for "Dependabot security updates"
3. Click **Enable** for "Dependabot version updates"
4. Verify that `.github/dependabot.yml` is recognized

**What this does:**
- Automatically creates PRs for security updates
- Creates PRs for dependency updates (weekly)
- Groups related dependencies to reduce PR noise

### Step 3: (Optional) Add NVD API Key

**Why**: Improves dependency scan rate limits (not required, but recommended)

1. Get free API key: https://nvd.nist.gov/developers/request-an-api-key
2. Go to **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Name: `NVD_API_KEY`
5. Value: Your NVD API key
6. Click **Add secret**

**What this does:**
- Increases rate limits for CVE database queries
- Faster dependency scans
- More reliable results

## Testing the Workflows

### Test Secret Scanning

1. Create a test branch:
   ```bash
   git checkout -b test/secret-scan
   ```

2. Add a test file with a fake secret:
   ```bash
   echo "API_KEY=sk_test_1234567890abcdef" > test-secret.txt
   git add test-secret.txt
   git commit -m "test: secret scanning"
   git push origin test/secret-scan
   ```

3. Create a PR and verify:
   - Secret scan workflow runs
   - PR is blocked or commented on
   - Remove the test file and push again

### Test Dependency Scanning

1. Manually trigger the workflow:
   - Go to **Actions** → **Dependency Security Scan**
   - Click **Run workflow**
   - Select branch: `main`
   - Click **Run workflow**

2. Wait for completion (may take 5-10 minutes)

3. Review results:
   - Check workflow artifacts for HTML report
   - Review any vulnerabilities found
   - Check GitHub Security tab for SARIF results

### Test Security Linting

1. Make a small change to app code:
   ```bash
   git checkout -b test/security-lint
   # Make a small change to any app file
   git commit -m "test: security linting"
   git push origin test/security-lint
   ```

2. Create a PR and verify:
   - Security lint workflow runs
   - Review lint results in artifacts
   - Check for any security issues

### Test Dependabot

1. Wait for Dependabot to create PRs (runs weekly on Monday)
2. Or manually trigger by updating a dependency version
3. Verify:
   - PRs are created with proper labels
   - Dependencies are grouped appropriately
   - Security updates are prioritized

## Verification Checklist

After setup, verify:

- [ ] GitHub Secret Scanning enabled
- [ ] Dependabot enabled
- [ ] NVD API key added (optional)
- [ ] Secret scan workflow runs on PRs
- [ ] Dependency scan workflow runs on schedule
- [ ] Security lint workflow runs on app code changes
- [ ] Dependabot creates PRs (may take up to a week)

## Troubleshooting

### Secret Scanning False Positives

If you get false positives:

1. Check `.gitleaks.toml` allowlist
2. Add patterns to `[allowlist.regexes]` if needed
3. Add paths to `[allowlist.paths]` if needed

### Dependency Scan Timeout

If dependency scan times out:

1. Check NVD API key is set (improves rate limits)
2. Increase timeout in workflow (currently 20 minutes)
3. Consider running only on schedule, not every PR

### Security Lint Issues

If security lint finds issues:

1. Review lint report in artifacts
2. Fix hardcoded values (move to BuildConfig or env vars)
3. Fix insecure random (use SecureRandom)
4. Fix SSL issues (use HTTPS, certificate pinning)

## Next Steps

After setup is complete:

1. Review initial scan results
2. Address any critical vulnerabilities
3. Set up alerts for security findings
4. Review security workflows monthly
5. Update security configurations as needed

## Related Documentation

- [Security Principles](./SECURITY_PRINCIPLES.md) - Core security principles
- [Security Quick Reference](./SECURITY_QUICK_REFERENCE.md) - Quick reference
- [Implementation Status](./IMPLEMENTATION_STATUS.md) - Implementation tracking

