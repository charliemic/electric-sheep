#!/bin/bash

# Docker-based Ollama setup (fully cross-platform)
# Uses Docker to run Ollama, works on macOS, Linux, and Windows (WSL)

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

# Check if Docker is available
check_docker() {
    if command -v docker &> /dev/null; then
        if docker info > /dev/null 2>&1; then
            log_success "Docker is available and running"
            return 0
        else
            log_error "Docker is installed but not running"
            log_info "Start Docker Desktop or Docker daemon"
            return 1
        fi
    else
        log_error "Docker is not installed"
        log_info "Install Docker: https://docs.docker.com/get-docker/"
        return 1
    fi
}

# Check if Ollama container is running
check_ollama_container() {
    if docker ps --format '{{.Names}}' | grep -q "^ollama$"; then
        log_success "Ollama container is running"
        return 0
    else
        log_warning "Ollama container is not running"
        return 1
    fi
}

# Start Ollama container
start_ollama_container() {
    log_info "Starting Ollama Docker container..."
    
    # Check if container exists but is stopped
    if docker ps -a --format '{{.Names}}' | grep -q "^ollama$"; then
        log_info "Starting existing Ollama container..."
        docker start ollama
    else
        log_info "Creating and starting Ollama container..."
        docker run -d \
            --name ollama \
            -p 11434:11434 \
            -v ollama:/root/.ollama \
            --restart unless-stopped \
            ollama/ollama:latest
    fi
    
    log_info "Waiting for Ollama to start..."
    sleep 5
    
    # Verify it's running
    if check_ollama_container; then
        log_success "Ollama container started"
        return 0
    else
        log_error "Failed to start Ollama container"
        return 1
    fi
}

# Check if model is available in container
check_model() {
    local model="${1:-llama3.2:1b}"
    
    log_info "Checking for model: $model"
    
    if docker exec ollama ollama list 2>/dev/null | grep -q "$model"; then
        log_success "Model $model is available"
        return 0
    else
        log_warning "Model $model is not available"
        return 1
    fi
}

# Pull model in container
pull_model() {
    local model="${1:-llama3.2:1b}"
    
    log_info "Pulling model: $model (this may take a few minutes)..."
    
    if docker exec ollama ollama pull "$model"; then
        log_success "Model $model pulled successfully"
        return 0
    else
        log_error "Failed to pull model $model"
        return 1
    fi
}

# Test Ollama
test_ollama() {
    log_info "Testing Ollama..."
    
    local test_prompt="Generate a realistic email for a 68-year-old retired teacher. Return only the email address."
    local response=$(docker exec ollama ollama run llama3.2:1b "$test_prompt" 2>&1 | tail -1)
    
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
    echo "🐳 Ollama Setup (Docker)"
    echo "═══════════════════════════════════════════════════════════════"
    echo ""
    
    # Step 1: Check Docker
    if ! check_docker; then
        exit 1
    fi
    
    # Step 2: Check/Start Ollama container
    if ! check_ollama_container; then
        start_ollama_container
    fi
    
    # Step 3: Check/Pull required model
    MODEL="${OLLAMA_MODEL:-llama3.2:1b}"
    if ! check_model "$MODEL"; then
        pull_model "$MODEL"
    fi
    
    # Step 4: Test Ollama
    if test_ollama; then
        echo ""
        log_success "Ollama Docker setup complete!"
        echo ""
        log_info "Configuration:"
        log_info "  Base URL: ${OLLAMA_BASE_URL:-http://localhost:11434}"
        log_info "  Model: $MODEL"
        log_info "  Container: ollama"
        echo ""
        log_info "Useful commands:"
        log_info "  docker ps | grep ollama          # Check container status"
        log_info "  docker logs ollama               # View logs"
        log_info "  docker stop ollama               # Stop container"
        log_info "  docker start ollama              # Start container"
        log_info "  docker rm ollama                 # Remove container"
        echo ""
    else
        log_warning "Ollama setup completed but test failed"
        log_info "Check logs: docker logs ollama"
    fi
}

# Run main
main

