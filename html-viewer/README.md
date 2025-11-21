# HTML Viewer & Converter Tool

A modern web-based tool for converting markdown files, logs, and other text formats into beautiful, shareable HTML documents.

## Features

- 📝 **Markdown to HTML** - Convert markdown files to styled HTML
- 📋 **Log Viewer** - Format and display log files with syntax highlighting
- 🎨 **Modern Design** - Clean, responsive UI with customizable themes
- 📤 **Easy Sharing** - Generate standalone HTML files or host online
- 🖼️ **Image Support** - Embed images directly (base64) or link externally
- 🔍 **Search & Navigation** - Built-in search and table of contents

## Tech Stack

- **Astro** - Modern static site framework (simpler than React, faster)
- **TypeScript** - Type-safe development
- **Tailwind CSS** - Utility-first styling
- **Marked** - Markdown parsing
- **Prism.js** - Syntax highlighting for code blocks

## Development

```bash
# Install dependencies
npm install

# Start dev server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

## Usage

### Command Line

```bash
# Convert markdown to HTML
npm run convert -- input.md output.html

# Convert with custom theme
npm run convert -- input.md output.html --theme dark
```

### Web Interface

1. Open the web interface
2. Upload or paste markdown/log content
3. Customize styling and options
4. Download or share the generated HTML

## Hosting

This tool can be hosted on:

- **Supabase** - Static hosting (if available)
- **Railway** - Simple deployment
- **GitHub Pages** - Free static hosting
- **Netlify/Vercel** - One-click deployment

## Architecture

```
html-viewer/
├── src/
│   ├── components/     # Astro components
│   ├── layouts/         # Page layouts
│   ├── pages/          # Routes
│   ├── lib/            # Utilities (converters, parsers)
│   └── styles/         # Global styles
├── public/             # Static assets
└── docs/              # Documentation
```

