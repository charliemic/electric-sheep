# Problem Space Analysis

## Current State

### Existing Tools

1. **`scripts/md-to-html.py`**
   - Manual markdown to HTML conversion
   - Embeds images as base64
   - Uses Water.css for styling
   - Hardcoded logic for specific document types
   - Outputs to CloudFiles for Google Drive sync

2. **`scripts/md-to-html.sh`**
   - Wrapper script
   - Uses pandoc if available, falls back to Python script
   - Outputs to CloudFiles directory

### Limitations

- ❌ Manual conversion process (command-line only)
- ❌ Hardcoded styling and layout
- ❌ No web interface for preview/editing
- ❌ Limited customization options
- ❌ No support for log file formatting
- ❌ No search or navigation features
- ❌ Static output only (no interactive features)

## Requirements

### Core Features

1. **Markdown Conversion**
   - Convert markdown to styled HTML
   - Support all markdown features (tables, code blocks, etc.)
   - Preserve image references (embed or link)
   - Generate table of contents

2. **Log File Support**
   - Format log files with syntax highlighting
   - Support common log formats (JSON, plain text, etc.)
   - Line numbers and timestamps
   - Filtering and search

3. **Web Interface**
   - Upload or paste content
   - Live preview
   - Customization options (themes, fonts, etc.)
   - Download generated HTML

4. **Sharing & Hosting**
   - Generate standalone HTML files
   - Option to host online
   - Easy sharing via links

### Technical Requirements

- Modern framework (not React - simpler alternative)
- Fast and lightweight
- Easy to deploy
- Works with existing ecosystem (Supabase/Railway)

## Solution Approach

### Framework Choice: Astro

**Why Astro?**
- ✅ Modern but simpler than React
- ✅ Great for static sites (perfect for HTML generation)
- ✅ Fast and lightweight
- ✅ Easy to deploy anywhere
- ✅ Can add interactivity where needed
- ✅ TypeScript support
- ✅ Component-based but simpler

**Alternatives Considered:**
- SvelteKit - Good but more complex for static sites
- Vanilla JS - Too much manual work
- Next.js - Overkill, React-based
- Vite + Vue - Good but Astro is simpler

### Architecture

```
┌─────────────────────────────────────┐
│   Web Interface (Astro)            │
│   - Upload/Paste content           │
│   - Live preview                   │
│   - Customization options          │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│   Conversion Engine                 │
│   - Markdown parser (marked)        │
│   - Log formatter                   │
│   - HTML generator                  │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│   Output                             │
│   - Standalone HTML file            │
│   - Hosted version (optional)       │
└─────────────────────────────────────┘
```

### Hosting Options

1. **Supabase Static Hosting** (if available)
   - Already in ecosystem
   - Free tier
   - Easy deployment

2. **Railway**
   - Already mentioned in docs
   - Simple deployment
   - Free tier available

3. **GitHub Pages**
   - Free
   - Easy setup
   - Automatic deployment

4. **Netlify/Vercel**
   - Free tier
   - One-click deployment
   - CDN included

## Implementation Plan

### Phase 1: Core Conversion (MVP)
- [x] Set up Astro project
- [ ] Markdown to HTML converter
- [ ] Basic web interface
- [ ] Download functionality

### Phase 2: Enhanced Features
- [ ] Log file support
- [ ] Syntax highlighting
- [ ] Theme customization
- [ ] Table of contents

### Phase 3: Hosting & Sharing
- [ ] Deploy to hosting platform
- [ ] Shareable links
- [ ] Integration with existing tools

## Success Criteria

- ✅ Convert markdown to beautiful HTML
- ✅ Web interface for easy use
- ✅ Standalone HTML files work offline
- ✅ Easy to share and host
- ✅ Faster and simpler than current solution

