#!/bin/bash

# Multi-Component Version Bump Script
# Supports: app, test-framework, html-viewer, metrics-dashboard, all
# Usage: ./scripts/bump-version.sh <component> <type> [--dry-run]
# Example: ./scripts/bump-version.sh app patch

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
VERSION_FILE="version.properties"
CHANGELOG_FILE="CHANGELOG.md"
DRY_RUN=false

# Parse arguments
COMPONENT="${1:-}"
VERSION_TYPE="${2:-}"
if [[ "$3" == "--dry-run" ]]; then
    DRY_RUN=true
fi

# Validate arguments
if [[ -z "$COMPONENT" ]] || [[ -z "$VERSION_TYPE" ]]; then
    echo -e "${RED}Error: Missing arguments${NC}"
    echo "Usage: $0 <component> <type> [--dry-run]"
    echo ""
    echo "Components: app, test-framework, html-viewer, metrics-dashboard, all"
    echo "Types: patch, minor, major"
    echo ""
    echo "Examples:"
    echo "  $0 app patch          # Bump Android app patch version"
    echo "  $0 test-framework minor  # Bump test framework minor version"
    echo "  $0 all major          # Bump all components major version"
    exit 1
fi

if [[ ! "$VERSION_TYPE" =~ ^(patch|minor|major)$ ]]; then
    echo -e "${RED}Error: Invalid version type '$VERSION_TYPE'${NC}"
    echo "Valid types: patch, minor, major"
    exit 1
fi

if [[ ! "$COMPONENT" =~ ^(app|test-framework|html-viewer|metrics-dashboard|all)$ ]]; then
    echo -e "${RED}Error: Invalid component '$COMPONENT'${NC}"
    echo "Valid components: app, test-framework, html-viewer, metrics-dashboard, all"
    exit 1
fi

# Function to bump version
bump_version() {
    local version=$1
    local type=$2
    
    IFS='.' read -ra VERSION_PARTS <<< "$version"
    local major=${VERSION_PARTS[0]}
    local minor=${VERSION_PARTS[1]}
    local patch=${VERSION_PARTS[2]}
    
    case $type in
        patch)
            patch=$((patch + 1))
            ;;
        minor)
            minor=$((minor + 1))
            patch=0
            ;;
        major)
            major=$((major + 1))
            minor=0
            patch=0
            ;;
    esac
    
    echo "$major.$minor.$patch"
}

# Function to read version from properties file
read_version() {
    local component=$1
    local properties_file=$2
    
    if [[ ! -f "$properties_file" ]]; then
        echo -e "${RED}Error: Version file not found: $properties_file${NC}"
        exit 1
    fi
    
    case $component in
        app)
            grep "^app.versionName=" "$properties_file" | cut -d'=' -f2
            ;;
        test-framework)
            grep "^test-framework.version=" "$properties_file" | cut -d'=' -f2
            ;;
        html-viewer)
            grep "^html-viewer.version=" "$properties_file" | cut -d'=' -f2
            ;;
        metrics-dashboard)
            grep "^metrics-dashboard.version=" "$properties_file" | cut -d'=' -f2
            ;;
    esac
}

# Function to update version in properties file
update_version_properties() {
    local component=$1
    local new_version=$2
    local properties_file=$3
    local temp_file=$(mktemp)
    
    case $component in
        app)
            # Update versionName
            sed "s/^app.versionName=.*/app.versionName=$new_version/" "$properties_file" > "$temp_file"
            # Increment versionCode
            local current_code=$(grep "^app.versionCode=" "$properties_file" | cut -d'=' -f2)
            local new_code=$((current_code + 1))
            sed "s/^app.versionCode=.*/app.versionCode=$new_code/" "$temp_file" > "${temp_file}.2"
            mv "${temp_file}.2" "$temp_file"
            ;;
        test-framework)
            sed "s/^test-framework.version=.*/test-framework.version=$new_version/" "$properties_file" > "$temp_file"
            ;;
        html-viewer)
            sed "s/^html-viewer.version=.*/html-viewer.version=$new_version/" "$properties_file" > "$temp_file"
            ;;
        metrics-dashboard)
            sed "s/^metrics-dashboard.version=.*/metrics-dashboard.version=$new_version/" "$properties_file" > "$temp_file"
            ;;
    esac
    
    mv "$temp_file" "$properties_file"
}

