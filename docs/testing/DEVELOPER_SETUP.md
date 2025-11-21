# Developer Setup Guide

## Quick Start

### 1. Check Dependencies

```bash
./scripts/check-dependencies.sh
```

This will check all required and optional dependencies.

### 2. Setup Optional Services

**Ollama (for AI email generation)**:
```bash
# Docker (recommended - cross-platform)
./scripts/setup-ollama-docker.sh

# Or native (macOS/Linux)
./scripts/setup-ollama.sh
```

### 3. Verify Setup

```bash
# Check everything is working
./scripts/check-dependencies.sh

# Test Ollama (if installed)
curl http://localhost:11434/api/tags
```

## Required Dependencies

- ✅ **Java 17+** - For Kotlin/Gradle
- ✅ **Gradle** - Build tool (wrapper included)
- ✅ **Android SDK** - For app development
- ✅ **ADB** - Android Debug Bridge
- ✅ **Appium** - Mobile automation framework
- ✅ **Tesseract OCR** - Text recognition
- ✅ **OpenCV** - Pattern recognition (via Gradle)

## Optional Dependencies

- ⚠️ **Ollama** - For AI email generation (falls back to procedural if not available)
- ⚠️ **Docker** - For cross-platform Ollama setup

## Platform-Specific Setup

### macOS

```bash
# Install dependencies
brew install openjdk@17 tesseract

# Install Appium
npm install -g appium @appium/doctor
appium driver install uiautomator2

# Setup Ollama (optional)
./scripts/setup-ollama-docker.sh  # Docker (recommended)
# or
brew install ollama && ./scripts/setup-ollama.sh  # Native
```

### Linux

```bash
# Install dependencies
sudo apt-get update
sudo apt-get install openjdk-17-jdk tesseract-ocr

# Install Appium
npm install -g appium @appium/doctor
appium driver install uiautomator2

# Setup Ollama (optional)
./scripts/setup-ollama-docker.sh  # Docker (recommended)
# or
curl -fsSL https://ollama.ai/install.sh | sh && ./scripts/setup-ollama.sh  # Native
```

### Windows (WSL)

```bash
# Use Linux instructions above
# Or use Docker for everything
./scripts/setup-ollama-docker.sh
```

## CI/CD Integration

### GitHub Actions

The framework includes GitHub Actions workflows that:
- ✅ Check dependencies automatically
- ✅ Setup Ollama via Docker
- ✅ Run tests with all dependencies

See: `.github/workflows/test-automation-dependencies.yml`

### Local CI/CD Testing

```bash
# Test dependency check
./scripts/check-dependencies.sh

# Test Ollama setup (Docker)
./scripts/setup-ollama-docker.sh

# Verify everything works
curl http://localhost:11434/api/tags
```

## Troubleshooting

### Dependencies Missing

```bash
# Run dependency check
./scripts/check-dependencies.sh

# Install missing dependencies based on output
```

### Ollama Not Working

```bash
# Check if running
curl http://localhost:11434/api/tags

# Restart (Docker)
docker restart ollama

# Restart (Native)
./scripts/setup-ollama.sh
```

### Appium Not Working

```bash
# Check if running
curl http://localhost:4723/status

# Start Appium
appium
```

## Next Steps

1. ✅ Run `./scripts/check-dependencies.sh`
2. ✅ Setup Ollama (optional): `./scripts/setup-ollama-docker.sh`
3. ✅ Verify: `curl http://localhost:11434/api/tags`
4. ✅ Run tests: `./scripts/run-persona-test-with-video.sh signup-tech-novice`

