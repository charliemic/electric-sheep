#!/bin/bash

# Script to link local Supabase project to cloud project
# Run this after creating a Supabase cloud project

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

echo "🔗 Linking Electric Sheep to Supabase Cloud"
echo "==========================================="
echo ""

# Check if already linked
if [ -f "$PROJECT_ROOT/.supabase/project.toml" ]; then
    echo "⚠️  Project appears to be already linked."
    read -p "Do you want to re-link? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Linking cancelled."
        exit 0
    fi
fi

echo "You'll need your Supabase project reference ID."
echo ""
echo "To find it:"
echo "  1. Go to: https://supabase.com/dashboard"
echo "  2. Select your project"
echo "  3. Go to: Settings → General"
echo "  4. Copy the 'Reference ID' (looks like: abcdefghijklmnop)"
echo ""

read -p "Enter your project reference ID: " PROJECT_REF

if [ -z "$PROJECT_REF" ]; then
    echo "❌ Project reference ID is required"
    exit 1
fi

echo ""
echo "🔐 Logging in to Supabase..."
echo "This will open your browser for authentication."
echo ""
supabase login

echo ""
echo "🔗 Linking to project: $PROJECT_REF"
supabase link --project-ref "$PROJECT_REF"

echo ""
echo "✅ Project linked successfully!"
echo ""
echo "📋 Next steps:"
echo "  1. Getting project credentials..."
echo ""

# Get status to show credentials
supabase status

echo ""
echo "📝 To get your credentials for the app:"
echo "  1. Go to: https://supabase.com/dashboard/project/$PROJECT_REF/settings/api"
echo "  2. Copy:"
echo "     - Project URL (API URL)"
echo "     - anon/public key"
echo ""
echo "  3. We'll configure these in the app next"
echo ""

