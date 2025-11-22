#!/bin/bash

# Changelog Generation Script
# Generates changelog entries from git commits since last release
# Usage: ./scripts/generate-changelog.sh [component] [version]
# Example: ./scripts/generate-changelog.sh app 1.0.1

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

CHANGELOG_FILE="CHANGELOG.md"
COMPONENT="${1:-}"
VERSION="${2:-}"

# If no component/version provided, generate for all components since last release
if [[ -z "$COMPONENT" ]] || [[ -z "$VERSION" ]]; then
    echo -e "${YELLOW}Usage: $0 <component> <version>${NC}"
    echo "Example: $0 app 1.0.1"
    echo ""
    echo "Or generate for all components since last release:"
    echo "  $0 all"
    exit 1
fi

# Function to get last release tag for component
get_last_tag() {
    local component=$1
    if [[ "$component" == "all" ]]; then
        # Get most recent unified tag
        git tag -l "v*" --sort=-version:refname | head -1
    else
        # Get most recent component-specific tag
        git tag -l "${component}-v*" --sort=-version:refname | head -1
    fi
}

# Function to parse commit message and categorize
categorize_commit() {
    local message=$1
    local component=$2
    
    # Conventional Commits format
    if [[ "$message" =~ ^(feat|feature)(\(.+\))?: ]]; then
        echo "Added"
    elif [[ "$message" =~ ^(fix|bugfix)(\(.+\))?: ]]; then
        echo "Fixed"
    elif [[ "$message" =~ ^(docs|documentation)(\(.+\))?: ]]; then
        echo "Documentation"
    elif [[ "$message" =~ ^(refactor)(\(.+\))?: ]]; then
        echo "Changed"
    elif [[ "$message" =~ ^(perf|performance)(\(.+\))?: ]]; then
        echo "Performance"
    elif [[ "$message" =~ ^(test)(\(.+\))?: ]]; then
        echo "Testing"
    elif [[ "$message" =~ ^(chore)(\(.+\))?: ]]; then
        echo "Miscellaneous"
    elif [[ "$message" =~ ^(breaking|BREAKING)(\(.+\))?: ]]; then
        echo "Breaking Changes"
    else
        echo "Changed"
    fi
}

# Function to format commit message for changelog
format_commit_message() {
    local message=$1
    # Remove conventional commit prefix
    message=$(echo "$message" | sed -E 's/^(feat|feature|fix|bugfix|docs|documentation|refactor|perf|performance|test|chore|breaking|BREAKING)(\(.+\))?: //')
    # Capitalize first letter
    echo "$message" | sed 's/^./\U&/'
}

# Function to get commits since last release
get_commits_since() {
    local component=$1
    local last_tag=$(get_last_tag "$component")
    
    if [[ -z "$last_tag" ]]; then
        # No previous release, get all commits
        git log --pretty=format:"%s" --reverse
    else
        # Get commits since last tag
        git log "${last_tag}..HEAD" --pretty=format:"%s" --reverse
    fi
}

# Function to filter commits by component
filter_commits_by_component() {
    local component=$1
    local commits="$2"
    
    if [[ "$component" == "all" ]]; then
        echo "$commits"
        return
    fi
    
    # Filter commits that mention component or affect component files
    while IFS= read -r commit; do
        local lower_commit=$(echo "$commit" | tr '[:upper:]' '[:lower:]')
        local lower_component=$(echo "$component" | tr '[:upper:]' '[:lower:]')
        
        # Check if commit mentions component
        if [[ "$lower_commit" =~ $lower_component ]] || [[ "$lower_commit" =~ "${lower_component//-/ }" ]]; then
            echo "$commit"
        fi
    done <<< "$commits"
}

