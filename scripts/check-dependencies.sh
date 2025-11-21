#!/bin/bash

# Dependency checker for test automation framework
# Checks all required dependencies and services

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Status tracking
ALL_GOOD=true

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
    ALL_GOOD=false
}

# Check command exists
check_command() {
    local cmd=$1
    local name=${2:-$cmd}
    
    if command -v "$cmd" &> /dev/null; then
        log_success "$name is installed"
        return 0
    else
        log_error "$name is not installed"
        return 1
    fi
}

# Check service is running
check_service() {
    local name=$1
    local url=$2
    local check_cmd=${3:-"curl -s $url > /dev/null 2>&1"}
    
    if eval "$check_cmd"; then
        log_success "$name is running"
        return 0
    else
        log_warning "$name is not running (optional)"
        return 1
    fi
}

# Check Ollama
check_ollama() {
    log_info "Checking Ollama..."
    
    if check_command ollama "Ollama"; then
        if check_service "Ollama service" "http://localhost:11434/api/tags"; then
            # Check if model is available
            MODEL="${OLLAMA_MODEL:-llama3.2:1b}"
            if ollama list 2>/dev/null | grep -q "$MODEL"; then
                log_success "Ollama model $MODEL is available"
            else
                log_warning "Ollama model $MODEL is not available (run: ollama pull $MODEL)"
            fi
        fi
    else
        log_warning "Ollama is optional - will use procedural email generation"
    fi
}

# Check Appium
check_appium() {
    log_info "Checking Appium..."
    
    if check_command appium "Appium"; then
        if check_service "Appium server" "http://localhost:4723/status"; then
            log_info "  Appium server is running on port 4723"
        else
            log_warning "  Appium server is not running (start with: appium)"
        fi
    fi
}

# Check Android tools
check_android() {
    log_info "Checking Android tools..."
    
    if [ -n "$ANDROID_HOME" ]; then
        log_success "ANDROID_HOME is set: $ANDROID_HOME"
    else
        log_warning "ANDROID_HOME is not set"
    fi
    
    check_command adb "ADB"
    
    # Check emulator (may be in ANDROID_HOME/emulator)
    if command -v emulator &> /dev/null || [ -f "$ANDROID_HOME/emulator/emulator" ]; then
        log_success "Android Emulator is available"
    else
        log_warning "Android Emulator not found (may be in ANDROID_HOME/emulator)"
    fi
}

# Check Java/Kotlin
check_java() {
    log_info "Checking Java..."
    
    if check_command java "Java"; then
        JAVA_VERSION=$(java -version 2>&1 | head -1)
        log_info "  $JAVA_VERSION"
    fi
}

# Check Gradle
check_gradle() {
    log_info "Checking Gradle..."
    
    if [ -f "$PROJECT_ROOT/gradlew" ]; then
        log_success "Gradle wrapper is available"
    else
        log_warning "Gradle wrapper not found"
    fi
}

# Check OCR tools
check_ocr() {
    log_info "Checking OCR tools..."
    
    if check_command tesseract "Tesseract OCR"; then
        TESSERACT_VERSION=$(tesseract --version 2>&1 | head -1)
        log_info "  $TESSERACT_VERSION"
    fi
}

# Check OpenCV
check_opencv() {
    log_info "Checking OpenCV..."
    
    # OpenCV is loaded as a library, so we check if it's in the build
    if [ -f "$PROJECT_ROOT/test-automation/build.gradle.kts" ]; then
        if grep -q "opencv" "$PROJECT_ROOT/test-automation/build.gradle.kts"; then
            log_success "OpenCV dependency configured"
        else
            log_warning "OpenCV dependency not found in build.gradle.kts"
        fi
    fi
}

# Check emulator
check_emulator() {
    log_info "Checking Android emulator..."
    
    if command -v adb &> /dev/null; then
        DEVICES=$(adb devices | grep -v "List" | grep "device" | wc -l | tr -d ' ')
        if [ "$DEVICES" -gt 0 ]; then
            log_success "Android device(s) connected: $DEVICES"
            adb devices | grep "device" | while read line; do
                log_info "  $line"
            done
        else
            log_warning "No Android devices connected"
        fi
    fi
}

# Main check function
main() {
    echo ""
    echo "═══════════════════════════════════════════════════════════════"
    echo "🔍 Dependency Check"
    echo "═══════════════════════════════════════════════════════════════"
    echo ""
    
    # Required dependencies
    log_info "=== Required Dependencies ==="
    check_java
    check_gradle
    check_android
    check_appium
    check_ocr
    check_opencv
    check_emulator
    
    echo ""
    log_info "=== Optional Dependencies ==="
    check_ollama
    
    echo ""
    echo "═══════════════════════════════════════════════════════════════"
    
    if [ "$ALL_GOOD" = true ]; then
        log_success "All required dependencies are available!"
        echo ""
        exit 0
    else
        log_error "Some required dependencies are missing"
        echo ""
        log_info "Run setup scripts to install missing dependencies:"
        log_info "  ./scripts/setup-ollama.sh  # For Ollama (optional)"
        echo ""
        exit 1
    fi
}

# Run main
main

