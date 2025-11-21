# Python Version Management Strategy

This document explains the Python version management approach for Electric Sheep.

## Current Setup

**Active Python**: Python 3.12.11 (pyenv)  
**Location**: `~/.pyenv/versions/3.12.11`  
**Shim**: `~/.pyenv/shims/python3`  
**Status**: ✅ Configured and working

## Version Management Options

### Option 1: pyenv (Current - Recommended)

**Pros**:
- ✅ Simple, already set up
- ✅ Works out of the box
- ✅ No additional tools needed
- ✅ Easy to update: `brew upgrade python@3.12`

**Cons**:
- ⚠️ Global Python version (affects all projects)
- ⚠️ Can't easily switch between versions per project

**Best for**: Single Python version, simple setup

### Option 2: Homebrew Python (Alternative)

**Pros**:
- ✅ Per-project Python versions (via `.python-version`)
- ✅ Easy to switch between versions
- ✅ Consistent across team members
- ✅ Can install multiple Python versions

**Cons**:
- ⚠️ Additional tool to install and configure
- ⚠️ Slightly more complex setup

**Best for**: Multiple Python versions, team consistency

## Recommendation

**Current setup**: **pyenv with Python 3.12.11** ✅

Reasons:
1. ✅ Per-project version control (via `.python-version`)
2. ✅ Consistent across team members
3. ✅ Easy to switch between versions
4. ✅ Isolated from system Python
5. ✅ Auto-activates when entering project directory

## Project Configuration

The project includes:
- **`.python-version`**: Set to `3.12.11` (matches your current setup)
- **`requirements.txt`**: Works with Python 3.11+ or 3.12+

## If You Want to Use pyenv

If you decide to use pyenv for stricter version control:

```bash
# Install pyenv
brew install pyenv

# Add to ~/.zshrc (already documented in ZSH_CONFIGURATION.md)
eval "$(pyenv init - zsh)"

# Install Python 3.12.11
pyenv install 3.12.11

# Set for this project
cd /path/to/electric-sheep
pyenv local 3.12.11

# Install dependencies
pip install -r requirements.txt
```

## Verifying Your Setup

```bash
# Check Python version
python3 --version  # Should show 3.12.11

# Check location
which python3  # Should show your Python location

# Check dependencies
python3 -c "import moviepy; print('✅ MoviePy available')"
```

## Updating Python

**With Homebrew**:
```bash
brew upgrade python@3.12
```

**With pyenv**:
```bash
pyenv install 3.12.12  # or newer version
pyenv local 3.12.12
```

## Troubleshooting

### Python Version Mismatch

If you see version conflicts:
1. Check `.python-version` matches your installed version
2. Or remove `.python-version` to use system Python
3. Or install pyenv and use the version in `.python-version`

### Dependencies Not Found

```bash
# Reinstall dependencies
python3 -m pip install --upgrade -r requirements.txt
```

### Multiple Python Versions

If you have multiple Python versions installed:
```bash
# Check which Python is active
which python3

# Use specific version
python3.12 -m pip install -r requirements.txt
```

## Summary

- ✅ **Current setup works**: Homebrew Python 3.12.11
- ✅ **No changes needed**: Project configured for 3.12.11
- ✅ **Optional pyenv**: Can be added later if needed
- ✅ **Flexible**: Works with Python 3.11+ or 3.12+