# Function to generate changelog entry
generate_changelog_entry() {
    local component=$1
    local version=$2
    local date=$(date +%Y-%m-%d)
    
    # Get commits since last release
    local commits=$(get_commits_since "$component")
    
    if [[ -z "$commits" ]]; then
        echo -e "${YELLOW}No commits found since last release${NC}"
        return
    fi
    
    # Filter commits by component (if not "all")
    if [[ "$component" != "all" ]]; then
        commits=$(filter_commits_by_component "$component" "$commits")
    fi
    
    if [[ -z "$commits" ]]; then
        echo -e "${YELLOW}No commits found for component: $component${NC}"
        return
    fi
    
    # Categorize commits (using temp files for compatibility)
    temp_dir=$(mktemp -d)
    category_order=("Added" "Changed" "Fixed" "Performance" "Documentation" "Testing" "Breaking_Changes" "Miscellaneous")
    
    while IFS= read -r commit; do
        local category=$(categorize_commit "$commit" "$component")
        local formatted=$(format_commit_message "$commit")
        
        # Normalize category name for filename
        local category_file="${category// /_}"
        echo "- $formatted" >> "${temp_dir}/${category_file}.txt"
    done <<< "$commits"
    
    # Generate changelog entry
    local entry="## [$version] - $date"
    if [[ "$component" != "all" ]]; then
        entry="## [$component $version] - $date"
    fi
    entry+=$'\n'
    entry+=$'\n'
    
    # Add categories in order
    for category in "${category_order[@]}"; do
        local category_file="${temp_dir}/${category// /_}.txt"
        if [[ -f "$category_file" ]]; then
            local display_name="${category//_/ }"
            entry+="### $display_name"$'\n'
            entry+=$(cat "$category_file")
            entry+=$'\n'
        fi
    done
    
    # Cleanup
    rm -rf "$temp_dir"
    
    echo "$entry"
}

# Main execution
if [[ "$COMPONENT" == "all" ]]; then
    # Generate for all components
    echo -e "${GREEN}Generating changelog for all components${NC}"
    
    # Get last unified release tag
    last_tag=$(get_last_tag "all")
    if [[ -z "$last_tag" ]]; then
        echo -e "${YELLOW}No previous release found. Generating from all commits.${NC}"
    else
        echo "Last release: $last_tag"
    fi
    
    # Generate entry
    entry=$(generate_changelog_entry "all" "$VERSION")
    
    if [[ -n "$entry" ]]; then
        # Insert into CHANGELOG.md after [Unreleased] section
        if [[ -f "$CHANGELOG_FILE" ]]; then
            entry_file=$(mktemp)
            echo "$entry" > "$entry_file"
            
            temp_file=$(mktemp)
            awk '
                /^## \[Unreleased\]/ {
                    print
                    getline
                    print
                    # Insert entry from file
                    while ((getline line < entry_file) > 0) {
                        print line
                    }
                    close(entry_file)
                    next
                }
                { print }
            ' entry_file="$entry_file" "$CHANGELOG_FILE" > "$temp_file"
            mv "$temp_file" "$CHANGELOG_FILE"
            rm -f "$entry_file"
            echo -e "${GREEN}Changelog updated: $CHANGELOG_FILE${NC}"
        else
            echo "$entry" > "$CHANGELOG_FILE"
            echo -e "${GREEN}Created changelog: $CHANGELOG_FILE${NC}"
        fi
    fi
else
    # Generate for specific component
    echo -e "${GREEN}Generating changelog for component: $COMPONENT, version: $VERSION${NC}"
    
    # Get last component-specific release tag
    last_tag=$(get_last_tag "$COMPONENT")
    if [[ -z "$last_tag" ]]; then
        echo -e "${YELLOW}No previous release found for $COMPONENT. Generating from all commits.${NC}"
    else
        echo "Last release: $last_tag"
    fi
    
    # Generate entry
    entry=$(generate_changelog_entry "$COMPONENT" "$VERSION")
    
    if [[ -n "$entry" ]]; then
        # Insert into CHANGELOG.md after [Unreleased] section
        if [[ -f "$CHANGELOG_FILE" ]]; then
            entry_file=$(mktemp)
            echo "$entry" > "$entry_file"
            
            temp_file=$(mktemp)
            awk '
                /^## \[Unreleased\]/ {
                    print
                    getline
                    print
                    # Insert entry from file
                    while ((getline line < entry_file) > 0) {
                        print line
                    }
                    close(entry_file)
                    next
                }
                { print }
            ' entry_file="$entry_file" "$CHANGELOG_FILE" > "$temp_file"
            mv "$temp_file" "$CHANGELOG_FILE"
            rm -f "$entry_file"
            echo -e "${GREEN}Changelog updated: $CHANGELOG_FILE${NC}"
        else
            echo "$entry" > "$CHANGELOG_FILE"
            echo -e "${GREEN}Created changelog: $CHANGELOG_FILE${NC}"
        fi
    fi
fi

echo -e "${GREEN}Changelog generation complete!${NC}"

