# Python Setup for Electric Sheep Test Automation

This document describes how to set up Python for the test automation video annotation pipeline.

## Overview

The test automation framework uses Python for video annotation (frame-accurate synchronization with test actions). 

**Python Version**: Python 3.11+ or 3.12+ is required (3.12.11 recommended).

**Version Management Options**:
- **Homebrew Python** (current default): Simple, works out of the box
- **pyenv** (optional): For stricter version control and multiple Python versions

## Prerequisites

- **macOS**: Homebrew (for Python or pyenv)
- **Linux**: Standard package manager (apt, yum, etc.)

## Quick Start (pyenv - Current Setup)

The project uses **pyenv** for Python version management. If pyenv is already set up:

```bash
# Verify pyenv is active
which python3  # Should show ~/.pyenv/shims/python3

# Verify Python version (should be 3.12.11)
python3 --version

# Install dependencies
python3 -m pip install -r requirements.txt

# Verify installation
python3 -c "import moviepy; print('✅ MoviePy installed')"
```

**Note**: pyenv automatically activates Python 3.12.11 when you enter the project directory (via `.python-version` file).

## Installation with pyenv (Current Setup)

The project uses **pyenv** for Python version management. If pyenv is not yet installed:

### Step 1: Install pyenv

**macOS (using Homebrew):**
```bash
brew install pyenv
```

**Linux (Ubuntu/Debian):**
```bash
curl https://pyenv.run | bash
```

**Linux (manual):**
```bash
git clone https://github.com/pyenv/pyenv.git ~/.pyenv
cd ~/.pyenv && src/configure && make -C src
```

### Step 2: Configure Shell

Add pyenv initialization to your `~/.zshrc` (or `~/.bashrc` on Linux):

```bash
# Python version management (pyenv)
if command -v pyenv >/dev/null 2>&1; then
    eval "$(pyenv init - zsh)"  # Use - bash for bash
fi
```

**Note**: The Electric Sheep ZSH configuration (see `docs/development/ZSH_CONFIGURATION.md`) already includes this setup.

Reload your shell:
```bash
source ~/.zshrc
```

### Step 3: Install Python Version

Install the Python version specified in `.python-version`:

```bash
# Install Python 3.12.11 (or version in .python-version)
pyenv install 3.12.11

# Verify installation
pyenv versions
```

**Note**: On macOS, you may need to install build dependencies:
```bash
brew install openssl readline sqlite3 xz zlib tcl-tk
```

### Step 4: Set Local Python Version

The project includes a `.python-version` file that pyenv will automatically use:

```bash
# Navigate to project root
cd /path/to/electric-sheep

# pyenv will automatically use .python-version
python --version  # Should show 3.12.11

# If not, explicitly set it:
pyenv local 3.12.11
```

### Step 5: Install Dependencies

```bash
# Install Python dependencies
pip install -r requirements.txt

# Verify installation
python -c "import moviepy; print('MoviePy installed successfully')"
```

## Verification

After setup, verify everything is working:

```bash
# Check Python version (should be 3.11+ or 3.12+)
python3 --version

# If using pyenv, check it's active
which python3  # With pyenv: ~/.pyenv/shims/python3
                # With Homebrew: /opt/homebrew/bin/python3 or ~/bin/python3

# Check dependencies
python3 -c "import moviepy; import PIL; print('✅ All dependencies installed')"
```

## Usage

### Running Video Annotation

```bash
# Annotate a test video
python scripts/annotate-video.py \
    /tmp/test_results/video.mp4 \
    /tmp/test_results/action_log.json \
    /tmp/test_results/video_annotated.mp4
```

### Virtual Environment (Optional)

While pyenv handles version management, you can also use a virtual environment for isolation:

```bash
# Create virtual environment
python -m venv venv

# Activate virtual environment
source venv/bin/activate  # On macOS/Linux
# or
venv\Scripts\activate  # On Windows

# Install dependencies
pip install -r requirements.txt
```

**Note**: Virtual environments are optional with pyenv, but can be useful for additional isolation.

## Troubleshooting

### pyenv Not Found

If `pyenv` command is not found:

1. **Check installation**:
   ```bash
   which pyenv
   ```

2. **Verify shell configuration**:
   ```bash
   grep pyenv ~/.zshrc
   ```

3. **Reload shell**:
   ```bash
   source ~/.zshrc
   ```

### Python Version Not Switching

If pyenv isn't switching Python versions:

1. **Check .python-version file**:
   ```bash
   cat .python-version
   ```

2. **Manually set version**:
   ```bash
   pyenv local 3.11.0
   ```

3. **Verify shims are in PATH**:
   ```bash
   echo $PATH | grep pyenv
   ```

### MoviePy Installation Issues

If MoviePy installation fails:

1. **Check Python version**:
   ```bash
   python --version  # Should be 3.11.0
   ```

2. **Install build dependencies** (macOS):
   ```bash
   brew install openssl readline sqlite3 xz zlib tcl-tk
   ```

3. **Upgrade pip**:
   ```bash
   pip install --upgrade pip
   ```

4. **Install with verbose output**:
   ```bash
   pip install -r requirements.txt -v
   ```

### FFmpeg Not Found

MoviePy requires FFmpeg for video processing:

**macOS:**
```bash
brew install ffmpeg
```

**Linux:**
```bash
sudo apt-get install ffmpeg  # Ubuntu/Debian
# or
sudo yum install ffmpeg      # CentOS/RHEL
```

**Verify FFmpeg:**
```bash
ffmpeg -version
```

## Current Setup (Homebrew Python)

Your current setup uses Homebrew Python 3.12.11, which works perfectly:

```bash
# Your Python is at:
which python3  # /Users/CharlieCalver/bin/python3 -> python3.12 -> Homebrew

# Install dependencies
python3 -m pip install -r requirements.txt
```

**Note**: This is the recommended approach if you only need one Python version. Use pyenv if you need multiple Python versions or stricter version control.

## CI/CD Considerations

For CI/CD, you can either:

1. **Use pyenv in CI**:
   ```yaml
   # .github/workflows/...
   - name: Set up Python
     uses: actions/setup-python@v4
     with:
       python-version: '3.11'
   - run: pip install -r requirements.txt
   ```

2. **Use system Python** (if available in CI environment)

## Best Practices

1. **Always use `.python-version`**: This ensures consistent Python versions across developers
2. **Keep `requirements.txt` updated**: Add new dependencies as needed
3. **Pin versions for production**: Consider using `requirements.txt` with pinned versions for CI/CD
4. **Test after Python updates**: Run video annotation after updating Python or dependencies

## Related Documentation

- `docs/development/ZSH_CONFIGURATION.md` - Shell configuration (includes pyenv setup)
- `docs/testing/VIDEO_MANAGEMENT_EVALUATION.md` - Video annotation architecture
- `requirements.txt` - Python dependencies

