#!/bin/bash

# Setup script for Ollama (self-hosted LLM service)
# Ensures Ollama is installed, running, and has required models
# 
# Cross-platform support:
# - macOS: Homebrew services or manual background process
# - Linux: systemd or manual background process
# - Windows: Manual background process (or use Docker)
# - Docker: Use setup-ollama-docker.sh for fully cross-platform setup

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Check if Ollama is installed
check_ollama_installed() {
    if command -v ollama &> /dev/null; then
        log_success "Ollama is installed"
        ollama --version
        return 0
    else
        log_warning "Ollama is not installed"
        return 1
    fi
}

# Install Ollama
install_ollama() {
    log_info "Installing Ollama..."
    
    if [[ "$OSTYPE" == "darwin"* ]]; then
        if command -v brew &> /dev/null; then
            log_info "Installing via Homebrew..."
            brew install ollama
            log_success "Ollama installed via Homebrew"
        else
            log_error "Homebrew not found. Please install Ollama manually:"
            log_info "Visit: https://ollama.ai/download"
            exit 1
        fi
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        log_info "Installing via install script..."
        curl -fsSL https://ollama.ai/install.sh | sh
        log_success "Ollama installed"
    else
        log_error "Unsupported OS. Please install Ollama manually:"
        log_info "Visit: https://ollama.ai/download"
        exit 1
    fi
}

# Check if Ollama service is running
check_ollama_running() {
    if curl -s http://localhost:11434/api/tags > /dev/null 2>&1; then
        log_success "Ollama service is running"
        return 0
    else
        log_warning "Ollama service is not running"
        return 1
    fi
}

# Start Ollama service (cross-platform)
start_ollama() {
    log_info "Starting Ollama service..."
    
    # Check if already running
    if check_ollama_running; then
        log_success "Ollama service is already running"
        return 0
    fi
    
    # Try platform-specific service management first
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS: Try Homebrew service first, fallback to manual
        if command -v brew &> /dev/null && brew services list 2>/dev/null | grep -q ollama; then
            log_info "Starting via Homebrew services..."
            brew services start ollama
            sleep 5
        else
            log_info "Starting Ollama manually (background process)..."
            nohup ollama serve > /tmp/ollama.log 2>&1 &
            OLLAMA_PID=$!
            echo $OLLAMA_PID > /tmp/ollama.pid
            sleep 5
            log_info "Ollama started (PID: $OLLAMA_PID, logs: /tmp/ollama.log)"
        fi
    elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
        # Linux: Try systemd first, fallback to manual
        if systemctl is-available ollama 2>/dev/null; then
            log_info "Starting via systemd..."
            sudo systemctl start ollama
            sleep 5
        else
            log_info "Starting Ollama manually (background process)..."
            nohup ollama serve > /tmp/ollama.log 2>&1 &
            OLLAMA_PID=$!
            echo $OLLAMA_PID > /tmp/ollama.pid
            sleep 5
            log_info "Ollama started (PID: $OLLAMA_PID, logs: /tmp/ollama.log)"
        fi
    else
        # Other platforms: Always manual
        log_info "Starting Ollama manually (background process)..."
        nohup ollama serve > /tmp/ollama.log 2>&1 &
        OLLAMA_PID=$!
        echo $OLLAMA_PID > /tmp/ollama.pid
        sleep 5
        log_info "Ollama started (PID: $OLLAMA_PID, logs: /tmp/ollama.log)"
    fi
    
    # Verify it started
    local max_attempts=10
    local attempt=0
    while [ $attempt -lt $max_attempts ]; do
        if check_ollama_running; then
            log_success "Ollama service started"
            return 0
        fi
        attempt=$((attempt + 1))
        log_info "Waiting for Ollama to start... (attempt $attempt/$max_attempts)"
        sleep 2
    done
    
    log_error "Failed to start Ollama service after $max_attempts attempts"
    log_info "Check logs: /tmp/ollama.log"
    return 1
}

# Check if required model is available
check_model() {
    local model="${1:-llama3.2:1b}"
    
    log_info "Checking for model: $model"
    
    if ollama list | grep -q "$model"; then
        log_success "Model $model is available"
        return 0
    else
        log_warning "Model $model is not available"
        return 1
    fi
}

# Pull required model
pull_model() {
    local model="${1:-llama3.2:1b}"
    
    log_info "Pulling model: $model (this may take a few minutes)..."
    
    if ollama pull "$model"; then
        log_success "Model $model pulled successfully"
        return 0
    else
        log_error "Failed to pull model $model"
        return 1
    fi
}

# Test Ollama with a simple generation
test_ollama() {
    log_info "Testing Ollama with a simple generation..."
    
    local test_prompt="Generate a realistic email for a 68-year-old retired teacher. Return only the email address."
    local response=$(ollama run llama3.2:1b "$test_prompt" 2>&1 | tail -1)
    
    if [[ -n "$response" ]] && [[ "$response" == *"@"* ]]; then
        log_success "Ollama test successful"
        log_info "Test response: $response"
        return 0
    else
        log_warning "Ollama test returned unexpected response: $response"
        return 1
    fi
}

# Main setup function
main() {
    echo ""
    echo "═══════════════════════════════════════════════════════════════"
    echo "🔧 Ollama Setup"
    echo "═══════════════════════════════════════════════════════════════"
    echo ""
    
    # Step 1: Check/Install Ollama
    if ! check_ollama_installed; then
        install_ollama
    fi
    
    # Step 2: Check/Start Ollama service
    if ! check_ollama_running; then
        start_ollama
    fi
    
    # Step 3: Check/Pull required model
    MODEL="${OLLAMA_MODEL:-llama3.2:1b}"
    if ! check_model "$MODEL"; then
        pull_model "$MODEL"
    fi
    
    # Step 4: Test Ollama
    if test_ollama; then
        echo ""
        log_success "Ollama setup complete!"
        echo ""
        log_info "Configuration:"
        log_info "  Base URL: ${OLLAMA_BASE_URL:-http://localhost:11434}"
        log_info "  Model: $MODEL"
        echo ""
        log_info "To verify manually:"
        log_info "  curl http://localhost:11434/api/tags"
        log_info "  ollama list"
        echo ""
    else
        log_warning "Ollama setup completed but test failed"
        log_info "You may need to check Ollama logs or restart the service"
    fi
}

# Run main
main

