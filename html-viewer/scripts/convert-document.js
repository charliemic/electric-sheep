#!/usr/bin/env node
/**
 * Convert a document to HTML using the HTML Viewer tool
 * Available in source control and CI/CD pipelines
 */

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

// Simple markdown to HTML converter
// In production, this could use the full Astro build system
function convertMarkdownToHTML(mdContent, title, theme = 'light') {
  // For now, use a simple approach - in production, use marked or full Astro build
  // This is a placeholder - the actual conversion should use the Astro components
  
  const html = `<!DOCTYPE html>
<html lang="en" class="${theme === 'dark' ? 'dark' : ''}">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${escapeHtml(title)}</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <script src="https://cdn.jsdelivr.net/npm/marked@11.0.0/marked.min.js"></script>
  <style>
    body { max-width: 900px; margin: 0 auto; padding: 2rem; line-height: 1.7; }
    /* Markdown styles */
  </style>
</head>
<body>
  <h1>${escapeHtml(title)}</h1>
  <div id="content"></div>
  <script>
    const md = ${JSON.stringify(mdContent)};
    const marked = window.marked || (() => {
      // Fallback if marked not loaded
      return md.replace(/\\n/g, '<br>');
    });
    document.getElementById('content').innerHTML = marked.parse(md);
  </script>
</body>
</html>`;
  
  return html;
}

function escapeHtml(text) {
  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  };
  return text.replace(/[&<>"']/g, m => map[m]);
}

// CLI usage
if (require.main === module) {
  const args = process.argv.slice(2);
  const inputFile = args[0];
  const outputFile = args[1];
  const title = args[2] || path.basename(inputFile, path.extname(inputFile));
  const theme = args[3] || 'light';

  if (!inputFile || !outputFile) {
    console.error('Usage: convert-document.js <input-file> <output-file> [title] [theme]');
    process.exit(1);
  }

  const mdContent = fs.readFileSync(inputFile, 'utf-8');
  const html = convertMarkdownToHTML(mdContent, title, theme);
  fs.writeFileSync(outputFile, html);
  console.log(`✅ Converted: ${inputFile} → ${outputFile}`);
}

module.exports = { convertMarkdownToHTML };

