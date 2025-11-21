# Ollama Setup Guide

## Overview

Ollama is a self-hosted LLM service used for generating realistic email addresses based on persona characteristics. It's **optional** - the system will fallback to procedural email generation if Ollama is not available.

## Installation Options

### Option 1: Docker (Recommended - Cross-Platform)

**Works on**: macOS, Linux, Windows (WSL), CI/CD

```bash
# Run setup script
./scripts/setup-ollama-docker.sh
```

**Benefits**:
- ✅ Fully cross-platform
- ✅ Isolated environment
- ✅ Easy to remove/restart
- ✅ Works in CI/CD

**Requirements**: Docker Desktop or Docker Engine

### Option 2: Native Installation

**macOS**:
```bash
brew install ollama
./scripts/setup-ollama.sh
```

**Linux**:
```bash
curl -fsSL https://ollama.ai/install.sh | sh
./scripts/setup-ollama.sh
```

**Windows**:
1. Download from: https://ollama.ai/download
2. Install the `.exe` file
3. Run `ollama serve` manually or use Docker option

## Quick Start

### Docker (Recommended)

```bash
# One command setup
./scripts/setup-ollama-docker.sh
```

### Native

```bash
# macOS/Linux
./scripts/setup-ollama.sh
```

## Verify Installation

```bash
# Check if Ollama is running
curl http://localhost:11434/api/tags

# List available models
ollama list  # Native
docker exec ollama ollama list  # Docker
```

## Configuration

The test automation framework uses these environment variables:

```bash
# Optional: Custom Ollama URL (default: http://localhost:11434)
export OLLAMA_BASE_URL="http://localhost:11434"

# Optional: Custom model (default: llama3.2:1b)
export OLLAMA_MODEL="llama3.2:1b"
```

## Docker Commands

```bash
# Check container status
docker ps | grep ollama

# View logs
docker logs ollama

# Stop container
docker stop ollama

# Start container
docker start ollama

# Remove container (keeps models in volume)
docker rm ollama

# Remove everything (including models)
docker rm -v ollama
```

## Troubleshooting

### Docker: Container Won't Start

**Problem**: `docker: Error response from daemon`

**Solution**:
```bash
# Check Docker is running
docker info

# Check if port 11434 is in use
lsof -i :11434  # macOS/Linux
netstat -ano | findstr :11434  # Windows

# Remove old container and recreate
docker rm ollama
./scripts/setup-ollama-docker.sh
```

### Native: Service Won't Start

**Problem**: Ollama service fails to start

**Solution**:
```bash
# Check if process is running
ps aux | grep ollama  # macOS/Linux
tasklist | findstr ollama  # Windows

# Check logs
cat /tmp/ollama.log  # If started manually
journalctl -u ollama  # Linux systemd
brew services list  # macOS Homebrew

# Start manually
ollama serve
```

### Model Not Found

**Problem**: `model not found` error

**Solution**:
```bash
# Native
ollama pull llama3.2:1b

# Docker
docker exec ollama ollama pull llama3.2:1b
```

### Slow Generation

**Problem**: Email generation is slow

**Solution**:
```bash
# Use a smaller, faster model
export OLLAMA_MODEL="llama3.2:1b"
ollama pull llama3.2:1b  # Native
docker exec ollama ollama pull llama3.2:1b  # Docker
```

## CI/CD Integration

### GitHub Actions

```yaml
- name: Setup Ollama (Docker)
  run: |
    docker run -d --name ollama -p 11434:11434 ollama/ollama:latest
    sleep 10
    docker exec ollama ollama pull llama3.2:1b

- name: Run Tests
  env:
    OLLAMA_BASE_URL: http://localhost:11434
    OLLAMA_MODEL: llama3.2:1b
  run: ./gradlew test
```

### GitLab CI

```yaml
services:
  - docker:dind

before_script:
  - docker run -d --name ollama -p 11434:11434 ollama/ollama:latest
  - sleep 10
  - docker exec ollama ollama pull llama3.2:1b
```

## Fallback Behavior

**If Ollama is not available**, the system will:
- ✅ Use procedural email generation
- ✅ Still generate persona-appropriate emails
- ✅ Continue test execution normally
- ⚠️ Emails may be less diverse/realistic

**Logs will show**:
```
Ollama not available, will use procedural email generation
Generated email for persona Sarah Johnson: sarah.johnson@gmail.com
```

## Optional vs Required

**Ollama is OPTIONAL**:
- ✅ Tests will run without it
- ✅ Email generation still works (procedural fallback)
- ✅ All other features work normally

**Ollama is RECOMMENDED for**:
- ✅ More realistic, diverse email addresses
- ✅ Better persona matching
- ✅ More human-like test data
- ✅ CI/CD consistency

## Next Steps

1. **Choose installation method**: Docker (recommended) or Native
2. **Run setup script**: `./scripts/setup-ollama-docker.sh` or `./scripts/setup-ollama.sh`
3. **Verify**: `curl http://localhost:11434/api/tags`
4. **Run Test**: The framework will automatically use Ollama if available
