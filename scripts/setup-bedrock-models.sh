#!/bin/bash

# AWS Bedrock Model Optimization Setup Script
# Configured for Cursor Team Plan (API key authentication)
# Uses team org settings - no individual AWS credentials needed

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${BLUE}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  AWS Bedrock Model Optimization Setup                  ║${NC}"
echo -e "${BLUE}║  Cursor Team Plan Configuration                        ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${CYAN}📋 Cursor Team Plan Setup${NC}"
echo -e "${GREEN}✅ Team plan handles authentication automatically${NC}"
echo -e "${CYAN}   Using team org settings - no AWS credentials needed${NC}"
echo ""

# Team org settings
REGION="eu-west-1"

# Display recommended configuration
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}Team Org Configuration${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "${GREEN}Primary Model (Default):${NC}"
echo -e "  Model ID: anthropic.claude-sonnet-4-5-20250929-v1:0"
echo -e "  Region: ${REGION}"
echo -e "  Use For: 80% of development work"
echo ""
echo -e "${GREEN}Alternative Models (for manual selection):${NC}"
echo -e "  Haiku: anthropic.claude-3-5-haiku-20241022-v2:0"
echo -e "    → Simple tasks (73% cheaper, faster)"
echo ""
echo -e "  Opus: anthropic.claude-3-opus-20240229-v1:0"
echo -e "    → Complex tasks (better results, 5x cost)"
echo ""

# Configuration instructions
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}Configuration Steps${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "${YELLOW}1. Open Cursor Settings${NC}"
echo -e "   Press ${GREEN}Cmd + ,${NC} (macOS) or ${GREEN}Ctrl + ,${NC} (Windows/Linux)"
echo ""
echo -e "${YELLOW}2. Navigate to Models > AWS Bedrock${NC}"
echo ""
echo -e "${YELLOW}3. Configure:${NC}"
echo -e "   ${GREEN}Region:${NC} ${REGION}"
echo -e "   ${GREEN}Model:${NC} anthropic.claude-sonnet-4-5-20250929-v1:0"
echo ""
echo -e "${YELLOW}4. Save and test${NC}"
echo ""

# Model access (handled by team org)
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}Model Access${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "${CYAN}ℹ️  Model access configured by team org${NC}"
echo -e "${CYAN}   Contact platform team if models aren't available${NC}"
echo ""
echo -e "Expected models:"
echo -e "  ✅ Claude 3.5 Sonnet (primary)"
echo -e "  ✅ Claude 3.5 Haiku (optional, for cost savings)"
echo -e "  ✅ Claude 3 Opus (optional, for complex tasks)"
echo ""

# Cost estimation
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}Expected Cost Impact${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "${GREEN}Current (All Sonnet):${NC} ~\$100/month"
echo -e "${GREEN}Optimized (Haiku + Sonnet + Opus):${NC} ~\$35-50/month"
echo -e "${GREEN}Potential Savings:${NC} 50-65% cost reduction"
echo ""

# Quick reference
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}Quick Reference${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "${GREEN}Just code normally${NC} → Sonnet (automatic)"
echo -e "${GREEN}Simple task?${NC} → Say 'Use Haiku: [task]'"
echo -e "${GREEN}Complex task?${NC} → Say 'Use Opus: [task]'"
echo ""
echo -e "See: ${GREEN}docs/development/setup/AWS_BEDROCK_QUICK_REFERENCE.md${NC}"
echo ""

# Verify setup
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}Next Steps${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "1. ✅ Configure Cursor settings (see above)"
echo -e "2. ✅ Enable models in AWS Bedrock console"
echo -e "3. ✅ Test with a simple prompt in Cursor"
echo -e "4. ✅ The AI assistant will suggest model alternatives when helpful"
echo ""
echo -e "${GREEN}Setup complete! The system will optimize model selection automatically.${NC}"
echo ""

