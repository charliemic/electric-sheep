#!/bin/bash

# Interactive Supabase setup script
# Guides user through initial Supabase configuration

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "🚀 Electric Sheep - Supabase Setup"
echo "===================================="
echo ""

# Check if Supabase CLI is installed
if ! command -v supabase &> /dev/null; then
    echo "❌ Supabase CLI is not installed."
    echo ""
    echo "Install it with:"
    echo "  brew install supabase/tap/supabase"
    echo ""
    echo "Or visit: https://supabase.com/docs/guides/cli"
    exit 1
fi

echo "✅ Supabase CLI found: $(supabase --version)"
echo ""

# Check if already initialized
if [ -f "$PROJECT_ROOT/supabase/config.toml" ]; then
    echo "⚠️  Supabase is already initialized in this project."
    read -p "Do you want to reinitialize? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Setup cancelled."
        exit 0
    fi
fi

echo "This setup will guide you through:"
echo "  1. Creating/linking a Supabase project"
echo "  2. Setting up local development environment"
echo "  3. Configuring app credentials"
echo ""

# Ask about project type
echo "📋 Project Setup"
echo "----------------"
echo "Choose your setup type:"
echo "  1) Use Supabase Cloud (free tier) - Recommended for production"
echo "  2) Use local Supabase (Docker) - For development only"
echo ""
read -p "Enter choice (1 or 2): " -n 1 -r SETUP_TYPE
echo ""

if [ "$SETUP_TYPE" = "1" ]; then
    echo ""
    echo "🌐 Supabase Cloud Setup"
    echo "----------------------"
    echo ""
    echo "You'll need:"
    echo "  - A Supabase account (create at https://supabase.com/dashboard)"
    echo "  - A new or existing Supabase project"
    echo ""
    read -p "Do you have a Supabase account? (y/N): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo ""
        echo "Please create an account at: https://supabase.com/dashboard"
        echo "Then run this script again."
        exit 0
    fi
    
    echo ""
    echo "Next steps:"
    echo "  1. Login to Supabase CLI"
    echo "  2. Create or select a project"
    echo "  3. Link to this repository"
    echo ""
    read -p "Ready to continue? (Y/n): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Nn]$ ]]; then
        exit 0
    fi
    
    # Login
    echo ""
    echo "🔐 Logging in to Supabase..."
    supabase login
    
    # List projects or create new
    echo ""
    echo "📁 Projects"
    echo "-----------"
    echo "Choose an option:"
    echo "  1) Link to existing project"
    echo "  2) Create new project (opens browser)"
    echo ""
    read -p "Enter choice (1 or 2): " -n 1 -r PROJECT_CHOICE
    echo ""
    
    if [ "$PROJECT_CHOICE" = "2" ]; then
        echo ""
        echo "Opening Supabase dashboard to create project..."
        echo "After creating, you'll need the project reference ID."
        open "https://supabase.com/dashboard/new"
        echo ""
        read -p "Press Enter when you've created the project..."
    fi
    
    # Link project
    echo ""
    read -p "Enter your Supabase project reference ID: " PROJECT_REF
    echo ""
    echo "🔗 Linking to project: $PROJECT_REF"
    supabase link --project-ref "$PROJECT_REF"
    
    echo ""
    echo "✅ Project linked successfully!"
    echo ""
    echo "Getting project credentials..."
    supabase status
    
elif [ "$SETUP_TYPE" = "2" ]; then
    echo ""
    echo "🐳 Local Supabase Setup (Docker)"
    echo "-------------------------------"
    echo ""
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        echo "❌ Docker is not installed or not running."
        echo "Please install Docker Desktop: https://www.docker.com/products/docker-desktop"
        exit 1
    fi
    
    if ! docker ps &> /dev/null; then
        echo "❌ Docker is not running."
        echo "Please start Docker Desktop and try again."
        exit 1
    fi
    
    echo "✅ Docker is running"
    echo ""
    echo "Starting local Supabase instance..."
    echo "This will:"
    echo "  - Start PostgreSQL database"
    echo "  - Start Supabase API"
    echo "  - Start Auth service"
    echo "  - Start Storage service"
    echo ""
    read -p "Continue? (Y/n): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Nn]$ ]]; then
        exit 0
    fi
    
    cd "$PROJECT_ROOT"
    supabase start
    
    echo ""
    echo "✅ Local Supabase is running!"
    echo ""
    supabase status
fi

echo ""
echo "📝 Next Steps"
echo "-------------"
echo ""
echo "1. Apply initial migration:"
echo "   supabase db reset"
echo ""
echo "2. Get your credentials:"
echo "   supabase status"
echo ""
echo "3. Update app configuration:"
echo "   - Add SUPABASE_URL to BuildConfig or local.properties"
echo "   - Add SUPABASE_ANON_KEY to BuildConfig or local.properties"
echo ""
echo "4. Test the connection in your app"
echo ""
echo "Setup complete! 🎉"