# Function to update package.json version
update_package_json() {
    local component=$1
    local new_version=$2
    local package_file=""
    
    case $component in
        html-viewer)
            package_file="html-viewer/package.json"
            ;;
        metrics-dashboard)
            package_file="scripts/metrics/package.json"
            ;;
    esac
    
    if [[ -n "$package_file" ]] && [[ -f "$package_file" ]]; then
        if [[ "$DRY_RUN" == "true" ]]; then
            echo -e "${YELLOW}[DRY RUN] Would update $package_file version to $new_version${NC}"
        else
            # Use jq if available, otherwise use sed
            if command -v jq &> /dev/null; then
                jq ".version = \"$new_version\"" "$package_file" > "${package_file}.tmp" && mv "${package_file}.tmp" "$package_file"
            else
                sed -i.bak "s/\"version\": \".*\"/\"version\": \"$new_version\"/" "$package_file"
                rm -f "${package_file}.bak"
            fi
        fi
    fi
}

# Function to create git tag
create_git_tag() {
    local component=$1
    local version=$2
    
    local tag_name="${component}-v${version}"
    
    if [[ "$DRY_RUN" == "true" ]]; then
        echo -e "${YELLOW}[DRY RUN] Would create git tag: $tag_name${NC}"
    else
        if git rev-parse "$tag_name" >/dev/null 2>&1; then
            echo -e "${RED}Error: Tag $tag_name already exists${NC}"
            exit 1
        fi
        git tag "$tag_name"
        echo -e "${GREEN}Created git tag: $tag_name${NC}"
    fi
}

# Function to update changelog
update_changelog() {
    local component=$1
    local old_version=$2
    local new_version=$3
    local version_type=$4
    
    if [[ "$DRY_RUN" == "true" ]]; then
        echo -e "${YELLOW}[DRY RUN] Would update CHANGELOG.md with $component v$new_version${NC}"
        return
    fi
    
    local date=$(date +%Y-%m-%d)
    local changelog_entry="## [$new_version] - $date

### Changed
- Bumped $component version ($version_type: $old_version → $new_version)
"
    
    # Insert after [Unreleased] section
    if [[ -f "$CHANGELOG_FILE" ]]; then
        local temp_file=$(mktemp)
        awk -v entry="$changelog_entry" '
            /^## \[Unreleased\]/ {
                print
                getline
                print
                print entry
                next
            }
            { print }
        ' "$CHANGELOG_FILE" > "$temp_file"
        mv "$temp_file" "$CHANGELOG_FILE"
    fi
}

# Main execution
echo -e "${GREEN}Bumping version for component: $COMPONENT, type: $VERSION_TYPE${NC}"
if [[ "$DRY_RUN" == "true" ]]; then
    echo -e "${YELLOW}[DRY RUN MODE]${NC}"
fi
echo ""

# Determine components to bump
if [[ "$COMPONENT" == "all" ]]; then
    COMPONENTS=("app" "test-framework" "html-viewer" "metrics-dashboard")
else
    COMPONENTS=("$COMPONENT")
fi

# Process each component
for comp in "${COMPONENTS[@]}"; do
    echo -e "${GREEN}Processing component: $comp${NC}"
    
    # Read current version
    current_version=$(read_version "$comp" "$VERSION_FILE")
    if [[ -z "$current_version" ]]; then
        echo -e "${RED}Error: Could not read version for $comp${NC}"
        exit 1
    fi
    
    echo "  Current version: $current_version"
    
    # Calculate new version
    new_version=$(bump_version "$current_version" "$VERSION_TYPE")
    echo "  New version: $new_version"
    
    # Update version.properties
    if [[ "$DRY_RUN" == "true" ]]; then
        echo -e "${YELLOW}  [DRY RUN] Would update $VERSION_FILE${NC}"
    else
        update_version_properties "$comp" "$new_version" "$VERSION_FILE"
        echo -e "${GREEN}  Updated $VERSION_FILE${NC}"
    fi
    
    # Update package.json for npm components
    if [[ "$comp" == "html-viewer" ]] || [[ "$comp" == "metrics-dashboard" ]]; then
        update_package_json "$comp" "$new_version"
    fi
    
    # Update changelog
    update_changelog "$comp" "$current_version" "$new_version" "$VERSION_TYPE"
    
    # Create git tag
    create_git_tag "$comp" "$new_version"
    
    echo ""
done

# Summary
echo -e "${GREEN}Version bump complete!${NC}"
if [[ "$DRY_RUN" == "true" ]]; then
    echo -e "${YELLOW}This was a dry run. No files were modified.${NC}"
    echo "Run without --dry-run to apply changes."
else
    echo ""
    echo "Next steps:"
    echo "  1. Review changes: git diff"
    echo "  2. Review CHANGELOG.md"
    echo "  3. Commit changes: git add . && git commit -m 'chore: bump version to $new_version'"
    echo "  4. Push tags: git push --tags"
fi

