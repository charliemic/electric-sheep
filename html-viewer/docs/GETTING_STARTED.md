# Getting Started

Quick start guide for the HTML Viewer & Converter tool.

## Installation

```bash
cd html-viewer
npm install
```

## Development

Start the development server:

```bash
npm run dev
```

Open http://localhost:4321 in your browser.

## Usage

### Web Interface

1. Open the web interface
2. Select input type (Markdown, Log File, or Plain Text)
3. Paste or type your content
4. Customize options:
   - Document title
   - Theme (Light/Dark/Auto)
   - Include table of contents
   - Embed images as base64
5. Click "Convert to HTML"
6. Preview the result
7. Click "Download HTML" to save

### Command Line (Future)

```bash
# Convert markdown file
npm run convert -- input.md output.html

# Convert with options
npm run convert -- input.md output.html --theme dark --toc
```

## Features

- ✅ **Markdown Support** - Full markdown syntax with tables, code blocks, etc.
- ✅ **Log Formatting** - Format log files with syntax highlighting
- ✅ **Plain Text** - Simple text to HTML conversion
- ✅ **Themes** - Light, dark, or auto (system preference)
- ✅ **Table of Contents** - Auto-generated from headings
- ✅ **Image Embedding** - Embed images as base64 (standalone HTML)
- ✅ **Syntax Highlighting** - Code blocks with Prism.js
- ✅ **Responsive Design** - Works on all screen sizes

## Next Steps

- See [HOSTING.md](./HOSTING.md) for deployment options
- See [PROBLEM_SPACE.md](./PROBLEM_SPACE.md) for architecture details

